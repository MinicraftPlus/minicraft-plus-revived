package minicraft.entity.furniture;

import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import org.jetbrains.annotations.NotNull;

public class Lantern extends Furniture {
	public enum Type {
		NORM("Lantern", 9, 0),
		IRON("Iron Lantern", 12, 2),
		GOLD("Gold Lantern", 15, 4);

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
	 *
	 * @param type Type of lantern.
	 */
	public Lantern(Lantern.Type type) {
		super(type.title, new LinkedSprite(SpriteType.Entity, type == Type.NORM ? "lantern" :
			type == Type.IRON ? "iron_lantern" : "gold_lantern"), new LinkedSprite(SpriteType.Item, type == Type.NORM ? "lantern" :
			type == Type.IRON ? "iron_lantern" : "gold_lantern"), 3, 2);
		this.type = type;
	}

	@Override
	public @NotNull Furniture copy() {
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
