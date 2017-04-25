package com.mojang.ld22.crafting;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.BucketLavaItem;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ListItem;
import com.mojang.ld22.screen.ModeMenu;
import java.util.ArrayList;
import java.util.List;

/// Is abstract; can't be instantiated.
public abstract class Recipe implements ListItem {
	public List<Item> costs = new ArrayList<Item>();  // A list of costs for the recipe
	public boolean canCraft = false; // checks if the player can craft the recipe
	public Item resultTemplate; // the result item of the recipe
	Resource resource;
	public ResourceItem ri;

	public Recipe(Item resultTemplate) {
		this.resultTemplate = resultTemplate; // assigns the result item
	}
	
	/** Adds a resource cost to the list; requires a type of resource, and an amount of it. */
	public Recipe addCost(Resource resource, int count) {
		costs.add(new ResourceItem(resource, count));
		return this;
	}
	
	public Recipe addCostBucketLava(int counts) {
		this.costs.add(new BucketLavaItem());
		return this;
	}
	
	public Recipe addCostTool(ToolType tool, int level, int counts) {
		costs.add(new ToolItem(tool, level));
		return this;
	}
	
	/** Checks if the player can craft the recipe */
	public void checkCanCraft(Player player) {
		for (int i = 0; i < costs.size(); i++) { //cycles through the costs list
			Item item = costs.get(i); //current item on the list
			if (item instanceof ResourceItem) {
				// if the item is a resource, convert it to a ResourceItem.
				ResourceItem ri = (ResourceItem) item;
				if (!player.inventory.hasResources(ri.resource, ri.count) && !ModeMenu.creative) {
					//if the player doesn't have the resources, then the recipe cannot be crafted.
					canCraft = false;
					return;
				}
			} else if (item instanceof ToolItem) {
				// if the item is a tool, convert it to a tool.
				ToolItem ti = (ToolItem) item;
				if (!player.inventory.hasTools(ti.type, ti.level) && !ModeMenu.creative) {
					// some recipes require tools to craft, such as the claymores.
					canCraft = false;
					return;
				}
			} else {
				if(!player.inventory.hasItem(item) && !ModeMenu.creative) {
					this.canCraft = false;
					return;
				}
			}
		}
		
		canCraft = true; // if not returned before here, the recipe can be crafted.
	}
	
	/** Renders the icon & text of an item to the screen. */
	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, resultTemplate.getSprite(), resultTemplate.getColor(), 0); //renders the item sprite.
		int textColor = canCraft ? Color.get(-1, 555, 555, 555) : Color.get(-1, 222, 222, 222); // gets the text color, based on whether the player can craft the item.
		Font.draw(resultTemplate.getName(), screen, x + 8, y, textColor); // draws the text to the screen
		
		// These 4 items are crafted in twos.
		String name = resultTemplate.getName();
		if(name == "Torch" || name == "string" || name == "Plank" || name == "St.Brick") {
			Font.draw(resultTemplate.getName() + " x2", screen, x + 8, y, textColor);
		}
		
		if(name == "arrow") {
			ri = (ResourceItem)resultTemplate;
			Font.draw(resultTemplate.getName() + " x" + ri.amount, screen, x + 8, y, textColor);
		}
	}
	
	public abstract void craft(Player player); // abstract method given to the sub-recipe classes.
	
	/** removes the resources from your inventory */
	public void deductCost(Player player) {
		for (int i = 0; i < costs.size(); i++) { //loops through the costs
			Item item = costs.get(i); //gets the current item in costs
			
			// convert and remove the item, whether it's a tool or a resource, or something else:
			if (item instanceof ResourceItem) {
				ResourceItem ri = (ResourceItem) item;
				player.inventory.removeResource(ri.resource, ri.count);
			} else if (item instanceof ToolItem) {
				ToolItem ti = (ToolItem) item;
				player.inventory.removeTool(ti.type, ti.level);
			} else {
				player.inventory.removeItem(item);
			}
		}
	}
}
