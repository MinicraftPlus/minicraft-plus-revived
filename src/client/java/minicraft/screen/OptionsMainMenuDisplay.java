package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.saveload.Save;
import minicraft.screen.entry.SelectEntry;

public class OptionsMainMenuDisplay extends Display {
	private final String origUpdateCheckVal = (String) Settings.get("updatecheck");

	public OptionsMainMenuDisplay() {
		super(true, new Menu.Builder(false, 6, RelPos.CENTER,
			Settings.getEntry("fps"),
			Settings.getEntry("sound"),
			Settings.getEntry("showquests"),
			new SelectEntry(new Localization.LocalizationString("minicraft.display.options_display.change_key_bindings"),
				() -> Game.setDisplay(new ControlsSettingsDisplay())),
			new SelectEntry(new Localization.LocalizationString("minicraft.display.options_display.language"),
				() -> Game.setDisplay(new LanguageSettingsDisplay())),
			Settings.getEntry("screenshot"),
			Settings.getEntry("updatecheck"),
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
		if (!origUpdateCheckVal.equals(Settings.get("updatecheck"))) {
			Game.updateHandler.checkForUpdate();
		}
	}
}
