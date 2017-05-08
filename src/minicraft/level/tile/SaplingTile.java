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
		int col = Color.get(20, 40, 50, -1);
		
		onType.render(screen, level, x, y);
		
		screen.render(x * 16 + 4, y * 16 + 4, 11 + 3 * 32, col, 0);
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
