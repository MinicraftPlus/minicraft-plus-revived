package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import org.jetbrains.annotations.NotNull;

public class Display {
	
	private Display parent = null;
	
	Menu[] menus;
	int selection;
	
	private boolean canExit;
	
	public Display() { this(new Menu[0]); }
	public Display(boolean canExit) { this(canExit, new Menu[0]); }
	public Display(Menu... menus) { this(true, menus); }
	public Display(boolean canExit, Menu... menus) {
		this.menus = menus;
		this.canExit = canExit;
		selection = 0;
	}
	
	// called during setMenu()
	public void init(Display parent) {
		this.parent = parent;
	}
	
	public void onExit() {}
	
	public Display getParent() { return parent; }
	
	public void tick(InputHandler input) {
		
		if(canExit && input.getKey("exit").clicked) {
			Game.exitMenu();
			return;
		}
		
		boolean changedSelection = false;
		
		if(menus.length > 1 && menus[selection].isSelectable()) { // if menu set is unselectable, it must have been intentional, so prevent the user from setting it back.
			int prevSel = selection;
			
			if (input.getKey("shift-left").clicked) selection--;
			if (input.getKey("shift-right").clicked) selection++;
			
			if(prevSel != selection) {
				Sound.select.play();
				
				int delta = selection - prevSel;
				selection = prevSel;
				do {
					selection += delta;
					if (selection < 0) selection = menus.length - 1;
					selection = selection % menus.length;
				} while(!menus[selection].isSelectable() && selection != prevSel);
				
				changedSelection = prevSel != selection;
			}
		}
		
		if(!changedSelection)
			menus[selection].tick(input);
	}
	
	/// sub-classes can do extra rendering here; this renders each menu that should be rendered, in the order of the array, such that the currently selected menu is rendered last, so it appears on top (if they even overlap in the first place).
	public void render(Screen screen) {
		int idx = selection;
		do {
			idx++;
			idx = idx % menus.length;
			if(menus[idx].shouldRender())
				menus[idx].render(screen);
		} while(idx != selection);
	}
	
	@NotNull
	public static ListEntry entryFactory(String text, Display menu) {
		return new SelectEntry(text, () -> Game.setMenu(menu));
	}
}
