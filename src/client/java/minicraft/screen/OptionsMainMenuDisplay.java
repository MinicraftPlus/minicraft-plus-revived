package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.FileHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.saveload.Save;
import minicraft.screen.entry.SelectEntry;

import java.util.ArrayList;

public class OptionsMainMenuDisplay extends Display {
	private final boolean prevHwaValue = (boolean) Settings.get("hwa");

	public OptionsMainMenuDisplay() {
		super(true, new Menu.Builder(false, 6, RelPos.LEFT,
			Settings.getEntry("fps"),
			Settings.getEntry("sound"),
			Settings.getEntry("showquests"),
			Settings.getEntry("hwa"),
			new SelectEntry("minicraft.display.options_display.change_key_bindings", () -> Game.setDisplay(new KeyInputDisplay())),
			new SelectEntry("minicraft.displays.controls", () -> Game.setDisplay(new ControlsDisplay())),
			new SelectEntry("minicraft.display.options_display.language", () -> Game.setDisplay(new LanguageSettingsDisplay())),
			new SelectEntry("minicraft.display.options_display.resource_packs", () -> Game.setDisplay(new ResourcePackDisplay()))
		)
			.setTitle("minicraft.displays.options_main_menu")
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
				"minicraft.display.options_display.popup.hwa_warning.title", callbacks, 2),
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
	}
}
