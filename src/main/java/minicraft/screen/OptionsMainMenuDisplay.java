package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Initializer;
import minicraft.core.Renderer;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class OptionsMainMenuDisplay extends Display {

    public static String originalAspectRatio = (String) Settings.get("aspectratio");

    public OptionsMainMenuDisplay() {
        super(true);

        Menu optionsMenu = new Menu.Builder(false, 6, RelPos.LEFT,
            Settings.getEntry("fps"),
            Settings.getEntry("sound"),
            new SelectEntry("Change Key Bindings", () -> Game.setDisplay(new KeyInputDisplay())),
            Settings.getEntry("language"),
            Settings.getEntry("aspectratio"),
            new BlankEntry(),
            new SelectEntry("Resource packs", () -> Game.setDisplay(new ResourcePackDisplay())))
            .setTitle("Main Menu Options")
            .createMenu();

        Menu popupMenu = new Menu.Builder(true, 4, RelPos.CENTER)
            .setShouldRender(false)
            .setSelectable(false)
            .setEntries(StringEntry.useLines(Color.RED, "A restart is required", "enter to confirm", "escape to cancel"))
            .setTitle("Confirm Action")
            .createMenu();

        menus = new Menu[]{
            optionsMenu,
            popupMenu
        };
    }

    @Override
	public void render(Screen screen) {
		super.render(screen);
        
        // Forcefully render the popup menu above everything else
        if(menus[1].shouldRender) {
            menus[1].render(screen);
        }
    }

    @Override
    public void tick(InputHandler input) {
        if (menus[1].shouldRender) {
            if (input.getKey("enter").clicked) {
                menus[1].shouldRender = false;
                Game.exitDisplay();
            } else if (input.getKey("exit").clicked) {
                menus[1].shouldRender = false;
            }
            return;
		}

        // If exit key is pressed, then display the popup menu if changes requiring a restart have been made
        if (input.getKey("exit").clicked && originalAspectRatio != (String) Settings.get("aspectratio")) {
            menus[1].shouldRender = true;
            return;
		}

        super.tick(input);
    }

    @Override
    public void onExit() {
        Localization.changeLanguage((String)Settings.get("language"));
        new Save();
        Game.MAX_FPS = (int)Settings.get("fps");
    }
}
