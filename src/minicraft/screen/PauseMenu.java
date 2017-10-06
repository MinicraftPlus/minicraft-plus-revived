package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class PauseMenu extends Display {
	
	public PauseMenu() {
		ArrayList<ListEntry> entries = new ArrayList<>();
		entries.addAll(Arrays.asList(
			entryFactory("Return to Game", null),
			entryFactory("Options", new OptionsMenu()),
			entryFactory("Change Key Bindings", new KeyInputMenu())
			));
		
		if(!Game.ISONLINE)
			entries.add(new SelectEntry("Make World Multiplayer", () -> {
				Game.setMenu(null);
				Game.startMultiplayerServer();
			}));
		
		entries.addAll(Arrays.asList(
			new SelectEntry("Save Game", () -> {
				Game.setMenu(null);
				if(!Game.isValidServer())
					new Save(Game.player, WorldSelectMenu.getWorldName());
				else
					Game.server.saveWorld();
			}),
			entryFactory("Load Game", new WorldSelectMenu()),
			entryFactory("Main Menu", new TitleMenu()),
			
			new StringEntry(Game.input.getMapping("up")+" and "+Game.input.getMapping("down")+" to Scroll", Color.GRAY),
			new StringEntry(Game.input.getMapping("select")+": Choose", Color.GRAY)
		));
		
		//Menu.Builder msgBuilder = new Menu.Builder(8);
		
		menus = new Menu[] {
			new Menu.Builder(true, 4, entries)
				.setTitle("Paused", 550)
				.createMenu()/*,
			
			msgBuilder.setEntries(new StringEntry("Save Game?"), new StringEntry("(Hint: Press \"r\" to save in-game)", Color.DARK_GRAY))
				.createMenu(),
			
			msgBuilder.setEntries(new StringEntry(""))*/
		};
	}
	
	/*@Override
	public Menu getMenu() {
		return new Menu(this, new Frame("", new Rectangle(4, 2, 32, 20, Rectangle.CORNERS)));
	}
	*/
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("pause").clicked)
			Game.exitMenu();
	}
	
	/*@Override
	public void render(Screen screen) {
		//Font.drawCentered("Paused", screen, 35, Color.YELLOW);
		
		Font.drawCentered(, screen, 140, Color.GRAY);
		Font.drawCentered(, screen, 150, Color.GRAY);
	}*/
}
