package minicraft.item;

import java.util.ArrayList;

import minicraft.core.*;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class BucketItem extends StackableItem {
	
	public enum Fill {
		Empty (Tiles.get("hole"), 333),
		Water (Tiles.get("water"), 005),
		Lava (Tiles.get("lava"), 400);
		
		public Tile contained;
		public int innerColor; // TODO make it so that the inside color is fetched from the tile color.
		
		Fill(Tile contained, int innerCol) {
			this.contained = contained;
			innerColor = innerCol;
		}
	}
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		for(Fill fill: Fill.values())
			items.add(new BucketItem(fill));
		
		return items;
	}
	
	private static Fill getFilling(Tile tile) {
		for(Fill fill: Fill.values())
			if(fill.contained.id == tile.id)
				return fill;
		
		return null;
	}
	
	private Fill filling;
	
	private BucketItem(Fill fill) { this(fill, 1); }
	private BucketItem(Fill fill, int count) {
		super(fill.name() + " Bucket", new Sprite(21, 4, Color.get(-1, 222, fill.innerColor, 555)), count);
		this.filling = fill;
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		Fill fill = getFilling(tile);
		if(fill == null) return false;
		boolean success = false;
		if(fill == Fill.Empty && filling != Fill.Empty) {
			level.setTile(xt, yt, filling.contained);
			if(!Game.isMode("creative")) player.activeItem = editBucket(player, Fill.Empty);
			return true;
		}
		else if(filling == Fill.Empty) {
			level.setTile(xt, yt, Tiles.get("hole"));
			if(!Game.isMode("creative")) player.activeItem = editBucket(player, fill);
			return true;
		}
		
		return false;
	}
	
	/** This method exists due to the fact that buckets are stackable, but only one should be changed at one time. */
	private BucketItem editBucket(Player player, Fill newFill) {
		if (count == 0) return null; // this honestly should never happen...
		if (count == 1) return new BucketItem(newFill);
		
		// this item object is a stack of buckets.
		count--;
		player.inventory.add(new BucketItem(newFill));
		return this;
	}
	
	public boolean matches(Item other) {
		return super.matches(other) && filling == ((BucketItem)other).filling;
	}
	
	public BucketItem clone() {
		return new BucketItem(filling, count);
	}
}
