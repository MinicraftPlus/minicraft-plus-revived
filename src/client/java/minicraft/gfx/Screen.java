package minicraft.gfx;

import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;

import java.util.Arrays;

public class Screen {

	public static final int w = Renderer.WIDTH; // Width of the screen
	public static final int h = Renderer.HEIGHT; // Height of the screen
	public static final Point center = new Point(w / 2, h / 2);

	private static final int MAXDARK = 128;

	/// x and y offset of screen:
	private int xOffset;
	private int yOffset;

	// Used for mirroring an image:
	private static final int BIT_MIRROR_X = 0x01; // Written in hexadecimal; binary: 01
	private static final int BIT_MIRROR_Y = 0x02; // Binary: 10

	public int[] pixels; // Pixels on the screen

	// Outdated Information:
	// Since each sheet is 256x256 pixels, each one has 1024 8x8 "tiles"
	// So 0 is the start of the item sheet 1024 the start of the tile sheet, 2048 the start of the entity sheet,
	// And 3072 the start of the gui sheet

	public Screen() {
		/// Screen width and height are determined by the actual game window size, meaning the screen is only as big as the window.
		pixels = new int[Screen.w * Screen.h]; // Makes new integer array for all the pixels on the screen.
	}

	/**
	 * Clears all the colors on the screen
	 */
	public void clear(int color) {
		// Turns each pixel into a single color (clearing the screen!)
		Arrays.fill(pixels, color);
	}

	public void render(int xp, int yp, int xt, int yt, int bits, MinicraftImage sheet) {
		render(xp, yp, xt, yt, bits, sheet, -1);
	}

	public void render(int xp, int yp, int xt, int yt, int bits, MinicraftImage sheet, int whiteTint) {
		render(xp, yp, xt, yt, bits, sheet, whiteTint, false);
	}

	/**
	 * This method takes care of assigning the correct spritesheet to assign to the sheet variable
	 **/
	public void render(int xp, int yp, int xt, int yt, int bits, MinicraftImage sheet, int whiteTint, boolean fullbright) {
		render(xp, yp, xt, yt, bits, sheet, whiteTint, fullbright, 0);
	}

	public void render(int xp, int yp, LinkedSprite sprite) {
		render(xp, yp, sprite.getSprite());
	}

	public void render(int xp, int yp, Sprite sprite) {
		render(xp, yp, sprite, false);
	}

	public void render(int xp, int yp, Sprite sprite, boolean fullbright) {
		render(xp, yp, sprite, 0, fullbright, 0);
	}

	public void render(int xp, int yp, Sprite sprite, int mirror, boolean fullbright) {
		render(xp, yp, sprite, mirror, fullbright, 0);
	}

	public void render(int xp, int yp, Sprite sprite, int mirror, boolean fullbright, int color) {
		for (int r = 0; r < sprite.spritePixels.length; r++) {
			for (int c = 0; c < sprite.spritePixels[r].length; c++) {
				Sprite.Px px = sprite.spritePixels[r][c];
				render(xp + c * 8, yp + r * 8, px, mirror, sprite.color, fullbright, color);
			}
		}
	}

	public void render(int xp, int yp, Sprite.Px pixel) {
		render(xp, yp, pixel, -1);
	}

	public void render(int xp, int yp, Sprite.Px pixel, int whiteTint) {
		render(xp, yp, pixel, 0, whiteTint);
	}

	public void render(int xp, int yp, Sprite.Px pixel, int mirror, int whiteTint) {
		render(xp, yp, pixel, mirror, whiteTint, false);
	}

	public void render(int xp, int yp, Sprite.Px pixel, int mirror, int whiteTint, boolean fullbright) {
		render(xp, yp, pixel, mirror, whiteTint, fullbright, 0);
	}

	public void render(int xp, int yp, Sprite.Px pixel, int mirror, int whiteTint, boolean fullbright, int color) {
		render(xp, yp, pixel.x, pixel.y, pixel.mirror ^ mirror, pixel.sheet, whiteTint, fullbright, color);
	}

