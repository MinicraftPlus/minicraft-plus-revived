package com.mojang.ld22.entity;

import com.mojang.ld22.gfx.Color;

public class IronLantern extends Furniture {
	public IronLantern() {
		super("I.Lantern");
		
		col0 = Color.get(-1, 100, 211, 433);
		col1 = Color.get(-1, 211, 322, 544);
		col2 = Color.get(-1, 100, 211, 433);
	    col3 = Color.get(-1, 000, 100, 322);
		
		col = Color.get(-1, 100, 322, 544);
		sprite = 5;
		xr = 3;
		yr = 2;
	}

	public int getLightRadius() {
		return 12;
	}
}