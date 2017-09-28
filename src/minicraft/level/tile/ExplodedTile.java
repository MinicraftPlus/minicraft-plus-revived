package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

/// This class is for tiles WHILE THEY ARE EXPLODING
public class ExplodedTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(ExplodedTile.class, new Sprite(14, 0, 3, 3, 0, 3), Sprite.dots(Color.get(555, 555, 555, 550)))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return !isSide || tile.connectsToLiquid();
		}
	};
	
	protected ExplodedTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		connectsToWater = true;
		connectsToLava = true;
	}
	
	public void steppedOn(Level level, int x, int y, Entity entity) {
		entity.hurt(this, x, y, 50);
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
