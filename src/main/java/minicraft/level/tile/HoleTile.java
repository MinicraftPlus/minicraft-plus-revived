package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

public class HoleTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(HoleTile.class, new LinkedSprite(SpriteType.Tile, "hole").setSpriteSize(3, 3)
		.setMirror(3), new LinkedSprite(SpriteType.Tile, "hole").setSpriteDim(3, 0, 2, 2))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return tile.connectsToLiquid();
		}
	};

	protected HoleTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		connectsToFluid = true;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		sprite.sparse.setColor(DirtTile.dCol(level.depth));
		sprite.render(screen, level, x, y);
	}

	@Override
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim();
	}
}
