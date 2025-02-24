package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

/// This class is for tiles WHILE THEY ARE EXPLODING
public class ExplodedTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "exploded")
		.setConnectionChecker((level, x, y, tile, side) -> tile instanceof ExplodedTile);

	protected ExplodedTile(String name) {
		super(name, sprite);
	}

	@Override
	public boolean connectsToFluid(Level level, int x, int y) {
		return true;
	}

	@Override
	public boolean connectsToSand(Level level, int x, int y) {
		return true;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {}
}
