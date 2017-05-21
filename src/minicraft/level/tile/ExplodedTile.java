package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.level.Level;

/// This class is for tiles WHILE THEY ARE EXPLODING
public class ExplodedTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(ExplodedTile.class, new Sprite(14, 0, 3, 3, 0, 0), Sprite.dots(Color.get(555, 555, 555, 550)))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return !isSide || tile.connectsToLiquid();
		}
	};
	
	protected static void addInstances() {
		Tiles.add(new ExplodedTile("Explode"));
	}
	
	private ExplodedTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		connectsToWater = true;
		connectsToLava = true;
	}
	
	//public void render(Screen screen, Level level, int x, int y) {
		//int col = Color.get(555, 555, 555, 550);
		/*int tcol1 = Color.get(3, 555, DirtTile.dCol(level.depth) - 111, DirtTile.dCol(level.depth));
		int tcol2 = Color.get(3, 555, level.sandColor - 110, level.sandColor);
		
		boolean u = !level.getTile(x, y - 1).connectsToLiquid();
		boolean d = !level.getTile(x, y + 1).connectsToLiquid();
		boolean l = !level.getTile(x - 1, y).connectsToLiquid();
		boolean r = !level.getTile(x + 1, y).connectsToLiquid();
		
		boolean su = u && level.getTile(x, y - 1).connectsToSand;
		boolean sd = d && level.getTile(x, y + 1).connectsToSand;
		boolean sl = l && level.getTile(x - 1, y).connectsToSand;
		boolean sr = r && level.getTile(x + 1, y).connectsToSand;
		
		if (!u && !l) {
			screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
		} else
			screen.render(
					x * 16 + 0,
					y * 16 + 0,
					(l ? 14 : 15) + (u ? 0 : 1) * 32,
					(su || sl) ? transitionColor2 : transitionColor1,
					0);
		
		if (!u && !r) {
			screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
		} else
			screen.render(
					x * 16 + 8,
					y * 16 + 0,
					(r ? 16 : 15) + (u ? 0 : 1) * 32,
					(su || sr) ? transitionColor2 : transitionColor1,
					0);

		if (!d && !l) {
			screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
		} else
			screen.render(
					x * 16 + 0,
					y * 16 + 8,
					(l ? 14 : 15) + (d ? 2 : 1) * 32,
					(sd || sl) ? transitionColor2 : transitionColor1,
					0);
		if (!d && !r) {
			screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
		} else
			screen.render(
					x * 16 + 8,
					y * 16 + 8,
					(r ? 16 : 15) + (d ? 2 : 1) * 32,
					(sd || sr) ? transitionColor2 : transitionColor1,
					0);
		*/
	//}
	
	public void steppedOn(Level level, int x, int y, Entity entity) {
		entity.hurt(this, x, y, 50);
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
