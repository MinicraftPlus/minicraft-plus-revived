package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class HeavenlyBerriesTile extends CropTile {
	private final SpriteLinker.LinkedSprite[] spritStages = new SpriteLinker.LinkedSprite[] {
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Tile, "heavenly_berries_stage0"),
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Tile, "heavenly_berries_stage1"),
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Tile, "heavenly_berries_stage2"),
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Tile, "heavenly_berries_stage3")
	};

	public HeavenlyBerriesTile(String name) {
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
