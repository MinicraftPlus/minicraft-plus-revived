package minicraft.screen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minicraft.core.FileHandler;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Color;
import minicraft.screen.Menu.Builder;
import minicraft.screen.WorldSelectDisplay.Action;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;

public class WorldEditDisplay extends Display {
	
	/// this class will be used to enact the extra actions (copy, delete, rename) that you can do for worlds in the WorldSelectMenu.
	
	private Action action;
	private String worldName;
	
	private static final String worldsDir = Game.gameDir + "/saves/";
	
	public WorldEditDisplay(Action action, String worldName) {
		super(true);
		this.action = action;
		this.worldName = worldName;
		
		Builder builder = new Builder(false, 8, RelPos.CENTER);
		ArrayList<ListEntry> entries = new ArrayList<>();
		
		if(action != Action.Delete) {
			List<String> names = WorldSelectDisplay.getWorldNames();
			if(action == Action.Rename)
				names.remove(worldName);
			entries.add(new StringEntry("New World Name:", action.color));
			entries.add(WorldGenDisplay.makeWorldNameInput("", names, worldName));
		} else {
			entries.addAll(Arrays.asList(
				new StringEntry("Are you sure you want to delete", action.color),
				new StringEntry("\""+worldName+"\"?", Color.tint(action.color, 1, true)),
				new StringEntry("This can not be undone!", action.color)
			));
		}
		
		entries.addAll(Arrays.asList(StringEntry.useLines(Color.WHITE, "",
			Game.input.getMapping("select")+" to confirm",
			Game.input.getMapping("exit")+" to cancel"
		)));
		
		builder.setEntries(entries);
		
		menus = new Menu[] {builder.createMenu()};
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		
		if(input.getKey("select").clicked) { // do action
			InputEntry entry;

			// The location of the world folder on the disk.
			File world = new File(worldsDir + worldName);

			// Do the action.
			switch (action) {
				case Delete:
					if(Game.debug) System.out.println("deleting world: " + world);
					File[] list = world.listFiles();
					for (File file : list) {
						file.delete();
					}
					world.delete();
					
					WorldSelectDisplay.refreshWorldNames();
					if(WorldSelectDisplay.getWorldNames().size() > 0)
						Game.setMenu(new WorldSelectDisplay());
					else
						Game.setMenu(new PlayDisplay());
				break;
				
				case Copy:
					entry = (InputEntry)menus[0].getCurEntry();
					if(!entry.isValid())
						break;
					//user hits enter with a valid new name; copy is created here.
					String oldname = worldName;
					String newname = entry.getUserInput();
					File newworld = new File(worldsDir + newname);
					newworld.mkdirs();
					if (Game.debug) System.out.println("copying world " + world + " to world " + newworld);
					// walk file tree
					try {
						FileHandler.copyFolderContents(new File(worldsDir+oldname).toPath(), newworld.toPath(), FileHandler.REPLACE_EXISTING, false);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Game.exitMenu();
					
				break;
				
				case Rename:
					entry = (InputEntry)menus[0].getCurEntry();
					if(!entry.isValid())
						break;

					// User hits enter with a vaild new name; name is set here:
					String name = entry.getUserInput();

					// Try to rename the file, if it works, return
					if (world.renameTo(new File(worldsDir + name))) {
						if (Game.debug) System.out.println("Renaming world " + world + " to new name: " + name);
					} else {
						System.err.println("ERROR: Rename failed in WorldEditDisplay.");
					}

					Game.exitMenu();
				break;
			}
		}
		// Display class will take care of exiting
	}
}
