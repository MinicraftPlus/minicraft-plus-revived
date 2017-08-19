package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

public abstract class ListEntry {
	
	public void tick(InputHandler input) {
		if(input.getKey("select").clicked)
			onSelect();
	}
	
	public void onSelect() {
	}
	
	//public abstract String getTextString(boolean isSelected); // creates a method for displaying the list item.
	public abstract void render(Screen screen, FontStyle style);
}
