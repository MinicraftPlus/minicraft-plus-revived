package minicraft.screen;

import javax.swing.Timer;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;

public class LoadingDisplay extends Display {
	
	private static float percentage = 0;
	
	private Timer t;
	
	public LoadingDisplay() {
		super(true,false);
		t = new javax.swing.Timer(500, e -> {
			World.initWorld();
			Game.setMenu(null);
		});
		t.setRepeats(false);
	}
	
	@Override
	public void init(Display parent) {
		super.init(parent);
		percentage = 0;
		t.start();
	}
	
	@Override
	public void onExit() {
		percentage = 0;
		if(!WorldSelectDisplay.loadedWorld()) {
			new Save(WorldSelectDisplay.getWorldName());
			Game.notifications.clear();
		}
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
		Font.drawCentered("Loading...", screen, Screen.h/2-Font.textHeight()/2, Color.RED);
		Font.drawCentered(percent+"%", screen, Screen.h/2+Font.textHeight()/2, Color.RED);
	}
}
