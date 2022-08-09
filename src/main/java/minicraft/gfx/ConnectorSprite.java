package minicraft.gfx;

import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.level.Level;
import minicraft.level.tile.ConnectTile;
import minicraft.level.tile.Tile;

public class ConnectorSprite {
	/**
		This class is meant for those tiles that look different when they are touching other tiles of their type; aka, they "connect" to them.

		Since I think connecting tile sprites tend to have three color categories, maybe this should have two extra colors..?

		This class will need to keep rack of the following sprites:
		-a sprite for each kind of intersection; aka a 3x3 grid of sprite pixels, that show the sprite for each position, totally surrounded, nothing of left, etc.

	*/

	public LinkedSpriteSheet sparse, sides, full;
	private Class<? extends Tile> owner;
	private boolean checkCorners;

	public ConnectorSprite(Class<? extends Tile> owner, LinkedSpriteSheet sparse, LinkedSpriteSheet sides, LinkedSpriteSheet full) {
		this(owner, sparse, sides, full, true);
	}
	public ConnectorSprite(Class<? extends Tile> owner, LinkedSpriteSheet sparse, LinkedSpriteSheet sides, LinkedSpriteSheet full, boolean cornersMatter) {
		this.owner = owner;
		this.sparse = sparse;
		this.sides = sides;
		this.full = full;
		this.checkCorners = cornersMatter;
	}
	public ConnectorSprite(Class<? extends Tile> owner, LinkedSpriteSheet sparse, LinkedSpriteSheet full) {
		this(owner, sparse, sparse, full, false);
	}

	public void render(Screen screen, Level level, int x, int y) { render(screen, level, x, y, -1); }

	public void render(Screen screen, Level level, int x, int y, int whiteTint) {
		//System.out.println("rendering sprite for tile " + owner);

		Tile ut = level.getTile(x, y - 1);
		Tile dt = level.getTile(x, y + 1);
		Tile lt = level.getTile(x - 1, y);
		Tile rt = level.getTile(x + 1, y);

		boolean u = connectsToDoEdgeCheck(ut, true);
		boolean d = connectsToDoEdgeCheck(dt, true);
		boolean l = connectsToDoEdgeCheck(lt, true);
		boolean r = connectsToDoEdgeCheck(rt, true);

		boolean ul = connectsToDoEdgeCheck(level.getTile(x - 1, y - 1), false);
		boolean dl = connectsToDoEdgeCheck(level.getTile(x - 1, y + 1), false);
		boolean ur = connectsToDoEdgeCheck(level.getTile(x + 1, y - 1), false);
		boolean dr = connectsToDoEdgeCheck(level.getTile(x + 1, y + 1), false);

		x = x << 4;
		y = y << 4;


		if (u && l) {
			if (ul || !checkCorners) full.getSprite().renderPixel(1, 1, screen, x, y);
			else sides.getSprite().renderPixel(0, 0, screen, x, y);
		} else
			sparse.getSprite().renderPixel(l ? 1 : 2, u ? 1 : 2, screen, x, y);


		if (u && r) {
			if (ur || !checkCorners) full.getSprite().renderPixel(0, 1, screen, x + 8, y);
			else sides.getSprite().renderPixel(1, 0, screen, x + 8, y);
		} else
			sparse.getSprite().renderPixel(r ? 1 : 0, u ? 1 : 2, screen, x + 8, y);


		if (d && l) {
			if (dl || !checkCorners) full.getSprite().renderPixel(1, 0, screen, x, y + 8);
			else sides.getSprite().renderPixel(0, 1, screen, x, y + 8);
		} else
			sparse.getSprite().renderPixel(l ? 1 : 2, d ? 1 : 0, screen, x, y + 8);


		if (d && r) {
			if (dr || !checkCorners) full.getSprite().renderPixel(0, 0, screen, x + 8, y + 8);
			else sides.getSprite().renderPixel(1, 1, screen, x + 8, y + 8);
		} else
			sparse.getSprite().renderPixel(r ? 1 : 0, d ? 1 : 0, screen, x + 8, y + 8);

	}

	// It is expected that some tile classes will override this on class instantiation.
	public boolean connectsTo(Tile tile, boolean isSide) {
		//System.out.println("original connection check");
		return tile.getClass() == owner;
	}

	public boolean connectsToDoEdgeCheck(Tile tile, boolean isSide) {
		if (tile.getClass() == ConnectTile.class) {
			return true;
		}
		else {
			return connectsTo(tile, isSide);
		}
	}

	public static Sprite makeSprite(int w, int h, int mirror, SpriteSheet sheet, boolean repeat, int[]... coords) {
		Sprite.Px[][] pixels = new Sprite.Px[h][w];
		int i = 0;
		for (int r = 0; r < h && i < coords.length; r++) {
			for (int c = 0; c < w && i < coords.length; c++) {
				int[] pos = coords[i];
				pixels[r][c] = new Sprite.Px(pos[0], pos[1], mirror, sheet);
				i++;
				if (i == coords.length && repeat) i = 0;
			}
		}

		return new Sprite(pixels);
	}
}
