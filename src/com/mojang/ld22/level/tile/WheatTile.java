package com.mojang.ld22.level.tile;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;

public class WheatTile extends Tile {
	public WheatTile(int id) {
		super(id);
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		int age = level.getData(x, y);
		int icon = age / 10;
		
		int col0 = Color.get(201, 311, 322, 40);
		int col00 = Color.get(201, 311, 40 + (icon) * 100, 30 + (icon - 3) * 2 * 100);
		int col000 = Color.get(0, 0, 40 + (icon) * 100, 30 + (icon - 3) * 2 * 100);
		
		int col1 = Color.get(301, 411, 321, 50);
		int col11 = Color.get(301, 411, 50 + (icon) * 100, 40 + (icon - 3) * 2 * 100);
		int col111 = Color.get(0, 0, 50 + (icon) * 100, 40 + (icon - 3) * 2 * 100);
		
		int col2 = Color.get(201, 311, 211, 40);
		int col22 = Color.get(201, 311, 40 + (icon) * 100, 30 + (icon - 3) * 2 * 100);
		int col222 = Color.get(0, 0, 40 + (icon) * 100, 30 + (icon - 3) * 2 * 100);
		
		int col3 = Color.get(101, 211, 100, 30);
		int col33 = Color.get(101, 211, 30 + (icon) * 100, 20 + (icon - 3) * 2 * 100);
		int col333 = Color.get(0, 0, 30 + (icon) * 100, 20 + (icon - 3) * 2 * 100);
		
		int col4 = Color.get(301, 411, 222, 50);
		int col44 = Color.get(301, 411, 50 + (icon) * 100, 40 + (icon - 3) * 2 * 100);
		int col444 = Color.get(0, 0, 50 + (icon) * 100, 40 + (icon - 3) * 2 * 100);
		
		
		
		if (level.dirtColor == 322){
		
		if (Game.Time == 0){
			int col = col0;
			if (icon >= 3) {
				col = col00;
				if (age == 50) {
					col = col000;
				}
				icon = 3;
			}
			
		screen.render(x * 16 + 0, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
		screen.render(x * 16 + 8, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
		}
		if (Game.Time == 1){
			int col = col1;
			if (icon >= 3) {
				col = col11;
				if (age == 50) {
					col = col111;
				}
				icon = 3;
			}
		screen.render(x * 16 + 0, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
		screen.render(x * 16 + 8, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
		}
		if (Game.Time == 2){
			int col = col2;
			if (icon >= 3) {
				col = col22;
				if (age == 50) {
					col = col222;
				}
				icon = 3;
			}
		screen.render(x * 16 + 0, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
		screen.render(x * 16 + 8, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
		}
		if (Game.Time == 3){
			int col = col3;
			if (icon >= 3) {
				col = col33;
				if (age == 50) {
					col = col333;
				}
				icon = 3;
			}
		screen.render(x * 16 + 0, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
		screen.render(x * 16 + 8, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
		}
		}
		if (level.dirtColor == 222){
			int col = col4;
			if (icon >= 3) {
				col = col44;
				if (age == 50) {
					col = col444;
				}
				icon = 3;
			}
		screen.render(x * 16 + 0, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
		screen.render(x * 16 + 8, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);	
		}
	}
	
	public boolean IfWater(Level level, int xs, int ys){
		if (level.getTile(xs - 1, ys) == Tile.water){
			return true;
		}
		if (level.getTile(xs + 1, ys) == Tile.water){
			return true;
		}
		if (level.getTile(xs, ys + 1) == Tile.water){
			return true;
		}
		if (level.getTile(xs, ys - 1) == Tile.water){
			return true;
		}
		if (level.getTile(xs - 1, ys - 1) == Tile.water){
			return true;
		}
		if (level.getTile(xs + 1, ys + 1) == Tile.water){
			return true;
		}
		if (level.getTile(xs - 1, ys + 1) == Tile.water){
			return true;
		}
		if (level.getTile(xs + 1, ys - 1) == Tile.water){
			return true;
		}
		return false;
	}
	
	public void tick(Level level, int xt, int yt) {
		if (random.nextInt(2) == 0) return;
		
		int age = level.getData(xt, yt);
		if (!IfWater(level, xt, yt)){
		if (age < 50) level.setData(xt, yt, age + 1);
		} else if (IfWater(level, xt, yt)){
		if (age < 50) level.setData(xt, yt, age + 2);
		}
	}
	
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.shovel) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.dirt, 0);
					return true;
				}
			}
		}
		return false;
	}
	
	public void steppedOn(Level level, int xt, int yt, Entity entity) {
		if (random.nextInt(60) != 0) return;
		if (level.getData(xt, yt) < 2) return;
		harvest(level, xt, yt);
	}
	
	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
	
		harvest(level, x, y);
	}
	
	private void harvest(Level level, int x, int y) {
		int age = level.getData(x, y);
		
		int count = random.nextInt(2) + 1;
		for (int i = 0; i < count; i++) {
			level.add(new ItemEntity(new ResourceItem(Resource.seeds), x * 16 + random.nextInt(10) + 3, y * 16 + random.nextInt(10) + 3));
		}
		
		count = 0;
		if (age >= 50) {
			count = random.nextInt(3) + 2;
		} else if (age >= 40) {
			count = random.nextInt(2) + 1;
		}
		for (int i = 0; i < count; i++) {
			level.add(new ItemEntity(new ResourceItem(Resource.wheat), x * 16 + random.nextInt(10) + 3, y * 16 + random.nextInt(10) + 3));
		}
		if (age >= 50){
		Player.score = Player.score + random.nextInt(5) + 1;
		}
		level.setTile(x, y, Tile.dirt, 0);
	}
}
	