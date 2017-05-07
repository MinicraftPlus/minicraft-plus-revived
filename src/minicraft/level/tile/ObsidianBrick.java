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
import minicraft.sound.Sound;

public class ObsidianBrick extends Tile {
	public ObsidianBrick(int id) {
		super(id);
		maySpawn = true;
	}

	public void render(Screen screen, Level level, int x, int y) {

		int col = Color.get(102, 102, 203, 203);

		/*int col1 = Color.get(102, 102, 203, 203);

		int col2 = Color.get(102, 102, 203, 203);

		int col3 = Color.get(000, 000, 203, 102);//int col3 = Color.get(002, 002, 203, 203);

		int col4 = Color.get(000, 000, 103, 103);

		if (level.dirtColor == 322) {

			if (Game.time == 0) {
				int col = col0;
				*/screen.render(x * 16 + 0, y * 16 + 0, 19 + 2 * 32, col, 0);
				screen.render(x * 16 + 8, y * 16 + 0, 19 + 2 * 32, col, 0);
				screen.render(x * 16 + 0, y * 16 + 8, 19 + 2 * 32, col, 0);
				screen.render(x * 16 + 8, y * 16 + 8, 19 + 2 * 32, col, 0);
			/*}
		}
		if (Game.time == 1) {
			int col = col1;
			screen.render(x * 16 + 0, y * 16 + 0, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 19 + 2 * 32, col, 0);
		}

		if (Game.time == 2) {
			int col = col2;
			screen.render(x * 16 + 0, y * 16 + 0, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 19 + 2 * 32, col, 0);
		}
		if (Game.time == 3) {
			int col = col3;
			screen.render(x * 16 + 0, y * 16 + 0, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 19 + 2 * 32, col, 0);
		}

		if (level.dirtColor == 222) {
			int col = col4;
			screen.render(x * 16 + 0, y * 16 + 0, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 0, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 0, y * 16 + 8, 19 + 2 * 32, col, 0);
			screen.render(x * 16 + 8, y * 16 + 8, 19 + 2 * 32, col, 0);
		}*/
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.pickaxe) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.hole, 0);
					Sound.monsterHurt.play();
					return true;
				}
			}
			if (tool.type == ToolType.pick) {
				if (player.payStamina(4 - tool.level)) {
					level.setTile(xt, yt, Tile.hole, 0);
					Sound.monsterHurt.play();
					return true;
				}
			}
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
