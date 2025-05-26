package minicraft.item;

import minicraft.core.Updater;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

import java.util.ArrayList;

public class HeartItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new HeartItem("Obsidian Heart", new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "obsidian_heart"), 5));

		return items;
	}

	private final int health; // The amount of health to increase by.
	private final int staminaCost; // The amount of stamina it costs to consume.

	private HeartItem(String name, SpriteLinker.LinkedSprite sprite, int health) {
		super(name, sprite);
		this.health = health;
		staminaCost = 7;
	}

	/**
	 * What happens when the player uses the item on a tile
	 */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir, ItemStack stack) {
		boolean success;

		if ((Player.baseHealth + Player.extraHealth) < Player.maxHealth) {
			Player.extraHealth += health; // Permanent increase of health by health variable (Basically 5)
			player.health += health; // Adds health to the player when used. (Almost like absorbing the item's power first time)
			success = true;
		} else {
			Updater.notifyAll("Health increase is at max!"); // When at max, health cannot be increased more and doesn't consume item
			return false;
		}

		return super.interactOn(success, stack);
	}

	@Override
	public boolean interactsWithWorld(ItemStack stack) {
		return false;
	}
}
