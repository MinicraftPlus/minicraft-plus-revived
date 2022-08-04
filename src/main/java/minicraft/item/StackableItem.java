package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;

// Some items are direct instances of this class; those instances are the true "items", like stone, wood, wheat, or coal; you can't do anything with them besides use them to make something else.

public class StackableItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new StackableItem("Wood", new LinkedSpriteSheet(SpriteType.Item, "wood")));
		items.add(new StackableItem("Leather", new LinkedSpriteSheet(SpriteType.Item, "leater")));
		items.add(new StackableItem("Wheat", new LinkedSpriteSheet(SpriteType.Item, "wheat")));
		items.add(new StackableItem("Key", new LinkedSpriteSheet(SpriteType.Item, "key")));
		items.add(new StackableItem("arrow", new LinkedSpriteSheet(SpriteType.Item, "arrow")));
		items.add(new StackableItem("string", new LinkedSpriteSheet(SpriteType.Item, "string")));
		items.add(new StackableItem("Coal", new LinkedSpriteSheet(SpriteType.Item, "coal")));
		items.add(new StackableItem("Iron Ore", new LinkedSpriteSheet(SpriteType.Item, "iron_ore")));
		items.add(new StackableItem("Lapis", new LinkedSpriteSheet(SpriteType.Item, "lapis")));
		items.add(new StackableItem("Gold Ore", new LinkedSpriteSheet(SpriteType.Item, "gold_ore")));
		items.add(new StackableItem("Iron", new LinkedSpriteSheet(SpriteType.Item, "iron_ingot")));
		items.add(new StackableItem("Gold", new LinkedSpriteSheet(SpriteType.Item, "gold_ingot")));
		items.add(new StackableItem("Rose", new LinkedSpriteSheet(SpriteType.Item, "red_flower")));
		items.add(new StackableItem("Gunpowder", new LinkedSpriteSheet(SpriteType.Item, "gunpowder")));
		items.add(new StackableItem("Slime", new LinkedSpriteSheet(SpriteType.Item, "slime")));
		items.add(new StackableItem("glass", new LinkedSpriteSheet(SpriteType.Item, "glass")));
		items.add(new StackableItem("cloth", new LinkedSpriteSheet(SpriteType.Item, "cloth")));
		items.add(new StackableItem("gem", new LinkedSpriteSheet(SpriteType.Item, "gem")));
		items.add(new StackableItem("Scale", new LinkedSpriteSheet(SpriteType.Item, "scale")));
		items.add(new StackableItem("Shard", new LinkedSpriteSheet(SpriteType.Item, "shard")));
		items.add(new StackableItem("Cloud Ore", new LinkedSpriteSheet(SpriteType.Item, "cloud_ore")));

		return items;
	}

	public int count;
	public int maxCount = 100;

	protected StackableItem(String name, LinkedSpriteSheet sprite) {
		super(name, sprite);
		count = 1;
	}
	protected StackableItem(String name, LinkedSpriteSheet sprite, int count) {
		this(name, sprite);
		this.count = count;
	}

	public boolean stacksWith(Item other) { return other instanceof StackableItem && other.getName().equals(getName()); }

	// This is used by (most) subclasses, to standardize the count decrement behavior. This is not the normal interactOn method.
	protected boolean interactOn(boolean subClassSuccess) {
		if (subClassSuccess && !Game.isMode("minicraft.settings.mode.creative"))
			count--;
		return subClassSuccess;
	}

	/** Called to determine if this item should be removed from an inventory. */
	@Override
	public boolean isDepleted() {
		return count <= 0;
	}

	@Override
	public StackableItem clone() {
		return new StackableItem(getName(), sprite, count);
	}

	@Override
	public String toString() {
		return super.toString() + "-Stack_Size:"+count;
	}

	public String getData() {
		return getName() + "_" + count;
	}

	@Override
	public String getDisplayName() {
		String amt = (Math.min(count, 999)) + " ";
		return " " + amt + Localization.getLocalized(getName());
	}
}
