package minicraft.entity.furniture;

import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import org.jetbrains.annotations.NotNull;

public class Lantern extends Furniture {
	public enum Type {
		NORM ("Lantern", 9, new LinkedSprite(SpriteType.Entity, "lantern"), new LinkedSprite(SpriteType.Item, "lantern")),
		IRON ("Iron Lantern", 12, new LinkedSprite(SpriteType.Entity, "iron_lantern"), new LinkedSprite(SpriteType.Item, "iron_lantern")),
		GOLD ("Gold Lantern", 15, new LinkedSprite(SpriteType.Entity, "gold_lantern"), new LinkedSprite(SpriteType.Item, "gold_lantern"));

		private final int light;
		private final String title;
		private final LinkedSprite sprite, itemSprite;

		Type(String title, int light, LinkedSprite sprite, LinkedSprite itemSprite) {
			this.title = title;
			this.light = light;
			this.sprite = sprite;
			this.itemSprite = itemSprite;
		}
	}

	public final Lantern.Type type;

	/**
	 * Creates a lantern of a given type.
	 * @param type Type of lantern.
	 */
	public Lantern(Lantern.Type type) {
		super(type.title, type.sprite, type.itemSprite, 3, 2);
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
