package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class TomatoTile extends CropTile {
	private final LinkedSprite[] spritStages = new LinkedSprite[] {
		new LinkedSprite(SpriteType.Tile, "tomato_stage0"),
		new LinkedSprite(SpriteType.Tile, "tomato_stage1"),
		new LinkedSprite(SpriteType.Tile, "tomato_stage2"),
		new LinkedSprite(SpriteType.Tile, "tomato_stage3")
	};

	public TomatoTile(String name) {
		super(name, "tomato seeds");
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		int age = (level.getData(x, y) >> 3) & maxStage;
		Tiles.get("Farmland").render(screen, level, x, y);
		int stage;
		if (age < 2) stage = 0;
		else if (age < 4) stage = 1;
		else if (age < 7) stage = 2;
		else stage = 3;
		screen.render(x * 16, y * 16, spritStages[stage]);
	}
}
