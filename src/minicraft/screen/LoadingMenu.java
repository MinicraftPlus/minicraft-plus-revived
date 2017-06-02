package minicraft.screen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public class LoadingMenu extends Menu implements ActionListener {
	//this is the last menu before the game/world starts/opens.
	//but how does stuff happen..?
	private Menu parent;
	Timer t;
	public static int percentage = 0;

	public LoadingMenu() {
		t = new Timer(400, this);
		percentage = 0;
	}

	public void tick() {
		t.start();
	}

	public void actionPerformed(ActionEvent e) {
		//something MUST call this... but what?
			//must be in another class.
		game.resetstartGame();
		game.setMenu(null);
		t.stop();
	}

	public void render(Screen screen) {
		int col = Color.get(-1, 300);
		int coll = Color.get(-1, 555);
		screen.clear(0);
		
		Font.drawCentered("Loading...", screen, screen.h - 105, col);
		//Font.draw("This should take 4 seconds or less", screen, 10, screen.h - 185, coll);
		//Font.draw("If not then restart because it froze", screen, 0, screen.h - 175, coll);
		Font.drawCentered(percentage + "%", screen, screen.h - 85, col);
	}
}
