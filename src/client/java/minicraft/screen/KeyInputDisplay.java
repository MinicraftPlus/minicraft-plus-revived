package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.KeyInputEntry;
import minicraft.screen.entry.StringEntry;

import java.util.ArrayList;
import java.util.HashSet;

public class KeyInputDisplay extends Display {
	private static Menu.Builder builder;

	private static KeyInputEntry[] getEntries() {
		String[] prefs = Game.input.getKeyPrefs();
		KeyInputEntry[] entries = new KeyInputEntry[prefs.length];
		HashSet<String> duplicated = getDuplicatedMappings();

		for (int i = 0; i < entries.length; i++)
			entries[i] = new KeyInputEntry(prefs[i], duplicated);

		return entries;
	}

	private static HashSet<String> getDuplicatedMappings() {
		HashSet<String> existedMappings = new HashSet<>();
		HashSet<String> duplicated = new HashSet<>();
		for (String pref : Game.input.getKeyPrefs()) {
			String[] mappings = pref.substring(pref.indexOf(";") + 1).split("\\|");
			for (String mapping : mappings) {
				if (existedMappings.contains(mapping)) {
					duplicated.add(mapping);
				} else {
					existedMappings.add(mapping);
				}
			}
		}

		return duplicated;
	}

	public KeyInputDisplay() {
		super(true);
		builder = new Menu.Builder(false, 0, RelPos.CENTER, getEntries())
			.setTitle("minicraft.displays.key_input.title")
			.setPositioning(new Point(Screen.w / 2, Screen.h - Font.textHeight() * 5), RelPos.TOP);

		menus = new Menu[] {
			builder.createMenu()
		};
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input); // ticks menu

		if (input.keyToChange != null) {
			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback(null, popup -> {
				if (input.keyToChange == null) {
					// the key has just been set, refreshes key bindings.
					menus[0] = builder.setEntries(getEntries())
						.setSelection(menus[0].getSelection(), menus[0].getDispSelection())
						.createMenu();
					Game.exitDisplay();
					return true;
				}

				return false;
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, callbacks, 4), StringEntry.useLines(Color.YELLOW,
				"minicraft.displays.key_input.popup_display.press_key_sequence")));
		} else if (input.getMappedKey("shift-d").isClicked()) {
			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
				input.resetKeyBindings();
				menus[0] = builder.setEntries(getEntries())
					.setSelection(menus[0].getSelection(), menus[0].getDispSelection())
					.createMenu();
				Game.exitDisplay();
				return true;
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig("minicraft.display.popup.title_confirm", callbacks, 4), StringEntry.useLines(Color.RED,
				"minicraft.displays.key_input.popup_display.confirm_reset", "minicraft.display.popup.enter_confirm", "minicraft.display.popup.escape_cancel")));
		}
	}

	public void render(Screen screen) {
		screen.clear(0);
		super.render(screen);

		String[] lines = {
			Localization.getLocalized("minicraft.displays.key_input.display.help.0"),
			Localization.getLocalized("minicraft.displays.key_input.display.help.1"),
			Localization.getLocalized("minicraft.displays.key_input.display.help.2"),
			Localization.getLocalized("minicraft.displays.key_input.display.help.3", Game.input.getMapping("exit"))
		};

		for (int i = 0; i < lines.length; i++)
			Font.drawCentered(lines[i], screen, Screen.h - Font.textHeight() * (4 - i), Color.WHITE);
	}
}
