package minicraft.item;

import java.util.ArrayList;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ClothingItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new ClothingItem("Red Clothes", new LinkedSpriteSheet(SpriteType.Item, "red_clothes"), Color.get(1, 204, 0, 0)));
		items.add(new ClothingItem("Blue Clothes", new LinkedSpriteSheet(SpriteType.Item, "blue_clothes"), Color.get(1, 0, 0, 204)));
		items.add(new ClothingItem("Green Clothes",  new LinkedSpriteSheet(SpriteType.Item, "green_clothes"), Color.get(1, 0, 204, 0)));
		items.add(new ClothingItem("Yellow Clothes",  new LinkedSpriteSheet(SpriteType.Item, "yellow_clothes"), Color.get(1, 204, 204, 0)));
		items.add(new ClothingItem("Black Clothes",  new LinkedSpriteSheet(SpriteType.Item, "black_clothes"), Color.get(1, 51)));
		items.add(new ClothingItem("Orange Clothes",  new LinkedSpriteSheet(SpriteType.Item, "orange_clothes"), Color.get(1, 255, 102, 0)));
		items.add(new ClothingItem("Purple Clothes",  new LinkedSpriteSheet(SpriteType.Item, "purple_clothes"), Color.get(1, 102, 0, 153)));
		items.add(new ClothingItem("Cyan Clothes",  new LinkedSpriteSheet(SpriteType.Item, "cyan_clothes"), Color.get(1, 0, 102, 153)));
		items.add(new ClothingItem("Reg Clothes",  new LinkedSpriteSheet(SpriteType.Item, "reg_clothes"), Color.get(1, 51, 51, 0)));

		return items;
	}

	private int playerCol;

	private ClothingItem(String name, LinkedSpriteSheet sprite, int pcol) { this(name, 1, sprite, pcol); }
	private ClothingItem(String name, int count, LinkedSpriteSheet sprite, int pcol) {
		super(name, sprite, count);
		playerCol = pcol;
	}

	// Put on clothes
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (player.shirtColor == playerCol) {
			return false;
		} else {
			player.shirtColor = playerCol;
			return super.interactOn(true);
		}
	}

	@Override
	public boolean interactsWithWorld() { return false; }

	public ClothingItem clone() {
		return new ClothingItem(getName(), count, sprite, playerCol);
	}
}
