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
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen.entry.StringEntry;

public class WorldSelectMenu extends ScrollingMenu {
	
	private static final StringEntry[] list = StringEntry.useStringArray("Load World", "New World");
	public static String worldname = "";
	
	public static boolean loadworld = false;
	
	private int selected = 0; //index of action; load or create.
	private String location = Game.gameDir + "/saves/";
	
	private ArrayList<StringEntry> worldnames;
	
	private String typingname = "";
	private boolean validName;
	private boolean confirmed;
	
	private enum Action {
		Main ("Select Option", 555),
		Create ("Name of New World:", 555),
		Load ("Load World", 555),
		Rename ("Rename World", 40),
		Delete ("Delete World", 500),
		Copy ("Copy World", 005);
		//Backup ("Backup World", 550);
		
		public String message;
		public int color;
		
		Action(String msg, int color) {
			message = msg;
			this.color = color;
		}
	}
	
	private Action mode;
	
	public WorldSelectMenu() {
		super(list);
		
		validName = false;
		confirmed = false;
		
		loadWorlds();
		
		if(worldnames.size() == 0) {
			mode = Action.Create;
			options = worldnames.toArray(new StringEntry[worldnames.size()]);
			selected = 0;
			if(!Game.HAS_GUI) {
				System.err.println("there are no worlds to load!");
				System.exit(1);
			}
		} else {
			mode = Action.Main;
			if(!Game.HAS_GUI)
				worldname = worldnames.get(0).getText();
		}
		
		if(!Game.HAS_GUI)
			loadworld = true; // you cannot really make a world without a GUI to start.
	}
	
	public void init(InputHandler input, Display parent) {
		super.init(input, parent);
		if(!Game.HAS_GUI) {
			if (Game.debug) System.out.println("gone through world select menu...");
			Game.setMenu(new LoadingDisplay()); // the choice has already been made, begin world load.
		}
	}
	
