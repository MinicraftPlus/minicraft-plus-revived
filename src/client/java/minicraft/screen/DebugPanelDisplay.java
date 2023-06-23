package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.entity.mob.Player;
import minicraft.gfx.Point;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.util.Logging;

import java.util.ArrayList;
import java.util.List;

public class DebugPanelDisplay extends Display {
	public DebugPanelDisplay() {
		super(new Menu.Builder(true, 0, RelPos.LEFT, getEntries())
			.setPositioning(new Point(9, 9), RelPos.BOTTOM_RIGHT)
			.setDisplayLength(6)
			.setSelectable(true)
			.setScrollPolicies(1, false)
			.setSearcherBar(true)
			.setTitle("minicraft.display.debug_panel")
			.createMenu());
	}

	private static List<ListEntry> getEntries() {
		ArrayList<ListEntry> entries = new ArrayList<>();

		entries.add(new SelectEntry("Print all players", () -> {
			// Print all players on all levels, and their coordinates.
			Logging.WORLD.info("Printing players on all levels.");
			for (Level value : Game.levels) {
				if (value == null) continue;
				value.printEntityLocs(Player.class);
			}
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Time set morning", () -> {
			Updater.changeTimeOfDay(Updater.Time.Morning);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Time set day", () -> {
			Updater.changeTimeOfDay(Updater.Time.Day);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Time set evening", () -> {
			Updater.changeTimeOfDay(Updater.Time.Evening);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Time set night", () -> {
			Updater.changeTimeOfDay(Updater.Time.Night);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Gamemode creative", () -> {
			Settings.set("mode", "minicraft.settings.mode.creative");
			Logging.WORLDNAMED.trace("Game mode changed from {} into {}.", Settings.get("mode"), "minicraft.settings.mode.creative");
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Gamemode survival", () -> {
			Settings.set("mode", "minicraft.settings.mode.survival");
			Logging.WORLDNAMED.trace("Game mode changed from {} into {}.", Settings.get("mode"), "minicraft.settings.mode.survival");
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Gamemode score", () -> {
			Settings.set("mode", "minicraft.settings.mode.score");
			Logging.WORLDNAMED.trace("Game mode changed from {} into {}.", Settings.get("mode"), "minicraft.settings.mode.score");
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Reset score time as 5 seconds", () -> {
			if (Game.isMode("minicraft.settings.mode.score")) {
				Updater.scoreTime = Updater.normSpeed * 5; // 5 seconds
			}
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Reset tick speed (TPS)", () -> {
			float prevSpeed = Updater.gamespeed;
			Updater.gamespeed = 1;
			Logging.WORLDNAMED.trace("Tick speed reset from {} into 1.", prevSpeed);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Increase tick speed (TPS)", () -> {
			float prevSpeed = Updater.gamespeed;
			if (Updater.gamespeed < 1) Updater.gamespeed *= 2;
			else if (Updater.normSpeed * Updater.gamespeed < 2000) Updater.gamespeed++;
			Logging.WORLDNAMED.trace("Tick speed increased from {} into {}.", prevSpeed, Updater.gamespeed);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Decrease tick speed (TPS)", () -> {
			float prevSpeed = Updater.gamespeed;
			if (Updater.gamespeed > 1) Updater.gamespeed--;
			else if (Updater.normSpeed * Updater.gamespeed > 5) Updater.gamespeed /= 2;
			Logging.WORLDNAMED.trace("Tick speed decreased from {} into {}.", prevSpeed, Updater.gamespeed);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Reduce health point", () -> {
			Game.player.health--;
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Reduce hunger point", () -> {
			Game.player.hunger--;
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Reset moving speed", () -> {
			Game.player.moveSpeed = 1;
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Increase moving speed", () -> {
			Game.player.moveSpeed++;
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Decrease moving speed", () -> {
			if (Game.player.moveSpeed > 1) Game.player.moveSpeed--; // -= 0.5D;
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Set tile stairs up", () -> {
			Game.levels[Game.currentLevel].setTile(Game.player.x>>4, Game.player.y>>4, Tiles.get("Stairs Up"));
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Set tile stairs down", () -> {
			Game.levels[Game.currentLevel].setTile(Game.player.x>>4, Game.player.y>>4, Tiles.get("Stairs Down"));
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Change level down (instant)", () -> {
			Game.exitDisplay();
			Game.setDisplay(new LevelTransitionDisplay(-1));
		}, false));
		entries.add(new SelectEntry("Change level up (instant)", () -> {
			Game.exitDisplay();
			Game.setDisplay(new LevelTransitionDisplay(1));
		}, false));
		entries.add(new SelectEntry("Change level up", () -> {
			World.scheduleLevelChange(1);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Change level down", () -> {
			World.scheduleLevelChange(-1);
			Game.exitDisplay();
		}, false));
		entries.add(new SelectEntry("Remove all potion effects", () -> {
			for (PotionType potionType : Game.player.potioneffects.keySet()) {
				PotionItem.applyPotion(Game.player, potionType, false);
			}
			Game.exitDisplay();
		}, false));

		return entries;
	}
}
