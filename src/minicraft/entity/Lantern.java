package minicraft.entity;

import minicraft.gfx.Color;

public class Lantern extends Furniture {
	private Lantern.Type type;
	
	public enum Type {
		NORM (9, Color.get(-1, 000, 222, 555)),
		IRON (12, Color.get(-1, 100, 322, 544)),
		GOLD (15, Color.get(-1, 110, 440, 553));
		
		public int col, light;
		
		private Type(int light, int col) {
			this.col = col;
			this.light = light;
		}
	}
	
	public Lantern(Lantern.Type type) {
		super("Lantern", type.col, 5, 3, 2);
		
		/*col0 = Color.get(-1, 111, 222, 555);
		col1 = Color.get(-1, 222, 333, 555);
		col2 = Color.get(-1, 111, 222, 555);
		col3 = Color.get(-1, 000, 111, 555);
		*/
		//col = Color.get(-1, 000, 222, 555);
		this.type = type;
		/*col = type.color;
		
		sprite = 5;
		xr = 3;
		yr = 2;*/
	}
	
	public Furniture copy() {
		return (Furniture) new Lantern(type);
	}
	
	/** Gets the size of the radius for light underground (Bigger number, larger light) */
	public int getLightRadius() {
		return type.light;
	}
}
