package minicraft.item;

import java.util.ArrayList;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class FoodItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new FoodItem("Baked Potato", new LinkedSpriteSheet(SpriteType.Item, "baked_potato"), 1));
		items.add(new FoodItem("Apple", new LinkedSpriteSheet(SpriteType.Item, "apple"), 1));
		items.add(new FoodItem("Raw Pork", new LinkedSpriteSheet(SpriteType.Item, "pork"), 1));
		items.add(new FoodItem("Raw Fish", new LinkedSpriteSheet(SpriteType.Item, "fish"), 1));
		items.add(new FoodItem("Raw Beef", new LinkedSpriteSheet(SpriteType.Item, "beef"), 1));
		items.add(new FoodItem("Bread", new LinkedSpriteSheet(SpriteType.Item, "bread"), 2));
		items.add(new FoodItem("Cooked Fish", new LinkedSpriteSheet(SpriteType.Item, "cooked_fish"), 3));
		items.add(new FoodItem("Cooked Pork", new LinkedSpriteSheet(SpriteType.Item, "cooked_pork"), 3));
		items.add(new FoodItem("Steak", new LinkedSpriteSheet(SpriteType.Item, "cooked_beef"), 3));
		items.add(new FoodItem("Gold Apple", new LinkedSpriteSheet(SpriteType.Item, "golden_apple"), 10));

		return items;
	}

	private int feed; // The amount of hunger the food "satisfies" you by.
	private int staminaCost; // The amount of stamina it costs to consume the food.

	private FoodItem(String name, LinkedSpriteSheet sprite, int feed) { this(name, sprite, 1, feed); }
	private FoodItem(String name, LinkedSpriteSheet sprite, int count, int feed) {
		super(name, sprite, count);
		this.feed = feed;
		staminaCost = 5;
	}

	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;
		if (count > 0 && player.hunger < Player.maxHunger && player.payStamina(staminaCost)) { // If the player has hunger to fill, and stamina to pay...
			player.hunger = Math.min(player.hunger + feed, Player.maxHunger); // Restore the hunger
			success = true;
		}

		return super.interactOn(success);
	}

	@Override
	public boolean interactsWithWorld() { return false; }

	public FoodItem clone() {
		return new FoodItem(getName(), sprite, count, feed);
	}
}
