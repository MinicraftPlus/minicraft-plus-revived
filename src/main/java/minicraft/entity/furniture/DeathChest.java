package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.StackableItem;

public class DeathChest extends Chest {
	private static Sprite normalSprite = new Sprite(10, 26, 2, 2, 2);
	private static Sprite redSprite = new Sprite(10, 24, 2, 2, 2);

	public int time; // Time passed (used for death chest despawn)
	private int redtick = 0; //This is used to determine the shade of red when the chest is about to expire.
	private boolean reverse; // What direction the red shade (redtick) is changing.
	private Inventory inventory = new Inventory() {{ unlimited = true; }}; // Implement the inventory locally instead.

	/**
	 * Creates a custom chest with the name Death Chest
	 */
	public DeathChest() {
		super("Death Chest");
		this.sprite = normalSprite;

		/// Set the expiration time based on the world difficulty.
		if (Settings.get("diff").equals("minicraft.settings.difficulty.easy")) {
			time = 300*Updater.normSpeed;
		} else if (Settings.get("diff").equals("minicraft.settings.difficulty.normal")) {
			time = 120*Updater.normSpeed;
		} else if (Settings.get("diff").equals("minicraft.settings.difficulty.hard")) {
			time = 30*Updater.normSpeed;
		}
	}

	public DeathChest(Player player) {
		this();
		this.x = player.x;
		this.y = player.y;
		for (Item i : player.getInventory().getItems()) {
			inventory.add(i.clone());
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
		Font.draw(timeString, screen, x - Font.textWidth(timeString)/2, y - Font.textHeight() - getBounds().getHeight()/2, Color.WHITE);
	}

	public boolean use(Player player) { return false; } // can't open it, just walk into it.

	public void take(Player player) {} // can't grab a death chest.

	@Override
	public void touchedBy(Entity other) {
		if(other instanceof Player) {
			Inventory playerInv = ((Player)other).getInventory();
			for (Item i : inventory.getItems()) {
				int total = 1;
				if (i instanceof StackableItem) total = ((StackableItem)i).count;

				int returned = playerInv.add(i);
				if (returned < total) {
					Game.notifications.add("Your inventory is full!");
					return;
				}

				inventory.removeItem(i);
			}

			remove();
			Game.notifications.add("Death chest retrieved!");
		}
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}
}
