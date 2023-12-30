package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// Some items are direct instances of this class; those instances are the true "items", like stone, wood, wheat, or coal; you can't do anything with them besides use them to make something else.

public class StackableItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new StackableItem("Wood", new LinkedSprite(SpriteType.Item, "wood")));
		items.add(new StackableItem("Leather", new LinkedSprite(SpriteType.Item, "leather")));
		items.add(new StackableItem("Wheat", new LinkedSprite(SpriteType.Item, "wheat")));
		items.add(new StackableItem("Key", new LinkedSprite(SpriteType.Item, "key")));
		items.add(new StackableItem("Arrow", new LinkedSprite(SpriteType.Item, "arrow")));
		items.add(new StackableItem("String", new LinkedSprite(SpriteType.Item, "string")));
		items.add(new StackableItem("Coal", new LinkedSprite(SpriteType.Item, "coal")));
		items.add(new StackableItem("Iron Ore", new LinkedSprite(SpriteType.Item, "iron_ore")));
		items.add(new StackableItem("Lapis", new LinkedSprite(SpriteType.Item, "lapis")));
		items.add(new StackableItem("Gold Ore", new LinkedSprite(SpriteType.Item, "gold_ore")));
		items.add(new StackableItem("Iron", new LinkedSprite(SpriteType.Item, "iron_ingot")));
		items.add(new StackableItem("Gold", new LinkedSprite(SpriteType.Item, "gold_ingot")));
		items.add(new StackableItem("Rose", new LinkedSprite(SpriteType.Item, "red_flower")));
		items.add(new StackableItem("Gunpowder", new LinkedSprite(SpriteType.Item, "gunpowder")));
		items.add(new StackableItem("Slime", new LinkedSprite(SpriteType.Item, "slime")));
		items.add(new StackableItem("Glass", new LinkedSprite(SpriteType.Item, "glass")));
		items.add(new StackableItem("Cloth", new LinkedSprite(SpriteType.Item, "cloth")));
		items.add(new StackableItem("Gem", new LinkedSprite(SpriteType.Item, "gem")));
		items.add(new StackableItem("Scale", new LinkedSprite(SpriteType.Item, "scale")));
		items.add(new StackableItem("Shard", new LinkedSprite(SpriteType.Item, "shard")));
		items.add(new StackableItem("Cloud Ore", new LinkedSprite(SpriteType.Item, "cloud_ore")));
		items.add(new StackableItem("Glass Bottle", new LinkedSprite(SpriteType.Item, "glass_bottle")));
		items.add(new StackableItem("Tomato", new LinkedSprite(SpriteType.Item, "tomato")));
		items.add(new StackableItem("Bone", new LinkedSprite(SpriteType.Item, "bone")));
		items.add(new StackableItem("Fertilizer", new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "fertilizer")));

		return items;
	}

	public int count;
	public int maxCount = 100;

	protected StackableItem(String name, LinkedSprite sprite) {
		super(name, sprite);
		count = 1;
	}

	protected StackableItem(String name, LinkedSprite sprite, int count) {
		this(name, sprite);
		this.count = count;
	}

	public boolean stacksWith(Item other) {
		return other instanceof StackableItem && other.getName().equals(getName());
	}

	// This is used by (most) subclasses, to standardize the count decrement behavior. This is not the normal interactOn method.
	protected boolean interactOn(boolean subClassSuccess) {
		if (subClassSuccess && !Game.isMode("minicraft.settings.mode.creative"))
			count--;
		return subClassSuccess;
	}

	/**
	 * Called to determine if this item should be removed from an inventory.
	 */
	@Override
	public boolean isDepleted() {
		return count <= 0;
	}

	@Override
	public @NotNull StackableItem copy() {
		return new StackableItem(getName(), sprite, count);
	}

	@Override
	public String toString() {
		return super.toString() + "-Stack_Size:" + count;
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
