package minicraft.screen;

import minicraft.core.io.InputHandler;
import minicraft.gfx.Screen;

public class PlayerGUIDisplay extends Display {
    private Display[] guiDisplays;

    public PlayerGUIDisplay() {
        super(true, false);
        guiDisplays = new Display[] {
            new QuestsDisplay(true)
        };
    }

    @Override
    public void tick(InputHandler input) {
        for (Display g : guiDisplays) g.tick(input);
    }

    @Override
    public void render(Screen screen) {
        for (Display g : guiDisplays) g.render(screen);
    }
}
