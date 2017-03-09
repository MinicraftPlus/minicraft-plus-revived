package com.mojang.ld22.crafting;

import java.util.ArrayList;
import java.util.List;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ListItem;

public abstract class Recipe implements ListItem {
	public List<Item> costs = new ArrayList<Item>();
	public boolean canCraft = false;
	private Resource resource;
	public Item resultTemplate;
	
	public Recipe(Item resultTemplate) {
		this.resultTemplate = resultTemplate;
	}
	
	public Recipe addCost(Resource resource, int count) {
		costs.add(new ResourceItem(resource, count));
		return this;
	}
	public Recipe addCostTool(ToolType tool, int level, int counts) {
		costs.add(new ToolItem(tool, level));
		return this;
	}
	
	public void checkCanCraft(Player player) {
		for (int i = 0; i < costs.size(); i++) {
			Item item = costs.get(i);
			if (item instanceof ResourceItem) {
				ResourceItem ri = (ResourceItem) item;
				if (!player.inventory.hasResources(ri.resource, ri.count)) {
					canCraft = false;
					return;
				}
			}
			else if (item instanceof ToolItem) {
				ToolItem ti = (ToolItem) item;
				if (!player.inventory.hasTools(ti.type, ti.level)) {
					canCraft = false;
					return;
				}
			}
		}
		canCraft = true;
	}
	
	
	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, resultTemplate.getSprite(), resultTemplate.getColor(), 0);
		int textColor = canCraft ? Color.get(-1, 555, 555, 555) : Color.get(-1, 222, 222, 222);
		Font.draw(resultTemplate.getName(), screen, x + 8, y, textColor);
		if (resultTemplate.getName() == "Torch"){
			Font.draw(resultTemplate.getName() + " x2", screen, x + 8, y, textColor);
		}
		if (resultTemplate.getName() == "string"){
			Font.draw(resultTemplate.getName() + " x2", screen, x + 8, y, textColor);
		}
		if (resultTemplate.getName() == "Plank"){
			Font.draw(resultTemplate.getName() + " x2", screen, x + 8, y, textColor);
		}
		if (resultTemplate.getName() == "St.Brick"){
			Font.draw(resultTemplate.getName() + " x2", screen, x + 8, y, textColor);
		}
	}
	
	public abstract void craft(Player player);
	
	public void deductCost(Player player) {
		for (int i = 0; i < costs.size(); i++) {
			Item item = costs.get(i);
			if (item instanceof ResourceItem) {
				ResourceItem ri = (ResourceItem) item;
				player.inventory.removeResource(ri.resource, ri.count);
			}
			else if (item instanceof ToolItem) {
				ToolItem ti = (ToolItem) item;
			player.inventory.removeTool(ti.type, ti.level);	
			}
		}
	}
}