package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class FarmTile extends Tile {
	public FarmTile(int id) {
		super(id);
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col0 = Color.get(201, 311, 322, 433);
		int col1 = Color.get(301, 411, 422, 533);
		int col2 = Color.get(201, 311, 322, 433);
		int col3 = Color.get(101, 211, 222, 333);
		int col4 = Color.get(301, 411, 422, 533);

		if (level.dirtColor == 322) {

			if (Game.time == 0) {
				int col = col0;
				screen.render(x * 16 + 0, y * 16 + 0, 2 + 32, col, 1);
				screen.render(x * 16 + 8, y * 16 + 0, 2 + 32, col, 0);
				screen.render(x * 16 + 0, y * 16 + 8, 2 + 32, col, 0);
				screen.render(x * 16 + 8, y * 16 + 8, 2 + 32, col, 1);
			}
			if (Game.time == 1) {
				int col = col1;
				screen.render(x * 16 + 0, y * 16 + 0, 2 + 32, col, 1);
				screen.render(x * 16 + 8, y * 16 + 0, 2 + 32, col, 0);
				screen.render(x * 16 + 0, y * 16 + 8, 2 + 32, col, 0);
				screen.render(x * 16 + 8, y * 16 + 8, 2 + 32, col, 1);
			}
			if (Game.time == 2) {
				int col = col2;
				screen.render(x * 16 + 0, y * 16 + 0, 2 + 32, col, 1);
				screen.render(x * 16 + 8, y * 16 + 0, 2 + 32, col, 0);
				screen.render(x * 16 + 0, y * 16 + 8, 2 + 32, col, 0);
				screen.render(x * 16 + 8, y * 16 + 8, 2 + 32, col, 1);
			}
			if (Game.time == 3) {
				int col = col3;
				screen.render(x * 16 + 0, y * 16 + 0, 2 + 32, col, 1);
				screen.render(x * 16 + 8, y * 16 + 0, 2 + 32, col, 0);
				screen.render(x * 16 + 0, y * 16 + 8, 2 + 32, col, 0);
				screen.render(x * 16 + 8, y * 16 + 8, 2 + 32, col, 1);
			}
		}
		if (level.dirtColor == 222) {
			int col = col4;
			screen.render(x * 16 + 0, y * 16 + 0, 2 + 32, col, 1);
			screen.render(x * 16 + 8, y * 16 + 0, 2 + 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 2 + 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 2 + 32, col, 1);
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

	public void tick(Level level, int xt, int yt) {
		int age = level.getData(xt, yt);
		if (age < 5) level.setData(xt, yt, age + 1);
	}

	public void steppedOn(Level level, int xt, int yt, Entity entity) {
		if (random.nextInt(60) != 0) return;
		if (level.getData(xt, yt) < 5) return;
		level.setTile(xt, yt, Tile.dirt, 0);
	}
}
