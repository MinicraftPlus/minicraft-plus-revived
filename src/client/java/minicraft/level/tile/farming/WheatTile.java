package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class WheatTile extends PlantTile {
	private LinkedSprite[] spritStages = new LinkedSprite[] {
		new LinkedSprite.SpriteLinkBuilder(SpriteType.Tile, "wheat_stage0").createSpriteLink(),
		new LinkedSprite.SpriteLinkBuilder(SpriteType.Tile, "wheat_stage1").createSpriteLink(),
		new LinkedSprite.SpriteLinkBuilder(SpriteType.Tile, "wheat_stage2").createSpriteLink(),
		new LinkedSprite.SpriteLinkBuilder(SpriteType.Tile, "wheat_stage3").createSpriteLink(),
		new LinkedSprite.SpriteLinkBuilder(SpriteType.Tile, "wheat_stage4").createSpriteLink(),
		new LinkedSprite.SpriteLinkBuilder(SpriteType.Tile, "wheat_stage5").createSpriteLink()
	};

	public WheatTile(String name) {
		super(name);
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		int age = level.getData(x, y);
		int icon = age / (maxAge / 5);

		Tiles.get("Farmland").render(screen, level, x, y);
		screen.render(x * 16, y * 16, spritStages[icon]);
	}
}
