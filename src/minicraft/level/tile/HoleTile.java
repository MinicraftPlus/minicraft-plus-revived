package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class HoleTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(HoleTile.class, new Sprite(14, 0, 3, 3, Color.get(3, 222, 211, 321), 3), Sprite.dots(Color.get(222, 222, 220, 220)))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return tile.connectsToLiquid();
		}
		
		public int getSparseColor(Tile tile, int origCol) {
			if(!tile.connectsToLiquid() && tile.connectsToSand)
				return Color.get(3, 222, 440, 550);
			else
				return origCol;
		}
	};
	
	protected HoleTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		connectsToWater = true;
		connectsToLava = true;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		sprite.sparse.color = Color.get(3, 222, 211, DirtTile.dCol(level.depth));
		sprite.render(screen, level, x, y);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim();
	}
}
