package minicraft.screen;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ListEntry;

public class ContainerMenu implements MenuData {
	
	
	
	@Override
	public Menu getMenu() {
		return new ScrollingMenu();
	}
	
	@Override
	public ListEntry[] getEntries() {
		return new ListEntry[0];
	}
	
	@Override
	public void tick(InputHandler input) {
		
	}
	
	@Override
	public void render(Screen screen) {
		
	}
	
	@Override
	public Centering getCentering() {
		return null;
	}
	
	@Override
	public int getSpacing() {
		return 0;
	}
}
