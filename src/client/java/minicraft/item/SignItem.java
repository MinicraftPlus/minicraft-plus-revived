package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteLinker;
import minicraft.level.Level;
import minicraft.level.tile.SignTile;
import minicraft.level.tile.Tile;
import minicraft.screen.SignDisplay;
import org.jetbrains.annotations.NotNull;

public class SignItem extends TileItem {
	private static final SpriteLinker.LinkedSprite sprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "sign");
	private static final String[] solidTiles = { "dirt", "Wood Planks", "Stone Bricks", "Obsidian", "Wool", "Red Wool", "Blue Wool",
	"Green Wool", "Yellow Wool", "Black Wool", "grass", "sand", "path", "ornate stone", "ornate obsidian" };


	public static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		items.add(new SignItem());
		return items;
	}

	private SignItem() { this(1); }
	private SignItem(int count) {
		super("Sign", sprite, count, null, solidTiles);
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (validTiles.contains(tile.name)) {
			level.setTile(xt, yt, SignTile.getSignTile(tile));
			Game.setDisplay(new SignDisplay(level, xt, yt));
			return super.interactOn(true);
		}
		return super.interactOn(false);
	}

	public @NotNull SignItem copy() {
		return new SignItem(count);
	}
}
