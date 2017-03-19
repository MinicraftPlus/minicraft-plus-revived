package com.mojang.ld22.gfx;

import java.awt.image.BufferedImage;

public class SpriteSheet {
	public int width, height; // width and height of the sprite sheet
	public int[] pixels; // integer array of the image's pixels

	public SpriteSheet(BufferedImage image) {
		//sets width and height to that of the image
		width = image.getWidth();
		height = image.getHeight();
		pixels = image.getRGB(0, 0, width, height, null, 0, width); //gets the color array of the image pixels
		for (int i = 0; i < pixels.length; i++) { // loops through all the pixels
			pixels[i] = (pixels[i] & 0xff) / 64; // divides the last 8 bits of the pixel by 64. Doesn't seem to do much at all.
		}
	}
}
