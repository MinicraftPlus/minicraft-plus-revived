package minicraft.screen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.Game;

public class LoadingMenu extends Menu implements ActionListener {
	//this is the last menu before the game/world starts/opens.
	private Menu parent;
	Timer t;
	public static double percentage = 0;
	
	public LoadingMenu() {
		t = new Timer(400, this);
		percentage = 0;
	}
	
	public void tick() {
		if(!Game.isValidClient())
			t.start();
	}
	
	// this method is called by the timer, when it runs out.
	public void actionPerformed(ActionEvent e) {
		game.initWorld();
		try {
			Thread.sleep((WorldSelectMenu.loadworld?100:300));
		} catch(InterruptedException ex) {}
		game.setMenu(null);
		t.stop();
	}

	public void render(Screen screen) {
		int col = Color.get(-1, 300);
		int coll = Color.get(-1, 555);
		screen.clear(0);
		
		int percent = (int) Math.round(percentage);
		Font.drawCentered("Loading...", screen, screen.h - 105, col);
		//Font.draw("This should take 4 seconds or less", screen, 10, screen.h - 185, coll);
		//Font.draw("If not then restart because it froze", screen, 0, screen.h - 175, coll);
		Font.drawCentered(percent + "%", screen, screen.h - 85, col);
	}
}
