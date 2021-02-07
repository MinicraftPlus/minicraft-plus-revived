package minicraft.entity.furniture;

import minicraft.gfx.Color;
import minicraft.gfx.Sprite;

public class Lantern extends Furniture {
	public enum Type {
		NORM ("Lantern", 9, 0),
		IRON ("Iron Lantern", 12, 2),
		GOLD ("Gold Lantern", 15, 4);
		
		protected int light, offset;
		protected String title;
			
		Type(String title, int light, int offset) {
			this.title = title;
			this.offset = offset;
			this.light = light;
		}
	}
	
	public Lantern.Type type;
	
	/**
	 * Creates a lantern of a given type.
	 * @param type Type of lantern.
	 */
	public Lantern(Lantern.Type type) {
		super(type.title, new Sprite(18 + type.offset, 26, 2, 2, 2), 3, 2);
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
