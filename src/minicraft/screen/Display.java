package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Screen;

public class Display {
	
	private Menu[] menus;
	private int selection;
	
	private boolean canExit;
	
	public Display(Menu... menus) { this(true, menus); }
	public Display(boolean canExit, Menu... menus) {
		this.menus = menus;
		this.canExit = canExit;
	}
	
	public void tick(InputHandler input) {
		
		if(canExit && input.getKey("exit").clicked) {
			Game.exitMenu();
			return;
		}
		
		boolean changedSelection = false;
		
		if(menus.length > 1) {
			int prevSel = selection;
			
			if (input.getKey("shift-left").clicked) selection--;
			if (input.getKey("shift-right").clicked) selection++;
			
			if(prevSel != selection) {
				changedSelection = true;
				Sound.select.play();
				if(selection < 0) selection = menus.length - 1;
				selection = selection % menus.length;
			}
		}
		
		if(!changedSelection)
			menus[selection].tick(input);
	}
	
	/// sub-classes can do extra rendering here.
	public void render(Screen screen) {
		menus[selection].render(screen);
	}
}
