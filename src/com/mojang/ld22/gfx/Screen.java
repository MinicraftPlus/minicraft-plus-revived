package com.mojang.ld22.gfx;

public class Screen {
	/*
	 * public static final int MAP_WIDTH = 64; // Must be 2^x public static final int MAP_WIDTH_MASK = MAP_WIDTH - 1;
	 * 
	 * public int[] tiles = new int[MAP_WIDTH * MAP_WIDTH]; public int[] colors = new int[MAP_WIDTH * MAP_WIDTH]; public int[] databits = new int[MAP_WIDTH * MAP_WIDTH];
	 */
	public int xOffset;
	public int yOffset;

	public static final int BIT_MIRROR_X = 0x01;
	public static final int BIT_MIRROR_Y = 0x02;

	public final int w, h;
	public int[] pixels;

	private SpriteSheet sheet;

	public Screen(int w, int h, SpriteSheet sheet) {
		this.sheet = sheet;
		this.w = w;
		this.h = h;

		pixels = new int[w * h];

		// Random random = new Random();

		/*
		 * for (int i = 0; i < MAP_WIDTH * MAP_WIDTH; i++) { colors[i] = Color.get(00, 40, 50, 40); tiles[i] = 0;
		 * 
		 * if (random.nextInt(40) == 0) { tiles[i] = 32; colors[i] = Color.get(111, 40, 222, 333); databits[i] = random.nextInt(2); } else if (random.nextInt(40) == 0) { tiles[i] = 33; colors[i] = Color.get(20, 40, 30, 550); } else { tiles[i] = random.nextInt(4); databits[i] = random.nextInt(4);
		 * 
		 * } }
		 * 
		 * Font.setMap("Testing the 0341879123", this, 0, 0, Color.get(0, 555, 555, 555));
		 */
	}

	public void clear(int color) {
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = color;
	}

	/*
	 * public void renderBackground() { for (int yt = yScroll >> 3; yt <= (yScroll + h) >> 3; yt++) { int yp = yt * 8 - yScroll; for (int xt = xScroll >> 3; xt <= (xScroll + w) >> 3; xt++) { int xp = xt * 8 - xScroll; int ti = (xt & (MAP_WIDTH_MASK)) + (yt & (MAP_WIDTH_MASK)) * MAP_WIDTH; render(xp, yp, tiles[ti], colors[ti], databits[ti]); } }
	 * 
	 * for (int i = 0; i < sprites.size(); i++) { Sprite s = sprites.get(i); render(s.x, s.y, s.img, s.col, s.bits); } sprites.clear(); }
	 */

	public void render(int xp, int yp, int tile, int colors, int bits) {
		xp -= xOffset;
		yp -= yOffset;
		boolean mirrorX = (bits & BIT_MIRROR_X) > 0;
		boolean mirrorY = (bits & BIT_MIRROR_Y) > 0;

		int xTile = tile % 32;
		int yTile = tile / 32;
		int toffs = xTile * 8 + yTile * 8 * sheet.width;

		for (int y = 0; y < 8; y++) {
			int ys = y;
			if (mirrorY) ys = 7 - y;
			if (y + yp < 0 || y + yp >= h) continue;
			for (int x = 0; x < 8; x++) {
				if (x + xp < 0 || x + xp >= w) continue;

				int xs = x;
				if (mirrorX) xs = 7 - x;
				int col = (colors >> (sheet.pixels[xs + ys * sheet.width + toffs] * 8)) & 255;
				if (col < 255) pixels[(x + xp) + (y + yp) * w] = col;
			}
		}
	}
	

	public void setOffset(int xOffset, int yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	private int[] dither = new int[] { 0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5, };

	public void overlay(Screen screen2, int xa, int ya) {
		int[] oPixels = screen2.pixels;
		int i = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (oPixels[i] / 10 <= dither[((x + xa) & 3) + ((y + ya) & 3) * 4]) pixels[i] = 0;
				i++;
			}

		}
	}

	public void renderLight(int x, int y, int r) {
		x -= xOffset;
		y -= yOffset;
		int x0 = x - r;
		int x1 = x + r;
		int y0 = y - r;
		int y1 = y + r;

		if (x0 < 0) x0 = 0;
		if (y0 < 0) y0 = 0;
		if (x1 > w) x1 = w;
		if (y1 > h) y1 = h;
		// System.out.println(x0 + ", " + x1 + " -> " + y0 + ", " + y1);
		for (int yy = y0; yy < y1; yy++) {
			int yd = yy - y;
			yd = yd * yd;
			for (int xx = x0; xx < x1; xx++) {
				int xd = xx - x;
				int dist = xd * xd + yd;
				// System.out.println(dist);
				if (dist <= r * r) {
					int br = 255 - dist * 255 / (r * r);
					if (pixels[xx + yy * w] < br) pixels[xx + yy * w] = br;
				}
			}
		}
	}
}