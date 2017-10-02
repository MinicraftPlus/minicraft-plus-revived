package minicraft.item;

import java.util.HashMap;

import minicraft.Game;
import minicraft.entity.Player;

public class Recipe {
	public HashMap<String, Integer> costs = new HashMap<String, Integer>();  // A list of costs for the recipe
	private String product; // the result item of the recipe
	public int amount;
	public boolean canCraft; // checks if the player can craft the recipe
	
	public Recipe(String createdItem, String... reqItems) {
		canCraft = false;
		String[] sep = createdItem.split("_");
		product = sep[0].toUpperCase(); // assigns the result item
		amount = Integer.parseInt(sep[1]);
		
		for(int i = 0; i < reqItems.length; i++) {
			String[] curSep = reqItems[i].split("_");
			String curItem = curSep[0].toUpperCase(); // the current cost that's being added to costs.
			int amt = Integer.parseInt(curSep[1]);
			boolean added = false;
			for(String cost: costs.keySet().toArray(new String[0])) { // loop through the costs that have already been added
				if(cost.equals(curItem)) {
					costs.put(cost, costs.get(cost)+amt);
					added = true;
					break;
				}
			}
			
			if(added) continue;
			costs.put(curItem, amt);
		}
	}
	
	public Item getProduct() {
		return Items.get(product);
	}
	
	public void checkCanCraft(Player player) { canCraft = getCanCraft(player); }
	/** Checks if the player can craft the recipe */
	private boolean getCanCraft(Player player) {
		if(Game.isMode("creative")) return true;
		
		for (String cost: costs.keySet().toArray(new String[0])) { //cycles through the costs list
			/// this method ONLY WORKS if costs does not contain two elements such that inventory.count will count an item it contains as matching more than once.
			if(player.inventory.count(Items.get(cost)) < costs.get(cost)) {
				return false;
			}
		}
		
		return true;
	}
	
	/** Renders the icon & text of an item to the screen. */
	/*public void renderInventory(Screen screen, int x, int y) {
		getProduct().sprite.render(screen, x, y); //renders the item sprite.
		int textColor = canCraft ? Color.WHITE : Color.DARK_GRAY; // gets the text color, based on whether the player can craft the item.
		
		String amountIndicator = amount > 1 ? " x"+amount : "";
		Font.draw(product + amountIndicator, screen, x + 8, y, textColor); // draws the text to the screen
	}*/
	
	// (WAS) abstract method given to the sub-recipe classes.
	public boolean craft(Player player) {
		if(!getCanCraft(player)) return false;
		
		if(!Game.isMode("creative")) {
			// remove the cost items from the inventory.
			for (String cost: costs.keySet().toArray(new String[0])) {
				player.inventory.removeItems(Items.get(cost), costs.get(cost));
			}
		}
		
		// add the crafted items.
		for(int i = 0; i < amount; i++)
			player.inventory.add(getProduct());
		
		return true;
	}
}
