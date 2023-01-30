package minicraft.util;

import minicraft.core.World;
import minicraft.entity.furniture.Chest;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.Recipe;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.saveload.Load;
import minicraft.screen.CraftingDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** World-wide. */
public class AdvancementElement {
	private static final HashSet<AdvancementElement> recipeUnlockingElements;

	static {
		try {
			recipeUnlockingElements = loadAdvancementFile("/resources/recipes.json", false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static HashSet<AdvancementElement> loadAdvancementFile(String filename, boolean displayable) throws IOException {
		HashSet<AdvancementElement> elements = new HashSet<>();
		JSONObject json = new JSONObject(String.join("", Load.loadFile(filename)));
		for (String key : json.keySet()) {
			loadAdvancementElement(elements, key, json.getJSONObject(key), displayable);
		}

		return elements;
	}

	public static void loadAdvancementElement(Collection<AdvancementElement> elements, String criterionName, JSONObject json, boolean displayable) {
		HashMap<String, AdvancementElement.ElementCriterion> criteria = new HashMap<>();
		JSONObject criteriaJson = json.getJSONObject("criteria");
		if (criteriaJson.isEmpty()) throw new IndexOutOfBoundsException("criteria is empty.");
		for (String key : criteriaJson.keySet()) {
			JSONObject criterion = criteriaJson.getJSONObject(key);
			criteria.put(key, new AdvancementElement.ElementCriterion(criterion.getString("trigger"), criterion.getJSONObject("conditions")));
		}

		HashSet<HashSet<String>> requirements = new HashSet<>();
		JSONArray requirementsJson = json.optJSONArray("requirements");
		if (requirementsJson != null) {
			for (int i = 0; i < requirementsJson.length(); i++) {
				HashSet<String> req = new HashSet<>();
				JSONArray reqJson = requirementsJson.getJSONArray(i);
				for (int j = 0; j < reqJson.length(); j++) {
					req.add(reqJson.getString(j));
				}
				requirements.add(req);
			}
		}

		HashMap<String, AdvancementElement.ElementCriterion> unlockingCriteria = new HashMap<>();
		JSONObject unlockingCriteriaJson = json.optJSONObject("unlocking_criteria");
		if (unlockingCriteriaJson != null) {
			for (String key : unlockingCriteriaJson.keySet()) {
				JSONObject criterion = unlockingCriteriaJson.getJSONObject(key);
				unlockingCriteria.put(key, new AdvancementElement.ElementCriterion(criterion.getString("trigger"), criterion.getJSONObject("conditions")));
			}
		}

		HashSet<HashSet<String>> unlockingRequirements = new HashSet<>();
		JSONArray unlockingRequirementsJson = json.optJSONArray("unlocking_requirements");
		if (unlockingRequirementsJson != null) {
			for (int i = 0; i < unlockingRequirementsJson.length(); i++) {
				HashSet<String> req = new HashSet<>();
				JSONArray reqJson = unlockingRequirementsJson.getJSONArray(i);
				for (int j = 0; j < reqJson.length(); j++) {
					req.add(reqJson.getString(j));
				}
				unlockingRequirements.add(req);
			}
		}

		ElementRewards rewards = loadRewards(json.optJSONObject("rewards"));
		elements.add(new AdvancementElement(criterionName,
			displayable ? json.getString("description"): null, criteria,
			rewards, requirements, unlockingCriteria, unlockingRequirements));
	}

	public static ElementRewards loadRewards(JSONObject json) {
		ArrayList<Item> items = new ArrayList<>();
		ArrayList<Recipe> recipes = new ArrayList<>();
		if (json != null) {
			JSONArray itemsJson = json.optJSONArray("items");
			if (itemsJson != null) {
				for (int i = 0; i < itemsJson.length(); i++) {
					items.add(Items.get(itemsJson.getString(i)));
				}
			}

			JSONObject recipesJson = json.optJSONObject("recipes");
			if (recipesJson != null) {
				for (String product : recipesJson.keySet()) {
					JSONArray costsJson = recipesJson.getJSONArray(product);
					String[] costs = new String[costsJson.length()];
					for (int j = 0; j < costsJson.length(); j++) {
						costs[j] = costsJson.getString(j);
					}

					recipes.add(new Recipe(product, costs));
				}
			}
		}

		return new ElementRewards(items, recipes);
	}

	public static void resetRecipeUnlockingElements() {
		recipeUnlockingElements.forEach(AdvancementElement::reset);
	}

	public static void loadRecipeUnlockingElements(JSONObject json) {
		resetRecipeUnlockingElements();
		for (String k : json.keySet()) {
			recipeUnlockingElements.stream().filter(e -> e.key.equals(k))
				.findFirst().ifPresent(element -> element.load(json.getJSONObject(k)));
		}
	}

	/** Saving and writing all data into the given JSONObject. */
	public static void saveRecipeUnlockingElements(JSONObject json) {
		recipeUnlockingElements.forEach(element -> element.save(json));
	}


	public final String key;
	public final String description;

	protected final HashMap<String, ElementCriterion> criteria = new HashMap<>();
	protected final @Nullable ElementRewards rewards;
	protected final HashSet<HashSet<String>> requirements = new HashSet<>();
	protected final HashMap<String, ElementCriterion> unlockingCriteria = new HashMap<>();
	protected final HashSet<HashSet<String>> unlockingRequirements = new HashSet<>();
	protected boolean completed = false;
	protected boolean unlocked = false;

	public AdvancementElement(String key, String description, Map<String, ElementCriterion> criteria,
							  @Nullable ElementRewards rewards, @NotNull Set<HashSet<String>> requirements,
							  @NotNull Map<String, ElementCriterion> unlockingCriteria,
							  @NotNull Set<HashSet<String>> unlockingRequirements) {
		this.key = key;
		this.description = description;
		this.criteria.putAll(criteria);
		this.criteria.forEach((k, criterion) -> criterion.element = this);
		this.rewards = rewards;
		this.requirements.addAll(requirements);
		this.unlockingCriteria.putAll(unlockingCriteria);
		this.unlockingRequirements.addAll(unlockingRequirements);
	}

	public static class ElementCriterion {
		protected final AdvancementTrigger trigger;
		protected final AdvancementTrigger.AdvancementTriggerConditionHandler.AdvancementCriterionConditions conditions;
		protected AdvancementElement element = null;
		// #completionTime field here is used for statistics and debugging.
		protected @Nullable LocalDateTime completionTime = null; // null if not completed.

		public ElementCriterion(String trigger, JSONObject conditions) {
			this.trigger = AdvancementTrigger.getTrigger(trigger);
			this.conditions = this.trigger.conditions.createCriterionConditions(conditions);
		}

		protected void registerCriterion() {
			if (completionTime == null)
				this.trigger.register(this);
		}

		public boolean isCompleted() {
			return completionTime != null;
		}

		public @Nullable LocalDateTime getCompletionTime() {
			return completionTime;
		}

		/**
		 * Marking the criterion as completed if it has not already been completed.
		 * @param inLoad If this is {@code false}, triggers the status updater.
		 * @param completionTime The completion time. Using the current datetime if this is {@code null}.
		 */
		public void markAsCompleted(boolean inLoad, @Nullable LocalDateTime completionTime) {
			if (this.completionTime == null) {
				trigger.registeredCriteria.remove(this);
				this.completionTime = completionTime == null ? LocalDateTime.now() : completionTime;
				if (!inLoad && element != null)
					element.update();
			}
		}

		public void reset() {
			completionTime = null;
		}
	}

	public static class ElementRewards {
		private final ArrayList<Item> items;
		private final ArrayList<Recipe> recipes;

		public ElementRewards(ArrayList<Item> items, ArrayList<Recipe> recipes) {
			this.items = items;
			this.recipes = recipes;
		}

		public ArrayList<Item> getItems() { return new ArrayList<>(items); }
		public ArrayList<Recipe> getRecipe() { return new ArrayList<>(recipes); }
	}

	@SuppressWarnings("unused")
	public @Nullable ElementRewards getRewards() {
		return rewards;
	}

	public void markAsCompleted(boolean inLoad) {
		if (!completed) {
			completed = true;
			if (!inLoad)
				update();
		}
	}

	protected boolean checkIsUnlocked() {
		if (unlocked) return true;
		return isUnlockable();
	}

	protected void registerUnlockingCriteria() {
		unlockingCriteria.values().forEach(criterion -> {
			if (criterion.completionTime == null)
				criterion.registerCriterion();
		});
	}

	protected void registerCriteria() {
		criteria.values().forEach(criterion -> {
			if (criterion.completionTime == null)
				criterion.registerCriterion();
		});
	}

	public boolean isCompleted() {
		return completed;
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	/** Is unlocked but not completed. */
	public boolean isDisplayableAtStatus() {
		return unlocked && !completed;
	}

	public int getNumCriteriaCompleted() {
		return (int) criteria.values().stream().filter(ElementCriterion::isCompleted).count();
	}
	public int getTotalNumCriteria() {
		return criteria.size();
	}
	public boolean shouldAllCriteriaBeCompleted() {
		return requirements.isEmpty();
	}

	protected boolean isUnlockable() {
		if (unlocked) return true;
		return unlockingRequirements.isEmpty() && unlockingCriteria.values().stream().allMatch(criterion -> criterion.completionTime != null) ||
			!unlockingRequirements.isEmpty() && unlockingRequirements.stream().allMatch(sublist -> sublist.stream().anyMatch(k -> {
				ElementCriterion criterion = criteria.get(k);
				return criterion != null && criterion.completionTime != null;
			}));
	}

	protected boolean checkIsCompleted() {
		return requirements.isEmpty() && criteria.values().stream().allMatch(criterion -> criterion.completionTime != null) ||
			!requirements.isEmpty() && requirements.stream().allMatch(sublist -> sublist.stream().anyMatch(k -> {
				ElementCriterion criterion = criteria.get(k);
				return criterion != null && criterion.completionTime != null;
			}));
	}

	/** Updating and refreshing by the data in this element. */
	public void update() {
		if (isUnlockable())
			registerUnlockingCriteria();

		if (!unlocked) {
			unlocked = checkIsUnlocked();
		}

		if (unlocked) registerCriteria();
		if (!completed && unlocked) {
			if (checkIsCompleted()) {
				markAsCompleted(true); // To not call #update() again.
				sendRewards();
			}
		}
	}

	protected void sendRewards() {
		if (rewards != null) {
			ArrayList<Item> items = rewards.getItems();
			if (items.size() > 0) {
				Chest chest = new Chest("Rewards");
				chest.x = World.player.x;
				chest.y = World.player.y;
				for (Item item : items) chest.getInventory().add(item);
				World.levels[World.currentLevel].add(chest);
			}

			ArrayList<Recipe> recipes = rewards.getRecipe();
			if (recipes.size() > 0) {
				recipes.forEach(CraftingDisplay::unlockRecipe);
			}
		}
	}

	public void reset() { reset(true); }
	protected void reset(boolean update) {
		completed = false;
		unlocked = false;
		criteria.values().forEach(ElementCriterion::reset);
		if (update) update();
	}

	/** Loading from a JSONObject of an element. */
	public void load(JSONObject json) {
		reset(false);
		completed = json.optBoolean("done");
		unlocked = json.optBoolean("unlocked");
		JSONObject criteriaJson = json.optJSONObject("criteria");
		if (criteriaJson != null) {
			for (String k : criteriaJson.keySet()) {
				ElementCriterion criterion = criteria.get(k);
				if (criterion != null) {
					criterion.markAsCompleted(true, LocalDateTime.parse(criteriaJson.getString(k)));
				}
			}
		}

		JSONObject unlockingCriteriaJson = json.optJSONObject("unlockingCriteria");
		if (unlockingCriteriaJson != null) {
			for (String k : unlockingCriteriaJson.keySet()) {
				ElementCriterion criterion = unlockingCriteria.get(k);
				if (criterion != null) {
					criterion.markAsCompleted(true, LocalDateTime.parse(unlockingCriteriaJson.getString(k)));
				}
			}
		}

		update();
	}

	/** Saving and writing data to the root JSONObject */
	public void save(JSONObject json) {
		JSONObject elementJson = new JSONObject();
		JSONObject criteriaJson = new JSONObject();
		criteria.forEach((k, criterion) -> {
			if (criterion.completionTime != null)
				criteriaJson.put(k, criterion.completionTime.toString());
		});
		if (!criteriaJson.isEmpty()) {
			elementJson.put("criteria", criteriaJson);
			elementJson.put("done", completed);
		}

		JSONObject unlockingCriteriaJson = new JSONObject();
		unlockingCriteria.forEach((k, criterion) -> {
			if (criterion.completionTime != null)
				unlockingCriteriaJson.put(k, criterion.completionTime.toString());
		});
		if (!unlockingCriteriaJson.isEmpty()) {
			elementJson.put("unlockingCriteria", unlockingCriteriaJson);
			elementJson.put("unlocked", unlocked);
		}

		if (!elementJson.isEmpty())
			json.put(key, elementJson);
	}

	public static abstract class AdvancementTrigger {
		// Used for threaded trigger handling.
		private static final Set<ElementCriterion> pendingCompletedCriteria = Collections.synchronizedSet(new HashSet<>());

		public static void tick() {
			for (Iterator<ElementCriterion> it = pendingCompletedCriteria.iterator(); it.hasNext();) {
				ElementCriterion criterion = it.next();
				criterion.markAsCompleted(false, null);
				it.remove(); // Action done.
			}
		}

		private static final ExecutorService executorService = Executors.newCachedThreadPool();

		protected final AdvancementTriggerConditionHandler conditions;

		protected AdvancementTrigger(AdvancementTriggerConditionHandler conditions) {
			this.conditions = conditions;
		}

		private static final HashMap<String, AdvancementTrigger> triggers = new HashMap<>();
		@SuppressWarnings("StaticInitializerReferencesSubClass")
		private static final AdvancementTrigger IMPOSSIBLE_TRIGGER = new ImpossibleTrigger();

		static {
			triggers.put("impossible", IMPOSSIBLE_TRIGGER);
			//noinspection StaticInitializerReferencesSubClass
			triggers.put("inventory_changed", new InventoryChangedTrigger());
		}

		@NotNull
		public static AdvancementElement.AdvancementTrigger getTrigger(String key) {
			return triggers.getOrDefault(key, AdvancementTrigger.IMPOSSIBLE_TRIGGER);
		}

		protected final HashSet<ElementCriterion> registeredCriteria = new HashSet<>();

		public void register(ElementCriterion criterion) {
			registeredCriteria.add(criterion);
		}

		/** This should be called by another thread if method {@link #singleThreadNeeded()} is not
		 * implemented to return true. If false, this should use {@link #pendingCompletedCriteria}
		 * to mark completed criteria instead of calling it directly as this method should be called
		 * by the global game tick updater to ensure the synchronization. */
		protected abstract void trigger0(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions);

		/** @return {@code true} if this trigger implementation requires single thread as the global game tick updater. */
		protected boolean singleThreadNeeded() {
			return false;
		}

		/** Triggering and checking passes by another thread. */
		public void trigger(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions) {
			if (!singleThreadNeeded()) executorService.submit(() -> trigger0(conditions));
			else trigger0(conditions);
		}

		@SuppressWarnings("unused")
		@NotNull
		public AdvancementElement.AdvancementTrigger.AdvancementTriggerConditionHandler getConditions() {
			return conditions;
		}

		public abstract static class AdvancementTriggerConditionHandler {
			protected AdvancementTriggerConditionHandler() {}

			@NotNull
			public abstract AdvancementCriterionConditions createCriterionConditions(JSONObject json) throws JSONException;

			/** A condition carrier for the corresponding trigger. */
			public abstract static class AdvancementTriggerConditions {}

			public abstract static class AdvancementCriterionConditions {
				protected AdvancementCriterionConditions() {}
			}
		}

		public static class ImpossibleTrigger extends AdvancementTrigger {
			protected ImpossibleTrigger() {
				super(new ImpossibleTriggerConditionHandler());
			}

			@Override
			public void register(ElementCriterion criterion) {} // No action.

			@Override
			protected void trigger0(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions) {} // No action.

			public static class ImpossibleTriggerConditionHandler extends AdvancementTriggerConditionHandler {

				@Override
				public @NotNull AdvancementElement.AdvancementTrigger.AdvancementTriggerConditionHandler.AdvancementCriterionConditions createCriterionConditions(JSONObject json) {
					return new AdvancementCriterionConditions() {}; // Empty.
				}
			}
		}

		public static class InventoryChangedTrigger extends AdvancementTrigger {
			protected InventoryChangedTrigger() {
				super(new InventoryChangedTriggerConditionHandler());
			}

			@Override
			protected void trigger0(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions) {
				if (conditions instanceof InventoryChangedTriggerConditionHandler.InventoryChangedTriggerConditions) {
					ArrayList<Item> items = ((InventoryChangedTriggerConditionHandler.InventoryChangedTriggerConditions) conditions).items;
					int maxSlots = ((InventoryChangedTriggerConditionHandler.InventoryChangedTriggerConditions) conditions).maxSlots;
					for (ElementCriterion criterion : new HashSet<>(registeredCriteria)) {
						if (criterion.conditions instanceof InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions) {
							InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions criterionConditions =
								(InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions) criterion.conditions;
							if (!InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.Rangeable.isAbsent(criterionConditions.slotsEmpty))
								if (!criterionConditions.slotsEmpty.inRange(maxSlots - items.size()))
									continue;
							if (!InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.Rangeable.isAbsent(criterionConditions.slotsFull))
								if (!criterionConditions.slotsFull.inRange((int) items.stream().filter(i ->
									!(i instanceof StackableItem) || ((StackableItem) i).count >= ((StackableItem) i).maxCount).count()))
									continue;
							if (!InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.Rangeable.isAbsent(criterionConditions.slotsOccupied))
								if (!criterionConditions.slotsOccupied.inRange(items.size()))
									continue;
							if (!criterionConditions.items.isEmpty() && !isConditionalMatch(items, criterionConditions.items)) {
								continue;
							}
							pendingCompletedCriteria.add(criterion); // All conditions passed.
						}
					}
				}
			}

			/** Modified from {@link #test(List, List)}. */
			private static boolean isConditionalMatch(ArrayList<Item> items,
													  HashSet<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions> itemConditions) {
				Set<HashMap<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions, String>> combinations = new HashSet<>();
				List<List<String>> combinationsOutput = new ArrayList<>();
				List<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions> conditionsList = new ArrayList<>();
				List<List<String>> itemStringCombinations = new ArrayList<>();
				itemConditions.forEach(c -> {
					if (!c.items.isEmpty()) {
						conditionsList.add(c);
						itemStringCombinations.add(new ArrayList<>(c.items));
					}
				});
				if (!conditionsList.isEmpty()) getCombinations(combinationsOutput, itemStringCombinations);
				for (List<String> c : combinationsOutput) {
					HashMap<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions, String> cOut = new HashMap<>();
					for (int i = 0; i < conditionsList.size(); i++) {
						cOut.put(conditionsList.get(i), c.get(i));
					}
					combinations.add(cOut);
				}

				if (combinations.isEmpty()) {
					return allMatch(items, new HashSet<>(itemConditions), new HashMap<>());
				} else {
					for (HashMap<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions, String> c : combinations) {
						if (allMatch(items, new HashSet<>(itemConditions), c))
							return true;
					}
					return false;
				}
			}

			/** Used by {@link #allMatch(Collection, Collection, HashMap)} for conditional check for each element. */
			private static boolean isMatch(Item item, InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions itemConditions,
										   @Nullable String selectedItem) {
				if (!InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.Rangeable.isAbsent(itemConditions.count))
					if (!itemConditions.count.inRange(item instanceof StackableItem ? ((StackableItem) item).count : 1))
						return false;
				if (!InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.Rangeable.isAbsent(itemConditions.durability) &&
					item instanceof ToolItem)
					if (!itemConditions.durability.inRange(((ToolItem) item).dur))
						return false;
				return selectedItem == null || item.getName().equalsIgnoreCase(selectedItem);
			}

			/** Modified from {@link #containsAll(List, List)}. */
			private static boolean allMatch(Collection<Item> source, Collection<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions> target,
											HashMap<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions, String> selectedItems) {
				for (Item e : source) {
					target.removeIf(conditions1 -> isMatch(e, conditions1, selectedItems.get(conditions1)));
					if (target.isEmpty()) {
						return true;
					}
				}

				return target.size() == 0;
			}

			/** Original archive of array elements matching. */
			@SuppressWarnings("unused") // Keeping for future reference.
			private static <T> boolean test(List<T> list, List<List<T>> matcher) {
				List<List<T>> combinations = new ArrayList<>();
				getCombinations(combinations, matcher);
				for (List<T> matcher1 : combinations) {
					if (containsAll(list, matcher1))
						return true;
				}
				return false;
			}

			/** Original archive of array elements matching. */
			private static <T> boolean containsAll(List<T> source, List<T> target) {
				for (T e : source) {
					target.remove(e);
					if (target.isEmpty()) {
						return true;
					}
				}

				return target.size() == 0;
			}

			private static <T> void getCombinations(List<List<T>> output, List<List<T>> matcher) {
				List<List<Integer>> indices = new ArrayList<>();
				List<Integer> elementSizes = new ArrayList<>();
				matcher.forEach(m -> elementSizes.add(m.size()));
				helper(indices, elementSizes, null, 0);
				for (List<Integer> ints : indices) {
					List<T> matcher1 = new ArrayList<>();
					for (int i = 0; i < ints.size(); i++) {
						matcher1.add(matcher.get(i).get(ints.get(i)));
					}
					output.add(matcher1);
				}
			}

			private static void helper(List<List<Integer>> output, List<Integer> elementSizes, @Nullable ArrayList<Integer> buffer, int index) {
				for (int i = 0; i < elementSizes.get(index); i++) {
					if (buffer == null) buffer = new ArrayList<>(Arrays.asList(new Integer[elementSizes.size()]));
					buffer.set(index, i);
					if (index < elementSizes.size() - 1) {
						helper(output, elementSizes, buffer, index + 1);
					} else {
						output.add(new ArrayList<>(buffer));
					}
				}
			}

			public static class InventoryChangedTriggerConditionHandler extends AdvancementTriggerConditionHandler {

				@Override
				public @NotNull AdvancementElement.AdvancementTrigger.AdvancementTriggerConditionHandler.AdvancementCriterionConditions createCriterionConditions(JSONObject json) throws JSONException {
					HashSet<InventoryChangedCriterionConditions.ItemConditions> items = new HashSet<>();
					InventoryChangedCriterionConditions.Rangeable slotsEmpty = null;
					InventoryChangedCriterionConditions.Rangeable slotsFull = null;
					InventoryChangedCriterionConditions.Rangeable slotsOccupied = null;
					JSONArray itemsJson = json.optJSONArray("items");
					if (itemsJson != null) {
						for (int i = 0; i < itemsJson.length(); i++) {
							JSONObject obj = itemsJson.getJSONObject(i);
							HashSet<String> itemItems = new HashSet<>();
							InventoryChangedCriterionConditions.Rangeable count = null;
							InventoryChangedCriterionConditions.Rangeable durability = null;
							JSONArray itemItemsJson = obj.optJSONArray("items");
							if (itemItemsJson != null) for (int j = 0; j < itemItemsJson.length(); j++) {
								itemItems.add(itemItemsJson.getString(j));
							}

							if (obj.has("count")) try {
								int val = obj.getInt("count");
								count = new InventoryChangedCriterionConditions.Rangeable(val, val);
							} catch (JSONException e) { // Not an integer.
								JSONObject val = obj.getJSONObject("count");
								count = new InventoryChangedCriterionConditions.Rangeable(val.has("min") ? val.getInt("min") : null,
									val.has("max") ? val.getInt("max") : null);
							}

							if (obj.has("durability")) try {
								int val = obj.getInt("durability");
								durability = new InventoryChangedCriterionConditions.Rangeable(val, val);
							} catch (JSONException e) { // Not an integer.
								JSONObject val = obj.getJSONObject("durability");
								durability = new InventoryChangedCriterionConditions.Rangeable(val.has("min") ? val.getInt("min") : null,
									val.has("max") ? val.getInt("max") : null);
							}

							items.add(new InventoryChangedCriterionConditions.ItemConditions(itemItems, count, durability));
						}
					}

					JSONObject slotsJson = json.optJSONObject("slots");
					if (slotsJson != null) {
						if (slotsJson.has("empty")) try {
							int val = slotsJson.getInt("empty");
							slotsEmpty = new InventoryChangedCriterionConditions.Rangeable(val, val);
						} catch (JSONException e) { // Not an integer.
							JSONObject val = slotsJson.getJSONObject("empty");
							slotsEmpty = new InventoryChangedCriterionConditions.Rangeable(val.has("min") ? val.getInt("min") : null,
								val.has("max") ? val.getInt("max") : null);
						}

						if (slotsJson.has("full")) try {
							int val = slotsJson.getInt("full");
							slotsFull = new InventoryChangedCriterionConditions.Rangeable(val, val);
						} catch (JSONException e) { // Not an integer.
							JSONObject val = slotsJson.getJSONObject("full");
							slotsFull = new InventoryChangedCriterionConditions.Rangeable(val.has("min") ? val.getInt("min") : null,
								val.has("max") ? val.getInt("max") : null);
						}

						if (slotsJson.has("occupied")) try {
							int val = slotsJson.getInt("occupied");
							slotsOccupied = new InventoryChangedCriterionConditions.Rangeable(val, val);
						} catch (JSONException e) { // Not an integer.
							JSONObject val = slotsJson.getJSONObject("occupied");
							slotsOccupied = new InventoryChangedCriterionConditions.Rangeable(val.has("min") ? val.getInt("min") : null,
								val.has("max") ? val.getInt("max") : null);
						}
					}

					return new InventoryChangedCriterionConditions(items, slotsEmpty, slotsFull, slotsOccupied);
				}

				public static class InventoryChangedTriggerConditions extends AdvancementTriggerConditions {
					private final ArrayList<Item> items = new ArrayList<>();
					private final int maxSlots;

					public InventoryChangedTriggerConditions(Inventory inventory) {
						items.addAll(inventory.getItems());
						maxSlots = inventory.getMaxSlots();
					}
				}

				public static class InventoryChangedCriterionConditions extends AdvancementCriterionConditions {
					public static class Rangeable {
						public final @Nullable Integer min;
						public final @Nullable Integer max;
						public Rangeable(@Nullable Integer min, @Nullable Integer max) {
							this.min = min;
							this.max = max;
						}

						@SuppressWarnings("BooleanMethodIsAlwaysInverted")
						public static boolean isAbsent(@Nullable Rangeable rangeable) {
							if (rangeable == null) return true;
							return rangeable.min == null && rangeable.max == null;
						}

						@SuppressWarnings("BooleanMethodIsAlwaysInverted")
						public boolean inRange(int value) {
							if (min == null && max == null) return true; // The range is not ranged.
							if (min == null) return max >= value;
							if (max == null) return min <= value;
							return max >= value && min <= value;
						}
					}

					private static class ItemConditions {
						private final HashSet<String> items = new HashSet<>();
						private final @Nullable Rangeable count;
						private final @Nullable Rangeable durability;
						private ItemConditions(Set<String> items, @Nullable Rangeable count, @Nullable Rangeable durability) {
							this.items.addAll(items);
							this.count = count;
							this.durability = durability;
						}
					}

					private final HashSet<ItemConditions> items = new HashSet<>();
					private final @Nullable Rangeable slotsEmpty;
					private final @Nullable Rangeable slotsFull;
					private final @Nullable Rangeable slotsOccupied;
					private InventoryChangedCriterionConditions(Set<ItemConditions> items, @Nullable Rangeable slotsEmpty,
																@Nullable Rangeable slotsFull, @Nullable Rangeable slotsOccupied) {
						this.items.addAll(items);
						this.slotsEmpty = slotsEmpty;
						this.slotsFull = slotsFull;
						this.slotsOccupied = slotsOccupied;
					}
				}
			}
		}
	}
}
