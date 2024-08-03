package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Mob;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

public class SaplingTile extends Tile {
	private static final SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "sapling");

	private Tile onType;
	private Tile growsTo;

	protected SaplingTile(String name, Tile onType, Tile growsTo) {
		super(name, sprite);
		this.onType = onType;
		this.growsTo = growsTo;
		maySpawn = true;
	}

	@Override
	public boolean connectsToGrass(Level level, int x, int y) {
		return onType.connectsToGrass(level, x, y);
	}

	@Override
	public boolean connectsToFluid(Level level, int x, int y) {
		return onType.connectsToFluid(level, x, y);
	}

	@Override
	public boolean connectsToSand(Level level, int x, int y) {
		return onType.connectsToSand(level, x, y);
	}

	public void render(Screen screen, Level level, int x, int y) {
		onType.render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public boolean tick(Level level, int x, int y) {
		int age = level.getData(x, y) + 1;
		if (age > 110) {
			// Don't grow if there is an entity on this tile.
			if (!level.isEntityOnTile(x, y)) {
				level.setTile(x, y, growsTo);
			}
		} else {
			level.setData(x, y, age);
		}
		return true;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		level.setTile(x, y, onType);
		Sound.play("monsterhurt");
		return true;
	}
}
