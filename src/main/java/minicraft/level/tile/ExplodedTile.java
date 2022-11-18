package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

/// This class is for tiles WHILE THEY ARE EXPLODING
public class ExplodedTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "exploded")
		.setConnectChecker((tile, side) -> tile.getClass() == ExplodedTile.class);

	protected ExplodedTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		connectsToFluid = true;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
