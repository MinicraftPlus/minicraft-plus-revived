package minicraft.gfx;

public class Sprite {
	/**
		This class needs to store a list of similar segments that make up a sprite, plus the color, just once for everything. There's usually four groups, but the components are:
			-spritesheet location (x, y)
			-mirror type
		
		That's it!
		The screen's render method only draws one 8x8 pixel of the spritesheet at a time, so the "sprite size" will be determined by how many repetitions of the above group there are.
	*/
	
	public Px[][] spritePixels;
	public int color;
	/// spritePixels is arranged so that the pixels are in their correct positions relative to the top left of the full sprite. This means that their render positions are built-in to the array.
	
	public Sprite(int sx, int sy, int sw, int sh) {
		this(sx, sy, sw, sh, 0, false, false);}
	public Sprite(int sx, int sy, int sw, int sh, int color) {
		this(sx, sy, sw, sh, color, false, false);}
	public Sprite(int sx, int sy, int sw, int sh, boolean mirrorHori, boolean mirrorVert) {
		this(sx, sy, sw, sh, 0, mirrorHori, mirrorVert);}
	public Sprite(int sx, int sy, int sw, int sh, int color, boolean mirrorHori, boolean mirrorVert) {
		spritePixels = new Px[sh][sw];
		this.color = color;
		int mirrorBits = (mirrorHori ? 1 : 0) + (mirrorVert ? 2 : 0);
		for(int r = 0; r < sh; r++)
			for(int c = 0; c < sw; c++)
				spritePixels[r][c] = new Px(sx+c, sy+r, mirrorBits);
	}
	
	public Sprite(Px[][] pixels) { this(pixels, 0); }
	public Sprite(Px[][] pixels, int color) {
		spritePixels = pixels;
		this.color = color;
	}
	
	public void render(Screen screen, int lvlx, int lvly) { render(screen, color, lvlx, lvly); }
	public void render(Screen screen, int color, int lvlx, int lvly) {
		/// here, x and y are entity coordinates, I think.
		
		for(int row = 0; row < spritePixels.length; row++) { // loop down through each row
			renderRow(row, screen, color, lvlx, lvly + row*8);
		}
	}
	
	public void renderRow(int r, Screen screen, int x, int y) { renderRow(r, screen, color, x, y); }
	public void renderRow(int r, Screen screen, int color, int x, int y) {
		Px[] row = spritePixels[r];
		for(int c = 0; c < row.length; c++) { // loop across through each column
			screen.render(x + c*8, y, row[c].sheetPos, color, row[c].mirror); // render the sprite pixel.
		}
	}
	
	public String toString() {
		String out = getClass().getName().replace("minicraft.gfx.", "")+"; pixels:";
		for(Px[] row: spritePixels)
			for(Px pixel: row)
				out += "\n"+pixel.toString();
		out += "\n";
		
		return out;
	}
	
	public class Px {
		protected int sheetPos, mirror;
		
		public Px(int sheetX, int sheetY, int mirroring) {
			//pixelX and pixelY are the relative positions each pixel should have relative to the top-left-most pixel of the sprite.
			sheetPos = sheetX + 32*sheetY;
			mirror = mirroring;
		}
		
		public String toString() {
			return "SpritePixel:x="+(sheetPos%32)+";y="+(sheetPos/32)+";mirror="+mirror;
		}
	}
}
