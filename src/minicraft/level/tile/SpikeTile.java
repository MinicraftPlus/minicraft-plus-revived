package minicraft.level.tile;

import minicraft.entity.Entity;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class SpikeTile extends Tile {
    private static Sprite sprite = new Sprite(20, 1, Color.get(-1, 30, 141, 252));

    public SpikeTile(String name) {
        super(name, (Sprite)null);
        connectsToSand = true;
        connectsToWater = true;
        connectsToLava = true;
        mobDamage = 1;
    }

    @Override
    public void render(Screen screen, Level level, int x, int y) {
        Tiles.get("Hole").render(screen, level, x, y);

        x = x << 4;
        y = y << 4;

        screen.render(x - 4, y, 20 + 1 * 32, Color.get(-1, 30, 141, 252), 0);
        screen.render(x + 4, y, 20 + 1 * 32, Color.get(-1, 30, 141, 252), 1);
        screen.render(x + 4, y, 20 + 1 * 32, Color.get(-1, 30, 141, 252), 0);
        screen.render(x + 12, y, 20 + 1 * 32, Color.get(-1, 30, 141, 252), 1);
        screen.render(x - 4, y + 8, 20 + 1 * 32, Color.get(-1, 30, 141, 252), 0);
        screen.render(x + 4, y + 8, 20 + 1 * 32, Color.get(-1, 30, 141, 252), 1);
        screen.render(x + 4, y + 8, 20 + 1 * 32, Color.get(-1, 30, 141, 252), 0);
        screen.render(x + 12, y + 8, 20 + 1 * 32, Color.get(-1, 30, 141, 252), 1);
    }

    @Override
    public boolean canHurtMob() {
        return true;
    }
}