	private void loadWorlds() {
		//find worlds (init step):
		worldnames = new ArrayList<StringEntry>();
		File folder = new File(location);
		folder.mkdirs();
		File[] listOfFiles = folder.listFiles();
		
		if(listOfFiles == null) {
			System.err.println("ERROR: Game location file folder is null, somehow...");
			return;
		}
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isDirectory()) {
				String path = location + listOfFiles[i].getName() + "/";
				File folder2 = new File(path);
				folder2.mkdirs();
				String[] files = folder2.list();
				if (files != null && files.length > 0 && files[0].endsWith(".miniplussave")) {
					worldnames.add(new StringEntry(listOfFiles[i].getName()));
					if(Game.debug) System.out.println("World found: " + listOfFiles[i].getName());
				}
			}
		}
		
		/*if(location == Game.loadDir && !Game.loadDir.equals(Game.gameDir)) {
			location = Game.gameDir;
			loadWorlds();
			location = Game.loadDir;
		}*/
		
		while(selected > 1 && selected > worldnames.size()-1)
			selected--;
	}
	
	public void tick() {
		// we start on Main mode.
		
		//System.out.println("current tick mode: " + mode);
		
		//select action
		super.tick();
		
		if(mode == Action.Create || confirmed && (mode == Action.Rename || mode == Action.Copy))
			typename();
		else {
			if (input.getKey("up").clicked) selected--;
			if (input.getKey("down").clicked) selected++;
			
			int len = getCurOpts().length;
			
			if (selected < 0) selected += len;
			if (selected >= len) selected -= len;
		}
		
		if(mode == Action.Load) {
			//System.out.println("checking for load type change");
			if(input.getKey("d").clicked)
				mode = Action.Delete;
			if(input.getKey("r").clicked) {
				mode = Action.Rename;
				typingname = worldnames.get(selected).getText();
				//renamingworldname = name;
				//inputDelay = true;
			}
			//if(input.getKey("b").clicked)
				//mode = Action.Backup;
			if(input.getKey("c").clicked) {
				mode = Action.Copy;
				typingname = worldnames.get(selected)+" copy";
				//inputDelay = true;
			}
			confirmed = false;
		}
		
		if (input.getKey("select").clicked) {
			File world;
			
			if((mode == Action.Delete || mode == Action.Rename || mode == Action.Copy/* || mode == Action.Backup*/) && !confirmed) {
				confirmed = true;
				if (Game.debug) System.out.println("confirmed selection; worldname="+worldnames.get(selected));
				return;
			}
			
			switch(mode) {
				case Main: // if there's one option, this shouldn't even run.
					if (Game.debug) System.out.println("main selection: " + options[selected]);
					if(list[selected].getText().equals("Load World")) {
						mode = Action.Load;
						loadworld = true;
					} else {
						mode = Action.Create;
						loadworld = false;
					}
					confirmed = false;
					selected = 0;
					break;
				
				case Create:
					if (validName) {
						//proceed to mode selection
						worldname = typingname;
						if (Game.debug) System.out.println("create mode: " + worldname);
						Game.setMenu(new ModeMenu());
					}
					break;
				
				case Load:
					//load the game
					worldname = worldnames.get(selected).getText();
					if (Game.debug) System.out.println("load mode: " + worldname);
					Sound.confirm.play();
					Game.setMenu(new LoadingDisplay());
					//Game.initWorld();
					//Game.setMenu((Menu) null);
					break;
				
				case Rename:
					if(!validName) break;
					//user hits enter with a vaild new name; name is set here:
					worldname = typingname;
					typingname = "";
					world = new File(location + "/" + worldnames.get(selected));
					if (Game.debug) System.out.println("renaming world " + world + " to new name: " + worldname);
					world.renameTo(new File(location + "/" + worldname));
					//Game.setMenu(new WorldSelectMenu());
					loadWorlds();
					mode = Action.Load;
					break;
				
				case Copy:
					if(!validName) break;
					//user hits enter with a vaild new name; copy is created here.
					worldname = typingname;
					typingname = "";
					String oldname = worldnames.get(selected).getText();
					String oldpath = location + "/" + oldname;
					world = new File(oldpath);
					String newpath = location + "/" + worldname;
					File newworld = new File(newpath);
					newworld.mkdirs();
					if (Game.debug) System.out.println("copying world " + world + " to world " + newworld);
					try {
						Files.walkFileTree(world.toPath(), new FileVisitor() {
							public FileVisitResult visitFile(Object file, BasicFileAttributes attr) {
								String path = file.toString();
								path = newpath+path.substring(path.indexOf(oldname)+oldname.length());
								File newFile = new File(path);
								newFile.mkdirs();
								if (Game.debug) System.out.println("copying file at \"" + oldpath + "\" to path \"" + newFile + "\"...");
								try {
									Files.copy(((Path)file), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
								} catch(Exception ex) {
									ex.printStackTrace();
								}
								return FileVisitResult.CONTINUE;
							}
							
							public FileVisitResult preVisitDirectory(Object o, BasicFileAttributes bfa) {
								return FileVisitResult.CONTINUE;
							}
							public FileVisitResult postVisitDirectory(Object o, IOException ex) {
								return FileVisitResult.CONTINUE;
							}
							public FileVisitResult visitFileFailed(Object o, IOException ex) {
								return FileVisitResult.CONTINUE;
							}
						});
						//Files.copy(world.toPath(), newworld.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch(Exception ex) {
						ex.printStackTrace();
					}
					
					loadWorlds();
					mode = Action.Load;
					//Game.setMenu(new WorldSelectMenu());
					break;
				
				case Delete:
					//delete the world
					world = new File(location + "/" + worldnames.get(selected));
					if(Game.debug) System.out.println("deleting world: " + world);
					File[] list = world.listFiles();
					if(list != null) {
						for (int i = 0; i < list.length; i++) {
							list[i].delete();
						}
					}
					
					world.delete();
					//createworld = false;
					//loadworld = false;
					loadWorlds();
					if (worldnames.size() > 0) {
						//Game.setMenu(new WorldSelectMenu());
						mode = Action.Load;
					} else {
						Game.setMenu(new TitleMenu());
					}
					break;
				
				//case Backup:
					//if (Game.debug) System.out.println("backup world mode");
					//world = new File(location + "/" + worldnames.get(selected));
					//JFileChooser jfc = new JFileChooser();
					//int sel = jfc.showOpenDialog(game);
					//break;
			}
		}
		
		if(input.getKey("exit").clicked) {
			switch(mode) {
				case Delete: case Rename: case Copy:// case Backup:
					mode = Action.Load;
					break;
				
				default:
					Game.setMenu(new TitleMenu());
					loadworld = false;
			}
		}
	}
	
	private StringEntry[] getCurOpts() {
		switch(mode) {
			case Main: return list;//(StringEntry[])this.options;
			case Create: return new StringEntry[0];
			default: return worldnames.toArray(new StringEntry[0]);
		}
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		StringEntry[] opts = getCurOpts();
		
		setHighlightColor(Color.get(-1, mode.color));
		setOffColor(Color.get(-1, 222));
		int controlCol = Color.get(-1, 333);
		
		String msg = mode.message;
		
		if(mode == Action.Main) {
			/*for (int i = 0; i < opts.length; i++) {
				String curOption = opts[i].getText();
				int col = fadeCol;
				if (i == selected) {
					curOption = "> " + curOption + " <";
					col = mainCol;
				}
				
				if (worldnames.size() == 0) {
					Font.drawCentered(curOption, screen, 80, col);
				} else {
					Font.drawCentered(curOption, screen, 80 + i*12, col);
				}
			}*/
			
			super.render(screen);
			
			//Font.drawCentered(input.getMapping("up")+" and "+input.getMapping("down")+" to move", screen, Screen.h - 170, controlCol);
			Font.drawCentered(input.getMapping("select")+" to confirm", screen, Screen.h - 60, controlCol);
			Font.drawCentered(input.getMapping("exit")+" to return", screen, Screen.h - 40, controlCol);
		}
		else if (worldnames.size() > 0 && mode != Action.Create && (!confirmed || mode != Action.Rename && mode != Action.Delete && mode != Action.Copy/* && mode != Action.Backup*/)) {
			if(mode != Action.Load)
				setOffColor(Color.get(-1, 555));
			
			//System.out.println("drawing worlds, mode="+mode + " -- selected: " + worldnames.get(selected));
			
			/*Font.drawCentered(worldnames.get(selected), screen, 80, mainCol);
			
			if (selected > 0) Font.drawCentered(worldnames.get(selected - 1), screen, 70, fadeCol);
			if (selected > 1) Font.drawCentered(worldnames.get(selected - 2), screen, 60, fadeCol);
			if (selected > 2) Font.drawCentered(worldnames.get(selected - 3), screen, 50, fadeCol);
			
			if (selected < worldnames.size() - 1)
				Font.drawCentered(worldnames.get(selected + 1), screen, 90, fadeCol);
			if (selected < worldnames.size() - 2)
				Font.drawCentered(worldnames.get(selected + 2), screen, 100, fadeCol);
			if (selected < worldnames.size() - 3)
				Font.drawCentered(worldnames.get(selected + 3), screen, 110, fadeCol);*/
			super.render(screen);
		}
		
		if(mode == Action.Create || confirmed && (mode == Action.Rename || mode == Action.Copy)) {
			Font.drawCentered("A-Z, 0-9, up to 36 Characters", screen, 80, controlCol);
			Font.drawCentered("(Space + Backspace as well)", screen, 92, controlCol);
			
			if(mode != Action.Create)
				msg += ": "+worldnames.get(selected);
			
			int worldnameCol = Color.get(-1, 005);
			if (!validName) {
				worldnameCol = Color.get(-1, 500);
				if (!typingname.equals("")) {
					Font.drawCentered("Cannot have 2 worlds", screen, 120, worldnameCol);
					Font.drawCentered(" with the same name!", screen, 132, worldnameCol);
				} else {
					Font.drawCentered("Name cannot be blank!", screen, 125, worldnameCol);
				}
			}
			Font.drawCentered(typingname, screen, 50, worldnameCol);
		}
		
		if(confirmed && mode == Action.Delete) {
			Font.drawCentered("Are you sure you want to delete", screen, Screen.h/2-10, Color.get(-1, 533));
			Font.drawCentered("\'"+worldnames.get(selected).getText()+"\'?", screen, Screen.h/2, Color.get(-1, 555));
			Font.drawCentered("This cannot be undone!", screen, Screen.h/2+10, Color.get(-1, 533));
		}
		
		Font.drawCentered(msg, screen, 20, mode.color);
		
		if(mode == Action.Load) {
			Font.drawCentered("C to copy", screen, Screen.h-26-8-8-8, Color.get(-1, Action.Copy.color));
			Font.drawCentered("R to rename", screen, Screen.h-26-8-8, Color.get(-1, Action.Rename.color));
			Font.drawCentered("D to delete", screen, Screen.h-26-8, Color.get(-1, Action.Delete.color));
			//Font.drawCentered("B to backup", screen, Screen.h-26, Color.get(-1, Action.Backup.color));
		}
		
		if(mode != Action.Main) {
			Font.drawCentered("Press "+input.getMapping("select")+" to " + mode.name() + " World", screen, Screen.h - 16, controlCol);
			Font.drawCentered("Press "+input.getMapping("exit")+" to cancel", screen, Screen.h - 8, controlCol);
		}
	}
	
	private void typename() {
		//System.out.println("listening to type keypress...");
		ArrayList<String> invalidNames = new ArrayList<String>();
		for(StringEntry wname: worldnames) {
			/// this will add all the names, unless we are renaming, in which case the current name is okay, and is not added.
			if(!(wname.getText().equals(worldname) && mode != Action.Rename && mode != Action.Copy)) {
				//System.out.println("adding invalid name: " + wname);
				invalidNames.add(wname.getText());
			}
		}
		
		if (input.getKey("backspace").clicked && typingname.length() > 0) //backspace support
			typingname = typingname.substring(0, typingname.length() - 1);
		
		if (typingname.length() < 36 && input.lastKeyTyped.length() > 0) {
			//ensure only valid characters are typed.
			java.util.regex.Pattern p = java.util.regex.Pattern.compile("[a-zA-Z0-9 ]"); // this does not include backspace, so it will not be "typed".
			if (p.matcher(input.lastKeyTyped).matches()) typingname += input.lastKeyTyped;
			input.lastKeyTyped = "";
		}
		
		//if (createworld) {
		
		if (typingname.equals("")) {//name cannot be blank
			validName = false;
			return;
		}
		
		validName = true;
		
		if (invalidNames.size() > 0) {
			for (int i = 0; i < invalidNames.size(); i++) {
				if (typingname.equalsIgnoreCase(invalidNames.get(i))) {
					validName = false;
					return;
				}
			}
		}
	}
}
