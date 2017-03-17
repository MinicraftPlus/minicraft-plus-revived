package com.mojang.ld22.entity;

import com.mojang.ld22.gfx.Color;

public class GodLantern extends Furniture {
	public GodLantern() {
		super("God Lantern");

		col0 = Color.get(-1, 110, 330, 442);
		col1 = Color.get(-1, 110, 440, 553);
		col2 = Color.get(-1, 110, 330, 442);
		col3 = Color.get(-1, 000, 220, 331);

		col = Color.get(-1, 110, 440, 553);
		sprite = 5;
		xr = 3;
		yr = 2;
	}

	public int getLightRadius() {
		return 333;
	}
}
