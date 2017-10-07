package minicraft.screen;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public class LoadingDisplay extends Display {
	
	private static float percentage = 0;
	
	// TODO I could use the Menu class to display the percentage... but that might be overcomplicated...
	public LoadingDisplay() {
		super(true,false);
		javax.swing.Timer t;
		t = new javax.swing.Timer(100, e -> {
			Game.initWorld();
			Game.setMenu(null);
		});
		t.setRepeats(false);
		t.start();
	}
	
	public static void setPercentage(float percent) {
		percentage = percent;
	}
	public static float getPercentage() { return percentage; }
	
	public static void progress(float amt) {
		percentage = Math.min(100, percentage+amt);
	}
	
	@Override
	public void render(Screen screen) {
		screen.clear(0);
		
		int percent = Math.round(percentage);
		Font.drawCentered("Loading...", screen, Game.HEIGHT/2-Font.textHeight()/2, Color.RED);
		Font.drawCentered(percent+"%", screen, Game.HEIGHT/2+Font.textHeight()/2, Color.RED);
	}
}
