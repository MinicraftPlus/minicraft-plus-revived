package minicraft.screen2;

import java.awt.Point;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen2.entry.InputEntry;
import minicraft.screen2.entry.ListEntry;
import minicraft.screen2.entry.SelectEntry;

public class WorldGenMenu implements MenuData {
	
	@Override
	public Menu getMenu() {
		return new ScrollingMenu(this, 5, 0.8f);
	}
	
	@Override
	public ListEntry[] getEntries() {
		return new ListEntry[] {
			new InputEntry("Enter World Name", "[a-zA-Z0-9 ]", 36),
			Settings.getEntry("mode"),
			Settings.getEntry("scoretime"),
			new SelectEntry("Create World", () -> {
				// TODO set world name
				Game.setMenu(new LoadingDisplay());
			}),
			Settings.getEntry("size"),
			Settings.getEntry("theme"),
			Settings.getEntry("type")
		};
	}
	
	@Override
	public void tick(InputHandler input) {}
	
	@Override
	public void render(Screen screen) {
		Font.drawCentered("World Gen Options", screen, 0, Color.get(-1, 555));
	}
	
	@Override
	public boolean centerEntries() {
		return true;
	}
	
	@Override
	public int getSpacing() {
		return 10;
	}
	
	@Override
	public Point getAnchor() {
		return new Point(Game.WIDTH/2, Font.textHeight()*2);
	}
}
