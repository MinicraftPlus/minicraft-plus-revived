package minicraft.item;

import java.util.ArrayList;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class FoodItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		items.add(new FoodItem("Bread", new Sprite(8, 4, Color.get(-1, 110, 330, 550)), 2));
		items.add(new FoodItem("Apple", new Sprite(9, 4, Color.get(-1, 100, 300, 500)), 1));
		items.add(new FoodItem("Raw Pork", new Sprite(20, 4, Color.get(-1, 211, 311, 411)), 1));
		items.add(new FoodItem("Raw Fish", new Sprite(24, 4, Color.get(-1, 660, 670, 680)), 1));
		items.add(new FoodItem("Raw Beef", new Sprite(20, 4, Color.get(-1, 200, 300, 400)), 1));
		items.add(new FoodItem("Pork Chop", new Sprite(20, 4, Color.get(-1, 220, 440, 330)), 3));
		items.add(new FoodItem("Cooked Fish", new Sprite(24, 4, Color.get(-1, 220, 330, 440)), 3));
		items.add(new FoodItem("Cooked Pork", new Sprite(20, 4, Color.get(-1, 220, 440, 330)), 3));
		items.add(new FoodItem("Steak", new Sprite(20, 4, Color.get(-1, 100, 333, 211)), 3));
		items.add(new FoodItem("Gold Apple", new Sprite(9, 4, Color.get(-1, 110, 440, 550)), 10));
		
		return items;
	}
	
	private int heal; // the amount of hunger the food "satisfies" you by.
	private int staminaCost; // the amount of stamina it costs to consume the food.
	
	private FoodItem(String name, Sprite sprite, int heal) { this(name, sprite, 1, heal); }
	private FoodItem(String name, Sprite sprite, int count, int heal) {
		super(name, sprite, count);
		this.heal = heal;
		staminaCost = 5;
	}
	
	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;
		if (count > 0 && player.hunger < Player.maxHunger && player.payStamina(staminaCost)) { // if the player has hunger to fill, and stamina to pay...
			player.hunger = Math.min(player.hunger + heal, Player.maxHunger); // restore the hunger
			success = true;
		}
		
		return super.interactOn(success);
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public FoodItem clone() {
		return new FoodItem(getName(), sprite, count, heal);
	}
}
