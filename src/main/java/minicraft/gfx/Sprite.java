package minicraft.gfx;

import java.awt.Rectangle;
import java.util.Random;

import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;

public class Sprite {
	/**
		This class needs to store a list of similar segments that make up a sprite, just once for everything. There's usually four groups, but the components are:
			-spritesheet location (x, y)
			-mirror type

		That's it!
		The screen's render method only draws one 8x8 pixel of the spritesheet at a time, so the "sprite size" will be determined by how many repetitions of the above group there are.
	*/

	static Random ran = new Random();

	public static LinkedSpriteSheet missingTexture(SpriteType type) {
		switch (type) {
			case Entity: return new LinkedSpriteSheet(SpriteType.Entity, "missing_entity");
			case Item: return new LinkedSpriteSheet(SpriteType.Item, "missing_item");
			case Tile: return new LinkedSpriteSheet(SpriteType.Tile, "missing_tile");
			default: return null;
		}
	}

	// public static Sprite randomDots(long seed, int offset) { TODO
	// 	ran.setSeed(seed);
	// 	return ConnectorSprite.makeSprite(2, 2, ran.nextInt(4), 1, false, (2 + ran.nextInt(4)) + offset * 32, (2 + ran.nextInt(4)) + offset * 32, (2 + ran.nextInt(4)) + offset * 32, (2 + ran.nextInt(4)) + offset * 32);
	// }

	protected Px[][] spritePixels;
	public int color = -1;
	protected java.awt.Rectangle sheetLoc;
	// spritePixels is arranged so that the pixels are in their correct positions relative to the top left of the full sprite. This means that their render positions are built-in to the array.

	/**
	 * 	Creates a reference to an 8x8 sprite in a spritesheet. Specify the position and sheet of the sprite to create.
	 * @param sx X position of the sprite in spritesheet coordinates.
	 * @param sy Y position of the sprite in spritesheet coordinates.
	 * @param sw Width of the sprite in spritesheet.
	 * @param sh Height of the sprite in spritesheet.
	 * @param sheet What spritesheet to use.
	 */
	public Sprite(int sx, int sy, int sw, int sh, SpriteSheet sheet) {
		this(sx, sy, sw, sh, sheet, 0);
	}
	public Sprite(int sx, int sy, int sw, int sh, SpriteSheet sheet, int mirror) {
		this(sx, sy, sw, sh, sheet, mirror, false);
	}
	public Sprite(int sx, int sy, int sw, int sh, SpriteSheet sheet, int mirror, boolean onepixel) {
		sheetLoc = new Rectangle(sx, sy, sw, sh);

		spritePixels = new Px[sh][sw];
		for (int r = 0; r < sh; r++)
			for (int c = 0; c < sw; c++)
				spritePixels[r][c] = new Px(sx + (onepixel ? 0 : c), sy + (onepixel ? 0 : r), mirror, sheet);
	}
	public Sprite(int sx, int sy, int sw, int sh, SpriteSheet sheet, boolean onepixel, int[][] mirrors) {
		sheetLoc = new Rectangle(sx, sy, sw, sh);

		spritePixels = new Px[sh][sw];
		for (int r = 0; r < sh; r++)
			for (int c = 0; c < sw; c++)
				spritePixels[r][c] = new Px(sx + (onepixel? 0 : c), sy + (onepixel ? 0 : r), mirrors[r][c], sheet);
	}

	public Sprite(Px[][] pixels) {
		spritePixels = pixels;
	}

	public java.awt.Dimension getSize() {
		return sheetLoc.getSize();
	}

	public void render(Screen screen, int x, int y) {
		// Here, x and y are screen coordinates.
		for (int row = 0; row < spritePixels.length; row++) { // Loop down through each row
			renderRow(row, screen, x, y + row * 8);
		}
	}
	public void render(Screen screen, int x, int y, int mirror) {
		for (int row = 0; row < spritePixels.length; row++) {
			renderRow(row, screen, x, y + row * 8, mirror);
		}
	}

	public void render(Screen screen, int x, int y, int mirror, int whiteTint) {
		for (int row = 0; row < spritePixels.length; row++) {
			renderRow(row, screen, x, y + row * 8, mirror, whiteTint);
		}
	}

	public void render(Screen screen, int x, int y, int mirror, int whiteTint, int color) {
		for (int row = 0; row < spritePixels.length; row++) {
			renderRow(row, screen, x, y + row * 8, mirror, whiteTint, color); // color: overwrites the colors of the original sprite in a single color
		}
	}

	public void renderRow(int r, Screen screen, int x, int y) {
		Px[] row = spritePixels[r];
		for (int c = 0; c < row.length; c++) { // Loop across through each column
			screen.render(x + c * 8, y, row[c].x, row[c].y, row[c].mirror, row[c].sheet, this.color); // Render the sprite pixel.
		}
	}
	public void renderRow(int r, Screen screen, int x, int y, int mirror) {
		Px[] row = spritePixels[r];
		for (int c = 0; c < row.length; c++) { // Loop across through each column
			screen.render(x + c * 8, y, row[c].x, row[c].y, mirror, row[c].sheet, this.color); // Render the sprite pixel.
		}
	}
	public void renderRow(int r, Screen screen, int x, int y, int mirror, int whiteTint) {
		Px[] row = spritePixels[r];
		for (int c = 0; c < row.length; c++) {
			screen.render(x + c * 8, y, row[c].x, row[c].y, (mirror != -1 ? mirror : row[c].mirror), row[c].sheet, whiteTint);
		}
	}

	public void renderRow(int r, Screen screen, int x, int y, int mirror, int whiteTint, int color) {
		Px[] row = spritePixels[r];
		for (int c = 0; c < row.length; c++) {
			screen.render(x + c * 8, y, row[c].x, row[c].y, (mirror != -1 ? mirror : row[c].mirror), row[c].sheet, whiteTint, false, color);
		}
	}

	protected void renderPixel(int c, int r, Screen screen, int x, int y) {
		renderPixel(c, r, screen, x, y, spritePixels[r][c].mirror);
	}
	protected void renderPixel(int c, int r, Screen screen, int x, int y, int mirror) {
		renderPixel(c, r, screen, x, y, mirror, this.color);
	}
	protected void renderPixel(int c, int r, Screen screen, int x, int y, int mirror, int whiteTint) {
		screen.render(x, y, spritePixels[r][c].x, spritePixels[r][c].y, mirror, spritePixels[r][c].sheet, whiteTint);
	}

	public String toString() {
		StringBuilder out = new StringBuilder(getClass().getName().replace("minicraft.gfx.", "") + "; pixels:");
		for (Px[] row: spritePixels)
			for (Px pixel: row)
				out.append("\n").append(pixel.toString());
		out.append("\n");

		return out.toString();
	}

	public static class Px {
		protected int x, y, mirror;
		protected SpriteSheet sheet;

		public Px(int sheetX, int sheetY, int mirroring, SpriteSheet sheet) {
			// pixelX and pixelY are the relative positions each pixel should have relative to the top-left-most pixel of the sprite.
			x = sheetX;
			y = sheetY;
			mirror = mirroring;
			this.sheet = sheet;
		}

		public String toString() {
			return "SpritePixel:x=" + x + ";y=" + y + ";mirror=" + mirror;
		}
	}
}
