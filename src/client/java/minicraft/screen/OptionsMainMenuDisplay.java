package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.FileHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.saveload.Save;
import minicraft.screen.entry.SelectEntry;

import java.util.ArrayList;

public class OptionsMainMenuDisplay extends Display {
	private final boolean prevHwaValue = (boolean) Settings.get("hwa");
	private final String origUpdateCheckVal = (String) Settings.get("updatecheck");

	public OptionsMainMenuDisplay() {
		super(true, new Menu.Builder(false, 6, RelPos.CENTER,
			Settings.getEntry("fps"),
			Settings.getEntry("sound"),
			Settings.getEntry("showquests"),
			Settings.getEntry("hwa"),
			new SelectEntry(Localization.getStaticDisplay("minicraft.display.options_display.change_key_bindings"),
				() -> Game.setDisplay(new ControlsSettingsDisplay())),
			new SelectEntry(Localization.getStaticDisplay("minicraft.display.options_display.language"),
				() -> Game.setDisplay(new LanguageSettingsDisplay())),
			Settings.getEntry("updatecheck"),
			new SelectEntry(Localization.getStaticDisplay("minicraft.display.options_display.resource_packs"),
				() -> Game.setDisplay(new ResourcePackDisplay()))
		)
			.setTitle(Localization.getStaticDisplay("minicraft.displays.options_main_menu"))
			.createMenu());
	}

	@Override
	public void tick(InputHandler input) {
		if (!prevHwaValue && (boolean) Settings.get("hwa") && FileHandler.OS.contains("windows") && input.inputPressed("EXIT")) {
			ArrayList<PopupDisplay.PopupActionCallback> callbacks = new ArrayList<>();
			callbacks.add(new PopupDisplay.PopupActionCallback("SELECT", m -> {
				Game.exitDisplay(2);
				return true;
			}));
			Game.setDisplay(new PopupDisplay(new PopupDisplay.PopupConfig(
				Localization.getStaticDisplay(
					"minicraft.display.options_display.popup.hwa_warning.title"), callbacks, 2),
				"minicraft.display.options_display.popup.hwa_warning.content",
				"minicraft.display.popup.escape_cancel", "minicraft.display.popup.enter_confirm"));
			return;
		}

		super.tick(input);
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
