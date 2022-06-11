package minicraft.item;

import java.util.ArrayList;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.SignTile;
import minicraft.level.tile.Tile;

public class SignItem extends TileItem {
	public static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		items.add(new SignItem());
		return items;
	}

	private SignItem() { this(1); }
	private SignItem(int count) {
		super("Torch", (new Sprite(12, 3, 0)), count, "", "dirt", "Wood Planks", "Stone Bricks", "Obsidian", "Wool", "Red Wool", "Blue Wool", "Green Wool", "Yellow Wool", "Black Wool", "grass", "sand");
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (validTiles.contains(tile.name)) {
			level.setTile(xt, yt, SignTile.getSignTile(tile));
			// TODO
			return super.interactOn(true);
		}
		return super.interactOn(false);
	}

	@Override
	public boolean equals(Item other) {
		return other instanceof SignItem;
	}

	@Override
	public int hashCode() { return 8931; }

	public SignItem clone() {
		return new SignItem(count);
	}
}
