package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

public class LavaTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "lava")
		.setConnectionChecker((level, x, y, tile, side) -> tile.connectsToFluid(level, x, y))
		.setSingletonWithConnective(true);

	protected LavaTile(String name) {
		super(name, sprite);
	}

	@Override
	public boolean connectsToSand(Level level, int x, int y) {
		return true;
	}

	@Override
	public boolean connectsToFluid(Level level, int x, int y) {
		return true;
	}

	@Override
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim();
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("dirt").render(screen, level, x, y);
		super.render(screen, level, x, y);
	}

	@Override
	public boolean tick(Level level, int xt, int yt) {
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("hole")) {
			level.setTile(xn, yn, this);
		}
		return false;
	}

	@Override
	public int getLightRadius(Level level, int x, int y) {
		return 6;
	}
}
