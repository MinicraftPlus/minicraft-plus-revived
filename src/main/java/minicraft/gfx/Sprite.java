package minicraft.gfx;

import minicraft.gfx.SpriteLinker.SpriteSheet;

/** This class represents a group of pixels on their sprite sheet(s). */
public class Sprite {
	/**
		This class needs to store a list of similar segments that make up a sprite, just once for everything. There's usually four groups, but the components are:
			-spritesheet location (x, y)
			-mirror type

		That's it!
		The screen's render method only draws one 8x8 pixel of the spritesheet at a time, so the "sprite size" will be determined by how many repetitions of the above group there are.
	*/

	protected Px[][] spritePixels;
	public int color = -1;
	// spritePixels is arranged so that the pixels are in their correct positions relative to the top left of the full sprite. This means that their render positions are built-in to the array.

	public Sprite(Px[][] pixels) {
		spritePixels = pixels;
	}

	public String toString() {
		StringBuilder out = new StringBuilder(getClass().getName().replace("minicraft.gfx.", "") + "; pixels:");
		for (Px[] row: spritePixels)
			for (Px pixel: row)
				out.append("\n").append(pixel.toString());
		out.append("\n");

		return out.toString();
	}

	/** This class represents a pixel on the sprite sheet. */
	public static class Px {
		protected int x, y, mirror;
		protected SpriteSheet sheet;

		public Px(int sheetX, int sheetY, int mirroring, SpriteSheet sheet) {
			// pixelX and pixelY are the relative positions each pixel should have relative to the top-left-most pixel of the sprite.
			x = sheetX;
			y = sheetY;
			mirror = mirroring;
			this.sheet = sheet;
		}

		public String toString() {
			return "SpritePixel:x=" + x + ";y=" + y + ";mirror=" + mirror;
		}
	}
}
