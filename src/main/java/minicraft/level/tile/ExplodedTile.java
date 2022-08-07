package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

/// This class is for tiles WHILE THEY ARE EXPLODING
public class ExplodedTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(ExplodedTile.class, new LinkedSpriteSheet(SpriteType.Tile, "exploded").setSpriteSize(3, 3), new LinkedSpriteSheet(SpriteType.Tile, "exploded").setSpriteDim(3, 0, 2, 2))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return !isSide || tile.connectsToLiquid();
		}
	};

	protected ExplodedTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		connectsToFluid = true;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
