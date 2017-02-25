package com.mojang.ld22.entity;

import com.mojang.ld22.gfx.Color;

public class Lantern extends Furniture {
	public Lantern() {
		super("Lantern");
		
		col0 = Color.get(-1, 111, 222, 555);
		col1 = Color.get(-1, 222, 333, 555);
		col2 = Color.get(-1, 111, 222, 555);
	    col3 = Color.get(-1, 000, 111, 555);
	    
		col = Color.get(-1, 000, 222, 555);
		sprite = 5;
		xr = 3;
		yr = 2;
	}

	public int getLightRadius() {
		return 9;
	}
}