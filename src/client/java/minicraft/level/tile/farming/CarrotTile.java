package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class CarrotTile extends CropTile {
	private final LinkedSprite[] spritStages = new LinkedSprite[]{
		new LinkedSprite(SpriteType.Tile, "carrot_stage0"),
		new LinkedSprite(SpriteType.Tile, "carrot_stage1"),
		new LinkedSprite(SpriteType.Tile, "carrot_stage2"),
		new LinkedSprite(SpriteType.Tile, "carrot_stage3")
	};

	public CarrotTile(String name) {
		super(name, null);
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		int age = (level.getData(x, y) >> 3) & maxAge;
		Tiles.get("Farmland").render(screen, level, x, y);
		int stage = (int) ((float) age / maxAge * 3);
		screen.render(x * 16, y * 16, spritStages[stage]);
	}
}
