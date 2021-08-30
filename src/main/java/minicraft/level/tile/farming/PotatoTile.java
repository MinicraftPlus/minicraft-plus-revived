package minicraft.level.tile.farming;

import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class PotatoTile extends Plant {
    public PotatoTile(String name) {
        super(name);
    }

    static {
        maxAge = 70;
    }

    @Override
    public void render(Screen screen, Level level, int x, int y) {
        int age = level.getData(x, y);
        int icon = age / (maxAge / 5);

        Tiles.get("Farmland").render(screen, level, x, y);

        screen.render(x * 16 + 0, y * 16 + 0, 13 + 1 * 32 + icon, 0, 1);
        screen.render(x * 16 + 8, y * 16 + 0, 13 + 1 * 32 + icon, 0, 1);
        screen.render(x * 16 + 0, y * 16 + 8, 13 + 1 * 32 + icon, 1, 1);
        screen.render(x * 16 + 8, y * 16 + 8, 13 + 1 * 32 + icon, 1, 1);
    }

    @Override
    protected void harvest(Level level, int x, int y, Entity entity) {
        int age = level.getData(x, y);

        int count = 0;
        if (age >= maxAge) {
            count = random.nextInt(3) + 2;
        } else if (age >= maxAge - maxAge / 5) {
            count = random.nextInt(2);
        }

        level.dropItem(x * 16 + 8, y * 16 + 8, count + 1, Items.get("Potato"));

        if (age >= maxAge && entity instanceof Player) {
            ((Player)entity).addScore(random.nextInt(4) + 1);
        }

        level.setTile(x, y, Tiles.get("Dirt"));
    }
}
