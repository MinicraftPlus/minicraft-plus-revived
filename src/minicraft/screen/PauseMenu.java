package minicraft.screen;

import java.awt.Point;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;

public class PauseMenu implements MenuData {
	
	
	@Override
	public Menu getMenu() {
		return new Menu(this, new Frame("", new Rectangle(4, 2, 32, 20, Rectangle.CORNERS)));
	}
	
	@Override
	public ListEntry[] getEntries() {
		return new ListEntry[] {
			entryFactory("Return to Game", null),
			entryFactory("Options", new OptionsMenu()),
			entryFactory("Change Key Bindings", new KeyInputMenu()),
			new SelectEntry("Make World Multiplayer", () -> {
				Game.setMenu((Menu)null);
				Game.startMultiplayerServer();
			}),
			new SelectEntry("Save Game", () -> {
				Game.setMenu((Menu)null);
				if(!Game.isValidServer())
					new Save(Game.player, WorldSelectMenu.getWorldName());
				else
					Game.server.saveWorld();
			}),
			entryFactory("Load Game", new WorldSelectMenu()),
			entryFactory("Main Menu", new TitleMenu())
		};
	}
	
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("pause").clicked) {
			Game.setMenu(getMenu().getParent());
			//return;
		}
		
		
	}
	
	@Override
	public void render(Screen screen) {
		Font.drawCentered("Paused", screen, 35, Color.get(-1, 550));
		
		Font.drawCentered(Game.input.getMapping("up")+" and "+Game.input.getMapping("down")+" to Scroll", screen, 140, Color.get(-1, 333));
		Font.drawCentered(Game.input.getMapping("select")+": Choose", screen, 150, Color.get(-1, 333));
	}
	
	@Override
	public boolean centerEntries() {
		return true;
	}
	
	@Override
	public int getSpacing() {
		return 4;
	}
	
	@Override
	public Point getAnchor() {
		return new Point(Game.WIDTH/2, Font.textHeight()*11 - 35);
	}
}
