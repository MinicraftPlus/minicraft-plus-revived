package minicraft.entity.furniture;

import minicraft.core.CrashHandler;
import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.ItemHolder;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.saveload.Load;
import minicraft.screen.ContainerDisplay;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class Chest extends Furniture implements ItemHolder {
	private Inventory inventory; // Inventory of the chest

	public Chest() {
		this("Chest");
	}

	public Chest(String name) {
		this(name, new LinkedSprite(SpriteType.Item, "chest"));
	}

	/**
	 * Creates a chest with a custom name.
	 *
	 * @param name Name of chest.
	 */
	public Chest(String name, LinkedSprite itemSprite) {
		super(name, new LinkedSprite(SpriteType.Entity, "chest"), itemSprite, 3, 3); // Name of the chest

		inventory = new Inventory(); // Initialize the inventory.
	}

	/**
	 * This is what occurs when the player uses the "Menu" command near this
	 */
	public boolean use(Player player) {
		Game.setDisplay(new ContainerDisplay(player, this));
		return true;
	}

	public void populateInvRandom(String lootTable, int depth) {
		try {
			String[] lines = Load.loadFile("/resources/data/chestloot/" + lootTable + ".txt").toArray(new String[]{});

			for (String line : lines) {
				//System.out.println(line);
				String[] data = line.split(",");
				if (!line.startsWith(":")) {
					inventory.tryAdd(Integer.parseInt(data[0]), Items.get(data[1]), data.length < 3 ? 1 : Integer.parseInt(data[2]));
				} else if (inventory.invSize() == 0) {
					// Adds the "fallback" items to ensure there's some stuff
					String[] fallbacks = line.substring(1).split(":");
					for (String item : fallbacks) {
						inventory.add(Items.get(item.split(",")[0]), Integer.parseInt(item.split(",")[1]));
					}
				}
			}
		} catch (IOException e) {
			CrashHandler.errorHandle(e, new CrashHandler.ErrorInfo("Loot table", CrashHandler.ErrorInfo.ErrorType.REPORT, "Couldn't read loot table \"" + lootTable + ".txt" + "\""));
		}
	}

	@Override
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (inventory.invSize() == 0)
			return super.interact(player, item, attackDir);
		return false;
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public void die() {
		if (level != null) {
			List<Item> items = inventory.getItems();
			level.dropItem(x, y, items.toArray(new Item[0]));
		}
		super.die();
	}
}
