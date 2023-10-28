package minicraft.gfx;

import com.aparapi.Kernel;
import com.aparapi.Range;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.util.MyUtils;
import org.intellij.lang.annotations.MagicConstant;

import java.util.ArrayDeque;
import java.util.HashMap;

public class Screen {

	public static final int w = Renderer.WIDTH; // Width of the screen
	public static final int h = Renderer.HEIGHT; // Height of the screen
	public static final Point center = new Point(w/2, h/2);

	private static final int MAXDARK = 128;

	/// x and y offset of screen:
	private int xOffset;
	private int yOffset;

	// Used for mirroring an image:
	private static final int BIT_MIRROR_X = 0x01; // Written in hexadecimal; binary: 01
	private static final int BIT_MIRROR_Y = 0x02; // Binary: 10

	protected int[] pixels; // Pixels on the screen

	private final ArrayDeque<Rendering> renderings = new ArrayDeque<>();
	private ScreenRenderingKernel renderingKernel;
//	private SpriteRenderingDelegate spriteRenderingDelegate;
//	private ClearRenderingDelegate clearRenderingDelegate;
//	private FillRectRenderingDelegate fillRectRenderingDelegate;
//	private DrawAxisLineRenderingDelegate drawAxisLineRenderingDelegate;
//	private OverlayRenderingDelegate overlayRenderingDelegate;

	private interface Rendering {
		void render();
	}

	private abstract static class RenderingDelegate {
		protected final Kernel kernel;

		public RenderingDelegate(Kernel kernel) {
			this.kernel = kernel;
			kernel.setExplicit(true);
		}
	}

	// Outdated Information:
	// Since each sheet is 256x256 pixels, each one has 1024 8x8 "tiles"
	// So 0 is the start of the item sheet 1024 the start of the tile sheet, 2048 the start of the entity sheet,
	// And 3072 the start of the gui sheet

	public Screen() {
		/// Screen width and height are determined by the actual game window size, meaning the screen is only as big as the window.
		pixels = new int[Screen.w * Screen.h]; // Makes new integer array for all the pixels on the screen.
	}

	/** Initializes the screen with the given pixel array instance. */
	public void init(int[] pixels) {
		this.pixels = pixels;
		renderingKernel = new ScreenRenderingKernel();
		renderingKernel.pixels = pixels;
		renderingKernel.setExplicit(true);
		renderingKernel.put(pixels);
//		spriteRenderingDelegate = new SpriteRenderingDelegate();
//		clearRenderingDelegate = new ClearRenderingDelegate();
//		fillRectRenderingDelegate = new FillRectRenderingDelegate();
//		drawAxisLineRenderingDelegate = new DrawAxisLineRenderingDelegate();
//		overlayRenderingDelegate = new OverlayRenderingDelegate();
	}

	/** Clears all the colors on the screen */
	public void clear(int color) {
		// Turns each pixel into a single color (clearing the screen!)
//		renderings.add(new ClearRendering(color));
		renderingKernel.executeScreenClear(color);
	}

//	private class ClearRendering implements Rendering {
//		private final int color;
//
//		public ClearRendering(int color) {
//			this.color = color;
//		}
//
//		@Override
//		public void render() {
//			clearRenderingDelegate.render(new int[] {color}, pixels);
//		}
//	}

//	private static class ClearRenderingDelegate extends RenderingDelegate {
//		@SuppressWarnings("MismatchedReadAndWriteOfArray")
//		private static Kernel makeKernel() {
//			int[] color = new int[1], pixels = new int[1];
//			return new Kernel() {
//				@Override
//				public void run() {
//					pixels[getGlobalId()] = color[0];
//				}
//			};
//		}
//
//		public ClearRenderingDelegate() {
//			super(makeKernel());
//		}
//
//		public void render(int[] color, int[] pixels) {
//			kernel.put(color).put(pixels);
//			kernel.execute(pixels.length);
//			kernel.get(pixels);
//		}
//	}

	public void flush() {
//		Rendering rendering;
//		while ((rendering = renderings.poll()) != null) {
//			rendering.render();
//		}
		renderingKernel.get(pixels);
	}

