package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class WaterTile extends Tile {
	private ConnectorSprite sprite = new ConnectorSprite(WaterTile.class, new Sprite(14, 0, 3, 3, Color.get(3, 105, 211, 321), 0), Sprite.dots(Color.get(005, 105, 115, 115)))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return !isSide || tile.connectsToWater;
		}
	};
	
	protected static void addInstances() {
		Tiles.add(new WaterTile("Water"));
	}
	
	private WaterTile(String name) {
		super(name, (ConnectorSprite)null);
		csprite = sprite;
		connectsToSand = true;
		connectsToWater = true;
	}

	//private Random wRandom = new Random();

	public void render(Screen screen, Level level, int x, int y) {
		long seed = (tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l;
		sprite.full = Sprite.randomDots(seed, sprite.full.color);
		sprite.render(screen, level, x, y);
		/*
		int col = Color.get(005, 105, 115, 115);
		int col1 = Color.get(3, 105, 211, DirtTile.dCol(level.depth));
		int col2 = Color.get(3, 105, 440, 550);
		
		wRandom.setSeed(
				(tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l);
		int transitionColor1 = col1;
		int transitionColor2 = col2;
		
		boolean u = !level.getTile(x, y - 1).connectsToWater;
		boolean d = !level.getTile(x, y + 1).connectsToWater;
		boolean l = !level.getTile(x - 1, y).connectsToWater;
		boolean r = !level.getTile(x + 1, y).connectsToWater;

		boolean su = u && level.getTile(x, y - 1).connectsToSand;
		boolean sd = d && level.getTile(x, y + 1).connectsToSand;
		boolean sl = l && level.getTile(x - 1, y).connectsToSand;
		boolean sr = r && level.getTile(x + 1, y).connectsToSand;

		if (!u && !l) {
			screen.render(x * 16 + 0, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
		} else
			screen.render(
					x * 16 + 0,
					y * 16 + 0,
					(l ? 14 : 15) + (u ? 0 : 1) * 32,
					(su || sl) ? transitionColor2 : transitionColor1,
					0);

		if (!u && !r) {
			screen.render(x * 16 + 8, y * 16 + 0, wRandom.nextInt(4), col, wRandom.nextInt(4));
		} else
			screen.render(
					x * 16 + 8,
					y * 16 + 0,
					(r ? 16 : 15) + (u ? 0 : 1) * 32,
					(su || sr) ? transitionColor2 : transitionColor1,
					0);

		if (!d && !l) {
			screen.render(x * 16 + 0, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
		} else
			screen.render(
					x * 16 + 0,
					y * 16 + 8,
					(l ? 14 : 15) + (d ? 2 : 1) * 32,
					(sd || sl) ? transitionColor2 : transitionColor1,
					0);
		if (!d && !r) {
			screen.render(x * 16 + 8, y * 16 + 8, wRandom.nextInt(4), col, wRandom.nextInt(4));
		} else
			screen.render(
					x * 16 + 8,
					y * 16 + 8,
					(r ? 16 : 15) + (d ? 2 : 1) * 32,
					(sd || sr) ? transitionColor2 : transitionColor1,
					0);
		*/
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim();
	}

	public void tick(Level level, int xt, int yt) {
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("hole")) {
			level.setTile(xn, yn, this, 0);
		}
		if (level.getTile(xn, yn) == Tiles.get("lava")) {
			level.setTile(xn, yn, Tiles.get("Stone Bricks"), 0);
		}
	}
}
