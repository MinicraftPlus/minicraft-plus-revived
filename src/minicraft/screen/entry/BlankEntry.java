package minicraft.screen.entry;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;

public class BlankEntry implements ListEntry {
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public void render(Screen screen, int x, int y, boolean isSelected) {}
	
	@Override
	public int getWidth() {
		return SpriteSheet.boxWidth;
	}
	
	@Override
	public boolean isSelectable() { return false; }
}
