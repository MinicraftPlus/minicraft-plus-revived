package minicraft.gfx;

import java.util.Arrays;

public class Color {
	
	/* To explain this class, you have to know how Bit-Shifting works.
	 	David made a small post, so go here if you don't already know: http://minicraftforums.com/viewtopic.php?f=9&t=2256
		
		Note: this class still confuses me a bit, lol. -David
		
		On bit shifting: the new bits will always be zero, UNLESS it is a negative number aka the left-most bit is 1. Then, shifting right will fill with 1's, it seems.
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
	
	/*
		To provide a method to the madness, all methods ending in "Color" are copies of the methods of the same name without the "Color" part, except they convert the given value to 24bit Java RGB rather than the normal, minicraft 8-bit total RGB (that really only goes up to 216).
		Methods containing the word "get" deal with converting the 0-5 rgb to something else, and unGet for the other way.
		
		Another point:
		
		rgbByte = int using 8 bits to store the r, g, and b.
		rgbInt = int using 24 bits to store r, g, and b, with 8 bits each; this is the classic 0-255 scale for each color component, compacted into one integer variable.
		rgb4Sprite = int using the 4 bytes in an int to store each of the 4 rgbBytes used for coloring a sprite.
		rgbReadable = int representing rgb in a readable decimal format, by having the values 0-5 in the 100s, 10s, and 1s place for r, g, and b respectively.
		
		So, the methods ending in "Color" deal with rgbInts, while their counterparts deal with rgbBytes.
	*/
	
	public static final int WHITE = Color.get(-1, 555);
	public static final int GRAY = Color.get(-1, 333);
	public static final int DARK_GRAY = Color.get(-1, 222);
	public static final int BLACK = Color.get(-1, 0);
	public static final int RED = Color.get(-1, 500);
	public static final int GREEN = Color.get(-1, 50);
	public static final int BLUE = Color.get(-1, 5);
	public static final int YELLOW = Color.get(-1, 550);
	public static final int MAGENTA = Color.get(-1, 505);
	public static final int CYAN = Color.get(-1, 55);
	
	/** This returns a integer with 4 rgb color values. */
	public static int get(int a, int b, int c, int d) {
		//converts each color to 8-bit, and shifts each one 8-bits to the left a certain number of times, to create 4 colors set one after the other.
		return (get(a) << 24) + (get(b) << 16) + (get(c) << 8) + (get(d));
	}
	public static int get(int a, int bcd) {
		return get(a, bcd, bcd, bcd); // just a shortcut.
	}
	
	/** converts a 0-5 scale rgb color to an 8-bit rgb color, using base 6 to encode it as a value from 0 to 255. (but instead of representing r, g, or b only, it holds all three in the one number.) */
	public static int get(int rgbReadable) {
		if (rgbReadable < 0) return 255; // if d is smaller than 0, then return 255. This is actually 255 for a reason: -1 is represented as 32 1's, all the bits on. 255 is 8 bits all on. So, to put it in another int, and not mess everything up, you have to use 255. 
		int r = rgbReadable / 100 % 10; // the red value is the remainder of (d/100) / 10
		int g = rgbReadable / 10 % 10; // the green value is the remainder of (d/10) / 10
		int b = rgbReadable % 10; // the blue value is the remainder of d / 10.
		
		return r * 36 + g * 6 + b; // returns (red value * 36) + (green value * 6) + (blue value)
	}
	
	private static int limit(int num, int min, int max) {
		if(num < min) num = min;
		if(num > max) num = max;
		return num;
	}
	
	// this makes an int that you would pass to the get(a,b,c,d), or get(d), method, from three separate 8-bit r,g,b values.
	public static int rgb(int red, int green, int blue) { // rgbInt array -> rgbReadable
		red = limit(red, 0, 250);
		green = limit(green, 0, 250);
		blue = limit(blue, 0, 250);
		
		return red / 50 * 100 + green / 50 * 10 + blue / 50; // this is: rgbReadable
	}
	
	/** This method darkens or lightens a color (0 to 216 value) by the specified amount. */
	public static int tint(int color, int amount, boolean isSpriteCol) {
		if(isSpriteCol) {
			int[] rgbBytes = separateEncodedSprite(color); // this just separates the four 8-bit sprite colors; they are still in base-6 added form.
			System.out.println("the 4 colors for the sprite, to be tinted: " + Arrays.toString(rgbBytes));
			System.out.println("the 4 colors for the sprite to be tinted, as inputted: " + Arrays.toString(separateEncodedSprite(color, true)));
			for(int i = 0; i < rgbBytes.length; i++) {
				rgbBytes[i] = tint(rgbBytes[i], amount);
			}
			return rgbBytes[0] << 24 | rgbBytes[1] << 16 | rgbBytes[2] << 8 | rgbBytes[3]; // this is: rgb4Sprite
		} else {
			return tint(color, amount); // this is: rgbByte
		}
	}
	private static int tint(int rgbByte, int amount) {
		if(rgbByte == 255) return 255; // see description of bit shifting above; it will hold the 255 value, not -1  
		
		int[] rgb = decodeRGB(rgbByte); // this returns the rgb values as 0-5 numbers.
		for(int i = 0; i < rgb.length; i++)
			rgb[i] = limit(rgb[i]+amount, 0, 5);
		
		return rgb[0] * 36 + rgb[1] * 6 + rgb[2]; // this is: rgbByte
	}
	
