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
	public Resource resource;
	public ToolType tool;
	public int count = 1;
	public int counts = 1;
	public int level = 0;

	public ResourceItem(Resource resource) {
		this.resource = resource;
	}

	public ResourceItem(Resource resource, int count) {
		this.resource = resource;
		this.count = count;
	}
	
	public void ToolItem(ToolType tool, int level, int counts) {
		this.tool = tool;
		this.level = level;
		this.counts = counts;
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

	public void onTake(ItemEntity itemEntity) {
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (resource.interactOn(tile, level, xt, yt, player, attackDir)) {
			count--;
			if (ModeMenu.creative) {
			if (count <= 0) count = 1;
			}
			return true;
		}
		return false;
	}

	public boolean isDepleted() {
		if (ItemResource.dur == 0){
		if (getName() == "Fish Rod"){
			ItemResource.dur = 15;
			count--;
			if (count == 0){
			Game.truerod = false;
			return true;
			} else if (count > 0){
			return false;
			}
		}
		}
		return count <= 0;
	}

}