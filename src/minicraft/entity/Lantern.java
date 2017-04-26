package minicraft.entity;

import minicraft.gfx.Color;

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
	
	/** Gets the size of the radius for light underground (Bigger number, larger light) */
	public int getLightRadius() {
		return 9;
	}
}
