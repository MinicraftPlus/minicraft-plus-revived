package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.saveload.Save;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PauseDisplay extends Display {

	public PauseDisplay() {
		ArrayList<ListEntry> entries = new ArrayList<>(Arrays.asList(
			new BlankEntry(),
			new SelectEntry(new Localization.LocalizationString("minicraft.displays.pause.return"),
				() -> Game.setDisplay(null)),
			new SelectEntry(new Localization.LocalizationString("minicraft.display.options_display"),
				() -> Game.setDisplay(new OptionsWorldDisplay())),
			new SelectEntry(new Localization.LocalizationString("minicraft.displays.achievements"),
				() -> Game.setDisplay(new AchievementsDisplay()))
		));

		if (TutorialDisplayHandler.inQuests())
			entries.add(new SelectEntry(new Localization.LocalizationString("minicraft.displays.quests"),
				() -> Game.setDisplay(new QuestsDisplay())));

		entries.add(new SelectEntry(new Localization.LocalizationString("minicraft.displays.pause.save"), () -> {
			Game.setDisplay(null);
			new Save(WorldSelectDisplay.getWorldName());
		}));

		entries.add(new SelectEntry(new Localization.LocalizationString("minicraft.displays.pause.menu"), () -> {
			ArrayList<ListEntry> items = new ArrayList<>(Arrays.asList(StringEntry.useLines("minicraft.displays.pause.display.exit_popup.0")));

			items.addAll(Arrays.asList(StringEntry.useLines(Color.RED, "minicraft.displays.pause.display.exit_popup.1")));
			items.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "minicraft.displays.pause.display.exit_popup.2")));
			items.add(new BlankEntry());
			items.add(new SelectEntry(new Localization.LocalizationString(
				"minicraft.displays.pause.display.exit_popup.cancel"), Game::exitDisplay));
			items.add(new SelectEntry(new Localization.LocalizationString(
				"minicraft.displays.pause.display.exit_popup.quit"), () -> {
				Game.setDisplay(new TitleDisplay());
				World.onWorldExits();
			}));
			items.add(new SelectEntry(new Localization.LocalizationString(
				"minicraft.displays.pause.display.exit_popup.save"), () -> {
				new Save(WorldSelectDisplay.getWorldName());
				Game.setDisplay(new TitleDisplay());
				World.onWorldExits();
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, null, 4), items.toArray(new ListEntry[0])));
		}));

		menus = new Menu[] {
			new Menu.Builder(true, 4, RelPos.CENTER, entries)
				.setTitle(new Localization.LocalizationString("minicraft.displays.pause"), Color.YELLOW)
				.createMenu()
		};
	}

	@Override
	public void init(Display parent) {
		super.init(null); // ignore; pause menus always lead back to the game
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		if (input.inputPressed("EXIT"))
			Game.exitDisplay();
	}
}
