package minicraft.util;

import minicraft.core.World;
import minicraft.entity.furniture.Chest;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.Recipe;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.level.tile.Tile;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * World-wide.
 */
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
			displayable ? json.getString("description") : null, criteria,
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

	/**
	 * Saving and writing all data into the given JSONObject.
	 */
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
		this.unlockingCriteria.forEach((k, criterion) -> criterion.element = this);
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

		@SuppressWarnings("unused")
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

		public ArrayList<Item> getItems() {
			return new ArrayList<>(items);
		}

		public ArrayList<Recipe> getRecipe() {
			return new ArrayList<>(recipes);
		}
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

	/**
	 * Warning: This method should be used carefully as this could impact
	 * the gaming experience deeply on the progress.
	 */
	public void deregisterCriteria() {
		criteria.values().forEach(criterion -> {
			criterion.trigger.registeredCriteria.remove(criterion);
		});
	}

	public boolean isCompleted() {
		return completed;
	}

	public boolean isUnlocked() {
		return unlocked;
	}

	/**
	 * Is unlocked but not completed.
	 */
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

	/**
	 * Updating and refreshing by the data in this element.
	 */
	public void update() {
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

	public void reset() {
		reset(true);
	}

	protected void reset(boolean update) {
		completed = false;
		unlocked = false;
		criteria.values().forEach(ElementCriterion::reset);
		if (update) update();
	}

	/**
	 * Loading from a JSONObject of an element.
	 */
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

	/**
	 * Saving and writing data to the root JSONObject
	 */
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
		private static final Set<ElementCriterion> pendingCompletedCriteria = ConcurrentHashMap.newKeySet();

		public static void tick() {
			for (Iterator<ElementCriterion> it = pendingCompletedCriteria.iterator(); it.hasNext(); ) {
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

		static {
			triggers.put("impossible", ImpossibleTrigger.INSTANCE);
			triggers.put("inventory_changed", InventoryChangedTrigger.INSTANCE);
			triggers.put("placed_tile", PlacedTileTrigger.INSTANCE);
			//noinspection StaticInitializerReferencesSubClass
			triggers.put("item_used_on_tile", ItemUsedOnTileTrigger.INSTANCE);
		}

		@NotNull
		public static AdvancementElement.AdvancementTrigger getTrigger(String key) {
			return triggers.getOrDefault(key, ImpossibleTrigger.INSTANCE);
		}

		protected final HashSet<ElementCriterion> registeredCriteria = new HashSet<>();

		public void register(ElementCriterion criterion) {
			registeredCriteria.add(criterion);
		}

		/**
		 * This should be called by another thread if method {@link #singleThreadNeeded()} is not
		 * implemented to return true. If false, this should use {@link #pendingCompletedCriteria}
		 * to mark completed criteria instead of calling it directly as this method should be called
		 * by the global game tick updater to ensure the synchronization.
		 */
		protected abstract void trigger0(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions);

		/**
		 * @return {@code true} if this trigger implementation requires single thread as the global game tick updater.
		 */
		protected boolean singleThreadNeeded() {
			return true;
		}

		/**
		 * Triggering and checking passes by another thread.
		 */
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
			protected AdvancementTriggerConditionHandler() {
			}

			@NotNull
			public abstract AdvancementCriterionConditions createCriterionConditions(JSONObject json) throws JSONException;

			/**
			 * A condition carrier for the corresponding trigger.
			 */
			public abstract static class AdvancementTriggerConditions {
			}

			public abstract static class AdvancementCriterionConditions {
				protected AdvancementCriterionConditions() {
				}

				public static class Rangeable<T extends Number & Comparable<T>> {
					public final @Nullable T min;
					public final @Nullable T max;

					public Rangeable(@Nullable T min, @Nullable T max) {
						this.min = min;
						this.max = max;
					}

					@SuppressWarnings("BooleanMethodIsAlwaysInverted")
					public static boolean isAbsent(@Nullable Rangeable<?> rangeable) {
						if (rangeable == null) return true;
						return rangeable.min == null && rangeable.max == null;
					}

					@SuppressWarnings("BooleanMethodIsAlwaysInverted")
					private boolean inRange(T value) {
						if (min == null && max == null) return true; // The range is not ranged.
						if (min == null) return max.compareTo(value) >= 0;
						if (max == null) return min.compareTo(value) <= 0;
						return max.compareTo(value) >= 0 && min.compareTo(value) <= 0;
					}

					@Override
					public String toString() {
						return isAbsent(this) ? "<unspecified range>" :
							min == null ? "max: " + max :
								max == null ? "min: " + min :
									String.format("min: %s;max: %s", min, max);
					}
				}

				public static class ItemConditions {
					private final HashSet<String> items = new HashSet<>();
					private final @Nullable Rangeable<Integer> count;
					private final @Nullable Rangeable<Integer> durability;

					private ItemConditions(Set<String> items, @Nullable Rangeable<Integer> count, @Nullable Rangeable<Integer> durability) {
						this.items.addAll(items);
						this.count = count;
						this.durability = durability;
					}

					/**
					 * @param json The JSON object by the {@code item} key.
					 * @return {@code null} if the object is {@code null}.
					 */
					private static ItemConditions getFromJson(JSONObject json) {
						if (json == null) return null;
						HashSet<String> items = new HashSet<>();
						Rangeable<Integer> count = null;
						Rangeable<Integer> durability = null;
						JSONArray itemItemsJson = json.optJSONArray("items");
						if (itemItemsJson != null) for (int j = 0; j < itemItemsJson.length(); j++) {
							items.add(itemItemsJson.getString(j));
						}

						if (json.has("count")) try {
							int val = json.getInt("count");
							count = new Rangeable<>(val, val);
						} catch (JSONException e) { // Not an integer.
							JSONObject val = json.getJSONObject("count");
							count = new Rangeable<>(val.has("min") ? val.getInt("min") : null,
								val.has("max") ? val.getInt("max") : null);
						}

						if (json.has("durability")) try {
							int val = json.getInt("durability");
							durability = new Rangeable<>(val, val);
						} catch (JSONException e) { // Not an integer.
							JSONObject val = json.getJSONObject("durability");
							durability = new Rangeable<>(val.has("min") ? val.getInt("min") : null,
								val.has("max") ? val.getInt("max") : null);
						}

						return new ItemConditions(items, count, durability);
					}

					@SuppressWarnings("BooleanMethodIsAlwaysInverted")
					private boolean matches(Item item) {
						if (!Rangeable.isAbsent(count))
							if (!count.inRange(item instanceof StackableItem ? ((StackableItem) item).count : 1))
								return false;
						if (!Rangeable.isAbsent(durability) && item instanceof ToolItem)
							if (!durability.inRange(((ToolItem) item).dur))
								return false;
						return items.isEmpty() || items.stream().anyMatch(s -> s.equalsIgnoreCase(item.getName()));
					}
				}

				/**
				 * Tile location.
				 */
				public static class LocationConditions {
					private final HashSet<String> tiles = new HashSet<>();
					private final @Nullable Integer level;
					private final @Nullable Integer data;
					private final @Nullable Rangeable<Double> x;
					private final @Nullable Rangeable<Double> y;

					private LocationConditions(Set<String> tiles, @Nullable Integer level, @Nullable Integer data,
					                           @Nullable Rangeable<Double> x, @Nullable Rangeable<Double> y) {
						this.tiles.addAll(tiles);
						this.level = level;
						this.data = data;
						this.x = x;
						this.y = y;
					}

					private static LocationConditions getFromJson(JSONObject json) {
						if (json == null) return null;
						HashSet<String> tiles = new HashSet<>();
						Integer level = null;
						Integer data = null;
						Rangeable<Double> x = null;
						Rangeable<Double> y = null;
						JSONObject tileJson = json.optJSONObject("tile");
						if (tileJson != null) {
							JSONArray tilesJson = tileJson.optJSONArray("tiles");
							if (tilesJson != null) for (int j = 0; j < tilesJson.length(); j++) {
								tiles.add(tilesJson.getString(j));
							}

							try {
								data = tileJson.getInt("data");
							} catch (JSONException ignored) {
							}
						}

						try {
							level = json.getInt("level");
						} catch (JSONException ignored) {
						}

						JSONObject positionJson = json.optJSONObject("position");
						if (json.has("x")) try {
							double val = positionJson.getDouble("x");
							x = new Rangeable<>(val, val);
						} catch (JSONException e) { // Not an integer.
							JSONObject val = positionJson.getJSONObject("x");
							x = new Rangeable<>(val.has("min") ? val.getDouble("min") : null,
								val.has("max") ? val.getDouble("max") : null);
						}

						if (json.has("y")) try {
							double val = positionJson.getDouble("y");
							y = new Rangeable<>(val, val);
						} catch (JSONException e) { // Not an integer.
							JSONObject val = positionJson.getJSONObject("y");
							y = new Rangeable<>(val.has("min") ? val.getDouble("min") : null,
								val.has("max") ? val.getDouble("max") : null);
						}

						return new LocationConditions(tiles, level, data, x, y);
					}

					@SuppressWarnings("BooleanMethodIsAlwaysInverted")
					private boolean matches(Tile tile, int data, int x, int y, int level) {
						if (this.level != null && !this.level.equals(level))
							return false;
						if (this.data != null && !this.data.equals(data))
							return false;
						if (!Rangeable.isAbsent(this.x))
							if (!this.x.inRange((double) x))
								return false;
						if (!Rangeable.isAbsent(this.y))
							if (!this.y.inRange((double) y))
								return false;
						return tiles.isEmpty() || tiles.stream().anyMatch(s -> s.equalsIgnoreCase(tile.name));
					}
				}
			}
		}

		public static class ImpossibleTrigger extends AdvancementTrigger {
			public static final ImpossibleTrigger INSTANCE = new ImpossibleTrigger();

			protected ImpossibleTrigger() {
				super(new ImpossibleTriggerConditionHandler());
			}

			@Override
			public void register(ElementCriterion criterion) {
			} // No action.

			@Override
			protected void trigger0(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions) {
			} // No action.

			public static class ImpossibleTriggerConditionHandler extends AdvancementTriggerConditionHandler {

				@Override
				public @NotNull AdvancementElement.AdvancementTrigger.AdvancementTriggerConditionHandler.AdvancementCriterionConditions createCriterionConditions(JSONObject json) {
					return new AdvancementCriterionConditions() {
					}; // Empty.
				}
			}
		}

		public static class InventoryChangedTrigger extends AdvancementTrigger {
			public static final InventoryChangedTrigger INSTANCE = new InventoryChangedTrigger();

			protected InventoryChangedTrigger() {
				super(new InventoryChangedTriggerConditionHandler());
			}

			@Override
			protected boolean singleThreadNeeded() {
				return false;
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
							if (!AdvancementTriggerConditionHandler.AdvancementCriterionConditions.Rangeable.isAbsent(criterionConditions.slotsEmpty))
								if (!criterionConditions.slotsEmpty.inRange(maxSlots - items.size()))
									continue;
							if (!AdvancementTriggerConditionHandler.AdvancementCriterionConditions.Rangeable.isAbsent(criterionConditions.slotsFull))
								if (!criterionConditions.slotsFull.inRange((int) items.stream().filter(i ->
									!(i instanceof StackableItem) || ((StackableItem) i).count >= ((StackableItem) i).maxCount).count()))
									continue;
							if (!AdvancementTriggerConditionHandler.AdvancementCriterionConditions.Rangeable.isAbsent(criterionConditions.slotsOccupied))
								if (!criterionConditions.slotsOccupied.inRange(items.size()))
									continue;
							if (!criterionConditions.items.isEmpty() && !isConditionalMatched(items, criterionConditions.items)) {
								continue;
							}
							pendingCompletedCriteria.add(criterion); // All conditions passed.
						}
					}
				}
			}

			/**
			 * Modified from {@link #test(List, List)}.
			 */
			private static boolean isConditionalMatched(ArrayList<Item> items,
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

			/**
			 * Used by {@link #allMatch(Collection, Collection, HashMap)} for conditional check for each element.
			 */
			private static boolean isMatched(Item item, InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions itemConditions,
			                                 @Nullable String selectedItem) {
				if (!itemConditions.matches(item))
					return false;
				return selectedItem == null || item.getName().equalsIgnoreCase(selectedItem);
			}

			/**
			 * Modified from {@link #containsAll(List, List)}.
			 */
			private static boolean allMatch(Collection<Item> source, Collection<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions> target,
			                                HashMap<InventoryChangedTriggerConditionHandler.InventoryChangedCriterionConditions.ItemConditions, String> selectedItems) {
				for (Item e : source) {
					target.removeIf(conditions1 -> isMatched(e, conditions1, selectedItems.get(conditions1)));
					if (target.isEmpty()) {
						return true;
					}
				}

				return target.size() == 0;
			}

			/**
			 * Original archive of array elements matching.
			 */
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

			/**
			 * Original archive of array elements matching.
			 */
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
				public @NotNull AdvancementCriterionConditions createCriterionConditions(JSONObject json) throws JSONException {
					HashSet<InventoryChangedCriterionConditions.ItemConditions> items = new HashSet<>();
					InventoryChangedCriterionConditions.Rangeable<Integer> slotsEmpty = null;
					InventoryChangedCriterionConditions.Rangeable<Integer> slotsFull = null;
					InventoryChangedCriterionConditions.Rangeable<Integer> slotsOccupied = null;
					JSONArray itemsJson = json.optJSONArray("items");
					if (itemsJson != null) {
						for (int i = 0; i < itemsJson.length(); i++) {
							items.add(InventoryChangedCriterionConditions.ItemConditions.getFromJson(itemsJson.getJSONObject(i)));
						}
					}

					JSONObject slotsJson = json.optJSONObject("slots");
					if (slotsJson != null) {
						if (slotsJson.has("empty")) try {
							int val = slotsJson.getInt("empty");
							slotsEmpty = new InventoryChangedCriterionConditions.Rangeable<>(val, val);
						} catch (JSONException e) { // Not an integer.
							JSONObject val = slotsJson.getJSONObject("empty");
							slotsEmpty = new InventoryChangedCriterionConditions.Rangeable<>(val.has("min") ? val.getInt("min") : null,
								val.has("max") ? val.getInt("max") : null);
						}

						if (slotsJson.has("full")) try {
							int val = slotsJson.getInt("full");
							slotsFull = new InventoryChangedCriterionConditions.Rangeable<>(val, val);
						} catch (JSONException e) { // Not an integer.
							JSONObject val = slotsJson.getJSONObject("full");
							slotsFull = new InventoryChangedCriterionConditions.Rangeable<>(val.has("min") ? val.getInt("min") : null,
								val.has("max") ? val.getInt("max") : null);
						}

						if (slotsJson.has("occupied")) try {
							int val = slotsJson.getInt("occupied");
							slotsOccupied = new InventoryChangedCriterionConditions.Rangeable<>(val, val);
						} catch (JSONException e) { // Not an integer.
							JSONObject val = slotsJson.getJSONObject("occupied");
							slotsOccupied = new InventoryChangedCriterionConditions.Rangeable<>(val.has("min") ? val.getInt("min") : null,
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
					private final HashSet<ItemConditions> items = new HashSet<>();
					private final @Nullable Rangeable<Integer> slotsEmpty;
					private final @Nullable Rangeable<Integer> slotsFull;
					private final @Nullable Rangeable<Integer> slotsOccupied;

					private InventoryChangedCriterionConditions(Set<ItemConditions> items, @Nullable Rangeable<Integer> slotsEmpty,
					                                            @Nullable Rangeable<Integer> slotsFull, @Nullable Rangeable<Integer> slotsOccupied) {
						this.items.addAll(items);
						this.slotsEmpty = slotsEmpty;
						this.slotsFull = slotsFull;
						this.slotsOccupied = slotsOccupied;
					}
				}
			}
		}

		public static class PlacedTileTrigger extends AdvancementTrigger {
			public static final PlacedTileTrigger INSTANCE = new PlacedTileTrigger();

			protected PlacedTileTrigger() {
				super(new PlacedTileTriggerConditionHandler());
			}

			@Override
			protected void trigger0(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions) {
				if (conditions instanceof PlacedTileTriggerConditionHandler.PlacedTileTriggerConditions) {
					Item item = ((PlacedTileTriggerConditionHandler.PlacedTileTriggerConditions) conditions).item;
					Tile tile = ((PlacedTileTriggerConditionHandler.PlacedTileTriggerConditions) conditions).tile;
					int data = ((PlacedTileTriggerConditionHandler.PlacedTileTriggerConditions) conditions).data;
					int x = ((PlacedTileTriggerConditionHandler.PlacedTileTriggerConditions) conditions).x;
					int y = ((PlacedTileTriggerConditionHandler.PlacedTileTriggerConditions) conditions).y;
					int level = ((PlacedTileTriggerConditionHandler.PlacedTileTriggerConditions) conditions).level;
					for (ElementCriterion criterion : new HashSet<>(registeredCriteria)) {
						if (criterion.conditions instanceof PlacedTileTriggerConditionHandler.PlacedTileCriterionConditions) {
							PlacedTileTriggerConditionHandler.PlacedTileCriterionConditions criterionConditions =
								(PlacedTileTriggerConditionHandler.PlacedTileCriterionConditions) criterion.conditions;
							if (criterionConditions.tile != null && !criterionConditions.tile.equalsIgnoreCase(tile.name))
								continue;
							if (criterionConditions.item != null && !criterionConditions.item.matches(item))
								continue;
							if (criterionConditions.location != null && !criterionConditions.location.matches(tile, data, x, y, level))
								continue;
							if (criterionConditions.data != null && !criterionConditions.data.equals(data))
								continue;
							criterion.markAsCompleted(false, null); // All conditions passed.
						}
					}
				}
			}

			public static class PlacedTileTriggerConditionHandler extends AdvancementTriggerConditionHandler {
				@Override
				public @NotNull AdvancementCriterionConditions createCriterionConditions(JSONObject json) throws JSONException {
					String tile = json.optString("tile", null);
					AdvancementCriterionConditions.ItemConditions item =
						AdvancementCriterionConditions.ItemConditions.getFromJson(json.optJSONObject("item"));
					AdvancementCriterionConditions.LocationConditions location =
						AdvancementCriterionConditions.LocationConditions.getFromJson(json.optJSONObject("location"));
					Integer data = null;
					try {
						data = json.getInt("data");
					} catch (JSONException ignored) {
					}
					return new PlacedTileCriterionConditions(tile, item, location, data);
				}

				public static class PlacedTileTriggerConditions extends AdvancementTriggerConditions {
					private final Item item;
					private final Tile tile;
					private final int data;
					private final int x;
					private final int y;
					private final int level;

					public PlacedTileTriggerConditions(Item item, Tile tile, int data, int x, int y, int level) {
						this.item = item;
						this.tile = tile;
						this.data = data;
						this.x = x;
						this.y = y;
						this.level = level;
					}
				}

				public static class PlacedTileCriterionConditions extends AdvancementCriterionConditions {
					private final @Nullable String tile;
					private final @Nullable ItemConditions item;
					private final @Nullable LocationConditions location;
					private final @Nullable Integer data;

					private PlacedTileCriterionConditions(@Nullable String tile, @Nullable ItemConditions item,
					                                      @Nullable LocationConditions location, @Nullable Integer data) {
						this.tile = tile;
						this.item = item;
						this.location = location;
						this.data = data;
					}
				}
			}
		}

		public static class ItemUsedOnTileTrigger extends AdvancementTrigger {
			public static final ItemUsedOnTileTrigger INSTANCE = new ItemUsedOnTileTrigger();

			protected ItemUsedOnTileTrigger() {
				super(new ItemUsedOnTileTriggerConditionHandler());
			}

			@Override
			protected void trigger0(AdvancementTriggerConditionHandler.AdvancementTriggerConditions conditions) {
				if (conditions instanceof ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions) {
					Item item = ((ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions) conditions).item;
					Tile tile = ((ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions) conditions).tile;
					int data = ((ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions) conditions).data;
					int x = ((ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions) conditions).x;
					int y = ((ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions) conditions).y;
					int level = ((ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions) conditions).level;
					for (ElementCriterion criterion : new HashSet<>(registeredCriteria)) {
						if (criterion.conditions instanceof ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileCriterionConditions) {
							ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileCriterionConditions criterionConditions =
								(ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileCriterionConditions) criterion.conditions;
							if (criterionConditions.location != null && !criterionConditions.location.matches(tile, data, x, y, level))
								continue;
							if (criterionConditions.item != null && !criterionConditions.item.matches(item))
								continue;
							criterion.markAsCompleted(false, null); // All conditions passed.
						}
					}
				}
			}

			public static class ItemUsedOnTileTriggerConditionHandler extends AdvancementTriggerConditionHandler {
				@Override
				public @NotNull AdvancementCriterionConditions createCriterionConditions(JSONObject json) throws JSONException {
					AdvancementTriggerConditionHandler.AdvancementCriterionConditions.LocationConditions location =
						AdvancementTriggerConditionHandler.AdvancementCriterionConditions.LocationConditions.getFromJson(json.optJSONObject("location"));
					AdvancementTriggerConditionHandler.AdvancementCriterionConditions.ItemConditions item =
						AdvancementTriggerConditionHandler.AdvancementCriterionConditions.ItemConditions.getFromJson(json.optJSONObject("item"));
					return new ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileCriterionConditions(location, item);
				}

				public static class ItemUsedOnTileTriggerConditions extends AdvancementTriggerConditions {
					private final Item item;
					private final Tile tile;
					private final int data;
					private final int x;
					private final int y;
					private final int level;

					public ItemUsedOnTileTriggerConditions(Item item, Tile tile, int data, int x, int y, int level) {
						this.item = item;
						this.tile = tile;
						this.data = data;
						this.x = x;
						this.y = y;
						this.level = level;
					}
				}

				public static class ItemUsedOnTileCriterionConditions extends AdvancementCriterionConditions {
					private final @Nullable LocationConditions location;
					private final @Nullable ItemConditions item;

					private ItemUsedOnTileCriterionConditions(@Nullable LocationConditions location, @Nullable ItemConditions item) {
						this.location = location;
						this.item = item;
					}
				}
			}
		}
	}
}