	public void render(int xp, int yp, int xt, int yt, int bits, MinicraftImage sheet) { render(xp, yp, xt, yt, bits, sheet, -1); }
    public void render(int xp, int yp, int xt, int yt, int bits, MinicraftImage sheet, int whiteTint) { render(xp, yp, xt, yt, bits, sheet, whiteTint, false); }
	/** This method takes care of assigning the correct spritesheet to assign to the sheet variable **/
    public void render(int xp, int yp, int xt, int yt, int bits, MinicraftImage sheet, int whiteTint, boolean fullbright) {
		render(xp, yp, xt, yt, bits, sheet, whiteTint, fullbright, 0);
    }

	public void render(int xp, int yp, LinkedSprite sprite) { render(xp, yp, sprite.getSprite()); }
	public void render(int xp, int yp, Sprite sprite) { render(xp, yp, sprite, false); }
	public void render(int xp, int yp, Sprite sprite, boolean fullbright) { render(xp, yp, sprite, 0, fullbright, 0); }
	public void render(int xp, int yp, Sprite sprite, int mirror, boolean fullbright) { render(xp, yp, sprite, mirror, fullbright, 0); }
	public void render(int xp, int yp, Sprite sprite, int mirror, boolean fullbright, int color) {
		boolean mirrorX = (mirror & BIT_MIRROR_X) > 0; // Horizontally.
		boolean mirrorY = (mirror & BIT_MIRROR_Y) > 0; // Vertically.
		for (int r = 0; r < sprite.spritePixels.length; r++) {
			int lr = mirrorY ? sprite.spritePixels.length - 1 - r : r;
			for (int c = 0; c < sprite.spritePixels[lr].length; c++) {
				Sprite.Px px = sprite.spritePixels[lr][mirrorX ? sprite.spritePixels[lr].length - 1 - c : c];
				render(xp + c * 8, yp + r * 8, px, mirror, sprite.color, fullbright, color);
			}
		}
	}

	public void render(int xp, int yp, Sprite.Px pixel) { render(xp, yp, pixel, -1); }
	public void render(int xp, int yp, Sprite.Px pixel, int whiteTint) { render(xp, yp, pixel, 0, whiteTint); }
	public void render(int xp, int yp, Sprite.Px pixel, int mirror, int whiteTint) { render(xp, yp, pixel, mirror, whiteTint, false); }
	public void render(int xp, int yp, Sprite.Px pixel, int mirror, int whiteTint, boolean fullbright) { render(xp, yp, pixel, mirror, whiteTint, fullbright, 0); }
	public void render(int xp, int yp, Sprite.Px pixel, int mirror, int whiteTint, boolean fullbright, int color) {
		render(xp, yp, pixel.x, pixel.y, pixel.mirror ^ mirror, pixel.sheet, whiteTint, fullbright, color);
	}

    /** Renders an object from the sprite sheet based on screen coordinates, tile (SpriteSheet location), colors, and bits (for mirroring). I believe that xp and yp refer to the desired position of the upper-left-most pixel. */
    public void render(int xp, int yp, int xt, int yt, int bits, MinicraftImage sheet, int whiteTint, boolean fullbright, int color) {
		if (sheet == null) return; // Verifying that sheet is not null.

		// Validation check
		if (xt * 8 + yt * 8 * sheet.width + 7 + 7 * sheet.width >= sheet.pixels.length) {
			render(xp, yp, 0, 0, bits, Renderer.spriteLinker.missingSheet(SpriteType.Item), -1, false, 0);
			return;
		}

		// xp and yp are originally in level coordinates, but offset turns them to screen coordinates.
		// xOffset and yOffset account for screen offset
		render(xp - xOffset, yp - yOffset, xt * 8, yt * 8, 8, 8, sheet, bits, whiteTint, fullbright, color);
	}

