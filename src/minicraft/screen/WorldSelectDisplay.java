package minicraft.screen;

import java.io.File;
import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.saveload.Version;
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
		Copy("C", Color.get(1, 0, 0, 255)),
		Rename("R", Color.get(1, 0, 255, 0)),
		Delete("D", Color.get(1, 255, 0, 0));
		
		public final String key;
		public final int color;
		
		Action(String key, int col) {
			this.key = key;
			this.color = col;
		}
		
		public static final Action[] values = Action.values();
	}
	
	private Action curAction = null;
	
	private static ArrayList<String> worldNames = null;
	private static ArrayList<Version> worldVersions = new ArrayList<>();
	
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
		
		worldVersions.clear();
		for (File listOfFile : listOfFiles) {
			if (listOfFile.isDirectory()) {
				String path = worldsDir + listOfFile.getName() + "/";
				File folder2 = new File(path);
				folder2.mkdirs();
				String[] files = folder2.list();
				if (files != null && files.length > 0 && files[0].endsWith(Save.extension)) {
					String name = listOfFile.getName();
					worldNames.add(name);
					//if(Game.debug) System.out.println("World found: " + name);
					worldVersions.add(new Load(name, false).getWorldVersion());
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
		super.init(parent);
		worldName = "";
		loadedWorld = true;
		
		ArrayList<String> worldNames = getWorldNames(true);
		
		SelectEntry[] entries = new SelectEntry[worldNames.size()];
		
		for(int i = 0; i < entries.length; i++) {
			String name = worldNames.get(i);
			final Version version = worldVersions.get(i);
			entries[i] = new SelectEntry(worldNames.get(i), () -> {
				if(curAction == null) {
					if(version.compareTo(Game.VERSION) > 0)
						return; // cannot load a game saved by a higher version!
					worldName = name;
					Game.setMenu(new LoadingDisplay());
				}
				else {
					Game.setMenu(new WorldEditDisplay(curAction, name));
					curAction = null;
				}
			}, false) {
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
		return loadedWorld;
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
		
		int sel = menus[0].getSelection();
		if(sel >= 0 && sel < worldVersions.size()) {
			Version version = worldVersions.get(sel);
			int col = Color.WHITE;
			if(version.compareTo(Game.VERSION) > 0) {
				col = Color.RED;
				Font.drawCentered(Localization.getLocalized("Higher version, cannot load world!"), screen, Font.textHeight() * 5, col);
			}
			Font.drawCentered(Localization.getLocalized("World Version:") + " " + (version.compareTo(new Version("1.9.2")) <= 0 ? "~" : "") + version, screen, Font.textHeight() * 7/2, col);
		}
		
		Font.drawCentered(Game.input.getMapping("select") + Localization.getLocalized(" to confirm"), screen, Screen.h - 60, Color.GRAY);
		Font.drawCentered(Game.input.getMapping("exit") + Localization.getLocalized(" to return"), screen, Screen.h - 40, Color.GRAY);
		
		String title = Localization.getLocalized("Select World");
		int color = Color.WHITE;
		
		if(curAction == null) {
			int y = Screen.h - Font.textHeight() * Action.values.length;
			
			for (Action a : Action.values) {
				Font.drawCentered(a.key + Localization.getLocalized(" to " + a), screen, y, a.color);
				y += Font.textHeight();
			}
		}
		else {
			title = Localization.getLocalized("Select a World to " + curAction);
			color = curAction.color;
		}
		
		Font.drawCentered(title, screen, 0, color);
	}
}
