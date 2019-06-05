package minicraft.gfx;

import java.awt.Rectangle;
import java.util.Random;

public class Sprite {
	/**
		This class needs to store a list of similar segments that make up a sprite, plus the color, just once for everything. There's usually four groups, but the components are:
			-spritesheet location (x, y)
			-mirror type
		
		That's it!
		The screen's render method only draws one 8x8 pixel of the spritesheet at a time, so the "sprite size" will be determined by how many repetitions of the above group there are.
	*/
	
	static Random ran = new Random();
	
	public static Sprite missingTexture(int w, int h) {
		return new Sprite(30, 30, w, h, Color.get(505, 505));
	}
	public static Sprite blank(int w, int h, int col) {
		return new Sprite(7, 2, w, h, Color.get(col, col));
	}
	
	public static Sprite repeat(int sx, int sy, int w, int h, int col) {
		return ConnectorSprite.makeSprite(w, h, col, 0, true, sx + sy * 32);
	}
	
	public static Sprite dots(int col) {
		return ConnectorSprite.makeSprite(2, 2, col, 0, false, 0, 1, 2, 3);
	}
	public static Sprite randomDots(long seed, int col) {
		ran.setSeed(seed);
		return ConnectorSprite.makeSprite(2, 2, col, ran.nextInt(4), false, ran.nextInt(4), ran.nextInt(4), ran.nextInt(4), ran.nextInt(4));
	}
	
	protected Px[][] spritePixels;
	public int color;
	protected java.awt.Rectangle sheetLoc;
	/// spritePixels is arranged so that the pixels are in their correct positions relative to the top left of the full sprite. This means that their render positions are built-in to the array.
	
	public Sprite(int pos, int color) {
		this(pos%32, pos/32, 1, 1, color);}
	public Sprite(int sx, int sy, int color) {
		this(sx, sy, 1, 1, color);
	}
	public Sprite(int sx, int sy, int sw, int sh) {
		this(sx, sy, sw, sh, 0, 0);}
	public Sprite(int sx, int sy, int sw, int sh, int color) {
		this(sx, sy, sw, sh, color, 0);}
	
	public Sprite(int sx, int sy, int sw, int sh, int color, int mirror) {
		this(sx, sy, sw, sh, color, mirror, false);}
	public Sprite(int sx, int sy, int sw, int sh, int color, int mirror, boolean onepixel) {
		this.color = color;
		sheetLoc = new Rectangle(sx, sy, sw, sh);
		
		spritePixels = new Px[sh][sw];
		for(int r = 0; r < sh; r++)
			for(int c = 0; c < sw; c++)
				spritePixels[r][c] = new Px(sx+(onepixel?0:c), sy+(onepixel?0:r), mirror);
	}
	public Sprite(int sx, int sy, int sw, int sh, int color, boolean onepixel, int[][] mirrors) {
		this.color = color;
		sheetLoc = new Rectangle(sx, sy, sw, sh);
		
		spritePixels = new Px[sh][sw];
		for(int r = 0; r < sh; r++)
			for(int c = 0; c < sw; c++)
				spritePixels[r][c] = new Px(sx+(onepixel?0:c), sy+(onepixel?0:r), mirrors[r][c]);
	}
	
	public Sprite(Px[][] pixels) { this(pixels, 0); }
	public Sprite(Px[][] pixels, int color) {
		spritePixels = pixels;
		this.color = color;
	}
	
	public int getPos() {
		return sheetLoc.x + sheetLoc.y * 32;
	}
	public java.awt.Dimension getSize() {
		return sheetLoc.getSize();
	}
	
	public static int[][] fillArray(int value, int s1, int s2) {
		int[][] values = new int[s1][s2];
		System.out.println("filling array");
		for(int i = 0; i < s1; i++)
			java.util.Arrays.fill(values[i], value);
		return values;
	}
	
	public void render(Screen screen, int x, int y) { render(screen, x, y, color); }
	public void render(Screen screen, int x, int y, int color) {
		/// here, x and y are screen coordinates.
		
		for(int row = 0; row < spritePixels.length; row++) { // loop down through each row
			renderRow(row, screen, x, y + row*8, color);
		}
	}
	
	public void renderRow(int r, Screen screen, int x, int y) { renderRow(r, screen, x, y, color); }
	public void renderRow(int r, Screen screen, int x, int y, int color) {
		Px[] row = spritePixels[r];
		for(int c = 0; c < row.length; c++) { // loop across through each column
			screen.render(x + c*8, y, row[c].sheetPos, color, row[c].mirror); // render the sprite pixel.
		}
	}
	
	protected void renderPixel(int c, int r, Screen screen, int x, int y) { renderPixel(c, r, screen, x, y, color); }
	protected void renderPixel(int c, int r, Screen screen, int x, int y, int color) {
		renderPixel(c, r, screen, x, y, color, spritePixels[r][c].mirror);
	}
	protected void renderPixel(int c, int r, Screen screen, int x, int y, int col, int mirror) {
		//System.out.println("rendering pixel ("+c+","+r+") at ("+x+","+y+")");
		screen.render(x, y, spritePixels[r][c].sheetPos, col, mirror); // render the sprite pixel.
	}
	
	public String toString() {
		StringBuilder out = new StringBuilder(getClass().getName().replace("minicraft.gfx.", "") + "; pixels:");
		for(Px[] row: spritePixels)
			for(Px pixel: row)
				out.append("\n").append(pixel.toString());
		out.append("\n");
		
		return out.toString();
	}
	
	public static class Px {
		protected int sheetPos, mirror;
		
		public Px(int sheetX, int sheetY, int mirroring) {
			//pixelX and pixelY are the relative positions each pixel should have relative to the top-left-most pixel of the sprite.
			sheetPos = sheetX + 32*sheetY;
			mirror = mirroring;
		}
		
		public String toString() {
			return "SpritePixel:x="+(sheetPos%32)+";y="+(sheetPos/32)+";mirror="+mirror;
		}
	}
}
