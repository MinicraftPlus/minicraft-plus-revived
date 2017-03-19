package com.mojang.ld22.gfx;

public class Sprite {
	public int x, y; // sprite coordinates
	public int img; // sprite image
	public int col; // sprite color
	public int bits; // sprite bits

	public Sprite(int x, int y, int img, int col, int bits) {
		this.x = x;
		this.y = y;
		this.img = img;
		this.col = col;
		this.bits = bits;
	}
}
