package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.gfx.SpriteManager.SpriteLink;
import minicraft.gfx.SpriteManager.SpriteType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// Some items are direct instances of this class; those instances are the true "items", like stone, wood, wheat, or coal; you can't do anything with them besides use them to make something else.

public class StackableItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new StackableItem("Wood", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "wood").createSpriteLink()));
		items.add(new StackableItem("Leather", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "leather").createSpriteLink()));
		items.add(new StackableItem("Wheat", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "wheat").createSpriteLink()));
		items.add(new StackableItem("Key", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "key").createSpriteLink()));
		items.add(new StackableItem("Arrow", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "arrow").createSpriteLink()));
		items.add(new StackableItem("String", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "string").createSpriteLink()));
		items.add(new StackableItem("Coal", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "coal").createSpriteLink()));
		items.add(new StackableItem("Iron Ore", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "iron_ore").createSpriteLink()));
		items.add(new StackableItem("Lapis", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "lapis").createSpriteLink()));
		items.add(new StackableItem("Gold Ore", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "gold_ore").createSpriteLink()));
		items.add(new StackableItem("Iron", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "iron_ingot").createSpriteLink()));
		items.add(new StackableItem("Gold", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "gold_ingot").createSpriteLink()));
		items.add(new StackableItem("Gunpowder", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "gunpowder").createSpriteLink()));
		items.add(new StackableItem("Slime", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "slime").createSpriteLink()));
		items.add(new StackableItem("Glass", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "glass").createSpriteLink()));
		items.add(new StackableItem("Cloth", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "cloth").createSpriteLink()));
		items.add(new StackableItem("Gem", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "gem").createSpriteLink()));
		items.add(new StackableItem("Scale", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "scale").createSpriteLink()));
		items.add(new StackableItem("Shard", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "shard").createSpriteLink()));
		items.add(new StackableItem("Cloud Ore", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "cloud_ore").createSpriteLink()));
		items.add(new StackableItem("Glass Bottle", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "glass_bottle").createSpriteLink()));
		items.add(new StackableItem("Tomato", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "tomato").createSpriteLink()));
		items.add(new StackableItem("Bone", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "bone").createSpriteLink()));
		items.add(new StackableItem("Fertilizer", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "fertilizer").createSpriteLink()));

		return items;
	}

	public int count;
	public int maxCount = 100;

	protected StackableItem(String name, SpriteLink sprite) {
		super(name, sprite);
		count = 1;
	}

	protected StackableItem(String name, SpriteLink sprite, int count) {
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
