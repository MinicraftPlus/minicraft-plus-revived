package minicraft.screen2;

import java.awt.Point;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.screen2.entry.ListEntry;

public interface MenuData {
	
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
}
