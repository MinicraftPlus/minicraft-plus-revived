package minicraft.screen2;

import java.awt.Point;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Settings;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen2.entry.ArrayEntry;
import minicraft.screen2.entry.ListEntry;

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
	public void render(Screen screen) {}
	
	@Override
	public boolean clearScreen() { return true; }
	
	@Override
	public boolean centerEntries() {
		return true;
	}
	
	@Override
	public int getSpacing() {
		return 6;
	}
	
	@Override
	public Point getAnchor() {
		return new Point(Game.WIDTH/2, Font.textHeight()*3);
	}
}
