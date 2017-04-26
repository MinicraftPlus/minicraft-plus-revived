package minicraft.gfx;

public class Screen {
	
	/// x and y offset of screen:
	public int xOffset;
	public int yOffset;
	
	// used for mirroring an image:
	public static final int BIT_MIRROR_X = 0x01; // written in hexadecimal; binary: 01
	public static final int BIT_MIRROR_Y = 0x02; // binary: 10

	public final int w, h; // width and height of the screen
	public int[] pixels; // pixels on the screen
	
	private SpriteSheet sheet; // the sprite sheet used in the game.
	
	public Screen(int w, int h, SpriteSheet sheet) {
		this.sheet = sheet;
		this.w = w;
		this.h = h;
		/// screen width and height are determined by the actual game window size, meaning the screen is only as big as the window.
		pixels = new int[w * h]; // makes new integer array for all the pixels on the screen.
	}
	
	/** Clears all the colors on the screen */
	public void clear(int color) {
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = color; // turns each pixel into a single color (clearing the screen!)
	}
	
	public int centerText(String text) {
		return (w - Font.textWidth(text)) / 2;
	}
	
	/** Renders an object from the sprite sheet based on screen coordinates, tile (SpriteSheet location), colors, and bits (for mirroring) */
	public void render(int xp, int yp, int tile, int colors, int bits) {
		// xp and yp are originally in level coordinates, but offset turns them to screen coordinates.
		xp -= xOffset; //account for screen offset
		yp -= yOffset;
		// determines if the image should be mirrored...
		boolean mirrorX = (bits & BIT_MIRROR_X) > 0; // horizontally.
		boolean mirrorY = (bits & BIT_MIRROR_Y) > 0; // vertically.
		
		int xTile = tile % 32; // gets x position of the spritesheet "tile"
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
		// this is called in few places, one of which is level.renderBackground, rigth before all the tiles are rendered. The offset is determined by the Game class (this only place renderBackground is called), by using the screen's width and the player's position in the level.
		// in other words, the offset is a conversion factor from level coordinates to screen coordinates. It makes a certain coord in the level the upper left corner of the screen, when subtracted from the tile coord.
		
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	
	/* Used for the scattered dots at the edge of the light radius underground. */
	private int[] dither = new int[] {0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5};
	
	/** Overlays the screen with pixels */
    public void overlay(Screen screen2, int xa, int ya) {
        int[] oPixels = screen2.pixels;  // The Integer array of pixels to overlay the screen with.
        int i = 0; // current pixel on the screen
        for (int y = 0; y < h; y++) { // loop through height of screen
            for (int x = 0; x < w; x++) { // loop through width of screen
                if (oPixels[i] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) {
                    if(minicraft.Game.currentLevel < 3) {
                        /* if the current pixel divided by 10 is smaller than the dither thingy with a complicated formula then it will fill the pixel with a black color. Yep, Nailed it! */
                            pixels[i] = 0;
                    } else {
                            int r = (pixels[i] / 36) % 6;
                            int g = (pixels[i] / 6) % 6;
                            int b = pixels[i] % 6;
                            if(r > 0) r--;
                            if(g > 0) g--;
                            if(b > 0) b--;
                            pixels[i] = r * 36 + g * 6 + b;
                    }
                }
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
		
		for (int yy = y0; yy < y1; yy++) { // loop through each y position
			int yd = yy - y; // get distance to the previous y position.
			yd = yd * yd; // square that distance
			for (int xx = x0; xx < x1; xx++) { // loop though each x pos
				int xd = xx - x; //get x delta
				int dist = xd * xd + yd; //square x delta, then add the y delta, to get total distance.
				
				if (dist <= r * r) {
					// if the distance moved is less or equal to the radius...
					int br = 255 - dist * 255 / (r * r); // area where light will be rendered.
					if (pixels[xx + yy * w] < br) pixels[xx + yy * w] = br; // pixel cannot be smaller than br.
				}
			}
		}
	}
}
