package minicraft.level.tile.farming;

import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class PotatoTile extends Plant {
    public PotatoTile(String name) {
        super(name);
    }

    @Override
    protected void harvest(Level level, int x, int y, Entity entity) {
        int age = level.getData(x, y);

        int count = 0;
        if (age >= maxAge) {
            count = random.nextInt(3) + 2;
        } else if (age >= maxAge - maxAge / 5) {
            count = random.nextInt(2) + 1;
        }

        level.dropItem(x*16+8, y*16+8, count + 1, Items.get("Potato"));

        if (age >= maxAge && entity instanceof Player) {
            ((Player)entity).addScore(random.nextInt(5) + 1);
        }

        level.setTile(x, y, Tiles.get("Dirt"));
    }
}
