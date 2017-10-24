package minicraft.screen;

import java.io.File;
import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.core.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.screen.entry.SelectEntry;

public class WorldSelectDisplay extends Display {
	
	// NOTE this will only be responsible for the world load selection screen.
	
	private static final String worldsDir = Game.gameDir + "/saves/";
	
	private static String worldName = "";
	private static boolean loadedWorld = true;
	
	public static String getWorldName() { return worldName; }
	public static void setWorldName(String world, boolean loaded) {
		worldName = world;
		loadedWorld = loaded;
	}
	
	enum Action {
		Copy("C", 5),
		Rename("R", 50),
		Delete("D", 500);
		
		public final String key;
		public final int color;
		
		Action(String key, int col) {
			this.key = key;
			this.color = Color.get(-1, col);
		}
		
		public static final Action[] values = Action.values();
	}
	
	private Action curAction = null;
	
	private static ArrayList<String> worldNames = null;
	
	public static void refreshWorldNames() { worldNames = null; }
	
	public static ArrayList<String> getWorldNames() { return getWorldNames(false); }
	private static ArrayList<String> getWorldNames(boolean recalc) {
		ArrayList<String> worldNames = new ArrayList<>();
		
		if(!recalc && WorldSelectDisplay.worldNames != null) {
			worldNames.addAll(WorldSelectDisplay.worldNames);
			return worldNames;
		}
		
		//find worlds (init step):
		File folder = new File(worldsDir);
		folder.mkdirs();
		File[] listOfFiles = folder.listFiles();
		
		if(listOfFiles == null) {
			System.err.println("ERROR: Game location file folder is null, somehow...");
			return new ArrayList<>();
		}
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isDirectory()) {
				String path = worldsDir + listOfFiles[i].getName() + "/";
				File folder2 = new File(path);
				folder2.mkdirs();
				String[] files = folder2.list();
				if (files != null && files.length > 0 && files[0].endsWith(Save.extension)) {
					worldNames.add(listOfFiles[i].getName());
					if(Game.debug) System.out.println("World found: " + listOfFiles[i].getName());
				}
			}
		}
		
		if(WorldSelectDisplay.worldNames == null)
			WorldSelectDisplay.worldNames = new ArrayList<>();
		else
			WorldSelectDisplay.worldNames.clear();
		
		WorldSelectDisplay.worldNames.addAll(worldNames);
		
		return worldNames;
	}
	
	public WorldSelectDisplay() {
		super(true);
	}
	
	@Override
	public void init(Display parent) {
		super.init(parent instanceof TitleDisplay ? parent : new TitleDisplay());
		worldName = "";
		loadedWorld = true;
		
		ArrayList<String> worldNames = getWorldNames(true);
		
		SelectEntry[] entries = new SelectEntry[worldNames.size()];
		
		for(int i = 0; i < entries.length; i++) {
			String name = worldNames.get(i);
			entries[i] = new SelectEntry(worldNames.get(i), () -> {
				if(curAction == null) {
					worldName = name;
					Game.setMenu(new LoadingDisplay());
				}
				else {
					Game.setMenu(new WorldEditDisplay(curAction, name));
					curAction = null;
				}
			}) {
				@Override
				public int getColor(boolean isSelected) {
					if(curAction != null && isSelected)
						return curAction.color;
					else
						return super.getColor(isSelected);
				}
			};
		}
		
		
		
		menus = new Menu[] {
			new Menu.Builder(false, 0, RelPos.CENTER, entries)
				.setDisplayLength(5)
				.setScrollPolicies(1, true)
				.createMenu()
		};
	}
	
	public static boolean loadedWorld() {
		return loadedWorld;//worldName.length() > 0;
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		
		if(curAction != null) return;
		
		for(Action a: Action.values) {
			if(input.getKey(a.key).clicked) {
				curAction = a;
				break;
			}
		}
	}
	
	@Override
	public void render(Screen screen) {
		super.render(screen);
		
		Font.drawCentered(Game.input.getMapping("select")+" to confirm", screen, Screen.h - 60, Color.GRAY);
		Font.drawCentered(Game.input.getMapping("exit")+" to return", screen, Screen.h - 40, Color.GRAY);
		
		String title = "Select World";
		int color = Color.WHITE;
		
		if(curAction == null) {
			int y = Screen.h - Font.textHeight() * Action.values.length;
			
			for (Action a : Action.values) {
				Font.drawCentered(a.key + " to " + a, screen, y, a.color);
				y += Font.textHeight();
			}
		}
		else {
			title = "Select a World to " + curAction;
			color = curAction.color;
		}
		
		Font.drawCentered(title, screen, 0, color);
	}
}
