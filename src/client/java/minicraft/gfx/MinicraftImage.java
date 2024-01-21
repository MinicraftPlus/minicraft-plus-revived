package minicraft.gfx;

import minicraft.core.CrashHandler;
import minicraft.gfx.SpriteLinker.LinkedSprite;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Although we have SpriteLinker, we still need SpriteSheet for buffering.
 * As BufferedImage is heavy. Our current rendering system still depends on this array.
 */
public class MinicraftImage {
	/**
	 * Each sprite tile size.
	 */
	public static final int boxWidth = 8;

	public final int width, height; // Width and height of the sprite sheet
	public final int[] pixels; // Integer array of the image's pixels

	/**
	 * Default with maximum size of image.
	 *
	 * @param image The image to be added.
	 * @throws IOException if I/O exception occurs.
	 */
	public MinicraftImage(BufferedImage image) throws IOException {
		this(image, image.getWidth(), image.getHeight());
	}

	/**
	 * Custom size.
	 *
	 * @param image  The image to be added.
	 * @param width  The width of the {@link MinicraftImage} to be applied to the {@link LinkedSprite}.
	 * @param height The height of the {@link MinicraftImage} to be applied to the {@link LinkedSprite}.
	 * @throws IOException
	 */
	public MinicraftImage(BufferedImage image, int width, int height) throws IOException {
		if (width % 8 != 0)
			CrashHandler.errorHandle(new IllegalArgumentException("Invalid width of SpriteSheet."), new CrashHandler.ErrorInfo(
				"Invalid SpriteSheet argument.", CrashHandler.ErrorInfo.ErrorType.HANDLED,
				String.format("Invalid width: {}, SpriteSheet width should be a multiple of 8.")
			));
		if (height % 8 != 0)
			CrashHandler.errorHandle(new IllegalArgumentException("Invalid height of SpriteSheet."), new CrashHandler.ErrorInfo(
				"Invalid SpriteSheet argument.", CrashHandler.ErrorInfo.ErrorType.HANDLED,
				String.format("Invalid height: {}, SpriteSheet height should be a multiple of 8.")
			));

		// Sets width and height to that of the image
		this.width = width - width % 8;
		this.height = height - height % 8;

		// If size is bigger than image source, throw error.
		if (this.width > image.getWidth() || this.height > image.getHeight()) {
			throw new IOException(new IndexOutOfBoundsException(String.format("Requested size %s*%s out of source size %s*%s",
				this.width, this.height, image.getWidth(), image.getHeight())));
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
}
