package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

public class LavaTile extends Tile {
	private ConnectorSprite sprite = new ConnectorSprite(LavaTile.class, new LinkedSpriteSheet(SpriteType.Tile, "lava").setSpriteSize(2, 2).setMirror(3), null)
	{
		public boolean connectsTo(Tile tile, boolean isSide) {
			return tile.connectsToFluid;
		}
	};

	protected LavaTile(String name) {
		super(name, (ConnectorSprite)null);
		super.csprite = sprite;
		connectsToSand = true;
		connectsToFluid = true;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		long seed = (tickCount + (x / 2 - y) * 4311) / 10 * 54687121l + x * 3271612l + y * 3412987161l;
		sprite.full = Sprite.randomTiles(seed, "lava");
		sprite.sparse.setColor(DirtTile.dCol(level.depth));
		sprite.render(screen, level, x, y);
	}

	@Override
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e.canSwim();
	}

	@Override
	public boolean tick(Level level, int xt, int yt) {
		int xn = xt;
		int yn = yt;

		if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
		else yn += random.nextInt(2) * 2 - 1;

		if (level.getTile(xn, yn) == Tiles.get("hole")) {
			level.setTile(xn, yn, this);
		}
		return false;
	}

	@Override
	public int getLightRadius(Level level, int x, int y) {
		return 6;
	}
}
