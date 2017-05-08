package minicraft.level.tile;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.level.Level;

public class StairsTile extends Tile {
	private boolean leadsUp;

	public StairsTile(int id, boolean leadsUp) {
		super(id);
		this.leadsUp = leadsUp;
	}
	
	private int getDirtColor() {
		switch(Game.currentLevel) {
			case 3: return 321;
			case 4: return 444;
			case 5: return 59;
			default: return 222;
		}
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		int dcol = getDirtColor();
		
		int col = 0;
		if (Game.currentLevel < 3 || Game.currentLevel == 5) {
			col = Color.get(dcol, 000, 333, 444);
		}
		else col = Color.get(dcol, 000, 444, 555);
		
		int color = col;
		int xt = 0;
		if (leadsUp) xt = 2;
		
		screen.render(x * 16 + 0, y * 16 + 0, xt + 2 * 32, color, 0);
		screen.render(x * 16 + 8, y * 16 + 0, xt + 1 + 2 * 32, color, 0);
		screen.render(x * 16 + 0, y * 16 + 8, xt + 3 * 32, color, 0);
		screen.render(x * 16 + 8, y * 16 + 8, xt + 1 + 3 * 32, color, 0);
	}
}
