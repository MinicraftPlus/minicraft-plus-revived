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
			pixels[i] = (pixels[i] & 0xff) / 64; // This sets the pixel value of each color, which determines which of the 4 sprite colors the pixel is set to.
			// The calculation is based on the blue value of the pixel, the last byte in the ARGB int returned. Since they should be gray, r g and b should be the same anyway.
			// The r/g/b value should be one of: 0, 51, 173, 255; those are the shades of gray the spritesheet uses.
			// Dividing each of these by 64, and truncating to an integer, gives: 0, 1, 2, 3.
			// That number, 0-3, is the index of the sprite color to use.
			// This explains why the first number in a sprite color determines the color of the black pixels, the 2nd controls the dark gray, etc.
		}
	}
}
