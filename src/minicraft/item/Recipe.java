package minicraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.BucketItem;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.Items;
import minicraft.screen.ListItem;
import minicraft.screen.ModeMenu;

public class Recipe implements ListItem {
	public HashMap<String, Integer> costs = new HashMap<String, Integer>();  // A list of costs for the recipe
	private String product; // the result item of the recipe
	public int amount;
	public boolean canCraft; // checks if the player can craft the recipe
	
	//public Recipe(String made, Item[]... req) { this(made, 1, req); }
	//public Recipe(Item made, int amt, Item[]... req) { this(made, amt, req); }
	public Recipe(String createdItem, String... reqItems) {
		canCraft = false;
		String[] sep = createdItem.split("_");
		product = sep[0].toUpperCase(); // assigns the result item
		amount = Integer.parseInt(sep[1]);
		//if(product instanceof StackableItem)
			//amount *= ((StackableItem)product).count;
		
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
	
	/*
	private void addCost(Item add, Item key, int amt) {
		if(key == null) key = add;
		if(!costs.containsKey(key)) costs.put(key, 0);
		
		if(add instanceof StackableItem)
			costs.put(key, costs.get(key)+((StackableItem)add).count*amt);
		else
			costs.put(key, costs.get(key)+amt);
	}*/
	
	/** Adds a item cost to the list; requires a type of item, and an amount of it. */
	/*public Recipe addCost(Item item, int count) {
		costs.put();
		return this;
	}*/
	/*
	public Recipe addCostBucketLava(int counts) {
		this.costs.add(new BucketItem());
		return this;
	}
	
	public Recipe addCostTool(ToolType tool, int level, int counts) {
		costs.add(new ToolItem(tool, level));
		return this;
	}
	*/
	
	public void checkCanCraft(Player player) { canCraft = getCanCraft(player); }
	/** Checks if the player can craft the recipe */
	private boolean getCanCraft(Player player) {
		if(ModeMenu.creative) return true;
		
		for (String cost: costs.keySet().toArray(new String[0])) { //cycles through the costs list
			/// this method ONLY WORKS if costs does not contain two elements such that inventory.count will count an item it contains as matching more than once.
			if(player.inventory.count(Items.get(cost)) < costs.get(cost)) {
				return false;
			}
			/*
			if (item instanceof StackableItem) {
				// if the item is a item, convert it to a StackableItem.
				StackableItem stack = (StackableItem) item;
				if (player.inventory.count(item) < stack.count) {
					//if the player doesn't have the items, then the recipe cannot be crafted.
					//canCraft = false;
					return false;
				}
			} else if (item instanceof ToolItem) {
				// if the item is a tool, convert it to a tool.
				ToolItem ti = (ToolItem) item;
				if (player.inventory.count(item) == 0) {
					// some recipes require tools to craft, such as the claymores.
					//canCraft = false;
					return false;
				}
			} else {
				if(player.inventory.count(item) == 0) {
					//canCraft = false;
					return false;
				}
			}*/
		}
		
		return true;
	}
	
	/** Renders the icon & text of an item to the screen. */
	public void renderInventory(Screen screen, int x, int y) {
		getProduct().sprite.render(screen, x, y); //renders the item sprite.
		int textColor = canCraft ? Color.get(-1, 555) : Color.get(-1, 222); // gets the text color, based on whether the player can craft the item.
		
		String amountIndicator = amount > 1 ? " x"+amount : "";
		Font.draw(product + amountIndicator, screen, x + 8, y, textColor); // draws the text to the screen
	}
	
	// (WAS) abstract method given to the sub-recipe classes.
	public boolean craft(Player player) {
		if(!getCanCraft(player)) return false;
		
		// remove the cost items from the inventory.
		for (String cost: costs.keySet().toArray(new String[0])) {
			player.inventory.removeItem(Items.get(cost));
		}
		
		// add the crafted items.
		if (product.equals("ARROW"))
			player.ac += amount;
		else {
			for(int i = 0; i < amount; i++) {
				player.inventory.add(getProduct());
			}
		}
		
		return true;
	}
	
	/** removes the items from your inventory */
	/*private void deductCost(Player player) {
		
	}*/
}
