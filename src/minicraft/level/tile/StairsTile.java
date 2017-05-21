package minicraft.level.tile;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;

public class StairsTile extends Tile {
	private static Sprite down = new Sprite(0, 0, 2, 2, Color.get(222, 000, 333, 444), 0);
	private static Sprite up = new Sprite(0, 2, 2, 2, Color.get(222, 000, 333, 444), 0);
	
	protected static void addInstances() {
		Tiles.add(new StairsTile("Stairs Up", true));
		Tiles.add(new StairsTile("Stairs Down", false));
	}
	
	private boolean leadsUp;
	
	private StairsTile(String name, boolean leadsUp) {
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
		/*
		screen.render(x * 16 + 0, y * 16 + 0, xt + 2 * 32, color, 0);
		screen.render(x * 16 + 8, y * 16 + 0, xt + 1 + 2 * 32, color, 0);
		screen.render(x * 16 + 0, y * 16 + 8, xt + 3 * 32, color, 0);
		screen.render(x * 16 + 8, y * 16 + 8, xt + 1 + 3 * 32, color, 0);
		*/
	}
}