	public void render(int xp, int yp, int xt, int yt, int tw, int th, MinicraftImage sheet) {
		render(xp, yp, xt, yt ,tw, th, sheet, -1);
	}
	public void render(int xp, int yp, int xt, int yt, int tw, int th, MinicraftImage sheet, int mirrors) {
		render(xp, yp, xt, yt ,tw, th, sheet, mirrors, -1);
	}
	public void render(int xp, int yp, int xt, int yt, int tw, int th, MinicraftImage sheet, int mirrors, int whiteTint) {
		render(xp, yp, xt, yt, tw, th, sheet, mirrors, whiteTint, false);
	}
	public void render(int xp, int yp, int xt, int yt, int tw, int th, MinicraftImage sheet, int mirrors, int whiteTint, boolean fullbright) {
		render(xp, yp, xt, yt, tw, th, sheet, mirrors, whiteTint, fullbright, 0);
	}
	public void render(int xp, int yp, int xt, int yt, int tw, int th, MinicraftImage sheet, int mirrors, int whiteTint, boolean fullbright, int color) {
		if (sheet == null) return; // Verifying that sheet is not null.

		// Determines if the image should be mirrored...
		boolean mirrorX = (mirrors & BIT_MIRROR_X) > 0; // Horizontally.
		boolean mirrorY = (mirrors & BIT_MIRROR_Y) > 0; // Vertically.
//		renderings.add(new SpriteRendering(xp, yp, xt + yt * sheet.width, tw, th, whiteTint, color,
//			mirrorX, mirrorY, fullbright, sheet));
		renderingKernel.executeSpriteRender(xp, yp, xt + yt * sheet.width, tw, th, whiteTint, color,
			mirrorX, mirrorY, fullbright, sheet);
	}

//	private class SpriteRendering implements Rendering {
//		private final int xp, yp, toffs, tw, th, whiteTint, color;
//		private final boolean mirrorX, mirrorY, fullBright;
//		private final MinicraftImage sheet;
//
//		public SpriteRendering(int xp, int yp, int toffs, int tw, int th, int whiteTint, int color, boolean mirrorX,
//		                       boolean mirrorY, boolean fullBright, MinicraftImage sheet) {
//			this.xp = xp;
//			this.yp = yp;
//			this.toffs = toffs;
//			this.tw = tw;
//			this.th = th;
//			this.whiteTint = whiteTint;
//			this.color = color;
//			this.mirrorX = mirrorX;
//			this.mirrorY = mirrorY;
//			this.fullBright = fullBright;
//			this.sheet = sheet;
//		}
//
//		@Override
//		public void render() {
//			spriteRenderingDelegate.render(new int[] {xp}, new int[] {yp}, new int[] {tw}, new int[] {th},
//				new int[] {toffs}, new int[] {sheet.width}, new int[] {whiteTint}, new int[] {color},
//				new boolean[] {mirrorX}, new boolean[] {mirrorY}, new boolean[] {fullBright}, sheet.pixels, pixels);
//		}
//	}

//	private static class SpriteRenderingDelegate extends RenderingDelegate {
//		@SuppressWarnings("MismatchedReadAndWriteOfArray")
//		private static Kernel makeKernel() {
//			// Constants
//			int WHITE = Color.WHITE;
//			int w = Screen.w, h = Screen.h;
//			// All of these variables are placeholders to bypass Aparapi limitations.
//			int[] yp = new int[1], xp = new int[1], tw = new int[] {2}, th = new int[] {2},
//				toffs = new int[1], sheetWidth = new int[] {2}, whiteTint = new int[] {-1}, color = new int[1];
//			boolean[] mirrorX = new boolean[1], mirrorY = new boolean[1], wBright = new boolean[1];
//			int[][] sheetPixels = f; int[] pixels = Renderer.screen.pixels;
//			return new Kernel() {
//				@Override
//				public void run() {
//					int x = getGlobalId(0);
//					int y = getGlobalId(1);
//					if (y + yp[0] < 0 || y + yp[0] >= h) return; // If the pixel is out of bounds, then skip the rest of the loop.
//					if (x + xp[0] < 0 || x + xp[0] >= w) return; // Skip rest if out of bounds.
//
//					int sx = mirrorX[0] ? tw[0] - 1 - x : x, sy = mirrorY[0] ? th[0] - 1 - y : y;
//					int col = sheetPixels[0][toffs[0] + sx + sy * sheetWidth[0]]; // Gets the color of the current pixel from the value stored in the sheet.
//					if (col >> 24 != 0) { // if not transparent
//						int index = (x + xp[0]) + (y + yp[0]) * w;
//						if (whiteTint[0] != -1 && col == 0x1FFFFFF) {
//							// If this is white, write the whiteTint over it
//							pixels[index] = Color.upgrade(whiteTint[0]);
//						} else {
//							// Inserts the colors into the image
//							if (wBright[0]) {
//								pixels[index] = WHITE;
//							} else {
//								if (color[0] != 0) {
//
//									pixels[index] = color[0];
//								} else {
//									pixels[index] = Color.upgrade(col);
//								}
//							}
//						}
//					}
//				}
//			};
//		}
//
//		public SpriteRenderingDelegate() {
//			super(makeKernel());
//		}
//
//		public void render(int[] val$xp, int[] val$yp, int[] val$tw, int[] val$th, int[] val$toffs, int[] val$sheetWidth, int[] val$whiteTint, int[] val$color,
//		                   boolean[] val$mirrorX, boolean[] val$mirrorY, boolean[] val$wBright, int[] val$sheetPixels, int[] val$pixels) {
//			f[0] = new int[new Random().nextInt(128)+10];
//			Arrays.fill(f[0], 256);
//			kernel.put(f).put(val$xp).put(val$yp).put(val$tw).put(val$th).put(val$toffs).put(val$sheetWidth).put(val$whiteTint)
//				.put(val$color).put(val$mirrorX).put(val$mirrorY).put(val$wBright).put(val$sheetPixels).put(val$pixels);
//			kernel.execute(Range.create2D(val$tw[0], val$th[0])); // Loops a whole sheet area.
//			kernel.get(val$pixels);
//		}
//	}

