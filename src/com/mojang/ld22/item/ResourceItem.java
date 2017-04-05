package com.mojang.ld22.item;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.resource.ItemResource;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.ModeMenu;

public class ResourceItem extends Item {
	public Resource resource; // The resource of this item
	public int count = 1;
	public int level = 0;
	public int amount = 1; // The amount of resources
	
	public ResourceItem(Resource resource) {
		this.resource = resource;
	}
	
	public ResourceItem(Resource resource, int count) {
		this.resource = resource;
		this.count = count;
	}
	
	public ResourceItem addamount(int amount) {
		this.amount = amount;
		return this;
	}
	
	public int getColor() {
		return resource.color;
	}

	public int getSprite() {
		return resource.sprite;
	}

	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, resource.sprite, resource.color, 0);
	}
	
	/** Renders the icon, name, and count of the resource. */
	public void renderInventory(Screen screen, int x, int y, boolean ininv) {
		screen.render(x, y, resource.sprite, resource.color, 0); // renders the icon
		String name = resource.name; // draws the name of the resource
		if(name.length() > 11 && !ininv) { // only draw part of the name if it's too long, and in the inventory.
			Font.draw(name.substring(0, 11), screen, x + 32, y, Color.get(-1, 555, 555, 555));
		} else {
			Font.draw(name, screen, x + 32, y, Color.get(-1, 555, 555, 555));
		}

		int cc = count; // count of the resource
		if(cc > 999) { // If the resource count is above 999, then just render 999 (for spacing reasons)
			cc = 999;
		}

		Font.draw("" + cc, screen, x + 8, y, Color.get(-1, 444, 444, 444)); // draws the resource count
	}
	
	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, resource.sprite, resource.color, 0);
		Font.draw(resource.name, screen, x + 32, y, Color.get(-1, 555, 555, 555));
		int cc = count;
		if (cc > 999) cc = 999;
		Font.draw("" + cc, screen, x + 8, y, Color.get(-1, 444, 444, 444));
	}

	public String getName() {
		return resource.name;
	}
	
	/** What happens when you pick up the item off the ground */
	public void onTake(ItemEntity itemEntity) {}
	
	/** What happens when you try to use this item on a tile. */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (resource.interactOn(tile, level, xt, yt, player, attackDir)) { // Calls the resource's 'interactOn()' method as a check
			if (!ModeMenu.creative)
				count--; // interaction was successful, meaning the item was used; so remove it.
			return true;
		}
		return false;
	}
	
	/** If the count is equal to, or less than 0. Then this will return true. */
	public boolean isDepleted() {
		return count <= 0;
	}
}
