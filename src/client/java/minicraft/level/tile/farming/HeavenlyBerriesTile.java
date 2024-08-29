package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteManager;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class HeavenlyBerriesTile extends CropTile {
	private final SpriteManager.SpriteLink[] spritStages = new SpriteManager.SpriteLink[] {
		new SpriteManager.SpriteLink.SpriteLinkBuilder(SpriteManager.SpriteType.Tile, "heavenly_berries_stage0").createSpriteLink(),
		new SpriteManager.SpriteLink.SpriteLinkBuilder(SpriteManager.SpriteType.Tile, "heavenly_berries_stage1").createSpriteLink(),
		new SpriteManager.SpriteLink.SpriteLinkBuilder(SpriteManager.SpriteType.Tile, "heavenly_berries_stage2").createSpriteLink(),
		new SpriteManager.SpriteLink.SpriteLinkBuilder(SpriteManager.SpriteType.Tile, "heavenly_berries_stage3").createSpriteLink()
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