	public void fillRect(int xp, int yp, int w, int h, int color) {
//		renderings.add(new FillRectRendering(xp, yp, color, w, h));
		renderingKernel.executeFillRect(xp, yp, color, w, h);
	}

//	private class FillRectRendering implements Rendering {
//		private final int xp, yp, color, w, h;
//
//		public FillRectRendering(int xp, int yp, int color, int w, int h) {
//			this.xp = xp;
//			this.yp = yp;
//			this.color = color;
//			this.w = w;
//			this.h = h;
//		}
//
//		@Override
//		public void render() {
//			fillRectRenderingDelegate.render(new int[] {xp}, new int[] {yp}, new int[] {color}, pixels, w, h);
//		}
//	}

//	private static class FillRectRenderingDelegate extends RenderingDelegate {
//		@SuppressWarnings("MismatchedReadAndWriteOfArray")
//		private static Kernel makeKernel() {
//			int w = Screen.w, h = Screen.h;
//			int[] xp = new int[1], yp = new int[1], color = new int[1], pixels = new int[1];
//			return new Kernel() {
//				@Override
//				public void run() {
//					int x = xp[0] + getGlobalId(0);
//					int y = yp[0] + getGlobalId(1);
//					if (y < 0 || y >= h || x < 0 || x >= w) return;
//					pixels[x + y * w] = color[0];
//				}
//			};
//		}
//
//		public FillRectRenderingDelegate() {
//			super(makeKernel());
//		}
//
//		public void render(int[] xp, int[] yp, int[] color, int[] pixels, int w, int h) {
//			kernel.put(xp).put(yp).put(color).put(pixels);
//			kernel.execute(Range.create2D(w, h));
//			kernel.get(pixels);
//		}
//	}

	/**
	 * Draw a straight line along an axis.
	 * @param axis The axis to draw along: {@code 0} for x-axis; {@code 1} for y-axis
	 * @param l The length of the line
	 */
	public void drawAxisLine(int xp, int yp, @MagicConstant(intValues = {0, 1}) int axis, int l, int color) {
//		renderings.add(new DrawAxisLineRendering(xp, yp, axis, color, l));
		renderingKernel.executeDrawAxisLine(xp, yp, axis, color, l);
	}

//	private class DrawAxisLineRendering implements Rendering {
//		private final int xp, yp, axis, color, l;
//
//		public DrawAxisLineRendering(int xp, int yp, int axis, int color, int l) {
//			this.xp = xp;
//			this.yp = yp;
//			this.axis = axis;
//			this.color = color;
//			this.l = l;
//		}
//
//		@Override
//		public void render() {
//			drawAxisLineRenderingDelegate.render(new int[] {xp}, new int[] {yp}, new int[] {axis},
//				new int[] {color}, pixels, l);
//		}
//	}

//	private static class DrawAxisLineRenderingDelegate extends RenderingDelegate {
//		@SuppressWarnings("MismatchedReadAndWriteOfArray")
//		private static Kernel makeKernel() {
//			int w = Screen.w;
//			int[] axis = new int[1], color = new int[1], xp = new int[1], yp = new int[1];
//			int[] pixels = new int[1];
//			return new Kernel() {
//				@Override
//				public void run() {
//					int id = getGlobalId();
//					if (axis[0] == 0) { // x-axis
//						pixels[xp[0] + id + yp[0] * w] = color[0];
//					} else { // y-axis
//						pixels[xp[0] + (yp[0] + id) * w] = color[0];
//					}
//				}
//			};
//		}
//
//		public DrawAxisLineRenderingDelegate() {
//			super(makeKernel());
//		}
//
//		public void render(int[] xp, int[] yp, int[] axis, int[] color, int[] pixels, int l) {
//			kernel.put(xp).put(yp).put(axis).put(color).put(pixels);
//			kernel.execute(l);
//			kernel.get(pixels);
//		}
//	}

