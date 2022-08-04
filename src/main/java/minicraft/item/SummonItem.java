package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import org.tinylog.Logger;

import java.util.ArrayList;

public class SummonItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new SummonItem("Totem of Air", new LinkedSpriteSheet(SpriteType.Item, "air_totem"), "Air Wizard"));

		return items;
	}

	private final String mob;

	private SummonItem(String name, LinkedSpriteSheet sprite, String mob) { this(name, sprite, 1, mob); }
	private SummonItem(String name, LinkedSpriteSheet sprite, int count, String mob) {
		super(name, sprite, count);
		this.mob = mob;
	}

	/** What happens when the player uses the item on a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;

		switch (mob) {
			case "Air Wizard":
				// Check if we are on the right level
				if (level.depth == 1) {

					// Pay stamina
					if (player.payStamina(2)) {
						AirWizard aw = new AirWizard();
						level.add(aw, player.x + 8, player.y + 8, false);
						Logger.tag("SummonItem").debug("Summoned new Air Wizard");
						success = true;
					}
				} else {
					Game.notifications.add(Localization.getLocalized("minicraft.notification.wrong_level_sky"));
				}

				break;
			default:
				Logger.tag("SummonItem").warn("Could not create SummonItem with mob, {}", mob);
				break;
		}

		return super.interactOn(success);
	}

	@Override
	public boolean interactsWithWorld() { return false; }

	public SummonItem clone() {
		return new SummonItem(getName(), sprite, count, mob);
	}
}
