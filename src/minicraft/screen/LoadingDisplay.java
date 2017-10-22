package minicraft.screen;

import javax.swing.Timer;

import minicraft.core.*;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public class LoadingDisplay extends Display {
	
	private static float percentage = 0;
	
	private Timer t;
	
	// TODO I could use the Menu class to display the percentage... but that might be overcomplicated...
	public LoadingDisplay() {
		super(true,false);
		t = new javax.swing.Timer(500, e -> {
			Game.initWorld();
			Game.setMenu(null);
		});
		t.setRepeats(false);
	}
	
	@Override
	public void init(Display parent) {
		super.init(parent);
		t.start();
	}
	
	@Override
	public void onExit() {
		percentage = 0;
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
		super.render(screen);
		int percent = Math.round(percentage);
		Font.drawCentered("Loading...", screen, Game.HEIGHT/2-Font.textHeight()/2, Color.RED);
		Font.drawCentered(percent+"%", screen, Game.HEIGHT/2+Font.textHeight()/2, Color.RED);
	}
}
