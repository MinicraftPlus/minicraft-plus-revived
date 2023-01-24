package minicraft.util;

import minicraft.core.World;
import minicraft.core.io.Settings;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvancementElement {
	private static final ArrayList<AdvancementElement> recipeUnlockingElements;

	static {
		try {
			recipeUnlockingElements = loadTutorialFile("/resources/recipes.json", false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ArrayList<AdvancementElement> loadTutorialFile(String filename, boolean displayable) throws IOException {
		ArrayList<AdvancementElement> elements = new ArrayList<>();
		JSONObject json = new JSONObject(String.join("", Load.loadFile(filename)));
		for (String key : json.keySet()) {
			loadTutorialElement(elements, key, json.getJSONObject(key), displayable);
		}

		return elements;
	}

	public static void loadTutorialElement(ArrayList<AdvancementElement> elements, String criterionName, JSONObject json, boolean displayable) {
		HashMap<String, AdvancementElement.ElementCriterion> criteria = new HashMap<>();
		JSONObject criteriaJson = json.getJSONObject("criteria");
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

		ElementRewards rewards = loadRewards(json.optJSONObject("rewards"));
		elements.add(new AdvancementElement(criterionName,
			displayable ? json.getString("description"): null, criteria, rewards, requirements));
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
	protected boolean isCompleted = false;

	public AdvancementElement(String key, String description, Map<String, ElementCriterion> criteria,
							  @Nullable ElementRewards rewards, Set<HashSet<String>> requirements) {
		this.key = key;
		this.description = description;
		this.criteria.putAll(criteria);
		this.criteria.forEach((k, criterion) -> criterion.element = this);
		this.rewards = rewards;
		this.requirements.addAll(requirements);
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
			this.trigger.register(this);
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
			if ((boolean) Settings.get("tutorials"))
				trigger.register(this);
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

	public ElementRewards getRewardRecipes() {
		return rewards;
	}

	public void markAsCompleted(boolean inLoad) {
		if (!isCompleted) {
			isCompleted = true;
			if (!inLoad)
				update();
		}
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	/** Updating and refreshing by the data in this element. */
	public void update() {
		if (!isCompleted) {
			if (requirements.isEmpty() && criteria.values().stream().allMatch(criterion -> criterion.completionTime != null) ||
				!requirements.isEmpty() && requirements.stream().allMatch(sublist -> sublist.stream().anyMatch(k -> {
					ElementCriterion criterion = criteria.get(k);
					return criterion != null && criterion.completionTime != null;
				})))
				markAsCompleted(true); // To not call #update() again.
		}
	}

	private void sendRewards() {
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

	public void reset() {
		isCompleted = false;
		criteria.values().forEach(ElementCriterion::reset);
	}

	/** Loading from a JSONObject of an element. */
	public void load(JSONObject json) {
		JSONObject criteriaJson = json.optJSONObject("criteria");
		isCompleted = json.optBoolean("done");
		if (criteriaJson != null) {
			for (String k : criteriaJson.keySet()) {
				ElementCriterion criterion = criteria.get(k);
				if (criterion != null) {
					criterion.markAsCompleted(true, LocalDateTime.parse(criteriaJson.getString(k)));
				}
			}
		}
	}

	/** Saving and writing data to the root JSONObject */
	public void save(JSONObject json) {
		JSONObject criteriaJson = new JSONObject();
		criteria.forEach((k, criterion) -> {
			if (criterion.completionTime != null)
				criteriaJson.put(k, criterion.completionTime.toString());
		});
		if (!criteriaJson.isEmpty()) {
			JSONObject elementJson = new JSONObject();
			elementJson.put("criteria", criteriaJson);
			elementJson.put("done", isCompleted);
			json.put(key, elementJson);
		}
	}

	public static abstract class AdvancementTrigger {
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

		public abstract void trigger(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions);

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
			public void trigger(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions) {} // No action.

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
			public void trigger(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions) {
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
							criterion.markAsCompleted(false, null); // All conditions passed.
						}
					}
				}
			}

			/** Modified from {@link #test(List, List)}. */
			private static boolean isConditionalMatch(ArrayList<Item> items, HashSet<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions> itemConditions) {
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
					return allMatch(items, itemConditions, new HashMap<>());
				} else {
					for (HashMap<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions, String> c : combinations) {
						if (allMatch(items, itemConditions, c))
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
