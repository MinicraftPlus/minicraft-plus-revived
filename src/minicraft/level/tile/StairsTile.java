package minicraft.level.tile;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class StairsTile extends Tile {
	private static Sprite down = new Sprite(0, 2, 2, 2, Color.get(222, 000, 333, 444), 0);
	private static Sprite up = new Sprite(2, 2, 2, 2, Color.get(222, 000, 333, 444), 0);
	
	private boolean leadsUp;
	
	protected StairsTile(String name, boolean leadsUp) {
		super(name, leadsUp?up:down);
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
		int col = 0;
		if (Game.currentLevel < 3 || Game.currentLevel == 5)
			col = Color.get(getDirtColor(), 000, 333, 444);
		else
			col = Color.get(getDirtColor(), 000, 444, 555);
		
		sprite.render(screen, x*16, y*16, col);
	}
}
