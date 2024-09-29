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
import minicraft.screen.entry.ControlSettingEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControlsSettingsDisplay extends Display {
	@RegExp
	private static final String regexPrintable = "\\p{Print}";

	private static ControlSettingEntry[] getEntries() {
		String[] prefs = Game.input.getKeyPrefs();
		ControlSettingEntry[] entries = new ControlSettingEntry[prefs.length];
		HashMap<Set<String>, HashSet<String>> duplicated = getDuplicatedMappings();

		for (int i = 0; i < entries.length; i++) {
			String k = prefs[i].substring(0, prefs[i].indexOf(';'));
			entries[i] = new ControlSettingEntry(prefs[i], duplicated.entrySet().stream()
				.filter(e -> e.getKey().contains(k)).flatMap(e -> e.getValue().stream()).collect(Collectors.toSet()));
		}

		return entries;
	}

	private static final Set<Set<String>> INTERACTING_CONTROL_GROUPS = Stream.of(
		// In App // CURSOR should be fixed and immutable, right?
		Stream.of("CURSOR-UP", "CURSOR-DOWN", "CURSOR-LEFT", "CURSOR-RIGHT", "SELECT", "EXIT",
			"SEARCHER-BAR", "PAGE-UP", "PAGE-DOWN", "SCREENSHOT", "FULLSCREEN").collect(Collectors.toSet()),
		// During gameplay
		Stream.of("MOVE-UP", "MOVE-DOWN", "MOVE-LEFT", "MOVE-RIGHT", "QUICKSAVE", "ATTACK", "MENU",
			"CRAFT", "PICKUP", "DROP-ONE", "DROP-STACK", "PAUSE", "POTIONEFFECTS", "SIMPPOTIONEFFECTS",
			"EXPANDQUESTDISPLAY", "TOGGLEHUD", "SCREENSHOT", "INFO", "FULLSCREEN").collect(Collectors.toSet()),
		// Menus during gameplay
		Stream.of("CURSOR-UP", "CURSOR-DOWN", "CURSOR-LEFT", "CURSOR-RIGHT", "SELECT", "EXIT", "MENU",
			"CRAFT", "DROP-ONE", "DROP-STACK", "SEARCHER-BAR", "PAGE-UP", "PAGE-DOWN", "SCREENSHOT", "FULLSCREEN")
			.collect(Collectors.toSet())
	).collect(Collectors.toSet());

	private static HashMap<Set<String>, HashSet<String>> getDuplicatedMappings() {
		HashMap<Set<String>, HashSet<String>> duplicatedMappings = new HashMap<>();
		for (Set<String> group : INTERACTING_CONTROL_GROUPS) {
			HashSet<String> existedMappings = new HashSet<>();
			HashSet<String> duplicated = new HashSet<>();
			for (String key : group) {
				String[] mappings = Game.input.getMapping(key).split("/");
				for (String mapping : mappings) {
					if (existedMappings.contains(mapping)) {
						duplicated.add(mapping);
					} else {
						existedMappings.add(mapping);
					}
				}
			}

			duplicatedMappings.put(group, duplicated);
		}

		return duplicatedMappings;
	}

	public ControlsSettingsDisplay() {
		super(true, new Menu.Builder(false, 0, RelPos.CENTER, getEntries())
			.setTitle(Localization.getStaticDisplay("minicraft.displays.controls_settings.title"))
			.setPositioning(new Point(Screen.w / 2, Screen.h - Font.textHeight() * 5), RelPos.TOP)
			.createMenu());
	}

	@NotNull
	private ArrayList<String> getAnyTroubles() {
		ArrayList<String> troubles = new ArrayList<>();
		for (String pref : Game.input.getKeyPrefs()) {
			if (pref.startsWith("CURSOR-")) {
				String[] mappings = pref.substring(pref.indexOf(";") + 1).split("\\|");
				if (Arrays.stream(mappings).anyMatch(k -> k.matches(regexPrintable)))
					troubles.add(pref.substring(0, pref.indexOf(";")));
			}
		}
		return troubles;
	}

	@Override
	public void tick(InputHandler input) {
		if (input.getMappedKey("EXIT").isClicked()) {
			ArrayList<String> troubles = getAnyTroubles();
			if (!troubles.isEmpty()) {
				ArrayList<ListEntry> entries = new ArrayList<>();
				Collections.addAll(entries, StringEntry.useLines("minicraft.displays.controls_settings.troublesome_input.confirm.msg.intro"));
				Collections.addAll(entries, StringEntry.useLines(Color.WHITE, false, String.join(", ", troubles)));
				Collections.addAll(entries, StringEntry.useLines("minicraft.displays.controls_settings.troublesome_input.confirm.msg.conclusion"));
				entries.add(new BlankEntry());
				entries.add(new StringEntry(Localization.getStaticDisplay("minicraft.display.popup.enter_confirm")));
				entries.add(new StringEntry(Localization.getStaticDisplay("minicraft.display.popup.escape_cancel")));

				ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
				callbacks.add(new PopupDisplay.PopupActionCallback("ENTER", m -> {
					Game.exitDisplay(2);
					return true;
				}));

				Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(
					Localization.getStaticDisplay("minicraft.displays.controls_settings.troublesome_input.confirm.title"),
					callbacks, 2),
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
						menus[0].setEntries(getEntries());
						Game.exitDisplay();
					};
					String[] splitStr = input.getChangedKey().split(";", 2);
					String[] keyStr = splitStr[1].split("\\|");
					if (splitStr[0].startsWith("CURSOR-") && Arrays.stream(keyStr).anyMatch(k -> k.matches(regexPrintable))) {
						ArrayList<ListEntry> entries = new ArrayList<>();
						Collections.addAll(entries, StringEntry.useLines("minicraft.displays.controls_settings.troublesome_input.warning.msg"));
						entries.add(new BlankEntry());
						entries.add(new StringEntry(Localization.getStaticDisplay(
							"minicraft.displays.controls_settings.troublesome_input.warning.remove", "ENTER")));
						entries.add(new StringEntry(Localization.getStaticDisplay(
							"minicraft.displays.controls_settings.troublesome_input.warning.return", "ESCAPE")));

						ArrayList<PopupDisplay.PopupActionCallback> callbacks1 = new ArrayList<>();
						callbacks1.add(new PopupDisplay.PopupActionCallback("ENTER", m -> {
							input.setKey(splitStr[0],
								Arrays.stream(keyStr).filter(k -> !k.matches(regexPrintable)).collect(Collectors.joining("|")));
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
							Localization.getStaticDisplay(
								"minicraft.displays.controls_settings.troublesome_input.warning.title"),
							callbacks1, 2),
							entries.toArray(new ListEntry[0])));
						return true;
					}

					action.act();
					return true;
				}

				return false;
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(null, callbacks, 4), StringEntry.useLines(Color.YELLOW,
				"minicraft.displays.controls_settings.popup_display.press_key_sequence")));
		} else if (input.getMappedKey("shift-d").isClicked()) {
			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback("select", popup -> {
				input.resetKeyBindings();
				menus[0].setEntries(getEntries());
				Game.exitDisplay();
				return true;
			}));

			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(Localization.getStaticDisplay(
				"minicraft.display.popup.title_confirm"), callbacks, 4), StringEntry.useLines(Color.RED,
				"minicraft.displays.controls_settings.popup_display.confirm_reset", "minicraft.display.popup.enter_confirm", "minicraft.display.popup.escape_cancel")));
		}
	}

	public void render(Screen screen) {
		screen.clear(0);
		super.render(screen);

		String[] lines = {
			Localization.getLocalized("minicraft.displays.controls_settings.display.help.0"),
			Localization.getLocalized("minicraft.displays.controls_settings.display.help.1"),
			Localization.getLocalized("minicraft.displays.controls_settings.display.help.2"),
			Localization.getLocalized("minicraft.displays.controls_settings.display.help.3", Game.input.getMapping("exit"))
		};

		for (int i = 0; i < lines.length; i++)
			Font.drawCentered(lines[i], screen, Screen.h - Font.textHeight() * (4 - i), Color.GRAY);
	}
}
