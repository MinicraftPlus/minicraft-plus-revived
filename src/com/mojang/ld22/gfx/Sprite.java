package com.mojang.ld22.gfx;

public class Sprite {
	/**
		This class needs to store a list of similar segments that make up a sprite, plus the color, just once for everything. There's usually four groups, but the components are:
			-spritesheet location (x, y)
			-mirror type
		
		That's it!
		The screen's render method only draws one 8x8 pixel of the spritesheet at a time, so the "sprite size" will be determined by how many repetitions of the above group there are.
	*/
	
	//private static final int sheetPxSize = 32, gamePxSize = 16;
	
	//public int x, y, w, h; // sprite coordinates
	//public int img; // sprite image
	//public int color; // sprite color
	//public int bits; // sprite bits
	//public boolean mx, my;
	public Px[][] spritePixels;
	/// spritePixels is arranged so that the pixels are in their correct positions relative to the top left of the full sprite. This means that their render positions are built-in to the array.
	
	public Sprite(Px[][] pixels) {
		//color = col;
		spritePixels = pixels;
	}
	
	public void render(Screen screen, int color, int lvlx, int lvly) {
		/// here, x and y are level coordinates, I think.
		
		for(int r = 0; r < spritePixels.length; r++) { // loop down through each row
			renderRow(spritePixels[r], screen, color, lvlx, lvly + r*8);
		}
		/*
		// xp = x pixels; yp = y pixels.
		for(int xp = 0; xp < width; xp++) {
			for(int yp = 0; yp < height; yp++) {
				int px = gamePxSize/2, xpa = xp%2, ypa = yp%2;
				int bitmirror = (mx ? xpa : 0) + (my ? ypa : 0);
				screen.render(x + xpa*px, y + ypa*px, sx+xpa + (sy+ypa) * sheetPxSize, col, bitmirror);
			}
		}*/
		
		/*screen.render(x * 16 + 8, y * 16 + 0, 9 + 2 * 32, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 8 + 3 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 9 + 3 * 32, col, 0);
		*/
	}
	
	public static void renderRow(Px[] row, Screen screen, int color, int x, int y) {
		for(int c = 0; c < row.length; c++) { // loop across through each column
			screen.render(x + c*8, y, row[c].sheetPos, color, row[c].mirror); // render the sprite pixel.
		}
	}
	
	public String toString() {
		String out = getClass().getName().replace("com.mojang.ld22.gfx.", "")+"; pixels:";
		for(Px[] row: spritePixels)
			for(Px pixel: row)
				out += "\n"+pixel.toString();
		out += "\n";
		
		return out;
	}
	
	public class Px {
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
