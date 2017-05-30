package minicraft.level.tile;

import java.util.Random;
import minicraft.entity.Entity;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class LavaTile extends Tile {
	private ConnectorSprite sprite = new ConnectorSprite(LavaTile.class, new Sprite(14, 0, 3, 3, Color.get(3, 500, 211, 322), 3), Sprite.dots(Color.get(500, 500, 520, 450)))
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return tile.connectsToLava;
		}
		
		public int getSparseColor(Tile tile, int origCol) {
			if(!tile.connectsToLava && tile.connectsToSand)
				return Color.get(3, 500, 440, 550);
			else
				return origCol;
		}
	};
	
	protected LavaTile(String name) {
		super(name, (ConnectorSprite)null);
		super.csprite = sprite;
		connectsToSand = true;
		connectsToLava = true;
	}

	private Random wRandom = new Random();
	
	public void render(Screen screen, Level level, int x, int y) {
		long seed = (tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l;
		sprite.full = Sprite.randomDots(seed, sprite.full.color);
		sprite.sparse.color = Color.get(3, 500, 211, DirtTile.dCol(level.depth));
		sprite.render(screen, level, x, y);
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
			level.setTile(xn, yn, this);
		}
	}

	public int getLightRadius(Level level, int x, int y) {
		return 6;
	}
}
