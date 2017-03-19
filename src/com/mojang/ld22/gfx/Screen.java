package com.mojang.ld22.gfx;

public class Screen {
	/*
	 * public static final int MAP_WIDTH = 64; // Must be 2^x public static final int MAP_WIDTH_MASK = MAP_WIDTH - 1;
	 *
	 * public int[] tiles = new int[MAP_WIDTH * MAP_WIDTH]; public int[] colors = new int[MAP_WIDTH * MAP_WIDTH]; public int[] databits = new int[MAP_WIDTH * MAP_WIDTH];
	 */
	
	/// x and y offset of screen:
	public int xOffset;
	public int yOffset;
	
	// used for mirroring an image:
	public static final int BIT_MIRROR_X = 0x01;
	public static final int BIT_MIRROR_Y = 0x02;

	public final int w, h; // width and height of the screen
	public int[] pixels; // pixels on the screen
	
	private SpriteSheet sheet; // the sprite sheet used in the game.

	public Screen(int w, int h, SpriteSheet sheet) {
		this.sheet = sheet;
		this.w = w;
		this.h = h;

		pixels = new int[w * h]; // makes new integer array for all the pixels on the screen.
		
		// Random random = new Random();

		/*
		 * for (int i = 0; i < MAP_WIDTH * MAP_WIDTH; i++) { colors[i] = Color.get(00, 40, 50, 40); tiles[i] = 0;
		 *
		 * if (random.nextInt(40) == 0) { tiles[i] = 32; colors[i] = Color.get(111, 40, 222, 333); databits[i] = random.nextInt(2); } else if (random.nextInt(40) == 0) { tiles[i] = 33; colors[i] = Color.get(20, 40, 30, 550); } else { tiles[i] = random.nextInt(4); databits[i] = random.nextInt(4);
		 *
		 * } }
		 *
		 * Font.setMap("Testing the 0341879123", this, 0, 0, Color.get(0, 555, 555, 555));
		 */
	}
	
	/** Clears all the colors on the screen */
	public void clear(int color) {
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = color; // turns each pixel into a single color (clearing the screen!)
	}
	
	public int centertext(String text) {
		return (w - text.length() * 8) / 2;
	}
	
	/*
	 * public void renderBackground() { for (int yt = yScroll >> 3; yt <= (yScroll + h) >> 3; yt++) { int yp = yt * 8 - yScroll; for (int xt = xScroll >> 3; xt <= (xScroll + w) >> 3; xt++) { int xp = xt * 8 - xScroll; int ti = (xt & (MAP_WIDTH_MASK)) + (yt & (MAP_WIDTH_MASK)) * MAP_WIDTH; render(xp, yp, tiles[ti], colors[ti], databits[ti]); } }
	 *
	 * for (int i = 0; i < sprites.size(); i++) { Sprite s = sprites.get(i); render(s.x, s.y, s.img, s.col, s.bits); } sprites.clear(); }
	 */
	
	/** Renders an object from the sprite sheet based on screen coordinates, tile (SpriteSheet location), colors, and bits (for mirroring) */
	public void render(int xp, int yp, int tile, int colors, int bits) {
		xp -= xOffset; //account for screen offset
		yp -= yOffset;
		// determines if the image should be mirrored...
		boolean mirrorX = (bits & BIT_MIRROR_X) > 0; // horizontally.
		boolean mirrorY = (bits & BIT_MIRROR_Y) > 0; // vertically.

		int xTile = tile % 32; // gets x position of the tile
		int yTile = tile / 32; // gets y position
		int toffs = xTile * 8 + yTile * 8 * sheet.width; // Gets the offset, the 8's represent the size of the tile. (8 by 8 pixels)
		
		for (int y = 0; y < 8; y++) { // Loops 8 times (because of the height of the tile)
			int ys = y; // current y pixel
			if (mirrorY) ys = 7 - y; // Reverses the pixel for a mirroring effect
			if (y + yp < 0 || y + yp >= h) continue; // If the pixel is out of bounds, then skip the rest of the loop.
			for (int x = 0; x < 8; x++) { // Loops 8 times (because of the width of the tile)
				if (x + xp < 0 || x + xp >= w) continue; // skip rest if out of bounds.

				int xs = x; // current x pixel
				if (mirrorX) xs = 7 - x; // Reverses the pixel for a mirroring effect
				int col = (colors >> (sheet.pixels[xs + ys * sheet.width + toffs] * 8)) & 255; // gets the color based on the passed in colors value.
				if (col < 255) pixels[(x + xp) + (y + yp) * w] = col; // Inserts the colors into the image.
			}
		}
	}
	
	/** Sets the offset of the screen */
	public void setOffset(int xOffset, int yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	
	/* Used for the scattered dots at the edge of the light radius underground. */
	private int[] dither = new int[] {0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5,};
	
	/** Overlays the screen with pixels */
	public void overlay(Screen screen2, int xa, int ya) {
		int[] oPixels = screen2.pixels;  // The Integer array of pixels to overlay the screen with.
		int i = 0; // current pixel on the screen
		for (int y = 0; y < h; y++) { // loop through height of screen
			for (int x = 0; x < w; x++) { // loop through width of screen
				/* if the current pixel divided by 10 is smaller than the dither thingy with a complicated formula
					then it will fill the pixel with a black color. Yep, Nailed it! */
				if (oPixels[i] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) pixels[i] = 0;
				i++; // moves to the next pixel.
			}
		}
	}

	public void renderLight(int x, int y, int r) {
		//applies offsets:
		x -= xOffset;
		y -= yOffset;
		//starting, ending, x, y, positions of the circle (of light)
		int x0 = x - r;
		int x1 = x + r;
		int y0 = y - r;
		int y1 = y + r;
		
		//prevent light from rendering off the screen:
		if (x0 < 0) x0 = 0;
		if (y0 < 0) y0 = 0;
		if (x1 > w) x1 = w;
		if (y1 > h) y1 = h;
		// if(com.mojang.ld22.Game.debug) System.out.println(x0 + ", " + x1 + " -> " + y0 + ", " + y1);
		for (int yy = y0; yy < y1; yy++) { // loop through each y position
			int yd = yy - y; // get distance to the previous y position.
			yd = yd * yd; // square that distance
			for (int xx = x0; xx < x1; xx++) { // loop though each x pos
				int xd = xx - x; //get x delta
				int dist = xd * xd + yd; //square x delta, then add the y delta, to get total distance.
				// if(com.mojang.ld22.Game.debug) System.out.println(dist);
				if (dist <= r * r) {
					// if the distance moved is less or equal to the radius...
					int br = 255 - dist * 255 / (r * r); // area where light will be rendered.
					if (pixels[xx + yy * w] < br) pixels[xx + yy * w] = br; // pixel cannot be smaller than br.
				}
			}
		}
	}
}
