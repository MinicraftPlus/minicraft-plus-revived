package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Mob;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.level.Level;

public class SaplingTile extends Tile {
	private Tile onType;
	private Tile growsTo;

	public SaplingTile(int id, Tile onType, Tile growsTo) {
		super(id);
		this.onType = onType;
		this.growsTo = growsTo;
		connectsToSand = onType.connectsToSand;
		connectsToGrass = onType.connectsToGrass;
		connectsToWater = onType.connectsToWater;
		connectsToLava = onType.connectsToLava;
		maySpawn = onType.maySpawn;
	}

	public void render(Screen screen, Level level, int x, int y) {
		int col0 = Color.get(10, 30, 40, -1);
		int col1 = Color.get(20, 40, 50, -1);
		int col2 = Color.get(10, 30, 40, -1);
		int col3 = Color.get(0, 20, 30, -1);
		int col4 = Color.get(20, 40, 50, -1);

		onType.render(screen, level, x, y);

		if (level.dirtColor == 322) {

			if (Game.time == 0) {
				int col = col0;
				screen.render(x * 16 + 4, y * 16 + 4, 11 + 3 * 32, col, 0);
			}
			if (Game.time == 1) {
				int col = col1;
				screen.render(x * 16 + 4, y * 16 + 4, 11 + 3 * 32, col, 0);
			}
			if (Game.time == 2) {
				int col = col2;
				screen.render(x * 16 + 4, y * 16 + 4, 11 + 3 * 32, col, 0);
			}
			if (Game.time == 3) {
				int col = col3;
				screen.render(x * 16 + 4, y * 16 + 4, 11 + 3 * 32, col, 0);
			}
		}
		if (level.dirtColor == 222) {
			int col = col4;
			screen.render(x * 16 + 4, y * 16 + 4, 11 + 3 * 32, col, 0);
		}
	}

	public void tick(Level level, int x, int y) {
		int age = level.getData(x, y) + 1;
		if (age > 100) {
			level.setTile(x, y, growsTo, 0);
		} else {
			level.setData(x, y, age);
		}
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		level.setTile(x, y, onType, 0);
	}
}
