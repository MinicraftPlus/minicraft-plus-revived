package minicraft.item;

import minicraft.core.Updater;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import minicraft.gfx.Color;

public class DevItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new DevItem("Speed Trinket", new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "potion").setColor(217)));
                items.add(new DevItem("Heart of the Creator", new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "obsidian_heart")));
                
		return items;
	}
        
	private int staminaCost; // The amount of stamina it costs to consume.
        private String name;

	private DevItem(String name, SpriteLinker.LinkedSprite sprite) {
		this(name, sprite, 1);
	}

	private DevItem(String name, SpriteLinker.LinkedSprite sprite, int count) {
		super(name, sprite, count);
		staminaCost = 20;
                this.name = name;
	}

	/**
	 * What happens when the player uses the item on a tile
	 */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;

		switch (name) {
                    case "Speed Trinket":
                        PotionItem.applyPotion(player, PotionType.Speed, 128000);
                        return true;
                    case "Heart of the Creator":
                        if ((Player.baseHealth + Player.extraHealth) < Player.maxHealth) {
                            Player.extraHealth += 20; // Permanent increase of health by health variable (Basically 5)
                            player.health += 20; // Adds health to the player when used. (Almost like absorbing the item's power first time)
                            success = true;
                    } else {
			Updater.notifyAll("Health increase is at max!"); // When at max, health cannot be increased more and doesn't consume item
			return false;
                    }
                }

		return super.interactOn(success);
	}

	@Override
	public boolean interactsWithWorld() {
		return false;
	}

	public @NotNull DevItem copy() {
		return new DevItem(getName(), sprite, count);
	}
}
