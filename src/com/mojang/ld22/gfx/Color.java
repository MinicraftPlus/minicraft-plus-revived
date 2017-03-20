package com.mojang.ld22.gfx;

public class Color {
	
	/* To explain this class, you have to know how Bit-Shifting works.
	 	David(Shaylor) made a small post, so go here if you don't already know: http://minicraftforums.com/viewtopic.php?f=9&t=2256
		
		Note: this class still confuses me a bit, lol. -David
	*/
	
	/*... he later explains:
		The basic idea is that every sprite in the game has 4 different colors,
			represented as shades of gray in the sprite-sheet.
		
		The three decimal digits in each integer are the RGB values (they range from 0 to 5).
			The 100's represent red, the 10's represent green, and the 1's represent blue.
		
		-1 is a special color; it is completely transparent.
		
		On the Spritesheet...
			The darkest shade (black) represents the first color,
			the dark gray is the second color,
			the light gray is the third color,
			and white is the fourth color.
	*/
	
	/** This returns a integer with 4 rgb color values. */
	public static int get(int a, int b, int c, int d) {
		//"get()"s each value, and shifts each one's bit to the left the specified number of times
		return (get(d) << 24) + (get(c) << 16) + (get(b) << 8) + (get(a));
	}
	
	//similar to get(), it looks like, but just one value..?
	public static int pixel(int a) {
		return (get(a) << 24) + (get(a) << 16) + (get(a) << 8) + get(a);
	}
	
	//looks like an error correction system.
	public static int rgb(int red, int green, int blue) {
		boolean rgb = false; //not even used...
		if (red > 255) {
			red = 255;
		}

		if (green > 255) {
			green = 255;
		}

		if (blue > 255) {
			blue = 255;
		}

		if (red < 50 && red != 0) {
			red = 50;
		}

		if (green < 50 && green != 0) {
			green = 50;
		}

		if (blue < 50 && blue != 0) {
			blue = 50;
		}

		int rgb1 = red / 50 * 100 + green / 50 * 10 + blue / 50;
		return rgb1;
	}
	
	/** gets the color to use based off an integer */
	public static int get(int d) {
		if (d < 0) return 255; // if d is smaller than 0, then return 255.
		int r = d / 100 % 10; // the red value is the remainder of (d/100) / 10
		int g = d / 10 % 10; // the green value is the remainder of (d/10) / 10
		int b = d % 10; // the blue value is the remainder of d / 10.
		return r * 36 + g * 6 + b; // returns (red value * 36) + (green value * 6) + (blue value)
		
		// Why do we need all this math to get the colors? I don't even know. -David
	}
}
