package minicraft.item;

import minicraft.core.Updater;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

import java.util.ArrayList;

public class HeartItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new HeartItem("Obsidian Heart", new Sprite(0, 20, 0), 5));

		return items;
	}

	private int health; // The amount of health to increase by.
	private int staminaCost; // The amount of stamina it costs to consume.

	private HeartItem(String name, Sprite sprite, int health) { this(name, sprite, 1, health); }
	private HeartItem(String name, Sprite sprite, int count, int health) {
		super(name, sprite, count);
		this.health = health;
		staminaCost = 7;
	}

	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;

		if ((Player.baseHealth + Player.extraHealth) < Player.maxHealth) {
			Player.extraHealth += health;
			player.health += health;
			success = true;
		}
		else {
			Updater.notifyAll("Health increase is at max!");
			return false;
		}

		return super.interactOn(success);
	}

	@Override
	public boolean interactsWithWorld() { return false; }

	public HeartItem clone() {
		return new HeartItem(getName(), sprite, count, health);
	}
}
