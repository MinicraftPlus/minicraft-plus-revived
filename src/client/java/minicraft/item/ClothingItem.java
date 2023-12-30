package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ClothingItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new ClothingItem("Red Clothes", new LinkedSprite(SpriteType.Item, "red_clothes"), Color.get(1, 204, 0, 0)));
		items.add(new ClothingItem("Blue Clothes", new LinkedSprite(SpriteType.Item, "blue_clothes"), Color.get(1, 0, 0, 204)));
		items.add(new ClothingItem("Green Clothes", new LinkedSprite(SpriteType.Item, "green_clothes"), Color.get(1, 0, 204, 0)));
		items.add(new ClothingItem("Yellow Clothes", new LinkedSprite(SpriteType.Item, "yellow_clothes"), Color.get(1, 204, 204, 0)));
		items.add(new ClothingItem("Black Clothes", new LinkedSprite(SpriteType.Item, "black_clothes"), Color.get(1, 51)));
		items.add(new ClothingItem("Orange Clothes", new LinkedSprite(SpriteType.Item, "orange_clothes"), Color.get(1, 255, 102, 0)));
		items.add(new ClothingItem("Purple Clothes", new LinkedSprite(SpriteType.Item, "purple_clothes"), Color.get(1, 102, 0, 153)));
		items.add(new ClothingItem("Cyan Clothes", new LinkedSprite(SpriteType.Item, "cyan_clothes"), Color.get(1, 0, 102, 153)));
		items.add(new ClothingItem("Reg Clothes", new LinkedSprite(SpriteType.Item, "reg_clothes"), Color.get(1, 51, 51, 0))); // Dark Green

		return items;
	}

	private int playerCol;

	private ClothingItem(String name, LinkedSprite sprite, int pcol) {
		this(name, 1, sprite, pcol);
	}

	private ClothingItem(String name, int count, LinkedSprite sprite, int pcol) {
		super(name, sprite, count);
		playerCol = pcol;
	}

	// Put on clothes
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (player.shirtColor == playerCol) {
			return false;
		} else {
			if (!Game.isMode("minicraft.settings.mode.creative")) {
				ClothingItem lastClothing = (ClothingItem) getAllInstances().stream().filter(i -> i instanceof ClothingItem && ((ClothingItem) i).playerCol == player.shirtColor)
					.findAny().orElse(null);
				if (lastClothing == null)
					lastClothing = (ClothingItem) Items.get("Reg Clothes");
				lastClothing = lastClothing.copy();
				lastClothing.count = 1;
				player.tryAddToInvOrDrop(lastClothing);
			}
			player.shirtColor = playerCol;
			return super.interactOn(true);
		}
	}

	@Override
	public boolean interactsWithWorld() {
		return false;
	}

	public @NotNull ClothingItem copy() {
		return new ClothingItem(getName(), count, sprite, playerCol);
	}
}
