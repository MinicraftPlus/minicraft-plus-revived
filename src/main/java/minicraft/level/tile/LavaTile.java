package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

public class LavaTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "lava")
		.setConnectChecker((tile, side) -> tile.connectsToFluid);

	protected LavaTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		connectsToFluid = true;
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