	/** Sets the offset of the screen */
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
	private static final int[] dither = new int[] {
		0, 8, 2, 10,
		12, 4, 14, 6,
		3, 11, 1, 9,
		15, 7, 13, 5
	};

	/** Overlays the screen with pixels */
    public void overlay(Screen screen2, int currentLevel, int xa, int ya) {
		double tintFactor = 0;
		if (currentLevel >= 3 && currentLevel < 5) {
			int transTime = Updater.dayLength / 4;
			double relTime = (Updater.tickCount % transTime) * 1.0 / transTime;

			switch (Updater.getTime()) {
				case Morning: tintFactor = Updater.pastDay1 ? (1-relTime) * MAXDARK : 0; break;
				case Day: tintFactor = 0; break;
				case Evening: tintFactor = relTime * MAXDARK; break;
				case Night: tintFactor = MAXDARK; break;
			}

			if (currentLevel > 3) tintFactor -= (tintFactor < 10 ? tintFactor : 10);
			tintFactor *= -1; // All previous operations were assuming this was a darkening factor.
		}
		else if(currentLevel >= 5)
			tintFactor = -MAXDARK;

	    // The Integer array of pixels to overlay the screen with.
//	    renderings.add(new OverlayRendering(xa, ya, currentLevel, (int) tintFactor, screen2.pixels));
	    renderingKernel.executeOverlayRender(xa, ya, currentLevel, (int) tintFactor, screen2.pixels);
    }

//	private class OverlayRendering implements Rendering {
//		private final int xa, ya, currentLevel, tintFactor;
//		private final int[] oPixels;
//
//		public OverlayRendering(int xa, int ya, int currentLevel, int tintFactor, int[] oPixels) {
//			this.xa = xa;
//			this.ya = ya;
//			this.currentLevel = currentLevel;
//			this.tintFactor = tintFactor;
//			this.oPixels = oPixels;
//		}
//
//		@Override
//		public void render() {
//			overlayRenderingDelegate.render(new int[] {xa}, new int[] {ya}, new int[] {currentLevel},
//				new int[] {tintFactor}, oPixels, pixels);
//		}
//	}

//	private static class OverlayRenderingDelegate extends RenderingDelegate {
//		@SuppressWarnings("MismatchedReadAndWriteOfArray")
//		private static Kernel makeKernel() {
//			int w = Screen.w;
//			int[] dither = Screen.dither;
//			int[] xa = new int[1], ya = new int[1], currentLevel = new int[1], tintFactor = new int[1];
//			int[] oPixels = new int[1], pixels = new int[1];
//			return new Kernel() {
//				@Override
//				public void run() {
//					int x = getGlobalId(0);
//					int y = getGlobalId(1);
//					int id = x + y * w;
//					if (oPixels[id] / 10 <= dither[((x + xa[0]) & 3) + ((y + ya[0]) & 3) * 4]) {
//
//						/// The above if statement is simply comparing the light level stored in oPixels with the minimum light level stored in dither. if it is determined that the oPixels[i] is less than the minimum requirements, the pixel is considered "dark", and the below is executed...
//						if (currentLevel[0] < 3) { // if in caves...
//							/// in the caves, not being lit means being pitch black.
//							pixels[id] = 0;
//						} else {
//							/// Outside the caves, not being lit simply means being darker.
//							pixels[id] = Color.tintColor(pixels[id], tintFactor[0]); // darkens the color one shade.
//						}
//					}
//
//					// Increase the tinting of all colors by 20.
//					pixels[id] = Color.tintColor(pixels[id], 20);
//				}
//			};
//		}
//
//		public OverlayRenderingDelegate() {
//			super(makeKernel());
//		}
//
//		public void render(int[] xa, int[] ya, int[] currentLevel, int[] tintFactor, int[] oPixels, int[] pixels) {
//			kernel.put(xa).put(ya).put(currentLevel).put(tintFactor).put(oPixels).put(pixels);
//			kernel.execute(Range.create2D(w, h));
//			kernel.get(pixels);
//		}
//	}

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

