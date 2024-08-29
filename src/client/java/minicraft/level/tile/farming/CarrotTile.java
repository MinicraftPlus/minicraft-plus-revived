package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteManager.SpriteLink;
import minicraft.gfx.SpriteManager.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class CarrotTile extends CropTile {
	private final SpriteLink[] spritStages = new SpriteLink[] {
		new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "carrot_stage0").createSpriteLink(),
		new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "carrot_stage1").createSpriteLink(),
		new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "carrot_stage2").createSpriteLink(),
		new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "carrot_stage3").createSpriteLink()
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
