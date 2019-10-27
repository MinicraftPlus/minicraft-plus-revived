package minicraft.gfx;

import java.awt.image.BufferedImage;

public class SpriteSheet {
	
	public static final int boxWidth = 8;
	
	public int width, height; // width and height of the sprite sheet
	public int[] pixels; // integer array of the image's pixels
	
	public SpriteSheet(BufferedImage image) {
		//sets width and height to that of the image
		width = image.getWidth();
		height = image.getHeight();
		pixels = image.getRGB(0, 0, width, height, null, 0, width); //gets the color array of the image pixels

		for (int i = 0; i < pixels.length; i++) { // loops through all the pixels
			int red;
			int green;
			int blue;

			// this should be a number from 0-255 that is the red of the pixel
			red = (pixels[i] & 0xff0000);

			// same, but green
			green = (pixels[i] & 0xff00);

			// same, but blue
			blue = (pixels[i] & 0xff);

			// this stuff is to figure out if the pixel is transparent or not
			int transparent = 1;

			// a value of 0 means transparent, a value of 1 means opaque
			if (pixels[i] >> 24 == 0x00) {
				transparent = 0;
			}

			// actually put the data in the array
			// uses 25 bits to store everything (8 for red, 8 for green, 8 for blue, and 1 for alpha)
			pixels[i] = (transparent << 24) + red + green + blue;

		}
	}
}
