package minicraft.level.tile.farming;

import minicraft.core.Renderer;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;

public class WheatTile extends PlantTile {

	public WheatTile(String name) {
		super(name);
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		int age = level.getData(x, y);
		int icon = age / (maxAge / 5);

		Tiles.get("Farmland").render(screen, level, x, y);

		screen.render(x * 16 + 0, y * 16 + 0, 13 + icon, 0, 0, Renderer.spriteLinker.getSpriteSheet(SpriteType.Tile, "wheat"));
		screen.render(x * 16 + 8, y * 16 + 0, 13 + icon, 0, 0, Renderer.spriteLinker.getSpriteSheet(SpriteType.Tile, "wheat"));
		screen.render(x * 16 + 0, y * 16 + 8, 13 + icon, 0, 1, Renderer.spriteLinker.getSpriteSheet(SpriteType.Tile, "wheat"));
		screen.render(x * 16 + 8, y * 16 + 8, 13 + icon, 0, 1, Renderer.spriteLinker.getSpriteSheet(SpriteType.Tile, "wheat"));
	}
}
