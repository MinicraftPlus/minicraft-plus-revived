package minicraft.screen;

import java.awt.Point;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;

public interface MenuData {
	
	// TODO If I made this an abstract class, I could create an abstract createMenu method which is protected, that returns a newly created menu. Then, it's kept in a field, while getMenu is public and returns the value of the menu field. 
	
	/// returns appropriate Menu subclass for this data
	Menu getMenu();
	
	/// returns the list of entries that this MenuData type uses; there may be calculations here, and fetching of data
	ListEntry[] getEntries();
	
	/// process custom key inputs for closing the menu, etc.
	void tick(InputHandler input);
	
	/// useful for not having to update things every frame; can go by an event-based system
	// however, I'll save that for later.
	//void onSelectionChange(ListEntry newSelected);
	
	/// renders background, including frame; all EXCEPT entries
	void render(Screen screen);
	
	/// returns boolean about if entries should be centered
	boolean centerEntries();
	
	/// returns the spacing between each entry
	int getSpacing();
	
	/// returns a point that will tell where to render the first entry
	Point getAnchor();
	
	default int autoExitDelay() {
		return 0;
	}
	
	// called when the menu is about to be replaced.
	default void onExit() {}
	
	default ListEntry entryFactory(String text, MenuData menu) {
		return new SelectEntry(text, () -> Game.setMenu(menu));
	}
	
	default MenuData menuFactory(boolean clearScreen, ListEntry... entries) {
		return menuFactory("Select an option", clearScreen, entries);
	}
	default MenuData menuFactory(String title, boolean clearScreen, ListEntry... entries) {
		return new MenuData() {
			public Menu getMenu() {
				return new Menu(this);
			}
			public ListEntry[] getEntries() {
				return entries;
			}
			public void tick(InputHandler input) {}
			public void render(Screen screen) {
				if(clearScreen) screen.clear(0);
				Font.drawCentered(title, screen, 4, Color.get(-1, 555));
			}
			public boolean centerEntries() { return true; }
			public int getSpacing() { return 8; }
			public Point getAnchor() {
				return new Point(Game.WIDTH/2, Game.HEIGHT/2);
			}
		};
	}
}