		int xp = x;
		int yp = y;
		int xa = x0;
		int ya = y0;
		int ww = x1 - x0;
		int hh = y1 - y0;
		int[] pixels = this.pixels;
		int w = Screen.w, h = Screen.h;
//		renderings.add(new Rendering(new Kernel() {
//			@Override
//			public void run() {
//				int xx = xa + getGlobalId(0);
//				int yy = ya + getGlobalId(1);
//				if (xx < 0 || xx >= w || yy < 0 || yy >= h) return;
//				int yd = yy - yp; // Get distance to the previous y position.
//				yd = yd * yd; // Square that distance
//				int xd = xx - xp; // Get x delta
//				int dist = xd * xd + yd; // Square x delta, then add the y delta, to get total distance.
//				if (dist <= r * r) {
//					// If the distance from the center (x,y) is less or equal to the radius...
//					int br = 255 - dist * 255 / (r * r); // area where light will be rendered. // r*r is becuase dist is still x*x+y*y, of pythag theorem.
//					// br = brightness... literally. from 0 to 255.
//					if (pixels[xx + yy * w] < br) pixels[xx + yy * w] = br; // Pixel cannot be smaller than br; in other words, the pixel color (brightness) cannot be less than br.
//				}
//			}
//		}, kernel -> kernel.put(pixels), new Range2DParam(ww, hh)));
	}

	private static final class ScreenRenderingKernel extends Kernel {
		private final HashMap<int[], int[]> sheetPixelMap = new HashMap<>();
		private @Constant final int[] dither = new int[] {
			0, 8, 2, 10,
			12, 4, 14, 6,
			3, 11, 1, 9,
			15, 7, 13, 5
		};
		final int w = Screen.w, h = Screen.h, WHITE = Color.WHITE;
		static final int FUNC_CLEAR = 0;
		static final int FUNC_RENDER = 1;
		static final int FUNC_FILL_RECT = 2;
		static final int FUNC_DRAW_AXIS_LINE = 3;
		static final int FUNC_OVERLAY = 4;
		int xp, yp, toffs, tw, th, whiteTint, color, sheetWidth, xa, ya, ww, hh, axis, currentLevel, tintFactor;
		int[] sheetPixels = new int[65536]; // Limited 65536 pixels
		public int[] pixels;
		int[] oPixels = new int[w*h];
		@Constant boolean[] mirrorX = new boolean[1], mirrorY = new boolean[1], fullBright = new boolean[1];
		@MagicConstant(intValues = {FUNC_CLEAR, FUNC_RENDER, FUNC_FILL_RECT, FUNC_DRAW_AXIS_LINE, FUNC_OVERLAY})
		private int function;

		@Override
		public void run() {
			if (function == ScreenRenderingKernel.FUNC_CLEAR) {
				screenClear();
			} else if (function == ScreenRenderingKernel.FUNC_RENDER) {
				spriteRender();
			} else if (function == ScreenRenderingKernel.FUNC_FILL_RECT) {
				fillRect();
			} else if (function == ScreenRenderingKernel.FUNC_DRAW_AXIS_LINE) {
				drawAxisLine();
			} else if (function == ScreenRenderingKernel.FUNC_OVERLAY) {
				overlayRender();
			}
		}

		private void copySheetPixels(int[] src) {
			if (sheetPixelMap.containsKey(src)) {
				sheetPixels = sheetPixelMap.get(src);
				return;
			}
			int[] target = new int[65536];
			Kernel kernel = new Kernel() {
				@Override
				public void run() {
					int id = getGlobalId();
					target[id] = src[id];
				}
			};
			kernel.execute(src.length);
			kernel.dispose();
			sheetPixelMap.put(src, target);
			sheetPixels = target;
		}

		public void screenClear() {
			pixels[getGlobalId()] = color;
		}

		public void executeScreenClear(int color) {
			this.color = color;
			this.function = FUNC_CLEAR;
			execute(Range.create(pixels.length));
		}

		public void spriteRender() {
			int x = getGlobalId(0);
			int y = getGlobalId(1);
			if (y + yp < 0 || y + yp >= h) return; // If the pixel is out of bounds, then skip the rest of the loop.
			if (x + xp < 0 || x + xp >= w) return; // Skip rest if out of bounds.

			int sx = mirrorX[0] ? tw - 1 - x : x, sy = mirrorY[0] ? th - 1 - y : y;
			int col = sheetPixels[toffs + sx + sy * sheetWidth]; // Gets the color of the current pixel from the value stored in the sheet.
			if (col >> 24 != 0) { // if not transparent
				int index = (x + xp) + (y + yp) * w;
				if (whiteTint != -1 && col == 0x1FFFFFF) {
					// If this is white, write the whiteTint over it
					pixels[index] = Color.upgrade(whiteTint);
				} else {
					// Inserts the colors into the image
					if (fullBright[0]) {
						pixels[index] = WHITE;
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

		public void executeSpriteRender(int xp, int yp, int toffs, int tw, int th, int whiteTint, int color, boolean mirrorX,
		                                boolean mirrorY, boolean fullBright, MinicraftImage sheet) {
			this.xp = xp;
			this.yp = yp;
			this.toffs = toffs;
			this.tw = tw;
			this.th = th;
			this.whiteTint = whiteTint;
			this.color = color;
			this.mirrorX = new boolean[]{mirrorX};
			this.mirrorY = new boolean[]{mirrorY};
			this.fullBright = new boolean[]{fullBright};
			copySheetPixels(sheet.pixels);
			this.sheetWidth = sheet.width;
			this.function = FUNC_RENDER;
			execute(Range.create2D(tw, th)); // Loops a whole sheet area.
		}

		public void fillRect() {
			int x = xp + getGlobalId(0);
			int y = yp + getGlobalId(1);
			if (y < 0 || y >= h || x < 0 || x >= w) return;
			pixels[x + y * w] = color;
		}

		public void executeFillRect(int xp, int yp, int color, int w, int h) {
			this.xp = xp;
			this.yp = yp;
			this.color = color;
			this.function = FUNC_FILL_RECT;
			execute(Range.create2D(w, h));
		}

		public void drawAxisLine() {
			int id = getGlobalId();
			if (axis == 0) { // x-axis
				pixels[xp + id + yp * w] = color;
			} else { // y-axis
				pixels[xp + (yp + id) * w] = color;
			}
		}

		public void executeDrawAxisLine(int xp, int yp, int axis, int color, int l) {
			this.xp = xp;
			this.yp = yp;
			this.axis = axis;
			this.color = color;
			this.function = FUNC_DRAW_AXIS_LINE;
			execute(Range.create(l));
		}

		public void overlayRender() {
			int x = getGlobalId(0);
			int y = getGlobalId(1);
			int id = x + y * w;
			if (oPixels[id] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) {

				/// The above if statement is simply comparing the light level stored in oPixels with the minimum light level stored in dither. if it is determined that the oPixels[i] is less than the minimum requirements, the pixel is considered "dark", and the below is executed...
				if (currentLevel < 3) { // if in caves...
					/// in the caves, not being lit means being pitch black.
					pixels[id] = 0;
				} else {
					/// Outside the caves, not being lit simply means being darker.
					pixels[id] = tintColor(pixels[id], tintFactor); // darkens the color one shade.
				}
			}

			// Increase the tinting of all colors by 20.
			pixels[id] = tintColor(pixels[id], 20);
		}

		private static int tintColor(int rgbInt, int amount) {
			if (rgbInt < 0) return rgbInt; // This is "transparent".

			int r = (rgbInt & 0xFF_00_00) >> 16;
			int g = (rgbInt & 0x00_FF_00) >> 8;
			int b = (rgbInt & 0x00_00_FF);

			r = MyUtils.clamp(r+amount, 0, 255);
			g = MyUtils.clamp(g+amount, 0, 255);
			b = MyUtils.clamp(b+amount, 0, 255);

			return r << 16 | g << 8 | b;
		}

		public void executeOverlayRender(int xa, int ya, int currentLevel, int tintFactor, int[] oPixels) {
			this.xa = xa;
			this.ya = ya;
			this.currentLevel = currentLevel;
			this.tintFactor = tintFactor;
			this.oPixels = oPixels;
			this.function = FUNC_OVERLAY;
			execute(Range.create2D(w, h));
		}
	}
}
