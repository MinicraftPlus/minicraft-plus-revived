package minicraft.screen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

public class LoadingDisplay extends MessageDisplay implements ActionListener {
	//this is the last menu before the game/world starts/opens.
	Timer t;
	private static double percentage = 0;
	
	public LoadingDisplay() {
		super(null);
		
		if(Game.HAS_GUI) {
			t = new Timer(400, this);
			
			style.setColor(Color.get(-1, 300));
		}
		
		percentage = 0;
	}
	
	public static double getPercentage() { return percentage; }
	
	public void init(Game game, minicraft.InputHandler init) {
		super.init(game, init);
		if(!Game.HAS_GUI)
			initWorld();
	}
	
	public void tick() {
		if(!Game.isValidClient() && t != null)
			t.start();
	}
	
	public static void setPercentage(double percent) {
		if(Math.round(percent) != Math.round(percentage) && Game.main.menu instanceof LoadingDisplay)
			((LoadingDisplay)Game.main.menu).setMessage(new String[] {"Loading...", (Math.round(percent)+"%")}); // this updates the percent display, but only when it actually changes.
		
		percentage = percent;
	}
	
	public static void progress(double percent) {
		setPercentage(Math.min(100.0, percent + percentage)); // this makes sure the progress bar doesn't add to over 100%.
	}
	
	// this method is called by the timer, when it runs out.
	public void actionPerformed(ActionEvent e) {
		if(t != null) {
			Timer save = t;
			t.stop();
			t = null; // prevents the timer from being activated again in this LoadingDisplay instance.
			save.stop();
			initWorld();
		} else if(Game.debug) {
			System.out.println("WARNING: loading menu timer was set off more than once.");
			if(e.getSource() instanceof Timer)
				((Timer)e.getSource()).stop();
		}
	}
	
	private void initWorld() {
		if(Game.debug) System.out.println("starting game initWorld...");
		
		game.initWorld();
		try {
			Thread.sleep((WorldSelectMenu.loadworld?100:300));
		} catch(InterruptedException ex) {}
		if(Game.debug) System.out.println("setting game menu to null from loading...");
		game.setMenu(null);
	}

	public void render(Screen screen) {
		screen.clear(0);
		super.render(screen);
		/*//int col =
		//int coll = Color.get(-1, 555);
		
		int percent = (int) Math.round(percentage);
		Font.drawCentered("Loading...", screen, Screen.h - 105, col);
		//Font.draw("This should take 4 seconds or less", screen, 10, Screen.h - 185, coll);
		//Font.draw("If not then restart because it froze", screen, 0, Screen.h - 175, coll);
		Font.drawCentered(percent + "%", screen, Screen.h - 85, col);
		*/
	}
}
