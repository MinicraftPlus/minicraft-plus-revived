package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.furniture.KnightStatue;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.awt.Rectangle;
import java.util.ArrayList;

public class SummonItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new SummonItem("Totem of Air", new LinkedSprite(SpriteType.Item, "air_totem"), "Air Wizard"));
		items.add(new SummonItem("Obsidian Poppet", new LinkedSprite(SpriteType.Item, "knight_statue"), "Obsidian Knight")); //TODO: Obsidian Poppet Textures

		return items;
	}

	private final String mob;

	private SummonItem(String name, LinkedSprite sprite, String mob) {
		this(name, sprite, 1, mob);
	}

	private SummonItem(String name, LinkedSprite sprite, int count, String mob) {
		super(name, sprite, count);
		this.mob = mob;
	}

	/**
	 * What happens when the player uses the item on a tile
	 */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;

		switch (mob) {
			case "Air Wizard":
				// Check if we are on the right level
				if (level.depth == 1) {
					if (!AirWizard.active) {

						// Pay stamina
						if (player.payStamina(2)) {
							AirWizard aw = new AirWizard();
							level.add(aw, player.x + 8, player.y + 8, false);
							Logger.tag("SummonItem").debug("Summoned new Air Wizard");
							success = true;
						}
					} else {
						Game.notifications.add(Localization.getLocalized("minicraft.notification.boss_limit"));
					}
				} else {
					Game.notifications.add(Localization.getLocalized("minicraft.notification.wrong_level_sky"));
				}

				break;
			case "Obsidian Knight":
				// Check if we are on the right level and tile
				if (level.depth == -4) {
					// If the player nears the center.
					if (new Rectangle(level.w / 2 - 3, level.h / 2 - 3, 7, 7).contains(player.x >> 4, player.y >> 4)) {
						if (!ObsidianKnight.active) {
							boolean exists = false;
							for (Entity e : level.getEntityArray()) {
								if (e instanceof KnightStatue) {
									exists = true;
									break;
								}
							}

							if (!exists) { // Prevent unintended behaviors
								// Pay stamina
								if (player.payStamina(2)) {
									level.add(new KnightStatue(5000), level.w / 2, level.h / 2, true);
									Logger.tag("SummonItem").debug("Summoned new Knight Statue");
									success = true;
								}
							} else {
								Game.notifications.add(Localization.getLocalized("minicraft.notification.knight_statue_exists"));
							}
						} else {
							Game.notifications.add(Localization.getLocalized("minicraft.notification.boss_limit"));
						}
					} else {
						Game.notifications.add(Localization.getLocalized("minicraft.notification.spawn_on_boss_tile"));
					}
				} else {
					Game.notifications.add(Localization.getLocalized("minicraft.notification.wrong_level_dungeon"));
				}
				break;
			default:
				Logger.tag("SummonItem").warn("Could not create SummonItem with mob, {}", mob);
				break;
		}

		return super.interactOn(success);
	}

	@Override
	public boolean interactsWithWorld() {
		return false;
	}

	public @NotNull SummonItem copy() {
		return new SummonItem(getName(), sprite, count, mob);
	}
}