	/**
	 * Renders an object from the sprite sheet based on screen coordinates, tile (SpriteSheet location), colors, and bits (for mirroring). I believe that xp and yp refer to the desired position of the upper-left-most pixel.
	 */
	public void render(int xp, int yp, int xt, int yt, int bits, MinicraftImage sheet, int whiteTint, boolean fullbright, int color) {
		if (sheet == null) return; // Verifying that sheet is not null.

		// xp and yp are originally in level coordinates, but offset turns them to screen coordinates.
		xp -= xOffset; //account for screen offset
		yp -= yOffset;

		// Determines if the image should be mirrored...
		boolean mirrorX = (bits & BIT_MIRROR_X) > 0; // Horizontally.
		boolean mirrorY = (bits & BIT_MIRROR_Y) > 0; // Vertically.

		// Validation check
		if (sheet == null || xt * 8 + yt * 8 * sheet.width + 7 + 7 * sheet.width >= sheet.pixels.length) {
			sheet = Renderer.spriteLinker.missingSheet(SpriteType.Item);
			xt = 0;
			yt = 0;
		}

		int xTile = xt; // Gets x position of the spritesheet "tile"
		int yTile = yt; // Gets y position
		int toffs = xTile * 8 + yTile * 8 * sheet.width; // Gets the offset of the sprite into the spritesheet pixel array, the 8's represent the size of the box. (8 by 8 pixel sprite boxes)

		// THIS LOOPS FOR EVERY PIXEL
		for (int y = 0; y < 8; y++) { // Loops 8 times (because of the height of the tile)
			int ys = y; // Current y pixel
			if (mirrorY) ys = 7 - y; // Reverses the pixel for a mirroring effect
			if (y + yp < 0 || y + yp >= h) continue; // If the pixel is out of bounds, then skip the rest of the loop.
			for (int x = 0; x < 8; x++) { // Loops 8 times (because of the width of the tile)
				if (x + xp < 0 || x + xp >= w) continue; // Skip rest if out of bounds.

				int xs = x; // Current x pixel
				if (mirrorX) xs = 7 - x; // Reverses the pixel for a mirroring effect

				int col = sheet.pixels[toffs + xs + ys * sheet.width]; // Gets the color of the current pixel from the value stored in the sheet.

				boolean isTransparent = (col >> 24 == 0);

				if (!isTransparent) {
					int index = (x + xp) + (y + yp) * w;

					if (whiteTint != -1 && col == 0x1FFFFFF) {
						// If this is white, write the whiteTint over it
						pixels[index] = Color.upgrade(whiteTint);
					} else {
						// Inserts the colors into the image
						if (fullbright) {
							pixels[index] = Color.WHITE;
						} else {
							if (color != 0) {

								pixels[index] = color;
							} else {
								pixels[index] = Color.upgrade(col);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Sets the offset of the screen
	 */
	public void setOffset(int xOffset, int yOffset) {
		// This is called in few places, one of which is level.renderBackground, right before all the tiles are rendered. The offset is determined by the Game class (this only place renderBackground is called), by using the screen's width and the player's position in the level.
		// In other words, the offset is a conversion factor from level coordinates to screen coordinates. It makes a certain coord in the level the upper left corner of the screen, when subtracted from the tile coord.

		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	/* Used for the scattered dots at the edge of the light radius underground.

		These values represent the minimum light level, on a scale from 0 to 25 (255/10), 0 being no light, 25 being full light (which will be portrayed as transparent on the overlay lightScreen pixels) that a pixel must have in order to remain lit (not black).
		each row and column is repeated every 4 pixels in the proper direction, so the pixel lightness minimum varies. It's highly worth note that, as the rows progress and loop, there's two sets or rows (1,4 and 2,3) whose values in the same column add to 15. The exact same is true for columns (sets are also 1,4 and 2,3), execpt the sums of values in the same row and set differ for each row: 10, 18, 12, 20. Which... themselves... are another set... adding to 30... which makes sense, sort of, since each column totals 15+15=30.
		In the end, "every other every row", will need, for example in column 1, 15 light to be lit, then 0 light to be lit, then 12 light to be lit, then 3 light to be lit. So, the pixels of lower light levels will generally be lit every other pixel, while the brighter ones appear more often. The reason for the variance in values is to provide EVERY number between 0 and 15, so that all possible light levels (below 16) are represented fittingly with their own pattern of lit and not lit.
		16 is the minimum pixel lighness required to ensure that the pixel will always remain lit.
	*/
	private static final int[] dither = new int[]{
		0, 8, 2, 10,
		12, 4, 14, 6,
		3, 11, 1, 9,
		15, 7, 13, 5
	};

	/**
	 * Overlays the screen with pixels
	 */
	public void overlay(Screen screen2, int currentLevel, int xa, int ya) {
		double tintFactor = 0;
		if (currentLevel >= 3 && currentLevel < 5) {
			int transTime = Updater.dayLength / 4;
			double relTime = (Updater.tickCount % transTime) * 1.0 / transTime;

			switch (Updater.getTime()) {
				case Morning:
					tintFactor = Updater.pastDay1 ? (1 - relTime) * MAXDARK : 0;
					break;
				case Day:
					tintFactor = 0;
					break;
				case Evening:
					tintFactor = relTime * MAXDARK;
					break;
				case Night:
					tintFactor = MAXDARK;
					break;
			}

			if (currentLevel > 3) tintFactor -= (tintFactor < 10 ? tintFactor : 10);
			tintFactor *= -1; // All previous operations were assuming this was a darkening factor.
		} else if (currentLevel >= 5)
			tintFactor = -MAXDARK;

		int[] oPixels = screen2.pixels;  // The Integer array of pixels to overlay the screen with.
		int i = 0; // Current pixel on the screen
		for (int y = 0; y < h; y++) { // loop through height of screen
			for (int x = 0; x < w; x++) { // loop through width of screen
				if (oPixels[i] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) {

					/// The above if statement is simply comparing the light level stored in oPixels with the minimum light level stored in dither. if it is determined that the oPixels[i] is less than the minimum requirements, the pixel is considered "dark", and the below is executed...
					if (currentLevel < 3) { // if in caves...
						/// in the caves, not being lit means being pitch black.
						pixels[i] = 0;
					} else {
						/// Outside the caves, not being lit simply means being darker.
						pixels[i] = Color.tintColor(pixels[i], (int) tintFactor); // darkens the color one shade.
					}
				}

				// Increase the tinting of all colors by 20.
				pixels[i] = Color.tintColor(pixels[i], 20);
				i++; // Moves to the next pixel.
			}
		}
	}

	public void renderLight(int x, int y, int r) {
		// Applies offsets:
		x -= xOffset;
		y -= yOffset;
		// Starting, ending, x, y, positions of the circle (of light)
		int x0 = x - r;
		int x1 = x + r;
		int y0 = y - r;
		int y1 = y + r;

		// Prevent light from rendering off the screen:
		if (x0 < 0) x0 = 0;
		if (y0 < 0) y0 = 0;
		if (x1 > w) x1 = w;
		if (y1 > h) y1 = h;

		for (int yy = y0; yy < y1; yy++) { // Loop through each y position
			int yd = yy - y; // Get distance to the previous y position.
			yd = yd * yd; // Square that distance
			for (int xx = x0; xx < x1; xx++) { // Loop though each x pos
				int xd = xx - x; // Get x delta
				int dist = xd * xd + yd; // Square x delta, then add the y delta, to get total distance.

				if (dist <= r * r) {
					// If the distance from the center (x,y) is less or equal to the radius...
					int br = 255 - dist * 255 / (r * r); // area where light will be rendered. // r*r is becuase dist is still x*x+y*y, of pythag theorem.
					// br = brightness... literally. from 0 to 255.
					if (pixels[xx + yy * w] < br)
						pixels[xx + yy * w] = br; // Pixel cannot be smaller than br; in other words, the pixel color (brightness) cannot be less than br.
				}
			}
		}
	}
}
