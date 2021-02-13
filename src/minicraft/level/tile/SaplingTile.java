package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class SaplingTile extends Tile {
	private static Sprite sprite = new Sprite(12, 1, 1);
	
	private Tile onType;
	private Tile growsTo;
	
	protected SaplingTile(String name, Tile onType, Tile growsTo) {
		super(name, sprite);
		this.onType = onType;
		this.growsTo = growsTo;
		connectsToSand = onType.connectsToSand;
		connectsToGrass = onType.connectsToGrass;
		connectsToFluid = onType.connectsToFluid;
		maySpawn = true;
	}

	public void render(Screen screen, Level level, int x, int y) {
		onType.render(screen, level, x, y);
		
		sprite.render(screen, x*16, y*16);
	}

	public boolean tick(Level level, int x, int y) {
		int age = level.getData(x, y) + 1;
		if (age > 100) {
			level.setTile(x, y, growsTo);
		} else {
			level.setData(x, y, age);
		}
		return true;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		level.setTile(x, y, onType);
		Sound.monsterHurt.play();
		return true;
	}
}
