package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.SelectableStringEntry;

import java.util.concurrent.atomic.AtomicBoolean;

public class OptionsMainMenuDisplay extends Display {
	private static ListEntry getCheckerEntry() {
		String origVal = (String) Settings.get("updatecheck");
		AtomicBoolean diff = new AtomicBoolean(false);
		AtomicBoolean changed = new AtomicBoolean(false);
		AtomicBoolean checking = new AtomicBoolean(false);
		Localization.LocalizationString defaultText = new Localization.LocalizationString(
			"minicraft.displays.options_main_menu.check_for_update");
		SelectableStringEntry entry = new SelectableStringEntry(defaultText, Color.DARK_GRAY) {
			int colorSelected = Color.DARK_GRAY, colorUnselected = Color.DIMMED_DARK_GRAY;

			@Override
			public void tick(InputHandler input) {
				super.tick(input);
				if (changed.get()) {
					changed.set(false);
					if (checking.get()) {
						checking.set(false);
						setText(defaultText);
						colorSelected = Color.DARK_GRAY;
						colorUnselected = Color.DIMMED_DARK_GRAY;
					}
				}

				if (checking.get()) {
					setText(new Localization.LocalizationString(false, Game.updateHandler.getStatusMessage()));
					colorSelected = Game.updateHandler.getStatusMessageColor();
					colorUnselected = Color.tint(colorSelected, -1, true);
				}

				if (input.inputPressed("SELECT")) {
					Game.updateHandler.checkForUpdate();
					checking.set(true);
				}
			}

			@Override
			public int getColor(boolean isSelected) {
				return isSelected ? colorSelected : colorUnselected;
			}
		};
		Settings.getEntry("updatecheck").setChangeAction(o -> {
			if (!diff.get()) diff.set(!origVal.equals(o));
			boolean disabled = o.equals("minicraft.settings.update_check.disabled") || !diff.get();
			entry.setSelectable(!disabled);
			entry.setVisible(!disabled);
			changed.set(true);
		});
		return entry;
	}

	public OptionsMainMenuDisplay() {
		super(true, new Menu.Builder(false, 6, RelPos.LEFT,
			Settings.getEntry("fps"),
			Settings.getEntry("sound"),
			Settings.getEntry("showquests"),
			new SelectEntry(new Localization.LocalizationString("minicraft.display.options_display.change_key_bindings"),
				() -> Game.setDisplay(new KeyInputDisplay())),
			new SelectEntry(new Localization.LocalizationString("minicraft.display.options_display.language"),
				() -> Game.setDisplay(new LanguageSettingsDisplay())),
			Settings.getEntry("screenshot"),
			Settings.getEntry("updatecheck"),
			getCheckerEntry(),
			new SelectEntry(new Localization.LocalizationString("minicraft.display.options_display.resource_packs"),
				() -> Game.setDisplay(new ResourcePackDisplay()))
		)
			.setTitle(new Localization.LocalizationString("minicraft.displays.options_main_menu"))
			.createMenu());
	}

	@Override
	public void onExit() {
		new Save();
		Game.MAX_FPS = (int) Settings.get("fps");
		Settings.getEntry("updatecheck").setChangeAction(null);
	}
}
