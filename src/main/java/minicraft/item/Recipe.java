package minicraft.item;

import java.util.HashMap;
import java.util.List;

import javafx.util.Pair;
import minicraft.core.Game;
import minicraft.entity.mob.Player;

public class Recipe {
	private HashMap<String, Integer> costs = new HashMap<String, Integer>();  // A list of costs for the recipe
	private String product; // The result item of the recipe
	private int amount;
	private boolean canCraft; // Checks if the player can craft the recipe
	
	public Recipe(String createdItem, int amount, List<Pair<String, Integer>> reqItems) {
		this.product = createdItem;
		this.amount = amount;
		
		for (int i = 0; i < reqItems.size(); i++) {
			 Pair<String, Integer> curSep = reqItems.get(i);
			 String curItem = curSep.getKey(); // The current cost that's being added to costs.
			 int amt = curSep.getValue();
			 boolean added = false;
			
			for (String cost: costs.keySet().toArray(new String[0])) { // Loop through the costs that have already been added
				 if (cost.equals(curItem)) {
					 costs.put(cost, costs.get(cost)+amt);
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
	public HashMap<String, Integer> getCosts() { return costs; }
	
	public int getAmount() { return amount; }
	public boolean getCanCraft() { return canCraft; }
	public boolean checkCanCraft(Player player) {
		canCraft = getCanCraft(player);
		return canCraft;
	}
	/** Checks if the player can craft the recipe */
	private boolean getCanCraft(Player player) {
		if (Game.isMode("creative")) return true;
		
		for (String cost: costs.keySet().toArray(new String[0])) { // Cycles through the costs list
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
		
		if (!Game.isMode("creative")) {
			// Remove the cost items from the inventory.
			for (String cost: costs.keySet().toArray(new String[0])) {
				player.getInventory().removeItems(Items.get(cost), costs.get(cost));
			}
		}
		
		// Rdd the crafted items.
		for (int i = 0; i < amount; i++)
			 player.getInventory().add(getProduct());
		
		return true;
	}
}
