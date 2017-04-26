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
	
	/** This returns a integer with 4 rgb color values. */
	public static int get(int a, int b, int c, int d) {
		//"get()"s each value, and shifts each one's bit to the left the specified number of times
		return (get(d) << 24) + (get(c) << 16) + (get(b) << 8) + (get(a));
	}
	public static int get(int a, int bcd) {
		return get(a, bcd, bcd, bcd); // just a shortcut.
	}
	
	/*//similar to get(), it looks like, but just one value..?
	public static int pixel(int a) {
		return (get(a) << 24) + (get(a) << 16) + (get(a) << 8) + get(a);
	}*/
	
	// this makes an int that you would pass to the get method, from seperate rgb values.
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
	
	/** gets the color to use based off an integer; used 4 times in 4 param. method, for each color. */
	public static int get(int d) {
		if (d < 0) return 255; // if d is smaller than 0, then return 255.
		int r = d / 100 % 10; // the red value is the remainder of (d/100) / 10
		int g = d / 10 % 10; // the green value is the remainder of (d/10) / 10
		int b = d % 10; // the blue value is the remainder of d / 10.
		return r * 36 + g * 6 + b; // returns (red value * 36) + (green value * 6) + (blue value)
		
		// Why do we need all this math to get the colors? I don't even know. -David
		// Well, looks like this method, at least, changes the number from multiples of ten to multiples of 6; maybe base 6? -Chris J
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
}
