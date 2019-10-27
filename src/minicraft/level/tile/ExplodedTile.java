package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

/// This class is for tiles WHILE THEY ARE EXPLODING
public class ExplodedTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(ExplodedTile.class, new Sprite(6, 22, 3, 3, 1, 3), new Sprite(9, 22, 2, 2, 1))
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
	
	public void steppedOn(Level level, int x, int y, Entity entity) {
		if(entity instanceof Mob)
			((Mob)entity).hurt(this, x, y, 50);
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
