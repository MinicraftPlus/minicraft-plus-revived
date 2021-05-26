package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.core.Game;
import minicraft.core.MyUtils;
import minicraft.core.Network;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.network.Analytics;
import minicraft.saveload.Save;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class PauseDisplay extends Display {
	
	public PauseDisplay() {
		String upString = Game.input.getMapping("cursor-up")+ Localization.getLocalized(" and ")+Game.input.getMapping("cursor-down")+Localization.getLocalized(" to Scroll");
		String selectString = Game.input.getMapping("select")+Localization.getLocalized(": Choose");
		
		
		ArrayList<ListEntry> entries = new ArrayList<>();
		entries.addAll(Arrays.asList(
			new BlankEntry(),
			new SelectEntry("Return to Game", () -> Game.setMenu(null)),
			new SelectEntry("Options", () -> Game.setMenu(new OptionsDisplay()))
			));
		
		if(!Game.ISONLINE) {
			entries.add(new SelectEntry("Make World Multiplayer", () -> {
				Game.setMenu(null);
				Analytics.LocalSession.ping();
				Network.startMultiplayerServer();
			}));
		}
		
		if(!Game.isValidClient()) {
			entries.add(new SelectEntry("Save Game", () -> {
				Game.setMenu(null);
				if(!Game.isValidServer())
					new Save(WorldSelectDisplay.getWorldName());
				else
					Game.server.saveWorld();
			}));
		}
		
		entries.addAll(Arrays.asList(
			new SelectEntry("Main Menu", () -> {
				ArrayList<ListEntry> items = new ArrayList<>();
				items.addAll(Arrays.asList(StringEntry.useLines(
					"Are you sure you want to",
					MyUtils.fromNetworkStatus("Exit the Game?", "Leave the Server?", "Close the Server?")
				)));
				
				if(!Game.isValidServer()) {
					int color = MyUtils.fromNetworkStatus(Color.RED, Color.GREEN, Color.TRANSPARENT);
					items.addAll(Arrays.asList(StringEntry.useLines(color, "",
						MyUtils.fromNetworkStatus("All unsaved progress", "Your progress", ""),
						MyUtils.fromNetworkStatus("will be lost!", "will be saved.", ""),
						""
					)));
				}
				
				items.add(new BlankEntry());
				items.add(new SelectEntry(Game.isValidServer()?"Cancel":"No", Game::exitMenu));
				
				if(Game.isValidServer())
					items.add(new SelectEntry("Save and Quit", () -> {
						Game.setMenu(new LoadingDisplay());
						new Save(WorldSelectDisplay.getWorldName());
						Game.setMenu(new TitleDisplay());
					}));
				
				items.add(new SelectEntry(Game.isValidServer()?"Quit without saving":"Yes", () -> Game.setMenu(new TitleDisplay())));
				
				Game.setMenu(new Display(false, true, new Menu.Builder(true, 8, RelPos.CENTER, items
				).createMenu()));
			}),
			
			new BlankEntry(),
			
			new StringEntry(upString, Color.GRAY),
			new StringEntry(selectString, Color.GRAY)
		));
		
		menus = new Menu[] {
			new Menu.Builder(true, 4, RelPos.CENTER, entries)
				.setTitle("Paused", Color.YELLOW)
				.createMenu()
		};
	}
	
	@Override
	public void init(Display parent) {
		super.init(null); // ignore; pause menus always lead back to the game
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		if (input.getKey("pause").clicked)
			Game.exitMenu();
	}
}
