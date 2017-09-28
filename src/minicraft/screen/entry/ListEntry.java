package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen.Menu;

public interface ListEntry {
	
	int COL_UNSLCT = Color.get(-1, 333);
	int COL_SLCT = Color.get(-1, 555);
	
	void tick(InputHandler input, Menu menu);
	
	// coordinates specify the top left corner of the entry space
	default void render(Screen screen, int x, int y, boolean isSelected) {
		Font.draw(toString(), screen, x, y, isSelected ? COL_SLCT : COL_UNSLCT);
	}
	
	default int getWidth() {
		return Font.textWidth(toString());
	}
	
	default int getHeight() {
		return Font.textHeight();
	}
	
	String toString();
}
