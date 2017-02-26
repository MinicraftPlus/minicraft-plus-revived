package com.mojang.ld22.level.tile;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.screen.StartMenu;
import com.mojang.ld22.sound.Sound;

public class LavaBrickTile extends Tile {
	public LavaBrickTile(int id) {
		super(id);
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(300, 300, 400, 400);
		screen.render(x * 16 + 0, y * 16 + 0, 19 + 2 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 19 + 2 * 32, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 19 + 2 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 19 + 2 * 32, col, 0);
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.pickaxe) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.lava, 0);
					Sound.monsterHurt.play();
					return true;
				}
			}
		}
		return false;
	}
	
	public void bumpedInto(Level level, int x, int y, Entity entity) {
		entity.hurt(this, x, y, 3);
	}
	
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canWool();
	}
}
	