package minicraft.screen;

import java.awt.Point;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Settings;
import minicraft.gfx.Font;
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
	public boolean centerEntries() { return true; }
	
	@Override
	public int getSpacing() { return 6; }
	
	@Override
	public Point getAnchor() { return new Point(Game.WIDTH/2, Font.textHeight()*3); }
	
	// TODO add feature to center menu, but align to left side; centering based on width of widest  
}
