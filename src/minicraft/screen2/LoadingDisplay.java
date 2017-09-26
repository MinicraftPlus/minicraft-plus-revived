package minicraft.screen2;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public class LoadingDisplay extends Display {
	
	private int percentage = 0;
	
	public LoadingDisplay() {}
	
	@Override
	public void render(Screen screen) {
		Font.drawCentered("Loading...", screen, Game.HEIGHT/2, Color.get(-1, 500));
		Font.drawCentered(""+percentage, screen, Game.HEIGHT/2, Color.get(-1, 500));
	}
}
