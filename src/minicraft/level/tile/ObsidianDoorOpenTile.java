package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.Mob;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.level.Level;

public class ObsidianDoorOpenTile extends Tile {
	public ObsidianDoorOpenTile(int id) {
		super(id);
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(203, 102);
		
		screen.render(x * 16 + 0, y * 16 + 0, 0 + 24 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 1 + 24 * 32, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 0 + 25 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 1 + 25 * 32, col, 0);
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		level.setTile(x, y, Tile.odc, 0);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canWool();
	}
}
