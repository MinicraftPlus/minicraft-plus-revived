package minicraft.screen;

import minicraft.core.Action;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.KeyInputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

public class KeyInputDisplay extends Display {
	@RegExp
	private static final String regexLetter = "^\\w$";
	private final Menu.Builder builder;

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

		menus = new Menu[]{
			builder.createMenu()
		};
	}

	@NotNull
	private ArrayList<String> getAnyTroubles() {
		ArrayList<String> troubles = new ArrayList<>();
		for (String pref : Game.input.getKeyPrefs()) {
			String[] mappings = pref.substring(pref.indexOf(";") + 1).split("\\|");
			if (Arrays.stream(mappings).anyMatch(k -> k.matches(regexLetter)))
				troubles.add(pref.substring(0, pref.indexOf(";")));
		}
		return troubles;
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getKey("EXIT").clicked) {
			ArrayList<String> troubles = getAnyTroubles();
			if (!troubles.isEmpty()) {
				ArrayList<ListEntry> entries = new ArrayList<>();
				Collections.addAll(entries, StringEntry.useLines("minicraft.displays.key_input.troublesome_input.confirm.msg.intro"));
				Collections.addAll(entries, StringEntry.useLines(Color.WHITE, false, String.join(", ", troubles)));
				Collections.addAll(entries, StringEntry.useLines("minicraft.displays.key_input.troublesome_input.confirm.msg.conclusion"));
				entries.add(new BlankEntry());
				entries.add(new StringEntry("minicraft.display.popup.enter_confirm"));
				entries.add(new StringEntry("minicraft.display.popup.escape_cancel"));

				ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
				callbacks.add(new PopupDisplay.PopupActionCallback("ENTER", m -> {
					Game.exitDisplay(2);
					return true;
				}));

				Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(
					"minicraft.displays.key_input.troublesome_input.confirm.title", callbacks, 2),
					entries.toArray(new ListEntry[0])));
				return;
			}
		}

		super.tick(input); // ticks menu

		if (input.keyToChange != null) {
			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback(null, popup -> {
				if (input.keyToChange == null) {
					Action action = () -> {
						// the key has just been set, refreshes key bindings.
						menus[0] = builder.setEntries(getEntries())
							.setSelection(menus[0].getSelection(), menus[0].getDispSelection())
							.createMenu();
						Game.exitDisplay();
					};
					String[] splitStr = input.getChangedKey().split(";", 2);
					String[] keyStr = splitStr[1].split("\\|");
					if (splitStr[0].startsWith("CURSOR-") && Arrays.stream(keyStr).anyMatch(k -> k.matches(regexLetter))) {
						ArrayList<ListEntry> entries = new ArrayList<>();
						Collections.addAll(entries, StringEntry.useLines("minicraft.displays.key_input.troublesome_input.warning.msg"));
						entries.add(new BlankEntry());
						entries.add(new StringEntry(Localization.getLocalized(
							"minicraft.displays.key_input.troublesome_input.warning.remove", "ENTER"), false));
						entries.add(new StringEntry(Localization.getLocalized(
							"minicraft.displays.key_input.troublesome_input.warning.return", "ESCAPE"), false));

						ArrayList<PopupDisplay.PopupActionCallback> callbacks1 = new ArrayList<>();
						callbacks1.add(new PopupDisplay.PopupActionCallback("ENTER", m -> {
							input.setKey(splitStr[0],
								Arrays.stream(keyStr).filter(k -> !k.matches(regexLetter)).collect(Collectors.joining("|")));
							Game.exitDisplay();
							action.act();
							return true;
						}));
						callbacks1.add(new PopupDisplay.PopupActionCallback("ESCAPE", m -> {
							Game.exitDisplay();
							action.act();
							return true;
						}));

						Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(
							"minicraft.displays.key_input.troublesome_input.warning.title", callbacks1, 2),
							entries.toArray(new ListEntry[0])));
						return true;
					}

					action.act();
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
