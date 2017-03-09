package com.mojang.ld22.item;

import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.Furniture;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.BookTestMenu;
import com.mojang.ld22.screen.CraftingMenu;

public class BucketWaterItem extends Item {
	public int getColor() {
		return Color.get(-1, 222, 005, 555);
	}
	
	public int getSprite() {
		return 21 + 4 * 32;
	}
	
	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
	}
	
	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
		Font.draw(getName(), screen, x + 8, y, Color.get(-1, 555, 555, 555));
	}
	
	
	public String getName() {
		return "W.Bucket";
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (tile == Tile.hole){
			level.setTile(xt, yt, Tile.water, 0);
		Item item = (new BucketItem());
		player.activeItem = item;
		}
			return true;
	}
}