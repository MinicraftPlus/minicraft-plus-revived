package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.saveload.Save;
import minicraft.screen.entry.BooleanEntry;
import minicraft.screen.entry.SelectEntry;

public class OptionsMainMenuDisplay extends Display {
	private final BooleanEntry controllersEntry = new BooleanEntry("minicraft.display.options_display.controller",
		Game.input.isControllerEnabled());

	public OptionsMainMenuDisplay() {
		super(true);
	    menus = new Menu[] {
		    new Menu.Builder(false, 6, RelPos.LEFT,
			    Settings.getEntry("fps"),
			    Settings.getEntry("sound"),
			    Settings.getEntry("showquests"),
			    new SelectEntry("minicraft.display.options_display.change_key_bindings", () -> Game.setDisplay(new KeyInputDisplay())),
			    new SelectEntry("minicraft.displays.controls", () -> Game.setDisplay(new ControlsDisplay())),
			    new SelectEntry("minicraft.display.options_display.language", () -> Game.setDisplay(new LanguageSettingsDisplay())),
			    Settings.getEntry("screenshot"),
			    controllersEntry,
			    new SelectEntry("minicraft.display.options_display.resource_packs", () -> Game.setDisplay(new ResourcePackDisplay()))
		    )
			    .setTitle("minicraft.displays.options_main_menu")
			    .createMenu()
	    };
    }

    @Override
    public void onExit() {
        new Save();
        Game.MAX_FPS = (int) Settings.get("fps");
	    Game.input.setControllerEnabled(controllersEntry.getValue());
    }
}
