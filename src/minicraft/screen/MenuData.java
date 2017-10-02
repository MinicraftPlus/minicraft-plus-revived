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
	
	enum RelPos {
		TOP_LEFT, TOP, TOP_RIGHT,
		LEFT, CENTER, RIGHT,
		BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;
		
		public int getVal() {
			return values.length - 1 - ordinal(); // reverses it, to fit with the indexes easily pointing to the upper right corner of where to draw.
		}
		
		private static final RelPos[] values = RelPos.values();
	}
	
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
	
	/// returns how entries should be centered
	Centering getCentering();
	
	/// returns the spacing between each entry
	int getSpacing();
	
	default int autoExitDelay() {
		return 0;
	}
	
	// called when the menu is about to be replaced.
	default void onExit() {}
	
	default ListEntry entryFactory(String text, MenuData menu) {
		return new SelectEntry(text, () -> Game.setMenu(menu));
	}
	
	default MenuData menuFactory(ListEntry... entries) {
		return menuFactory(Centering.CENTER_ALL, entries);
	}
	default MenuData menuFactory(Centering centering, ListEntry... entries) {
		return menuFactory("Select an option", centering, true, entries);
	}
	default MenuData menuFactory(String title, Centering centering, boolean clearScreen, ListEntry... entries) {
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
			
			public Centering getCentering() { return centering; }
			public int getSpacing() { return 8; }
		};
	}
	
	class Centering {
		
		public static final Centering CENTER_ALL = make(new Point(Game.WIDTH/2, Game.HEIGHT/2), RelPos.CENTER, RelPos.CENTER);
		
		public final Point anchor;
		public final RelPos menu, line;
		
		private Centering(Point anchor, RelPos menuCentering, RelPos lineCentering) {
			this.anchor = anchor;
			this.menu = menuCentering;
			this.line = lineCentering;
		}
		
		public static Centering make(Point anchor, RelPos menuCentering, RelPos lineCentering) {
			return new Centering(anchor, menuCentering, lineCentering);
		}
		
		public static Centering make(Point anchor) {
			return make(anchor, false);
		}
		public static Centering make(Point anchor, boolean centerAll) {
			if(!centerAll)
				return new Centering(anchor, RelPos.BOTTOM_RIGHT, RelPos.LEFT);
			else
				return new Centering(anchor, RelPos.CENTER, RelPos.CENTER);
		}
	}
}
