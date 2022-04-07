package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.screen.entry.SelectEntry;

public class OptionsMainMenuDisplay extends Display {

    public OptionsMainMenuDisplay() {
        super(true, new Menu.Builder(false, 6, RelPos.LEFT,
            Settings.getEntry("fps"),
            Settings.getEntry("screenresolution"),
            Settings.getEntry("sound"),
            new SelectEntry("Change Key Bindings", () -> Game.setMenu(new KeyInputDisplay())),
            Settings.getEntry("language")
            /*new SelectEntry("Resource packs", () -> Game.setMenu(new ResourcePackDisplay()))*/)
            .setTitle("Main Menu Options")
            .createMenu()
        );
    }

    @Override
	public void render(Screen screen) {
		super.render(screen);
        
		// Warning users about screen resoluton requiring a restart
		Font.drawCentered("Some settings may require a restart", screen, Screen.h-20, Color.RED);
    }

    @Override
    public void onExit() {
        Localization.changeLanguage((String)Settings.get("language"));
        new Save();
        Game.MAX_FPS = (int)Settings.get("fps");
    }
}
