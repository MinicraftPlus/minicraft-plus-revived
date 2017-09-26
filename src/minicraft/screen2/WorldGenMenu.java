package minicraft.screen2;

import java.awt.Point;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen2.entry.ArrayEntry;
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
			new ArrayEntry<String>("Game Mode", "Survival", "Creative", "Hardcore", "Score"),
			new ArrayEntry<Integer>("Time (Score Mode)", /*10, */20, 40, 60/*, 120*/),
			new SelectEntry("Create World", () -> Game.setMenu(new LoadingDisplay())),
			new ArrayEntry<Integer>("World Size", 128, 256, 512),
			new ArrayEntry<String>("World Theme", "Normal", "Forest", "Desert", "Plain", "Hell"),
			new ArrayEntry<String>("Terrain Type", "Island", "Box", "Mountain", "Irregular")
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
