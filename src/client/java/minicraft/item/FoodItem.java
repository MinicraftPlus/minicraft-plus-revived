package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FoodItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new FoodItem("Baked Potato", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "baked_potato").createSpriteLink(), 1));
		items.add(new FoodItem("Apple", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "apple").createSpriteLink(), 1));
		items.add(new FoodItem("Raw Pork", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "pork").createSpriteLink(), 1));
		items.add(new FoodItem("Raw Fish", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "fish").createSpriteLink(), 1));
		items.add(new FoodItem("Raw Beef", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "beef").createSpriteLink(), 1));
		items.add(new FoodItem("Bread", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "bread").createSpriteLink(), 2));
		items.add(new FoodItem("Cooked Fish", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "cooked_fish").createSpriteLink(), 3));
		items.add(new FoodItem("Cooked Pork", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "cooked_pork").createSpriteLink(), 3));
		items.add(new FoodItem("Steak", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "cooked_beef").createSpriteLink(), 3));
		items.add(new FoodItem("Gold Apple", new LinkedSprite.SpriteLinkBuilder(SpriteType.Item, "golden_apple").createSpriteLink(), 10));

		return items;
	}

	private int feed; // The amount of hunger the food "satisfies" you by.
	private int staminaCost; // The amount of stamina it costs to consume the food.

	private FoodItem(String name, LinkedSprite sprite, int feed) { this(name, sprite, 1, feed); }
	private FoodItem(String name, LinkedSprite sprite, int count, int feed) {
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

	public @NotNull FoodItem copy() {
		return new FoodItem(getName(), sprite, count, feed);
	}
}
