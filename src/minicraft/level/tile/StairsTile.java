package minicraft.level.tile;

import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class StairsTile extends Tile {
	private static Sprite down = new Sprite(21, 0, 2, 2, 1, 0);
	private static Sprite up = new Sprite(19, 0, 2, 2, 1, 0);
	
	protected StairsTile(String name, boolean leadsUp) {
		super(name, leadsUp?up:down);
		maySpawn = false;
	}
	
	@Override
	public void render(Screen screen, Level level, int x, int y) {
		sprite.render(screen, x*16, y*16, 0, DirtTile.dCol(level.depth));
	}
}
