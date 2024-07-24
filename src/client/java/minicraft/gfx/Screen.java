package minicraft.gfx;

import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

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

	private final BufferedImage image;
	private final int[] pixels;

	private final ArrayDeque<Rendering> renderings = new ArrayDeque<>();
	private final LightOverlay lightOverlay;
	private ClearRendering lastClearRendering = null;

	// Outdated Information:
	// Since each sheet is 256x256 pixels, each one has 1024 8x8 "tiles"
	// So 0 is the start of the item sheet 1024 the start of the tile sheet, 2048 the start of the entity sheet,
	// And 3072 the start of the gui sheet

	public Screen(BufferedImage image) {
		/// Screen width and height are determined by the actual game window size, meaning the screen is only as big as the window.buffer = new BufferedImage(Screen.w, Screen.h);
		this.image = image;
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		lightOverlay = new LightOverlay();
	}

	private interface Rendering {
		/** Invoked by {@link Renderer#render()}. */
		void render(Graphics2D graphics);
	}

	private void queue(Rendering rendering) {
		renderings.add(rendering);
	}

	private static abstract class ClearRendering implements Rendering {}

	private static class SolidClearRendering extends ClearRendering {
		private final int color;

		public SolidClearRendering(int color) {
			this.color = color;
		}

		@Override
		public void render(Graphics2D graphics) {

			graphics.setColor(new java.awt.Color(color));
			graphics.fillRect(0, 0, Screen.w, Screen.h);
		}
	}

	private static class PlainClearRendering extends ClearRendering {
		@Override
		public void render(Graphics2D graphics) {
			graphics.clearRect(0, 0, Screen.w, Screen.h);
		}
	}

	private class SpriteRendering implements Rendering {
		private final int xp, yp, xt, yt, tw, th, mirrors, whiteTint, color;
		private final boolean fullBright;
		private final MinicraftImage sheet;

		public SpriteRendering(int xp, int yp, int xt, int yt, int tw, int th,
		                       int mirrors, int whiteTint, boolean fullBright, int color, MinicraftImage sheet) {
			this.xp = xp;
			this.yp = yp;
			this.xt = xt;
			this.yt = yt;
			this.tw = tw;
			this.th = th;
			this.mirrors = mirrors;
			this.whiteTint = whiteTint;
			this.fullBright = fullBright;
			this.color = color;
			this.sheet = sheet;
		}

		@Override
		public void render(Graphics2D graphics) {
			int toffs = xt + yt * sheet.width;
			// Determines if the image should be mirrored...
			boolean mirrorX = (mirrors & BIT_MIRROR_X) > 0; // Horizontally.
			boolean mirrorY = (mirrors & BIT_MIRROR_Y) > 0; // Vertically.
			for (int y = 0; y < th; ++y) { // Relative
				if (y + yp < 0) continue; // If the pixel is out of bounds, then skip the rest of the loop.
				if (y + yp >= h) break;
				int sy = mirrorY ? th - 1 - y : y; // Source relative; reverse if necessary
				for (int x = 0; x < tw; ++x) { // Relative
					if (x + xp < 0) continue; // Skip rest if out of bounds.
					if (x + xp >= w) break;
					int sx = mirrorX ? tw - 1 - x : x; // Source relative; reverse if necessary
					int col = sheet.pixels[toffs + sx + sy * sheet.width]; // Gets the color of the current pixel from the value stored in the sheet.
					if (col >> 24 != 0) { // if not transparent
						int index = (xp + x) + (yp + y) * w;
						if (whiteTint != -1 && col == 0x1FFFFFF) {
							// If this is white, write the whiteTint over it
							pixels[index] = Color.upgrade(whiteTint);
						} else {
							// Inserts the colors into the image
							if (fullBright) {
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
	}

	private static class FillRectRendering implements Rendering {
		private final int xp, yp, w, h, color;

		public FillRectRendering(int xp, int yp, int w, int h, int color) {
			this.xp = xp;
			this.yp = yp;
			this.w = w;
			this.h = h;
			this.color = color;
		}

		@Override
		public void render(Graphics2D graphics) {
			graphics.setColor(new java.awt.Color(color));
			graphics.fillRect(xp, yp, w, h);
		}
	}

	private static class DrawRectRendering implements Rendering {
		private final int xp, yp, w, h, color;

		public DrawRectRendering(int xp, int yp, int w, int h, int color) {
			this.xp = xp;
			this.yp = yp;
			this.w = w;
			this.h = h;
			this.color = color;
		}

		@Override
		public void render(Graphics2D graphics) {
			graphics.setColor(new java.awt.Color(color));
			graphics.drawRect(xp, yp, w, h);
		}
	}

	private static class DrawLineRendering implements Rendering {
		private final int x0, y0, x1, y1, color;

		public DrawLineRendering(int x0, int y0, int x1, int y1, int color) {
			this.x0 = x0;
			this.y0 = y0;
			this.x1 = x1;
			this.y1 = y1;
			this.color = color;
		}

		@Override
		public void render(Graphics2D graphics) {
			graphics.setColor(new java.awt.Color(color));
			graphics.drawLine(x0, y0, x1, y1);
		}
	}

	/** Placeholder way, for Sign cursor rendering */
	private class DrawLineSpecialRendering implements Rendering {
		private final int x0, y0, l;
		private final @MagicConstant(intValues = {0, 1}) int axis; // 0: x-axis; 1: Y-axis

		public DrawLineSpecialRendering(int x0, int y0, int l, int axis) {
			this.x0 = x0;
			this.y0 = y0;
			this.l = l;
			this.axis = axis;
		}

		@Override
		public void render(Graphics2D graphics) {
			switch (axis) {
				case 0:
					for (int i = 0; i < l; i++) { // 1 pixel high and 8 pixel wide
						int idx = x0 + i + y0 * Screen.w;
						pixels[idx] = Color.getLightnessFromRGB(pixels[idx]) >= .5 ? Color.BLACK : Color.WHITE;
					}
					break;
				case 1:
					for (int i = 0; i < l; i++) { // 8 pixel high and 1 pixel wide
						int idx = x0 + (y0 + i) * Screen.w;
						pixels[idx] = Color.getLightnessFromRGB(pixels[idx]) >= .5 ? Color.BLACK : Color.WHITE;
					}
					break;
			}
		}
	}

	private class OverlayRendering implements Rendering {
		private final int currentLevel, xa, ya;
		private final double darkFactor;

		private OverlayRendering(int currentLevel, int xa, int ya, double darkFactor) {
			this.currentLevel = currentLevel;
			this.xa = xa;
			this.ya = ya;
			this.darkFactor = darkFactor;
		}

		@Override
		public void render(Graphics2D graphics) {
			double alpha = lightOverlay.getOverlayOpacity(currentLevel, darkFactor);
			BufferedImage overlay = lightOverlay.render(xa, ya);
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .02f)); // Lightening
			graphics.setColor(java.awt.Color.WHITE);
			graphics.fillRect(0, 0, w, h);
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha)); // Shaders
			graphics.drawImage(overlay, null, 0, 0);
		}
	}

	/**
	 * Clears all the colors on the screen
	 */
	public void clear(int color) {
		// Turns each pixel into a single color (clearing the screen!)
		if (color == 0) {
			queueClearRendering(new PlainClearRendering());
		} else {
			queueClearRendering(new SolidClearRendering(color));
		}
	}

	private void queueClearRendering(ClearRendering clearRendering) {
		lastClearRendering = clearRendering;
		queue(clearRendering);
	}

	public void flush() {
		Graphics2D g2d = image.createGraphics();
		Rendering rendering;
		do { // Skips until the latest clear rendering is obtained.
			rendering = renderings.poll(); // This can prevent redundant renderings operated.
			if (rendering == null) return;
		} while (rendering != lastClearRendering);
		do { // Renders all renderings until all are operated.
			rendering.render(g2d);
		} while ((rendering = renderings.poll()) != null);
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
		// xOffset and yOffset account for screen offset
		render(xp - xOffset, yp - yOffset, xt * 8, yt * 8, 8, 8, sheet, bits, whiteTint, fullbright, color);
	}

	public void render(int xp, int yp, int xt, int yt, int tw, int th, MinicraftImage sheet) {
		render(xp, yp, xt, yt ,tw, th, sheet, 0);
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
	// Any single pixel from the image can be rendered using this method.
	public void render(int xp, int yp, int xt, int yt, int tw, int th, MinicraftImage sheet, int mirrors, int whiteTint, boolean fullBright, int color) {
		if (sheet == null) return; // Verifying that sheet is not null.

		// Validation check
		if (xt + tw > sheet.width && yt + th > sheet.height) {
			render(xp, yp, 0, 0, mirrors, Renderer.spriteLinker.missingSheet(SpriteType.Item));
			return;
		}

		queue(new SpriteRendering(xp, yp, xt, yt, tw, th, mirrors, whiteTint, fullBright, color, sheet));
	}

	public void fillRect(int xp, int yp, int w, int h, int color) {
		queue(new FillRectRendering(xp, yp, w, h, color));
	}

	public void drawRect(int xp, int yp, int w, int h, int color) {
		queue(new DrawRectRendering(xp, yp, w, h, color));
	}

	/**
	 * Draw a straight line along an axis.
	 * @param axis The axis to draw along: {@code 0} for x-axis; {@code 1} for y-axis
	 * @param l The length of the line
	 */
	public void drawAxisLine(int xp, int yp, @MagicConstant(intValues = {0, 1}) int axis, int l, int color) {
		switch (axis) {
			case 0: queue(new DrawLineRendering(xp, yp, xp + l, yp, color)); break;
			case 1: queue(new DrawLineRendering(xp, yp, xp, yp + l, color)); break;
		}
	}

	public void drawLine(int x0, int y0, int x1, int y1, int color) {
		queue(new DrawLineRendering(x0, y0, x1, y1, color));
	}

	/** Placeholder line drawing method specialized for sign cursor drawing */
	public void drawLineSpecial(int x0, int y0, @MagicConstant(intValues = {0, 1}) int axis, int l) {
		queue(new DrawLineSpecialRendering(x0, y0, l, axis));
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

	/**
	 * Overlays the screen with pixels
	 */
	public void overlay(int currentLevel, int xa, int ya) {
		double darkFactor = 0;
		if (currentLevel >= 3 && currentLevel < 5) {
			int transTime = Updater.dayLength / 4;
			double relTime = (Updater.tickCount % transTime) * 1.0 / transTime;

			switch (Updater.getTime()) {
				case Morning:
					darkFactor = Updater.pastDay1 ? (1 - relTime) * MAXDARK : 0;
					break;
				case Day:
					darkFactor = 0;
					break;
				case Evening:
					darkFactor = relTime * MAXDARK;
					break;
				case Night:
					darkFactor = MAXDARK;
					break;
			}

			if (currentLevel > 3) darkFactor -= (darkFactor < 10 ? darkFactor : 10);
		} else if (currentLevel >= 5)
			darkFactor = MAXDARK;

		// The Integer array of pixels to overlay the screen with.
		queue(new OverlayRendering(currentLevel, xa, ya, darkFactor));
	}

	public void renderLight(int x, int y, int r) {
		// Applies offsets:
		lightOverlay.renderLight(x - xOffset, y - yOffset, r);
	}

	private static class LightOverlay {
		private static final int[] dither = new int[] {
			0, 8, 2, 10,
			12, 4, 14, 6,
			3, 11, 1, 9,
			15, 7, 13, 5
		};

		public final float[] graFractions;
		public final java.awt.Color[] graColors;
		public final BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		public final byte[] bufPixels;
		public final BufferedImage overlay = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		public final int[] olPixels;
		public final HashMap<@NotNull Point, @NotNull Integer> lights = new HashMap<>();

		public LightOverlay() {
			bufPixels = ((DataBufferByte) buffer.getRaster().getDataBuffer()).getData();
			olPixels = ((DataBufferInt) overlay.getRaster().getDataBuffer()).getData();
			ArrayList<Float> graFractions = new ArrayList<>();
			ArrayList<java.awt.Color> graColors = new ArrayList<>();
			BigDecimal oneFiftieth = BigDecimal.ONE.divide(BigDecimal.valueOf(50), MathContext.UNLIMITED);
			BigDecimal twoFiveFive = BigDecimal.valueOf(255);
			for (BigDecimal i = BigDecimal.ZERO; i.compareTo(BigDecimal.ONE) <= 0; i = i.add(oneFiftieth)) {
				graFractions.add(i.floatValue());
				graColors.add(new java.awt.Color(255, 255, 255, 255 - i.pow(4).multiply(twoFiveFive).intValue()));
			}
			this.graFractions = new float[graFractions.size()];
			for (int i = 0; i < graFractions.size(); ++i) this.graFractions[i] = graFractions.get(i);
			this.graColors = graColors.toArray(new java.awt.Color[0]);
		}

		/**
		 * Gets the overlay light darkness opacity instantly.
		 * @param currentLevel the current level index of the target
		 * @param darkFactor the tint factor to darken, from {@code 1} to {@code 128}
		 * @return opacity of darkness from {@code 0} to {@code 1}
		 */
		public double getOverlayOpacity(int currentLevel, double darkFactor) {
			// The above if statement is simply comparing the light level stored in oPixels with the minimum light level stored in dither. if it is determined that the oPixels[i] is less than the minimum requirements, the pixel is considered "dark", and the below is executed...
			if (currentLevel < 3) { // if in caves...
				// in the caves, not being lit means being pitch black.
				return 1;
			} else {
				// Outside the caves, not being lit simply means being darker.
				return darkFactor / 160; // darkens the color one shade.
			}
		}

		public void renderLight(int x, int y, int r) {
			lights.put(new Point(x, y), r);
		}

		public BufferedImage render(int xa, int ya) {
			Graphics2D g2d = buffer.createGraphics();
			g2d.setBackground(java.awt.Color.BLACK);
			g2d.clearRect(0, 0, w, h);
			for (Map.Entry<Point, Integer> e : lights.entrySet()) {
				int x = e.getKey().x, y = e.getKey().y, r = e.getValue();
				boolean[] surrounds = new boolean[8];
				for (int xx = -16; xx < 17; ++xx) {
					for (int yy = -16; yy < 17; ++yy) {
						if (xx != 0 || yy != 0) {
							Point pp = new Point(x + xx, y + yy);
							if (lights.containsKey(pp) && r <= lights.get(pp)) {
								double theta = Math.atan2(yy, xx);
								if (theta < 0) theta += 2 * Math.PI; // Ensures it is positive.
								surrounds[(int) (theta * 4 / Math.PI)] = true;
							}
						}
					}
				}

				// Reduce lighting circles on screen
				if (IntStream.range(0, surrounds.length).allMatch(i -> surrounds[i])) {
					g2d.setColor(java.awt.Color.WHITE);
					g2d.fillRect(x - 8, y - 8, 16, 16);
				} else {
					g2d.setPaint(new RadialGradientPaint(x, y, r, graFractions, graColors));
					g2d.fillOval(x - r, y - r, r * 2, r * 2);
				}
			}
			g2d.dispose();
			lights.clear();

			for (int x = 0; x < w; ++x) {
				for (int y = 0; y < h; ++y) {
					int i = x + y * w;
					// Grade of lightness
					int grade = bufPixels[i] & 0xFF;
					// (a + b) & 3 acts like (a + b) % 4
					if (grade / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) {
						olPixels[i] = (255 - grade) << 24;
					} else {
						olPixels[i] = 0;
					}
				}
			}
			return overlay;
		}
	}
}
