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

        builder.setSize(138, 138);

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

        // used for world sizes bigger than 128, since the map can only render 128x128 pixels
        int[] offset = new int[2];
        offset[0] = 0;
        offset[1] = 0;

        // used to indicate which directions can be traveled in
        // North : 0
        // West : 1
        // South : 2
        // East : 3
        boolean[] arrows = new boolean[4];
        for (int i = 0; i < 3; i++) {
            arrows[i] = false;
        }

        if (level.w == 256) {
            if ((Game.player.x - 8) / 16  > 128) { // make sure to convert entity coords to tile coords
                offset[0] = 1;
                arrows[3] = true;
            } else {
                arrows[1] = true;
            }
            if ((Game.player.y - 8) / 16 > 128) {
                offset[1] = 1;
                arrows[0] = true;
            } else {
                arrows[2] = true;
            }
        }

        for (int i = 0; i < 128; i++) {
            for (int c = 0; c < 128; c++) {
                int color = 0;

                Tile tile = level.getTile(i + (offset[0] * 128), c + (offset[1] * 128));
                for (int e = 0; e < MapData.values().length; e++) {
                    if (MapData.values()[e].tileID == tile.id) {
                        color = MapData.values()[e].color;
                        break;
                    }
                }

                // by drawing with only one pixel at a time we can draw with much more precision
                screen.render(i + menuBounds.getLeft() + 5, c + menuBounds.getTop() + 5, 4 + 13 * 32, Color.get(-1, color, -1, -1), 0);
            }
        }

        if (arrows[0]) {
            screen.render(menuBounds.getWidth() / 2 + menuBounds.getLeft() - 4, menuBounds.getTop() - 8, 10 + 13 * 32, Color.get(-1, 400, 500, 500), 0);
        } if (arrows[1]) {
            screen.render(menuBounds.getRight() + 1, menuBounds.getHeight() / 2 + menuBounds.getTop(), 5 + 13 * 32, Color.get(-1, 400, 500, 500), 1);
        } if (arrows[2]) {
            screen.render(menuBounds.getWidth() / 2 + menuBounds.getLeft() - 4, menuBounds.getBottom() + 1, 10 + 13 * 32, Color.get(-1, 400, 500, 500), 2);
        } if (arrows[3]) {
            screen.render(menuBounds.getLeft() - 8, menuBounds.getHeight() / 2 + menuBounds.getTop(), 5 + 13 * 32, Color.get(-1, 400, 500, 500), 0);
        }

        // render the marker for the player
        screen.render((Game.player.x - 8) / 16 + menuBounds.getLeft() + 4 - (offset[0] * 128), (Game.player.y - 8) / 16 + menuBounds.getTop() + 4 - (offset[1] * 128), 4 + 13 * 32, Color.get(-1, 500), 0);
    }
}
