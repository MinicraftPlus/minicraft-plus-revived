package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.StackableItem;

public class DeathChest extends Chest {
	private static LinkedSprite normalSprite = new LinkedSprite(SpriteType.Entity, "chest");
	private static LinkedSprite redSprite = new LinkedSprite(SpriteType.Entity, "red_chest");

	public int time; // Time passed (used for death chest despawn)
	private int redtick = 0; //This is used to determine the shade of red when the chest is about to expire.
	private boolean reverse; // What direction the red shade (redtick) is changing.
	private Inventory inventory = new Inventory() {{
		unlimited = true;
	}}; // Implement the inventory locally instead.

	/**
	 * Creates a custom chest with the name Death Chest
	 */
	public DeathChest() {
		super("Death Chest", new LinkedSprite(SpriteType.Item, "dungeon_chest"));
		this.sprite = normalSprite;

		/// Set the expiration time based on the world difficulty.
		if (Settings.get("diff").equals("minicraft.settings.difficulty.easy")) {
			time = 450 * Updater.normSpeed;
		} else if (Settings.get("diff").equals("minicraft.settings.difficulty.normal")) {
			time = 300 * Updater.normSpeed;
		} else if (Settings.get("diff").equals("minicraft.settings.difficulty.hard")) {
			time = 150 * Updater.normSpeed;
		}
	}

	public DeathChest(Player player) {
		this();
		this.x = player.x;
		this.y = player.y;
		for (Item i : player.getInventory().getItems()) {
			inventory.add(i.copy());
		}
	}

	// For death chest time count, I imagine.
	@Override
	public void tick() {
		super.tick();
		//name = "Death Chest:"; // add the current

		if (inventory.invSize() == 0) {
			remove();
		}

		if (time < 30 * Updater.normSpeed) { // If there is less than 30 seconds left...
			redtick += reverse ? -1 : 1; // inc/dec-rement redtick, changing the red shading.

			/// These two statements keep the red color oscillating.
			if (redtick > 13) {
				reverse = true;
				this.sprite = normalSprite;
			}
			if (redtick < 0) {
				reverse = false;
				this.sprite = redSprite;
			}
		}

		if (time > 0) {
			time--; // Decrement the time if it is not already zero.
		}

		if (time == 0) {
			die(); // Remove the death chest when the time expires, spilling all the contents.
		}
	}

	public void render(Screen screen) {
		super.render(screen);
		String timeString = (time / Updater.normSpeed) + "S";
		Font.draw(timeString, screen, x - Font.textWidth(timeString) / 2, y - Font.textHeight() - getBounds().getHeight() / 2, Color.WHITE);
	}

	public boolean use(Player player) {
		return false;
	} // can't open it, just walk into it.

	public void take(Player player) {
	} // can't grab a death chest.

	@Override
	public void touchedBy(Entity other) {
		if (other instanceof Player) {
			Inventory playerInv = ((Player) other).getInventory();
			for (Item i : inventory.getItems()) {
				if (playerInv.add(i) != null) {
					Game.notifications.add("Your inventory is full!");
					return;
				}

				inventory.removeItem(i);
			}

			remove();
			Game.notifications.add(Localization.getLocalized("minicraft.notification.death_chest_retrieved"));
		}
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}
}
