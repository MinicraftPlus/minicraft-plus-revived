package minicraft.level.tile.farming;

import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class BeetrootTile extends PlantTile {
	private final LinkedSprite[] spritStages = new LinkedSprite[] {
		new LinkedSprite(SpriteType.Tile, "beetroot_stage0"),
		new LinkedSprite(SpriteType.Tile, "beetroot_stage1"),
		new LinkedSprite(SpriteType.Tile, "beetroot_stage2"),
		new LinkedSprite(SpriteType.Tile, "beetroot_stage3")
	};

	public BeetrootTile(String name) {
		super(name, "beetroot seeds");
		maxStage = 3;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		int age = (level.getData(x, y) >> 3) & maxStage;
		Tiles.get("Farmland").render(screen, level, x, y);
		screen.render(x * 16, y * 16, spritStages[age]);
	}

	@Override
	public void performBonemeal(Level level, int x, int y) {
		int data = level.getData(x, y);
		int stage = (data >> 3) & maxStage;
		if (stage < maxStage && random.nextInt(4) == 0)
			level.setData(x, y, (data & ~(maxStage << 3)) + ((stage + 1) << 3));
	}
}
