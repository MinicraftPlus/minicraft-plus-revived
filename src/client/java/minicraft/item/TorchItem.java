package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.TorchTile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TorchItem extends TileItem {

	public static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		items.add(new TorchItem());
		return items;
	}

	private TorchItem() {
		this(1);
	}

	private TorchItem(int count) {
		super("Torch", new LinkedSprite(SpriteType.Item, "torch"), count, null, "dirt", "Wood Planks", "Stone Bricks", "Obsidian", "Wool", "Red Wool", "Blue Wool", "Green Wool", "Yellow Wool", "Black Wool", "grass", "sand", "path", "ornate stone", "ornate obsidian");
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (validTiles.contains(tile.name)) {
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
	public int hashCode() {
		return 8931;
	}

	public @NotNull TorchItem copy() {
		return new TorchItem(count);
	}
}
