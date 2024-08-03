package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

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
		int age = (level.getData(x, y) >> 3) & maxAge;
		Tiles.get("Farmland").render(screen, level, x, y);
		int stage = (int) ((float) age / maxAge * 5);
		screen.render(x << 4, y << 4, spritStages[stage]);
	}
}
