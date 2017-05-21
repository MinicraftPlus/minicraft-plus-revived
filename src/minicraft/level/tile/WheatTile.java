package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class WheatTile extends Tile {
	
	protected static void addInstances() {
		Tiles.add(new WheatTile("Wheat"));
	}
	
	private WheatTile(String name) {
		super(name, (Sprite)null);
	}

	public void render(Screen screen, Level level, int x, int y) {
		int age = level.getData(x, y);
		int icon = age / 10;
		
		int col = Color.get(301, 411, 321, 50);
		int col1 = Color.get(301, 411, 50 + (icon) * 100, 40 + (icon - 3) * 2 * 100);
		int col2 = Color.get(0, 0, 50 + (icon) * 100, 40 + (icon - 3) * 2 * 100);
		
		if (icon >= 3) {
			col = col1;
			if (age == 50) {
				col = col2;
			}
			icon = 3;
		}

		screen.render(x * 16 + 0, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 4 + 3 * 32 + icon, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
		screen.render(x * 16 + 8, y * 16 + 8, 4 + 3 * 32 + icon, col, 1);
	}
	
	public boolean IfWater(Level level, int xs, int ys) {
		Tile[] areaTiles = level.getAreaTiles(xs, ys, 1);
		for(Tile t: areaTiles)
			if(t == Tiles.get("Water"))
				return true;
		
		return false;
	}
	
	public void tick(Level level, int xt, int yt) {
		if (random.nextInt(2) == 0) return;

		int age = level.getData(xt, yt);
		if (!IfWater(level, xt, yt)) {
			if (age < 50) level.setData(xt, yt, age + 1);
		} else if (IfWater(level, xt, yt)) {
			if (age < 50) level.setData(xt, yt, age + 2);
		}
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Shovel) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tiles.get("dirt"), 0);
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
		
		level.dropItem(x*16, y*16, 1, 2, Items.get("seeds"));
		
		int count = 0;
		if (age >= 50) {
			count = random.nextInt(3) + 2;
		} else if (age >= 40) {
			count = random.nextInt(2) + 1;
		}
		
		level.dropItem(x*16, y*16, count, Items.get("Wheat"));
		
		if (age >= 50) {
			Player.score += random.nextInt(5) + 1;
		}
		level.setTile(x, y, Tiles.get("dirt"), 0);
	}
}
