package minicraft.item;

import java.util.ArrayList;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.TorchTile;

public class TorchItem extends TileItem {
	
	public static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		items.add(new TorchItem());
		return items;
	}
	
	private TorchItem() { this(1); }
	private TorchItem(int count) {
		super("Torch", (new Sprite(18, 4, Color.get(-1, 500, 520, 320))), count, "", "dirt", "Wood Planks", "Stone Bricks", "Obsidian", "Wool", "grass", "sand");
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if(validTiles.contains(tile.name)) {
			level.setTile(xt, yt, TorchTile.getTorchTile(tile));
			return super.interactOn(true);
		}
		return super.interactOn(false);
	}
	
	@Override
	public boolean equals(Item other) {
		return other instanceof TorchItem;
	}
	
	@Override
	public int hashCode() { return 8931; }
	
	public TorchItem clone() {
		return new TorchItem(count);
	}
}
