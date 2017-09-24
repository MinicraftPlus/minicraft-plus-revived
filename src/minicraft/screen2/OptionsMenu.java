package minicraft.screen2;

import java.awt.Point;

import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.screen2.entry.ListEntry;

public class OptionsMenu implements MenuData {
	
	public static int diff = 1;
	public static boolean isSoundAct = true;
	public static boolean autosave = false;
	public static boolean unlockedskin = false;
	
	@Override
	public Menu getMenu() {
		return null;
	}
	
	@Override
	public ListEntry[] getEntries() {
		return new ListEntry[] {
			
		};
	}
	
	@Override
	public void tick(InputHandler input) {
		
	}
	
	@Override
	public void render(Screen screen) {
		
	}
	
	@Override
	public boolean centerEntries() {
		return false;
	}
	
	@Override
	public int getSpacing() {
		return 0;
	}
	
	@Override
	public Point getAnchor() {
		return null;
	}
}
