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

public class PauseDisplay extends Display {

	public PauseDisplay() {
		String upString = Localization.getLocalized("minicraft.displays.pause.display.help.scroll", Game.input.getMapping("cursor-up"), Game.input.getMapping("cursor-down"));
		String selectString = Localization.getLocalized("minicraft.displays.pause.display.help.choose", Game.input.getMapping("select"));

		ArrayList<ListEntry> entries = new ArrayList<>(Arrays.asList(
			new BlankEntry(),
			new SelectEntry("minicraft.displays.pause.return", () -> Game.setDisplay(null)),
			new SelectEntry("minicraft.display.options_display", () -> Game.setDisplay(new OptionsWorldDisplay())),
			new SelectEntry("minicraft.displays.achievements", () -> Game.setDisplay(new AchievementsDisplay()))
		));

		if (TutorialDisplayHandler.inQuests())
			entries.add(new SelectEntry("minicraft.displays.quests", () -> Game.setDisplay(new QuestsDisplay())));

		entries.add(new SelectEntry("minicraft.displays.pause.save", () -> {
			Game.setDisplay(null);
			new Save(WorldSelectDisplay.getWorldName());
		}));

		entries.addAll(Arrays.asList(
			new SelectEntry("minicraft.displays.pause.menu", () -> {
				ArrayList<ListEntry> items = new ArrayList<>(Arrays.asList(StringEntry.useLines("minicraft.displays.pause.display.exit_popup.0")));

				items.addAll(Arrays.asList(StringEntry.useLines(Color.RED, "minicraft.displays.pause.display.exit_popup.1")));
				items.add(new BlankEntry());
				items.add(new SelectEntry("minicraft.displays.pause.display.exit_popup.cancel", Game::exitDisplay));
				items.add(new SelectEntry("minicraft.displays.pause.display.exit_popup.quit", () -> {
					Game.setDisplay(new TitleDisplay());
					World.onWorldExits();
				}));

				Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, null, 8), items.toArray(new ListEntry[0])));
			}),

			new BlankEntry(),

			new StringEntry(upString, Color.GRAY, false),
			new StringEntry(selectString, Color.GRAY, false)
		));

		menus = new Menu[] {
			new Menu.Builder(true, 4, RelPos.CENTER, entries)
				.setTitle("minicraft.displays.pause", Color.YELLOW)
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
		if (input.inputPressed("pause"))
			Game.exitDisplay();
	}
}
