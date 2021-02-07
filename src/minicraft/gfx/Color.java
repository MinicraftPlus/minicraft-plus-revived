package minicraft.gfx;

public class Color {
	
	/* To explain this class, you have to know how Bit-Shifting works.
	 	David made a small post, so go here if you don't already know: http://minicraftforums.com/viewtopic.php?f=9&t=2256
		
		Note: this class still confuses me a bit, lol. -David
		
		On bit shifting: the new bits will always be zero, UNLESS it is a negative number aka the left-most bit is 1. Then, shifting right will fill with 1's, it seems.
	*/
	
	/*
		To provide a method to the madness, all methods ending in "Color" are copies of the methods of the same name without the "Color" part, except they convert the given value to 24bit Java RGB rather than the normal, minicraft 13-bit total RGB.
		Methods containing the word "get" deal with converting the minicraft rgb to something else, and unGet for the other way.
		
		Another point:
		
		rgbByte = int using 8 bits to store the r, g, and b.
		rgbInt = int using 24 bits to store r, g, and b, with 8 bits each; this is the classic 0-255 scale for each color component, compacted into one integer variable.
		rgb4Sprite = int using the 4 bytes in an int to store each of the 4 rgbBytes used for coloring a sprite.
		rgbReadable = int representing rgb in a readable decimal format, by having the values 0-5 in the 100s, 10s, and 1s place for r, g, and b respectively.
		rgbMinicraft = int using 12 bits for r, g, and b, 4 bits each, and one for transparency

		So, the methods ending in "Color" deal with rgbInts, while their counterparts deal with rgbBytes.
	*/
	
	public static final int TRANSPARENT = Color.get(0, 0);
	public static final int WHITE = Color.get(1, 255);
	public static final int GRAY = Color.get(1, 153);
	public static final int DARK_GRAY = Color.get(1, 51);
	public static final int BLACK = Color.get(1, 0);
	public static final int RED = Color.get(1, 255, 0, 0);
	public static final int GREEN = Color.get(1, 0, 255, 0);
	public static final int BLUE = Color.get(1, 0, 0, 255);
	public static final int YELLOW = Color.get(1, 255, 255, 0);
	public static final int MAGENTA = Color.get(1, 255, 0, 255);
	public static final int CYAN = Color.get(1, 0, 255, 255);
	
	/** This returns a minicraftrgb.
	 * a should be between 0-1, r,g,and b should be 0-255 */
	public static int get(int a, int r, int g, int b) {
		return (a << 24) + (r << 16) + (g << 8) + (b);
	}
	public static int get(int a, int copy) {
		return get(a, copy, copy, copy);
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
	
	/** This method darkens or lightens a color by the specified amount. */
	public static int tint(int color, int amount, boolean isSpriteCol) {
		if(isSpriteCol) {
			int[] rgbBytes = separateEncodedSprite(color); // this just separates the four 8-bit sprite colors; they are still in base-6 added form.
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
	
	/// this turns a 25-bit minicraft color into a 24-bit rgb color.
	protected static int upgrade(int rgbMinicraft) {

		return rgbMinicraft & 0xFF_FF_FF;
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

	/// for sprite colors
	public static String toString(int col) {
		return java.util.Arrays.toString(Color.separateEncodedSprite(col, true));
	}
}
