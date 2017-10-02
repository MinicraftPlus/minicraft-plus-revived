package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Settings;
import minicraft.gfx.Screen;
import minicraft.screen.entry.ArrayEntry;
import minicraft.screen.entry.ListEntry;

public class OptionsMenu implements MenuData {
	
	@Override
	public Menu getMenu() {
		return new Menu(this);
	}
	
	@Override
	public ListEntry[] getEntries() {
		return new ArrayEntry[] {
			Settings.getEntry("diff"),
			Settings.getEntry("sound"),
			Settings.getEntry("autosave"),
			Settings.getEntry("skinon")
		};
	}
	
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public void render(Screen screen) {
		screen.clear(0);
	}
	
	@Override
	public Centering getCentering() { return Centering.make(Game.CENTER, RelPos.CENTER, RelPos.LEFT); }
	
	@Override
	public int getSpacing() { return 6; }
}
