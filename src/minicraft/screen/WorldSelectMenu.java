package minicraft.screen;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;

public class WorldSelectMenu implements MenuData {
	
	// NOTE this will only be responsible for the world load selection screen.
	
	private static final String worldsDir = Game.gameDir + "/saves";
	
	private static String worldName = "";
	
	public static String getWorldName() { return worldName; }
	public static void setWorldName(String world) { worldName = world; }
	
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
	
	public WorldSelectMenu() {
		worldName = "";
	}
	
	public static boolean loadWorld() {
		return worldName.length() > 0;
	}
	
	@Override
	public Menu getMenu() {
		return new ScrollingMenu(this, true, 5, 1);
	}
	
	@Override
	public ListEntry[] getEntries() {
		ArrayList<String> worldNames = new ArrayList<>();
		
		//find worlds (init step):
		File folder = new File(worldsDir);
		folder.mkdirs();
		File[] listOfFiles = folder.listFiles();
		
		if(listOfFiles == null) {
			System.err.println("ERROR: Game location file folder is null, somehow...");
			return new ListEntry[0];
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
		
		SelectEntry[] entries = new SelectEntry[worldNames.size()];
		
		for(int i = 0; i < entries.length; i++) {
			String name = worldNames.get(i);
			entries[i] = new SelectEntry(worldNames.get(i), () -> {
				worldName = name;
				Game.setMenu(new LoadingDisplay());
			});
		}
		
		return entries;
	}
	
	@Override
	public void tick(InputHandler input) {
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
		Font.drawCentered(Game.input.getMapping("select")+" to confirm", screen, Screen.h - 60, Color.get(-1, 333));
		Font.drawCentered(Game.input.getMapping("exit")+" to return", screen, Screen.h - 40, Color.get(-1, 333));
		
		String title = "Select World";
		int color = Color.get(-1, 555);
		
		if(curAction == null) {
			int y = Game.HEIGHT - 18 - Font.textHeight() * Action.values.length;
			
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
		
		/*Font.drawCentered("C to copy", screen, Screen.h-26-8-8-8, Color.get(-1, Action.Copy.color));
		Font.drawCentered("R to rename", screen, Screen.h-26-8-8, Color.get(-1, Action.Rename.color));
		Font.drawCentered("D to delete", screen, Screen.h-26-8, Color.get(-1, Action.Delete.color));
		//Font.drawCentered("B to backup", screen, Screen.h-26, Color.get(-1, Action.Backup.color));
		*/
	}
	
	@Override
	public boolean clearScreen() { return true; }
	
	@Override
	public boolean centerEntries() {
		return true;
	}
	
	@Override
	public int getSpacing() {
		return 0;
	}
	
	@Override
	public Point getAnchor() {
		return new Point(Game.WIDTH/2, Game.HEIGHT/4);
	}
}
