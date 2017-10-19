package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public abstract class ListEntry {
	
	public static final int COL_UNSLCT = Color.GRAY;
	public static final int COL_SLCT = Color.WHITE;
	
	private boolean selectable = true, visible = true;
	
	public abstract void tick(InputHandler input);
	
	// coordinates specify the top left corner of the entry space
	public void render(Screen screen, int x, int y, boolean isSelected) {
		if(visible)
			Font.draw(toString(), screen, x, y, getColor(isSelected));
	}
	
	public int getColor(boolean isSelected) { return isSelected ? COL_SLCT : COL_UNSLCT; }
	
	public int getWidth() {
		return Font.textWidth(toString());
	}
	
	public static int getHeight() {
		return Font.textHeight();
	}
	
	public final boolean isSelectable() { return selectable && visible; }
	public final boolean isVisible() { return visible; }
	
	public final void setSelectable(boolean selectable) { this.selectable = selectable; }
	public final void setVisible(boolean visible) { this.visible = visible; }
	
	public abstract String toString();
}