	/** seperates a 4-part sprite color (rgb4Sprite) into it's original 4 component colors (which are each rgbBytes) */
	/// reverse of Color.get(a, b, c, d).
	public static int[] separateEncodedSprite(int rgb4Sprite) { return separateEncodedSprite(rgb4Sprite, false); }
	public static int[] separateEncodedSprite(int rgb4Sprite, boolean convertToReadable) {
		// the numbers are stored, most to least shifted, as d, c, b, a.
		int a = (rgb4Sprite >> 24) & 0xFF; // See note at top; this is to remove left-hand 1's.
		int b = (rgb4Sprite & 0x00_FF_00_00) >> 16;
		int c = (rgb4Sprite & 0x00_00_FF_00) >> 8;
		int d = (rgb4Sprite & 0x00_00_00_FF);
		
		if(convertToReadable) {
			// they become rgbReadable
			a = unGet(a);
			b = unGet(b);
			c = unGet(c);
			d = unGet(d);
		} // else, they are rgbByte
		
		return new int[] {a, b, c, d};
	}
	
	/** This turns a 216 scale rgb int into a 0-5 scale "concatenated" rgb int. (aka rgbByte -> r/g/b Readables) */
	public static int[] decodeRGB(int rgbByte) {
		int r = (rgbByte / 36) % 6;
		int g = (rgbByte / 6) % 6;
		int b = rgbByte % 6;
		return new int[] {r, g, b};
	}
	
	public static int unGet(int rgbByte) { // rgbByte -> rgbReadable
		int[] cols = decodeRGB(rgbByte);
		return cols[0]*100 + cols[1]*10 + cols[2];
	}
	
	public static int mixRGB(int rgbByte1, int rgbByte2) { // rgbByte,rgbByte -> rgbReadable
		if(rgbByte1 == 255 || rgbByte2 == 255) return -1;
		int newcol = (rgbByte1 + rgbByte2) / 2;
		return unGet(newcol);
		//int[] col1 = decodeRGB(get(rgb1));
		//int[] col2 = decodeRGB(get(rgb2));
		//return ((col1[0] + col2[0])/2) * 100 + ((col1[1] + col2[1])/2) * 10 + ((col1[2] + col2[2])/2);
	}
	
	/// this turns a 0-216 combined minicraft color into a 24-bit r,g,b color. AKA: rgbByte -> rgbInt
	protected static int upgrade(int rgbByte) {
		if(rgbByte == 255) return 0xFF_FF_FF_FF;
		
		int r = ((rgbByte / 36) % 6) * 51;
		int g = ((rgbByte / 6) % 6) * 51;
		int b = (rgbByte % 6) * 51;
		
		int mid = (r * 30 + g * 59 + b * 11) / 100;
		
		int r1 = ((r + mid) / 2) * 230 / 255 + 10;
		int g1 = ((g + mid) / 2) * 230 / 255 + 10;
		int b1 = ((b + mid) / 2) * 230 / 255 + 10;
		
		return r1 << 16 | g1 << 8 | b1;
	}
	
	/// this takes a 24 bit color, and turns it into an 8-bit color minicraft sprites use. AKA: rgbInt -> rgbByte
	protected static int downgrade(int rgbInt) { // 
		int[] comps = decodeRGBColor(rgbInt);
		return get(rgb(comps[0], comps[1], comps[2])); // this is: rgbByte
	}
	
	/// this is like get(d), above, but it returns a 24 bit color, with r,g,b each taking 8 bits, rather than them all taking 8 bits together.
	protected static int getColor(int rgbReadable) { // rgbReadable -> rgbInt
		if(rgbReadable < 0) return 0x01_FF_FF_FF;
		
		int r = rgbReadable / 100 % 10; // the red value is the remainder of (d/100) / 10
		int g = rgbReadable / 10 % 10; // the green value is the remainder of (d/10) / 10
		int b = rgbReadable % 10; // the blue value is the remainder of d / 10.
		
		/// this is taken from Game.java when it's making the colors. I think the end result is a 0-5 r, g, or b turning into a 0-255 r, g, or b.
		int rr = (r * 255 / 5);
		int gg = (g * 255 / 5);
		int bb = (b * 255 / 5);
		int mid = (rr * 30 + gg * 59 + bb * 11) / 100;
		
		int r1 = ((rr + mid * 1) / 2) * 230 / 255 + 10;
		int g1 = ((gg + mid * 1) / 2) * 230 / 255 + 10;
		int b1 = ((bb + mid * 1) / 2) * 230 / 255 + 10;
		
		return r1 << 16 | g1 << 8 | b1; // wow, this would work... it's like:
		/*
			pattern 1: 00101010 00000000 00000000
			pattern 2: 00000000 01001110 00000000
			pattern 3: 00000000 00000000 11011101
			OR'ing those three bit patterns just kind of joins them.
		*/
	}
	
	protected static int tintColor(int rgbInt, int amount) {
		if(rgbInt < 0) return rgbInt; // this is "transparent".
		
		int[] comps = decodeRGBColor(rgbInt);
		
		for(int i = 0; i < comps.length; i++)
			comps[i] = limit(comps[i]+amount, 0, 255);
		
		return comps[0] << 16 | comps[1] << 8 | comps[2];
	}
	
	protected static int[] decodeRGBColor(int rgbInt) {
		int r = (rgbInt & 0xFF_00_00) >> 16;
		int g = (rgbInt & 0x00_FF_00) >> 8;
		int b = (rgbInt & 0x00_00_FF);
		
		return new int[] {r, g, b};
	}
	
	/// this is for color testing.
	public static void main(String[] args) {
		int r, g, b;
		
		r = new Integer(args[0]);
		g = new Integer(args[1]);
		b = new Integer(args[2]);
		
		System.out.println(rgb(r, g, b));
	}
	
	protected static String toStringSingle(int col) { return java.util.Arrays.toString(decodeRGB(col)); }
	
	/// for sprite colors
	public static String toString(int col) {
		return java.util.Arrays.toString(Color.separateEncodedSprite(col, true));
	}
}
