package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class MapDisplay extends Display {

    public MapDisplay() {

        Menu.Builder builder = new Menu.Builder(false, 0, RelPos.CENTER)
                .setFrame(332, 1, 332);

        builder.setSize(147, 147);

        menus = new Menu[1];
        menus[0] = builder.createMenu();

        menus[0].shouldRender = true;
    }

    @Override
    public void tick(InputHandler input) {
        if (input.getKey("menu").clicked || input.getKey("attack").clicked)
            Game.exitMenu();
    }

    @Override
    public void render(Screen screen) {
        menus[0].render(screen);

        Level level = Game.levels[Game.currentLevel];

        Rectangle menuBounds = menus[0].getBounds();

        for (int i = 0; i < level.w; i++) {
            for (int c = 0; c < level.h; c++) {
                int color = 0;

                Tile tile = level.getTile(i, c);
                for (int e = 0; e < MapData.values().length; e++) {
                    if (MapData.values()[e].tileID == tile.id) {
                        color = MapData.values()[e].color;
                        break;
                    }
                }

                // by drawing over all but one pixel of previous tiles we can draw with much more precision
                screen.render(i + menuBounds.getLeft() + 6, c + menuBounds.getTop() + 6, 3 + 13 * 32, Color.get(color, color), 0);
            }
        }

        // render the marker for the player
        screen.render((Game.player.x - 8) / 16 + menuBounds.getLeft() + 6, (Game.player.y - 8) / 16 + menuBounds.getTop() + 6, 4 + 13 * 32, Color.get(-1, 500), 0);
    }
}
