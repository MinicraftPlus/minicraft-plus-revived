package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.saveload.Save;
import minicraft.screen.entry.SelectEntry;

public class OptionsMainMenuDisplay extends Display {

    public OptionsMainMenuDisplay() {
        super(true, new Menu.Builder(false, 6, RelPos.LEFT,
            Settings.getEntry("fps"),
            Settings.getEntry("sound"),
            new SelectEntry("Change Key Bindings", () -> Game.setMenu(new KeyInputDisplay())),
            Settings.getEntry("language")
            //Settings.getEntry("textures") // old, If you want you can activate it, it does not affect the texture pack system, but it would not make much sense
            /*new SelectEntry("Resource packs", () -> Game.setMenu(new ResourcePackDisplay()))*/) // New resource packs system
            .setTitle("Main Menu Options")
            .createMenu()
        );
    }

    @Override
    public void onExit() {
        Localization.changeLanguage((String)Settings.get("language"));
        new Save();
        Game.MAX_FPS = (int)Settings.get("fps");
    }
}
