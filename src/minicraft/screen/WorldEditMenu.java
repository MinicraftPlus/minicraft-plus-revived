package minicraft.screen;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minicraft.core.Game;
import minicraft.core.InputHandler;
import minicraft.gfx.Color;
import minicraft.screen.Menu.Builder;
import minicraft.screen.WorldSelectMenu.Action;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;

public class WorldEditMenu extends Display {
	
	/// this class will be used to enact the extra actions (copy, delete, rename) that you can do for worlds in the WorldSelectMenu.
	
	private Action action;
	private String worldName;
	
	private static final String worldsDir = Game.gameDir + "/saves/";
	
	public WorldEditMenu(Action action, String worldName) {
		super(true);
		this.action = action;
		this.worldName = worldName;
		
		Builder builder = new Builder(false, 8, RelPos.CENTER);
		ArrayList<ListEntry> entries = new ArrayList<>();
		
		if(action != Action.Delete) {
			List<String> names = WorldSelectMenu.getWorldNames();
			if(action == Action.Rename)
				names.remove(worldName);
			//String prompt = action == Action.Rename ? "New World Name:" : "New World Name";
			entries.add(new StringEntry("New World Name:", action.color));
			entries.add(WorldGenMenu.makeWorldNameInput("", names, worldName));
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
		
		if(input.getKey("select").clicked) {
			// do action
			InputEntry entry;
			File world = new File(worldsDir + worldName);
			switch (action) {
				case Delete:
					if(Game.debug) System.out.println("deleting world: " + world);
					File[] list = world.listFiles();
					for (int i = 0; i < list.length; i++) {
						list[i].delete();
					}
					world.delete();
					
					if(WorldSelectMenu.getWorldNames().size() > 0)
						Game.setMenu(new WorldSelectMenu());
					else
						Game.setMenu(new TitleMenu());
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
					try {
						Files.walkFileTree(world.toPath(), new FileVisitor<Path>() {
							public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
								String path = file.toString();
								path = path.replace(worldsDir+oldname, worldsDir+newname);
								File newFile = new File(path);
								newFile.mkdirs();
								if (Game.debug) System.out.println("copying file at \"" + file + "\" to path \"" + newFile + "\"...");
									try {
										Files.copy(file, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
									} catch(Exception ex) {
										ex.printStackTrace();
									}
								return FileVisitResult.CONTINUE;
							}
							public FileVisitResult preVisitDirectory(Path p, BasicFileAttributes bfa) {
								return FileVisitResult.CONTINUE;
							}
							public FileVisitResult postVisitDirectory(Path p, IOException ex) {
								return FileVisitResult.CONTINUE;
							}
							public FileVisitResult visitFileFailed(Path p, IOException ex) {
								return FileVisitResult.CONTINUE;
							}
						});
					} catch(Exception ex) {
						ex.printStackTrace();
					}
					
					Game.setMenu(new WorldSelectMenu());
					
				break;
				
				case Rename:
					entry = (InputEntry)menus[0].getCurEntry();
					if(!entry.isValid())
						break;
					//user hits enter with a vaild new name; name is set here:
					String name = entry.getUserInput();
					if (Game.debug) System.out.println("renaming world " + world + " to new name: " + name);
					world.renameTo(new File(worldsDir + name));
					Game.setMenu(new WorldSelectMenu());
				break;
			}
		}
		// Display class will take care to exiting
	}
	
}
