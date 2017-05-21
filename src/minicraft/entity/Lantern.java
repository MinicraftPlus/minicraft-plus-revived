package minicraft.entity;

import minicraft.gfx.Color;
import minicraft.gfx.Sprite;

public class Lantern extends Furniture {
	//private Lantern.Type type;
	
	public enum Type {
		NORM ("Lantern", 9, Color.get(-1, 000, 222, 555)),
		IRON ("Iron Lantern", 12, Color.get(-1, 100, 322, 544)),
		GOLD ("Gold Lantern", 15, Color.get(-1, 110, 440, 553));
		
		protected int col, light;
		protected String title;
			
		private Type(String title, int light, int col) {
			this.title = title;
			this.col = col;
			this.light = light;
		}
	}
	
	public Lantern.Type type;
	
	public Lantern(Lantern.Type type) {
		super(type.title, new Sprite(10, 8, 2, 2, type.col), 3, 2);
		this.type = type;
		//this.light = light;
		/*col0 = Color.get(-1, 111, 222, 555);
		col1 = Color.get(-1, 222, 333, 555);
		col2 = Color.get(-1, 111, 222, 555);
		col3 = Color.get(-1, 000, 111, 555);
		*/
		//col = Color.get(-1, 000, 222, 555);
		//this.type = type;
		/*col = type.color;
		
		sprite = 5;
		xr = 3;
		yr = 2;*/
	}
	
	@Override
	public Furniture clone() {
		//System.out.println("type: " + type);
		return (Furniture) new Lantern(type);
	}
	
	/** Gets the size of the radius for light underground (Bigger number, larger light) */
	public int getLightRadius() {
		return type.light;
	}
}
