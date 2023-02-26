package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.screen.QuestsDisplay;

public class PotatoTile extends CropTile {
	private final LinkedSprite[] spritStages = new LinkedSprite[] {
		new LinkedSprite(SpriteType.Tile, "potato_stage0"),
		new LinkedSprite(SpriteType.Tile, "potato_stage1"),
		new LinkedSprite(SpriteType.Tile, "potato_stage2"),
		new LinkedSprite(SpriteType.Tile, "potato_stage3"),
		new LinkedSprite(SpriteType.Tile, "potato_stage4"),
		new LinkedSprite(SpriteType.Tile, "potato_stage5")
	};

    public PotatoTile(String name) {
        super(name, null);
    }

    @Override
    public void render(Screen screen, Level level, int x, int y) {
        int age = (level.getData(x, y) >> 3) & maxStage;
        Tiles.get("Farmland").render(screen, level, x, y);
		int stage;
		if (age < 1) stage = 0;
		else if (age < 3) stage = 1;
		else if (age < 4) stage = 2;
		else if (age < 5) stage = 3;
		else if (age < 7) stage = 4;
		else stage = 5;
		screen.render(x * 16, y * 16, spritStages[stage]);
    }
}
