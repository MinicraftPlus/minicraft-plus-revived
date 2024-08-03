package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteManager.SpriteLink;
import minicraft.gfx.SpriteManager.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class PotatoTile extends CropTile {
	private final SpriteLink[] spritStages = new SpriteLink[] {
		new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "potato_stage0").createSpriteLink(),
		new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "potato_stage1").createSpriteLink(),
		new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "potato_stage2").createSpriteLink(),
		new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "potato_stage3").createSpriteLink(),
		new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "potato_stage4").createSpriteLink(),
		new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "potato_stage5").createSpriteLink()
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
