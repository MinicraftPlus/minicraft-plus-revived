package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

public class HoleTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "hole")
		.setConnectChecker((tile, side) -> tile.connectsToLiquid());

	protected HoleTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		connectsToFluid = true;
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
}
