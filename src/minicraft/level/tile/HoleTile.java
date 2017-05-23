package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class HoleTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(HoleTile.class, new Sprite(14, 0, 3, 3, Color.get(3, 222, 211, 321), 3), Sprite.dots(Color.get(222, 222, 220, 220)))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return !isSide || tile.connectsToLiquid();
		}
	};
	
	protected HoleTile(String name) {
		super(name, sprite);
		connectsToSand = true;
		connectsToWater = true;
		connectsToLava = true;
	}
	/*
	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(222, 222, 220, 220);
		int col1 = Color.get(3, 222, 211, DirtTile.dCol(level.depth));
		int col2 = Color.get(3, 222, 440, 550);
		
		int transitionColor1 = col1;
		int transitionColor2 = col2;
		
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
	}*/

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim();
	}
}
