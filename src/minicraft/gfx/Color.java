package minicraft.gfx;

public class Color {
	
	/* To explain this class, you have to know how Bit-Shifting works.
	 	David made a small post, so go here if you don't already know: http://minicraftforums.com/viewtopic.php?f=9&t=2256
		
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
	
	/**
		To provide a method to the madness, all methods ending in "Color" are copies of the methods of the same name without the "Color" part, execpt they convert the given value to 24bit Java RGB rather than the normal, minicraft 8-bit total RGB (that really only goes up to 216).
	*/
	
	/** This returns a integer with 4 rgb color values. */
	public static int get(int a, int b, int c, int d) {
		//converts each color to 8-bit, and shifts each one 8-bits to the left a certain number of times, to create 4 colors set one after the other.
		// if r,g,b were 8 bits each, these would have to be shifted 72, 48, 24. The number would be 2^98; far too big for an int. Heck, this number is barely small enough, being... 2^32. wait, that's bigger than 2^31-1!
		return (get(d) << 24) + (get(c) << 16) + (get(b) << 8) + (get(a));
	}
	public static int get(int a, int bcd) {
		return get(a, bcd, bcd, bcd); // just a shortcut.
	}
	//public static int get(int abcd) {return get(abcd, abcd, abcd, abcd);}
	
	/** converts a 0-5 scale rgb color to a 24-bit color, r,g,b are each 8 bits. Used 4 times in 4 param. get() method, for each color. */
	public static int get(int d) {
		if (d < 0) return 255; // if d is smaller than 0, then return 255.
		int r = d / 100 % 10; // the red value is the remainder of (d/100) / 10
		int g = d / 10 % 10; // the green value is the remainder of (d/10) / 10
		int b = d % 10; // the blue value is the remainder of d / 10.
		
		return r * 36 + g * 6 + b; // returns (red value * 36) + (green value * 6) + (blue value)
		
		// Why do we need all this math to get the colors? I don't even know. -David
		// This is just converting minicraft colors, which can only go 0 to 5, to regular, 8-bit rgb colors, which go from 0 to 255. Though, these add up to 216, max... -Chris J
	}
	
	/*//similar to get(), it looks like, but just one value..?
	public static int pixel(int a) {
		return (get(a) << 24) + (get(a) << 16) + (get(a) << 8) + get(a);
	}*/
	
	// this makes an int that you would pass to the get(a,b,c,d), or get(d), method, from three seperate 8-bit r,g,b values.
	public static int rgb(int red, int green, int blue) {
		if (red > 255) red = 255;
		if (green > 255) green = 255;
		if (blue > 255) blue = 255;
		
		if (red < 50 && red != 0) red = 50;
		if (green < 50 && green != 0) green = 50;
		if (blue < 50 && blue != 0) blue = 50;
		
		int rgb = red / 50 * 100 + green / 50 * 10 + blue / 50;
		return rgb;
	}
	
	/** This method darkens or lightens a color (0 to 216 value) by the specified amount. */
	public static int tint(int color, int amount, boolean isSpriteCol) {
		if(isSpriteCol) {
			int[] colors = seperateEncodedSprite(color); // this just seperates the four 8-bit sprite colors; they are still in base-6 added form.
			for(int i = 0; i < colors.length; i++) {
				colors[i] = tint(colors[i], amount);
			}
			return (colors[0] << 24) + (colors[1] << 16) + (colors[2] << 8) + colors[3];
		} else {
			return tint(color, amount);
		}
	}
	private static int tint(int rgb, int amount) {
		if(rgb == -1) return -1;
		
		int r = (rgb / 36) % 6;
		int g = (rgb / 6) % 6;
		int b = rgb % 6;
		r+=amount; g+=amount; b+=amount;
		
		if(r<0) r = 0; if(g<0) g = 0; if(b<0) b = 0;
		if(r>255) r = 255; if(g>255) g = 255; if(b>255) b = 255;
		
		return r * 36 + g * 6 + b;
	}
	
