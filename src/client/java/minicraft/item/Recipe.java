package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.mob.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Recipe {
	private final TreeMap<String, Integer> costs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);  // A list of costs for the recipe
	private final String product; // The result item of the recipe
	private final int amount;
	private boolean canCraft; // Checks if the player can craft the recipe

	public Recipe(String createdItem, String... reqItems) {
		canCraft = false;
		String[] sep = createdItem.split("_");
		product = sep[0].toUpperCase(); // Assigns the result item
		amount = Integer.parseInt(sep[1]);

		for (String reqItem : reqItems) {
			String[] curSep = reqItem.split("_");
			String curItem = curSep[0].toUpperCase(); // The current cost that's being added to costs.
			int amt = Integer.parseInt(curSep[1]);
			boolean added = false;

			for (String cost : costs.keySet().toArray(new String[0])) { // Loop through the costs that have already been added
				if (cost.equals(curItem)) {
					costs.put(cost, costs.get(cost) + amt);
					added = true;
					break;
				}
			}

			if (added) continue;
			costs.put(curItem, amt);
		}
	}

	public Item getProduct() {
		return Items.get(product);
	}

	public Map<String, Integer> getCosts() {
		return new HashMap<>(costs);
	}

	public int getAmount() {
		return amount;
	}

	public boolean getCanCraft() {
		return canCraft;
	}

	public boolean checkCanCraft(Player player) {
		canCraft = getCanCraft(player);
		return canCraft;
	}

	/**
	 * Checks if the player can craft the recipe
	 */
	private boolean getCanCraft(Player player) {
		if (Game.isMode("minicraft.settings.mode.creative")) return true;

		for (String cost : costs.keySet().toArray(new String[0])) { // Cycles through the costs list
			/// This method ONLY WORKS if costs does not contain two elements such that inventory.count will count an item it contains as matching more than once.
			if (player.getInventory().count(Items.get(cost)) < costs.get(cost)) {
				return false;
			}
		}

		return true;
	}

	// (WAS) abstract method given to the sub-recipe classes.
	public boolean craft(Player player) {
		if (!getCanCraft(player)) return false;

		if (!Game.isMode("minicraft.settings.mode.creative")) {
			// Remove the cost items from the inventory.
			for (String cost : costs.keySet().toArray(new String[0])) {
				player.getInventory().removeItems(Items.get(cost), costs.get(cost));
			}
		}

		// Rdd the crafted items.
		for (int i = 0; i < amount; i++) {
			Item product = getProduct();
			if (player.getInventory().add(product) != null)
				player.getLevel().dropItem(player.x, player.y, product);
		}

		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Recipe) {
			Recipe r = (Recipe) obj;
			return this.amount == r.amount &&
				this.product.equalsIgnoreCase(r.product) &&
				this.costs.equals(r.costs);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		int result = costs.hashCode();
		result = 31 * result + product.hashCode();
		result = 31 * result + amount;
		return result;
	}

	@Override
	public String toString() {
		return product + ":" + amount +
			"[" + String.join(";", costs.entrySet().stream().<CharSequence>map(e -> e.getKey() + ":" + e.getValue())::iterator) + "]";
	}
}
