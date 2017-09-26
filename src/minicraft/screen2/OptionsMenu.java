package minicraft.screen2;

import java.awt.Point;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen2.entry.*;

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
			new ArrayEntry<String>("Difficulty", "Easy", "Normal", "Hard"),
			new BooleanEntry("Sound", true),
			new BooleanEntry("Autosave", true),
			new BooleanEntry("Wear Suit", false)
		};
	}
	
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public void render(Screen screen) {}
	
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
