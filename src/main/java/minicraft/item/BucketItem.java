package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BucketItem extends StackableItem {

	public enum Filling {
		Empty (Tiles.get("hole"), 2),
		Water (Tiles.get("water"), 0),
		Lava (Tiles.get("lava"), 1);

		public final Tile tile;
		public final int offset;

		Filling(Tile tile, int offset) {
			this.tile = tile;
			this.offset = offset;
		}
	}

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		for (Filling filling : Filling.values())
			items.add(new BucketItem(filling));

		return items;
	}

	private static Filling getFilling(Tile tile) {
		for (Filling filling : Filling.values())
			if (filling.tile.id == tile.id)
				return filling;

		return null;
	}

	private final Filling filling;

	private BucketItem(Filling filling) { this(filling, 1); }
	private BucketItem(Filling filling, int count) {
		super(filling.toString() + " Bucket", new LinkedSprite(SpriteType.Item, filling == Filling.Empty ? "bucket" :
			filling == Filling.Lava ? "lava_bucket" : "water_bucket"), count);
		this.filling = filling;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		Filling filling = getFilling(tile);
		if (filling == null) return false;

		if (this.filling != Filling.Empty) {
			if (filling == Filling.Empty) {
				level.setTile(xt, yt, this.filling.tile);
				if (!Game.isMode("minicraft.settings.mode.creative")) player.activeItem = editBucket(player, Filling.Empty);
				return true;
			} else if (filling == Filling.Lava && this.filling == Filling.Water) {
				level.setTile(xt, yt, Tiles.get("Obsidian"));
				if (!Game.isMode("minicraft.settings.mode.creative")) player.activeItem = editBucket(player, Filling.Empty);
				return true;
			}
		} else { // This is an empty bucket
			level.setTile(xt, yt, Tiles.get("hole"));
			if (!Game.isMode("minicraft.settings.mode.creative")) player.activeItem = editBucket(player, filling);
			return true;
		}

		return false;
	}

	/** This method exists due to the fact that buckets are stackable, but only one should be changed at one time. */
	private BucketItem editBucket(Player player, Filling newFilling) {
		if (count == 0) return null; // This honestly should never happen...
		if (count == 1) return new BucketItem(newFilling);

		// This item object is a stack of buckets.
		count--;
		if (player.getInventory().add(new BucketItem(newFilling)) == 0) {
			player.getLevel().dropItem(player.x, player.y, new BucketItem(newFilling));
		}
		return this;
	}

	public boolean equals(Item other) {
		return super.equals(other) && filling == ((BucketItem)other).filling;
	}

	@Override
	public int hashCode() { return super.hashCode() + filling.offset * 31; }

	public @NotNull BucketItem copy() {
		return new BucketItem(filling, count);
	}
}
