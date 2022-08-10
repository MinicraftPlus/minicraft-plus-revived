package minicraft.gfx;

import java.awt.image.BufferedImage;

public class SpriteSheet {

	public static final int boxWidth = 8;

	public int width, height; // Width and height of the sprite sheet
	public int[] pixels; // Integer array of the image's pixels

	/** Default with maximum size of image. */
	public SpriteSheet(BufferedImage image) { this(image, image.getWidth(), image.getHeight()); }
	public SpriteSheet(BufferedImage image, int width, int height) {
		this.width = width;
		this.height = height;
		// Sets width and height to that of the image
		pixels = image.getRGB(0, 0, width, height, null, 0, width); // Gets the color array of the image pixels

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
