package minicraft.level.tile.farming;

import minicraft.core.Renderer;
import minicraft.core.io.Sound;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class PotatoTile extends PlantTile {
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
        screen.render(x * 16, y * 16, 13 + icon * 2, 0, 0, Renderer.spriteLinker.getSpriteSheet(SpriteType.Tile, "potato"));
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

		// Play sound.
		Sound.monsterHurt.play();

        level.setTile(x, y, Tiles.get("Dirt"));
    }
}
