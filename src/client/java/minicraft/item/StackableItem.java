package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// Some items are direct instances of this class; those instances are the true "items", like stone, wood, wheat, or coal; you can't do anything with them besides use them to make something else.

public class StackableItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new StackableItem("Wood", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "wood").createSpriteLink()));
		items.add(new StackableItem("Leather", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "leather").createSpriteLink()));
		items.add(new StackableItem("Wheat", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "wheat").createSpriteLink()));
		items.add(new StackableItem("Key", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "key").createSpriteLink()));
		items.add(new StackableItem("arrow", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "arrow").createSpriteLink()));
		items.add(new StackableItem("string", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "string").createSpriteLink()));
		items.add(new StackableItem("Coal", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "coal").createSpriteLink()));
		items.add(new StackableItem("Iron Ore", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "iron_ore").createSpriteLink()));
		items.add(new StackableItem("Lapis", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "lapis").createSpriteLink()));
		items.add(new StackableItem("Gold Ore", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "gold_ore").createSpriteLink()));
		items.add(new StackableItem("Iron", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "iron_ingot").createSpriteLink()));
		items.add(new StackableItem("Gold", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "gold_ingot").createSpriteLink()));
		items.add(new StackableItem("Rose", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "red_flower").createSpriteLink()));
		items.add(new StackableItem("Gunpowder", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "gunpowder").createSpriteLink()));
		items.add(new StackableItem("Slime", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "slime").createSpriteLink()));
		items.add(new StackableItem("glass", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "glass").createSpriteLink()));
		items.add(new StackableItem("cloth", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "cloth").createSpriteLink()));
		items.add(new StackableItem("gem", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "gem").createSpriteLink()));
		items.add(new StackableItem("Scale", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "scale").createSpriteLink()));
		items.add(new StackableItem("Shard", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "shard").createSpriteLink()));
		items.add(new StackableItem("Cloud Ore", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "cloud_ore").createSpriteLink()));
		items.add(new StackableItem("Glass Bottle", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "glass_bottle").createSpriteLink()));

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
	public @NotNull StackableItem copy() {
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
