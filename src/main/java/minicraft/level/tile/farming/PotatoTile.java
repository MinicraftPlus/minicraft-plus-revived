package minicraft.level.tile.farming;

import minicraft.core.io.Sound;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.screen.QuestsDisplay;

public class PotatoTile extends PlantTile {
	private LinkedSprite[] spritStages = new LinkedSprite[] {
		new LinkedSprite(SpriteType.Tile, "potato_stage0"),
		new LinkedSprite(SpriteType.Tile, "potato_stage1"),
		new LinkedSprite(SpriteType.Tile, "potato_stage2"),
		new LinkedSprite(SpriteType.Tile, "potato_stage3"),
		new LinkedSprite(SpriteType.Tile, "potato_stage4"),
		new LinkedSprite(SpriteType.Tile, "potato_stage5")
	};

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
		screen.render(x * 16, y * 16, spritStages[icon]);
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
		if (count > 0) {
			QuestsDisplay.questCompleted("minicraft.quest.tutorial.farming.harvest");
		}

        if (age >= maxAge && entity instanceof Player) {
            ((Player)entity).addScore(random.nextInt(4) + 1);
        }

		// Play sound.
		Sound.play("monsterhurt");

        level.setTile(x, y, Tiles.get("Dirt"));
    }
}
