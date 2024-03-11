package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

public class WaterTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "water")
		.setConnectionChecker((level, x, y, tile, side) -> tile.connectsToFluid(level, x, y))
		.setSingletonWithConnective(true);

	protected WaterTile(String name) {
		super(name, sprite);
	}

	@Override
	public boolean connectsToFluid(Level level, int x, int y) {
		return true;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("dirt").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	@Override
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim();
	}

	@Override
	public boolean tick(Level level, int xt, int yt) {
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) instanceof HoleTile) {
			level.setTile(xn, yn, this);
		}

		// These set only the non-diagonally adjacent lava tiles to obsidian
		for (int x = -1; x < 2; x++) {
			if (level.getTile(xt + x, yt) instanceof LavaTile)
				level.setTile(xt + x, yt, Tiles.get("Raw Obsidian"));
		}
		for (int y = -1; y < 2; y++) {
			if (level.getTile(xt, yt + y) instanceof LavaTile)
				level.setTile(xt, yt + y, Tiles.get("Raw Obsidian"));
		}
		return false;
	}
}