	/** seperates a 4-part sprite color into it's original 4 component colors (which each have an rgb value) */
	/// reverse of Color.get(a, b, c, d).
	private static int[] seperateEncodedSprite(int col) {
		int ap = (col >> 24) << 24;
		int bp = ((col - ap) >> 16) << 16;
		int cp = ((col - ap - bp) >> 8) << 8;
		int d = col-ap-bp-cp;
		int a = ap >> 24, b = bp >> 16, c = cp >> 8;
		
		return new int[] {a, b, c, d};
		/// the colors have been seperated. Now they must be converted from 216 scale to 0-5 scale.
		/*for(int i = 0; i < cols.length; i++) {
			cols[i] = unGetRGB(cols[i]);
		}
		return cols;*/
	}
	/** This turns a 216 scale rgb int into a 0-5 scale "concatenated" rgb int. */
	public static int[] decodeRGB(int rgb) {
		int r = (rgb / 36) % 6;
		int g = (rgb / 6) % 6;
		int b = rgb % 6;
		return new int[] {r, g, b};
	}
	
	/// this turns a 0-216 combined minicraft color into a 24-bit r,g,b color.
	protected static int upgrade(int col) {
		int[] rgbs = decodeRGB(col);
		int newcol = rgbs[0]*100 + rgbs[1]*10 + rgbs[2];
		return getColor(newcol);
	}
	
	/// this takes a 24 bit color, and turns it into an 8-bit color minicraft sprites use.
	protected static int downgrade(int col) {
		int[] comps = decodeRGBColor(col);
		return rgb(comps[0], comps[1], comps[2]);
	}
	
	/// this is like get(d), above, but it returns a 24 bit color, with r,g,b each taking 8 bits, rather than them all taking 8 bits together.
	protected static int getColor(int d) {
		int r = d / 100 % 10; // the red value is the remainder of (d/100) / 10
		int g = d / 10 % 10; // the green value is the remainder of (d/10) / 10
		int b = d % 10; // the blue value is the remainder of d / 10.
		
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
	
	protected static int tintColor(int rgb, int amount) {
		if(rgb == -1) return -1;
		
		int[] comps = decodeRGBColor(rgb);
		int r = comps[0], g = comps[1], b = comps[2];
		
		r+=amount; g+=amount; b+=amount;
		
		if(r<0) r = 0; if(g<0) g = 0; if(b<0) b = 0;
		if(r>255) r = 255; if(g>255) g = 255; if(b>255) b = 255;
		
		return r << 16 | g << 8 | b;
	}
	
	protected static int[] decodeRGBColor(int col) {
		int r = col >> 16;
		int rp = r << 16;
		int g = (col - rp) >> 8;
		int b = col-rp-(g<<8);
		
		return new int[] {r, g, b};
	}
	
	/// this is for color testing.
	public static void main(String[] args) {
		int r, g, b, d;
		//if(args.length >= 1) {
		for(String color: args) {
			d = Integer.parseInt(color);
			int a = get(d);
			r = a / 36 % 6;
			g = a / 6 % 6;
			b = a % 6;
			
			System.out.println(color+" -> " + r + g + b); // they should be the same number on both sides; this is to find the overflow color, say if you use 159 or something.
			
			//System.out.println("laid out: r=" + r + ", g=" + g + ", b=" + b);
			//System.out.println("pixel " + d + ": " + pixel(d));
		}
		/*if(args.length == 3) {
			r = Integer.parseInt(args[0]);
			g = Integer.parseInt(args[1]);
			b = Integer.parseInt(args[2]);
			d = r * 36 + g * 6 + b;
			System.out.println("added: " + d);
			System.out.println("resultant: " + get(d));
			//System.out.println("pixel: " + d + ": " + pixel(d));
			//System.out.println();
		}*/
		
	}
	
	protected static String toStringSingle(int col) {
		return java.util.Arrays.toString(decodeRGB(col));
	}
	
	/// for sprite colors
	public static String toString(int col) {
		int[] cols = seperateEncodedSprite(col);
		int[][] rgbs = new int[cols.length][];
		for(int i = 0; i < cols.length; i++)
			rgbs[i] = decodeRGB(cols[i]);
		
		String[] colstrs = new String[cols.length];
		for(int i = 0; i < colstrs.length; i++) {
			String rgb = "";
			for(int j = 0; j < rgbs[i].length; j++)
				rgb += rgbs[i][j];
			colstrs[i] = ""+rgb;
		}
		
		return java.util.Arrays.toString(colstrs);
	}
}
