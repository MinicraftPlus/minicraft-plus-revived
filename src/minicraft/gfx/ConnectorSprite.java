package minicraft.gfx;

import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ConnectorSprite {
	/**
		This class is meant for those tiles that look different when they are touching other tiles of their type; aka, they "connect" to them.
		
		Since I think connecting tile sprites tend to have three color categories, maybe this should have two extra colors..?
		
		This class will need to keep rack of the following sprites:
		-a sprite for each kind of intersection; aka a 3x3 grid of sprite pixels, that show the sprite for each position, totally surrounded, nothing of left, etc.
		
	*/
	
	public Sprite sparse, sides, full;
	private Class<? extends Tile> owner;
	private boolean checkCorners;
	
	public ConnectorSprite(Class<? extends Tile> owner, Sprite sparse, Sprite sides, Sprite full) {
		this(owner, sparse, sides, full, true);
	}
	public ConnectorSprite(Class<? extends Tile> owner, Sprite sparse, Sprite sides, Sprite full, boolean cornersMatter) {
		this.owner = owner;
		this.sparse = sparse;
		this.sides = sides;
		this.full = full;
		this.checkCorners = cornersMatter;
	}
	
	public void render(Screen screen, Level level, int x, int y) { render(screen, level, x, y, sparse.color, sides.color, full.color); }
	public void render(Screen screen, Level level, int x, int y, int colsparse, int colside, int colfull) {
		//System.out.println("rendering sprite for tile " + owner);
		
		boolean u = connectsTo(level.getTile(x, y - 1), true);
		boolean d = connectsTo(level.getTile(x, y + 1), true);
		boolean l = connectsTo(level.getTile(x - 1, y), true);
		boolean r = connectsTo(level.getTile(x + 1, y), true);
		
		boolean ul = connectsTo(level.getTile(x - 1, y - 1), false);
		boolean dl = connectsTo(level.getTile(x - 1, y + 1), false);
		boolean ur = connectsTo(level.getTile(x + 1, y - 1), false);
		boolean dr = connectsTo(level.getTile(x + 1, y + 1), false);
		
		x = x << 4;
		y = y << 4;
		
		if (u && l) {
			if (ul) full.renderPixel(0, 0, screen, x, y, colfull);
			else sides.renderPixel(0, 0, screen, x, y, colside);
		} else
			sparse.renderPixel(l?1:2, u?1:2, screen, x, y, colsparse);
		
		if (u && r) {
			if (ur) full.renderPixel(1, 0, screen, x+8, y, colfull);
			else sides.renderPixel(1, 0, screen, x+8, y, colside);
		} else
			sparse.renderPixel(r?1:0, u?1:2, screen, x+8, y, colsparse);

		if (d && l) {
			if (dl) full.renderPixel(0, 1, screen, x, y+8, colfull);
			else sides.renderPixel(0, 1, screen, x, y+8, colside);
		} else
			sparse.renderPixel(l?1:2, d?1:0, screen, x, y+8, colsparse);
		
		if (d && r) {
			if (dr) full.renderPixel(1, 1, screen, x+8, y+8, colfull);
			else sides.renderPixel(1, 1, screen, x+8, y+8, colside);
		} else
			sparse.renderPixel(r?1:0, d?1:0, screen, x+8, y+8, colsparse);
	}
	
	// it is expected that some tile classes will override this on class instantiation.
	public boolean connectsTo(Tile tile, boolean isSide) {
		//System.out.println("original connection check");
		return tile.getClass() == owner;
	}
	
	public static Sprite makeSprite(int w, int h, int color, int mirror, boolean repeat, int... coords) {
		Sprite.Px[][] pixels = new Sprite.Px[h][w];
		int i = 0;
		for(int r = 0; r < h && i < coords.length; r++) {
			for(int c = 0; c < w && i < coords.length; c++) {
				int pos = coords[i];
				pixels[r][c] = new Sprite.Px(pos%32, pos/32, mirror);
				i++;
				if(i == coords.length && repeat) i = 0;
			}
		}
		
		return new Sprite(pixels, color);
	}
}
