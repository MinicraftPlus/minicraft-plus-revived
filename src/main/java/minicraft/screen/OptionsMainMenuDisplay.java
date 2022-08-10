package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.core.io.Localization.LocaleInformation;
import minicraft.saveload.Save;
import minicraft.screen.entry.SelectEntry;

public class OptionsMainMenuDisplay extends Display {

    public OptionsMainMenuDisplay() {
        super(true);

        Menu optionsMenu = new Menu.Builder(false, 6, RelPos.LEFT,
			Settings.getEntry("fps"),
			Settings.getEntry("sound"),
			Settings.getEntry("showquests"),
			new SelectEntry("minicraft.display.options_display.change_key_bindings", () -> Game.setDisplay(new KeyInputDisplay())),
			Settings.getEntry("language"),
			new SelectEntry("minicraft.displays.options_main_menu.resource_packs", () -> Game.setDisplay(new ResourcePackDisplay()))
		)
			.setTitle("minicraft.displays.options_main_menu")
			.createMenu();

        menus = new Menu[]{
            optionsMenu
        };
    }

    @Override
    public void onExit() {
        Localization.changeLanguage(((LocaleInformation)Settings.get("language")).locale.toLanguageTag());
        new Save();
        Game.MAX_FPS = (int)Settings.get("fps");
    }
}
