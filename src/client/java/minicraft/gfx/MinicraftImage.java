package minicraft.gfx;

import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.util.Logging;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Although we have SpriteLinker, we still need SpriteSheet for buffering.
 * As BufferedImage is heavy. Our current rendering system still depends on this array.
 */
public class MinicraftImage {
	/** Each sprite tile size. */
	public static final int boxSize = 8;

	public final int width, height; // Width and height of the sprite sheet
	public final int[] pixels; // Integer array of the image's pixels

	/**
	 * Default with maximum size of image.
	 * @param image The image to be added.
	 */
	public MinicraftImage(BufferedImage image) {
		 this(image, image.getWidth(), image.getHeight());
	}
	/**
	 * Custom size.
	 * @param image The image to be added.
	 * @param width The width of the {@link MinicraftImage} to be applied to the {@link LinkedSprite}.
	 * @param height The height of the {@link MinicraftImage} to be applied to the {@link LinkedSprite}.
	 * @throws IndexOutOfBoundsException requested dimensions are out of source boundaries.
	*/
	public MinicraftImage(BufferedImage image, int width, int height) throws IndexOutOfBoundsException {
		if (width % 8 != 0)
			Logging.GFX.warn("Invalid width of SpriteSheet or SpriteSheet argument: width {} should be a multiple of 8.", width);
		if (height % 8 != 0)
			Logging.GFX.warn("Invalid height of SpriteSheet or SpriteSheet argument: height {} should be a multiple of 8.", height);

		// Sets width and height to that of the image
		this.width = width - width % 8;
		this.height = height - height % 8;

		// If size is bigger than image source, throws error.
		if (this.width > image.getWidth() || this.height > image.getHeight()) {
			throw new IndexOutOfBoundsException(String.format("Requested size %s*%s out of source size %s*%s",
				this.width, this.height, image.getWidth(), image.getHeight()));
		}

		pixels = image.getRGB(0, 0, width, height, null, 0, width); // Gets the color array of the image pixels

		// Applying the RGB array into Minicraft rendering system 25 bits RBG array.
		for (int i = 0; i < pixels.length; i++) { // Loops through all the pixels
			int red;
			int green;
			int blue;

			// This should be a number from 0-255 that is the red of the pixel
			red = (pixels[i] & 0xff0000);

			// Same, but green
			green = (pixels[i] & 0xff00);

			// Same, but blue
			blue = (pixels[i] & 0xff);

			// This stuff is to figure out if the pixel is transparent or not
			int transparent = 1;

			// A value of 0 means transparent, a value of 1 means opaque
			if (pixels[i] >> 24 == 0x00) {
				transparent = 0;
			}

			// Actually put the data in the array
			// Uses 25 bits to store everything (8 for red, 8 for green, 8 for blue, and 1 for alpha)
			pixels[i] = (transparent << 24) + red + green + blue;
		}
	}

	/** This class represents a 8*8 pixel block unit on the sprite sheet. */
	public class Px {
		final int x, y, mirror;

		public Px(int sheetX, int sheetY, int mirroring) {
			// pixelX and pixelY are the relative positions each pixel should have relative to the top-left-most pixel of the sprite.
			x = sheetX;
			y = sheetY;
			mirror = mirroring;
		}

		public MinicraftImage getSheet() {
			return MinicraftImage.this;
		}

		public String toString() {
			return "SpritePixel:x=" + x + ";y=" + y + ";mirror=" + mirror;
		}
	}
}
