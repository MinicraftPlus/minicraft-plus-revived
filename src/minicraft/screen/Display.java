package minicraft.screen;

import minicraft.core.*;
import minicraft.core.InputHandler;
import minicraft.core.Sound;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import org.jetbrains.annotations.NotNull;

public class Display {
	
	private Display parent = null;
	
	protected Menu[] menus;
	int selection;
	
	private boolean canExit, clearScreen;
	
	public Display() { this(new Menu[0]); }
	public Display(Menu... menus) { this(false, true, menus); }
	public Display(boolean clearScreen) { this(clearScreen, true, new Menu[0]); }
	public Display(boolean clearScreen, Menu... menus) { this(clearScreen, true, menus); }
	public Display(boolean clearScreen, boolean canExit) { this(clearScreen, canExit, new Menu[0]); }
	public Display(boolean clearScreen, boolean canExit, Menu... menus) {
		this.menus = menus;
		this.canExit = canExit;
		this.clearScreen = clearScreen;
		selection = 0;
	}
	
	private boolean setParent = false;
	// called during setMenu()
	public void init(Display parent) {
		if(!setParent) {
			setParent = true;
			this.parent = parent;
		}
	}
	
	public void onExit() {}
	
	public Display getParent() { return parent; }
	public Menu getCurMenu() { return menus[selection]; }
	
	public void tick(InputHandler input) {
		
		if(canExit && input.getKey("exit").clicked) {
			Game.exitMenu();
			return;
		}
		
		if(menus.length == 0) return;
		
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
			
			if(changedSelection)
				onSelectionChange(prevSel, selection);
		}
		
		if(!changedSelection)
			menus[selection].tick(input);
	}
	
	protected void onSelectionChange(int oldSel, int newSel) {}
	
	/// sub-classes can do extra rendering here; this renders each menu that should be rendered, in the order of the array, such that the currently selected menu is rendered last, so it appears on top (if they even overlap in the first place).
	public void render(Screen screen) {
		if(clearScreen)
			screen.clear(0);
		
		if(menus.length == 0)
			return;
		
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
