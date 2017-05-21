package minicraft.item;

import java.util.ArrayList;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.TorchTile;

public class TorchItem extends TileItem {
	
	public static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new TorchItem());
		return items;
	}
	
	public TorchItem() { this(1); }
	public TorchItem(int count) {
		super("Torch", (new Sprite(18, 4, Color.get(-1, 500, 520, 320))), count, null, "dirt", "Wood Planks", "Stone Bricks", "wool", "red Wool", "blue Wool", "green Wool", "yellow Wool", "black Wool", "grass", "sand");
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if(reqTiles.contains(tile)) {
			level.setTile(xt, yt, TorchTile.getTorchTile(tile), 0);
			return true;
		}
		return false;
	}
	
	public boolean matches(Item other) {
		return other instanceof TorchItem;
		/* this is all actually unnecessary, because it happens when the item is no longer referenced.
		...
		if(other instanceof TorchItem == false) return false;
		TorchItem otherTorch = (TorchItem)other;
		if(model == otherTorch.model) return true;
		//if here, than not both are null.
		if(model == null || otherTorch.model == null) return false;
		// neither torch-tile model is null.
		return model.matches(otherTorch.model);
		*/
	}
	
	public TorchItem clone() {
		return new TorchItem(count);
	}
}
