package minicraft.entity;

import minicraft.gfx.Color;
import minicraft.gfx.Sprite;

public class Lantern extends Furniture {
	public enum Type {
		NORM ("Lantern", 9, Color.get(-1, 000, 222, 555)),
		IRON ("Iron Lantern", 12, Color.get(-1, 100, 322, 544)),
		GOLD ("Gold Lantern", 15, Color.get(-1, 110, 440, 553));
		
		protected int col, light;
		protected String title;
			
		Type(String title, int light, int col) {
			this.title = title;
			this.col = col;
			this.light = light;
		}
	}
	
	public Lantern.Type type;
	
	/**
	 * Creates a lantern of a given type.
	 * @param type Type of lantern.
	 */
	public Lantern(Lantern.Type type) {
		super(type.title, new Sprite(10, 8, 2, 2, type.col), 3, 2);
		this.type = type;
	}
	
	@Override
	public Furniture clone() {
		return new Lantern(type);
	}
	
	/** 
	 * Gets the size of the radius for light underground (Bigger number, larger light) 
	 */
	@Override
	public int getLightRadius() {
		return type.light;
	}
}
