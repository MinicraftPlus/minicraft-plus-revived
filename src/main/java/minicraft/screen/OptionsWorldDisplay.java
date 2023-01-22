package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.core.io.Localization.LocaleInformation;
import minicraft.gfx.Color;
import minicraft.saveload.Save;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionsWorldDisplay extends Display {
	private boolean confirmOff = false;

	public OptionsWorldDisplay() {
		super(true);

		List<ListEntry> entries = getEntries();

		if ((boolean) Settings.get("tutorials")) {
			entries.add(new BlankEntry());
			entries.add(new SelectEntry("minicraft.displays.options_world.turn_off_tutorials", () -> {
				confirmOff = true;
				selection = 1;
				menus[selection].shouldRender = true;
			}));
		}

		menus = new Menu[] {
			new Menu.Builder(false, 6, RelPos.LEFT, entries)
					.setTitle("minicraft.displays.options_world")
					.createMenu(),
			new Menu.Builder(true, 4, RelPos.CENTER)
				.setShouldRender(false)
				.setSelectable(false)
				.setEntries(StringEntry.useLines(Color.RED, "minicraft.displays.options_world.off_tutorials_confirm_popup",
					"minicraft.display.popup.enter_confirm", "minicraft.display.popup.escape_cancel"))
				.setTitle("minicraft.display.popup.title_confirm")
				.createMenu()
		};
	}

	@Override
	public void tick(InputHandler input) {
		if (confirmOff) {
			if (input.getKey("exit").clicked) {
				confirmOff = false;
				menus[1].shouldRender = false;
				selection = 0;
			} else if (input.getKey("select").clicked) {
				confirmOff = false;
				QuestsDisplay.tutorialOff();

				menus[1].shouldRender = false;
				menus[0].setEntries(getEntries());
				menus[0].setSelection(0);
				selection = 0;
			}

			return;
		}

		super.tick(input); // ticks menu
	}

	private List<ListEntry> getEntries() {
		return new ArrayList<>(Arrays.asList(Settings.getEntry("diff"),
			Settings.getEntry("fps"),
			Settings.getEntry("sound"),
			Settings.getEntry("autosave"),
			Settings.getEntry("showquests"),
			new SelectEntry("minicraft.display.options_display.change_key_bindings", () -> Game.setDisplay(new KeyInputDisplay())),
			Settings.getEntry("language"),
			Settings.getEntry("screenshot"),
			new SelectEntry("minicraft.displays.options_main_menu.resource_packs", () -> Game.setDisplay(new ResourcePackDisplay()))
		));
	}

	@Override
	public void onExit() {
		Localization.changeLanguage(((LocaleInformation)Settings.get("language")).locale.toLanguageTag());
		new Save();
		Game.MAX_FPS = Settings.getFPS();
	}
}
