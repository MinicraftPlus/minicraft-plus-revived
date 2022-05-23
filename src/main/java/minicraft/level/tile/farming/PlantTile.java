package minicraft.level.tile.farming;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class PlantTile extends FarmTile {
    protected static int maxAge = 100;

    protected PlantTile(String name) {
        super(name, null);
    }

    @Override
    public void steppedOn(Level level, int xt, int yt, Entity entity) {
        super.steppedOn(level, xt, yt, entity);
        harvest(level, xt, yt, entity);
    }

    @Override
    public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
        harvest(level, x, y, source);
        return true;
    }

    @Override
    public boolean tick(Level level, int xt, int yt) {
        if (random.nextInt(2) == 0) return false;

        int age = level.getData(xt, yt);
        if (age < maxAge) {
            if (!IfWater(level, xt, yt)) level.setData(xt, yt, age + 1);
            else if (IfWater(level, xt, yt)) level.setData(xt, yt, age + 2);
            return true;
        }

        return false;
    }

    protected boolean IfWater(Level level, int xs, int ys) {
        Tile[] areaTiles = level.getAreaTiles(xs, ys, 1);
        for(Tile t: areaTiles)
            if(t == Tiles.get("Water"))
                return true;

        return false;
    }

    /** Default harvest method, used for everything that doesn't really need any special behavior. */
    protected void harvest(Level level, int x, int y, Entity entity) {
        int age = level.getData(x, y);

        level.dropItem(x * 16 + 8, y * 16 + 8, 1, Items.get(name + " Seeds"));

        int count = 0;
        if (age >= maxAge) {
            count = random.nextInt(3) + 2;
        } else if (age >= maxAge - maxAge / 5) {
            count = random.nextInt(2) + 1;
        }

        level.dropItem(x * 16 + 8, y * 16 + 8, count, Items.get(name));

        if (age >= maxAge && entity instanceof Player) {
            ((Player)entity).addScore(random.nextInt(5) + 1);
        }

		// Play sound.
		Sound.monsterHurt.play();

        level.setTile(x, y, Tiles.get("Dirt"));
    }
}
