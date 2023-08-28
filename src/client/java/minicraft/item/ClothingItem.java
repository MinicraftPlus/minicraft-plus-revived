package minicraft.item;

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
	private static final int DEFAULT_COLOR = Color.get(1, 51, 51, 0); // Dark Green

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new ClothingItem("Clothes", 0));

		return items;
	}

	private int color;

	private ClothingItem(String name, int color) {
		super(name, new LinkedSprite(SpriteType.Item, "clothes").setColor(color == 0 ? DEFAULT_COLOR : color));
		this.color = color;
	}

	public void setColor(int color) {
		sprite.setColor((this.color = color) == 0 ? DEFAULT_COLOR : color);
	}

	// Put on clothes
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		ClothingItem lastClothing = (ClothingItem) Items.get("Clothes").copy();
		lastClothing.count = 1;
		lastClothing.setColor(player.shirtColor);
		player.tryAddToInvOrDrop(lastClothing);
		player.shirtColor = color;
		return super.interactOn(true);
	}

	@Override
	public boolean interactsWithWorld() { return false; }

	public @NotNull ClothingItem copy() {
		return new ClothingItem(getName(), color);
	}
}
