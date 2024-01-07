package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class WheatTile extends CropTile {
	private final LinkedSprite[] spritStages = new LinkedSprite[]{
		new LinkedSprite(SpriteType.Tile, "wheat_stage0"),
		new LinkedSprite(SpriteType.Tile, "wheat_stage1"),
		new LinkedSprite(SpriteType.Tile, "wheat_stage2"),
		new LinkedSprite(SpriteType.Tile, "wheat_stage3"),
		new LinkedSprite(SpriteType.Tile, "wheat_stage4"),
		new LinkedSprite(SpriteType.Tile, "wheat_stage5")
	};

	public WheatTile(String name) {
		super(name, "wheat seeds");
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		int age = (level.getData(x, y) >> 3) & maxAge;
		Tiles.get("Farmland").render(screen, level, x, y);
		int stage = (int) ((float) age / maxAge * 5);
		screen.render(x * 16, y * 16, spritStages[stage]);
	}
}
