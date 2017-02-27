package com.mojang.ld22.screen;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.screen.Menu;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.TitleMenu;
import com.mojang.ld22.sound.Sound;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldSelectMenu extends Menu {
	
	private Menu parent;
	
	private int selected = 0; //index of action; load or create.
	private int worldselected = 0;//index of selected world?
	public static boolean loadworld = false;
	boolean createworld = false;
	boolean delete = false;
	boolean rename = false;
	boolean fw = false;//="found-world"; tells if there are any pre-existing worlds.
	String name = "";
	boolean hasworld = false;
	String renamingworldname = "";
	String location = Game.gameDir + "/saves/";
	File folder;
	ArrayList<String> worldnames;
	public static String worldname = "";
	private static final String[] options = new String[]{"Load World", "New World"};
	public int tick;
	int wncol;
	
	public WorldSelectMenu(Menu parent) {
		this.parent = parent;//Always TitleMenu?
		tick = 0;
		wncol = Color.get(0, 5, 5, 5);
		
		//find worlds:
		worldnames = new ArrayList<String>();
		folder = new File(location);
		folder.mkdirs();
		File[] listOfFiles = folder.listFiles();
		
		for(int i = 0; i < listOfFiles.length; ++i) {
			if(listOfFiles[i].isDirectory()) {
				String path = location + listOfFiles[i].getName() + "/";
				File folder2 = new File(path);
				folder2.mkdirs();
				String[] Files = folder2.list();
				if(Files.length > 0 && Files[0].endsWith(".miniplussave")) {
					worldnames.add(listOfFiles[i].getName());
					System.out.println("World found: " + listOfFiles[i].getName());
				}
			}
		}
		
		if(worldnames.size() > 0) {
			fw = true;
		}
	}
	
	public void tick() {
		//loadworld, createworld, rename, and delete all start as false.
		
		//select action
		if(input.up.clicked)
			selected--;
		if(input.down.clicked)
			selected++;
		
		//automatically choose 
		if(!fw)
			selected = 1;
		
		byte len = 2; //only two choices
		if(selected < 0)
			selected += len;
			
		if(selected >= len)
			selected -= len;
		
		
		if(loadworld) {
			if(input.up.clicked)
				worldselected--;
			if(input.down.clicked)
				worldselected++;
			
			if(worldselected < 0)
				worldselected = 0;
			
			//prevent scrolling past the last one
			if(worldselected > worldnames.size() - 1)
				worldselected = worldnames.size() - 1;
		}
		
		if(createworld) {
			typename(); //check for input to type worldname
			if(input.pause.clicked) {
				//cancel to title screen
				createworld = false;
				loadworld = false;
				game.setMenu(new TitleMenu());
			}
			
			if(input.enter.clicked && wncol == Color.get(0, 5, 5, 5)) {
				//proceed to mode selection
				worldname = name;
				name = "";
				game.setMenu(new ModeMenu());
			}
		}
		
		File world;
		if(loadworld && input.menu.clicked && !rename) {
			if(!delete) {
				//load the game
				worldname = worldnames.get(worldselected);
				Sound.test.play();
				game.resetstartGame();
				game.setMenu((Menu)null);
			} else {
				//delete the world
				world = new File(location + "/" + worldnames.get(worldselected));
				System.out.println(world);
				File[] list = world.listFiles();
				
				for(int i = 0; i < list.length; i++) {
					list[i].delete();
				}
				
				world.delete();
				createworld = false;
				loadworld = false;
				if(worldnames.size() > 0) {
					game.setMenu(new WorldSelectMenu(parent));
				} else {
					game.setMenu(new TitleMenu());
				}
			}
		}
		
		if(input.attack.clicked && !rename && !createworld) {
			if(!delete && !rename) {
				//return to title screen
				createworld = false;
				loadworld = false;
				game.setMenu(new TitleMenu());
			} else if(delete) {
				//cancel delete
				delete = false;
			} else if(rename) {
				//cancel rename? never will happen...
				rename = false;
			}
		}
		
		if(input.d.clicked && !rename && !createworld)//toggle delete world mode
			delete = !delete;
		
		if(input.r.clicked && !rename && !createworld) {
			//toggle rename world mode
			if(!rename) {
				name = worldnames.get(worldselected);
				renamingworldname = name;
				rename = true;
			} else {
				rename = false;
			}
		}
		
		if(rename) {
			tick++;
			if(input.pause.clicked) {
				//cancel renaming
				tick = 0;
				rename = false;
			}
			
			if(input.enter.clicked && wncol == Color.get(0, 5, 5, 5)) {
				//user hits enter with a vaild new name; name is set here:
				worldname = name;
				name = "";
				world = new File(location + "/" + worldnames.get(worldselected));
				world.renameTo(new File(location + "/" + worldname));
				game.setMenu(new WorldSelectMenu(parent));
				tick = 0;
				rename = false;
			}
			
			if(tick > 1) { //currently renaming a world
				typename();
			}
		}
		
		if(!createworld && !loadworld) {
			//this executes at first, before you choose load or save
			if(input.menu.clicked) {
				System.out.println(selected);
				if(selected == 0) {
					loadworld = true;
					createworld = false;
				}
				
				if(selected == 1) {
					name = "";
					createworld = true;
					loadworld = false;
				}
			}
			
			if(input.attack.clicked) {
				//exit to title screen
				createworld = false;
				loadworld = false;
				game.setMenu(new TitleMenu());
			}
		}
		
	}
	
	public void render(Screen screen) {
		screen.clear(0);
		int col;
		if(!createworld && !loadworld) {
			byte var6 = 2;
			
			for(col = 0; col < var6; col++) {
				if(fw || col != 0) {
					String var7 = options[col];
					int col1 = Color.get(0, 222, 222, 222);
					if(col == selected) {
						var7 = "> " + var7 + " <";
						col1 = Color.get(0, 555, 555, 555);
					}
					
					if(!fw) {
						Font.draw(var7, screen, centertext(var7), 80, col1);
					} else {
						Font.draw(var7, screen, centertext(var7), 80 + col * 12, col1);
					}
				}
			}
			
			Font.draw("Arrow keys to move", screen, centertext("Arrow keys to move"), screen.h - 170, Color.get(0, 444, 444, 444));
			Font.draw("X to confirm", screen, centertext("X to confirm"), screen.h - 60, Color.get(0, 444, 444, 444));
			Font.draw("C to go back to the title screen", screen, centertext("C to go back to the title screen"), screen.h - 40, Color.get(0, 444, 444, 444));
		} else {
			String msg;
			if(createworld && !loadworld) {
				msg = "Name of New World";
				col = Color.get(-1, 555, 555, 555);
				Font.draw(msg, screen, centertext(msg), 20, col);
				Font.draw(name, screen, centertext(name), 50, wncol);
				Font.draw("A-Z, 0-9, up to 36 Characters", screen, centertext("A-Z, 0-9, up to 36 Characters"), 80, col);
				Font.draw("(Space + Backspace as well)", screen, centertext("(Space + Backspace as well)"), 92, col);
				if(wncol == Color.get(0, 500, 500, 500)) {
					if(!name.equals("")) {
						Font.draw("Cannot have 2 worlds", screen, centertext("Cannot have 2 worlds"), 120, wncol);
						Font.draw(" with the same name!", screen, centertext(" with the same name!"), 132, wncol);
					} else {
						Font.draw("Name cannot be blank!", screen, centertext("Name cannot be blank!"), 125, wncol);
					}
				}
				
				Font.draw("Press Enter to create", screen, centertext("Press Enter to create"), 162, col);
				Font.draw("Press Esc to cancel", screen, centertext("Press Esc to cancel"), 172, col);
			} else if(!createworld && loadworld) {
				msg = "Load World";
				col = Color.get(-1, 555, 555, 555);
				int col2 = Color.get(-1, 222, 222, 222);
				if(delete) {
					msg = "Delete World!";
					col = Color.get(-1, 500, 500, 500);
				}
				
				if(worldnames.size() > 0) {
					Font.draw(msg, screen, centertext(msg), 20, col);
					Font.draw(worldnames.get(worldselected), screen, centertext(worldnames.get(worldselected)), 80, col);
					if(worldselected > 0) {
						Font.draw(worldnames.get(worldselected - 1), screen, centertext(worldnames.get(worldselected - 1)), 70, col2);
					}
					
					if(worldselected > 1) {
						Font.draw(worldnames.get(worldselected - 2), screen, centertext(worldnames.get(worldselected - 2)), 60, col2);
					}
					
					if(worldselected > 2) {
						Font.draw(worldnames.get(worldselected - 3), screen, centertext(worldnames.get(worldselected - 3)), 50, col2);
					}
					
					if(worldselected < worldnames.size() - 1) {
						Font.draw(worldnames.get(worldselected + 1), screen, centertext(worldnames.get(worldselected + 1)), 90, col2);
					}
					
					if(worldselected < worldnames.size() - 2) {
						Font.draw(worldnames.get(worldselected + 2), screen, centertext(worldnames.get(worldselected + 2)), 100, col2);
					}
					
					if(worldselected < worldnames.size() - 3) {
						Font.draw(worldnames.get(worldselected + 3), screen, centertext(worldnames.get(worldselected + 3)), 110, col2);
					}
				} else {
					game.setMenu(new TitleMenu());
				}
				
				if(!delete && !rename) {
					Font.draw("Arrow keys to move", screen, centertext("Arrow keys to move"), screen.h - 44, Color.get(0, 444, 444, 444));
					Font.draw("X to confirm", screen, centertext("X to confirm"), screen.h - 32, Color.get(0, 444, 444, 444));
					Font.draw("C to go back to the title screen", screen, centertext("C to go back to the title screen"), screen.h - 20, Color.get(0, 444, 444, 444));
					Font.draw("D to delete a world", screen, centertext("D to delete a world"), screen.h - 70, Color.get(0, 400, 400, 400));
					Font.draw("R to rename world", screen, centertext("R to rename world"), screen.h - 60, Color.get(0, 40, 40, 40));
				} else if(delete) {
					Font.draw("X to delete", screen, centertext("X to delete"), screen.h - 48, Color.get(0, 444, 444, 444));
					Font.draw("C to cancel", screen, centertext("C to cancel"), screen.h - 36, Color.get(0, 444, 444, 444));
				} else if(rename) {
					screen.clear(0);
					Font.draw("Rename World", screen, centertext("Rename World"), 20, col);
					Font.draw(name, screen, centertext(name), 50, wncol);
					Font.draw("A-Z, 0-9, up to 36 Characters", screen, centertext("A-Z, 0-9, up to 36 Characters"), 80, col);
					Font.draw("(Space + Backspace as well)", screen, centertext("(Space + Backspace as well)"), 92, col);
					if(wncol == Color.get(0, 500, 500, 500)) {
						if(!name.equals("")) {
							Font.draw("Cannot have 2 worlds", screen, centertext("Cannot have 2 worlds"), 120, wncol);
							Font.draw(" with the same name!", screen, centertext(" with the same name!"), 132, wncol);
						} else {
							Font.draw("Name cannot be blank!", screen, centertext("Name cannot be blank!"), 125, wncol);
						}
					}
					
					Font.draw("Press Enter to rename", screen, centertext("Press Enter to rename"), 162, col);
					Font.draw("Press Esc to cancel", screen, centertext("Press Esc to cancel"), 172, col);
				}
			}
		}
	}/* helper function to keep from repeating code so much
	private void drawScreenText(String text, Screen screen, int height, Color color) {
		Font.draw(text, screen, centertext(text), height, color);
	}*/
	
	public void typename() {
		ArrayList<String> namedworldnames = new ArrayList<String>();
		if(createworld) {
			//typing name of new world
			if(worldnames.size() > 0) {
				for(int i = 0; i < worldnames.size(); i++) {
					if(!name.equals(worldnames.get(i).toLowerCase())) {
						wncol = Color.get(0, 5, 5, 5);
					} else {
						wncol = Color.get(0, 500, 500, 500);
						break;
					}
				}
			} else {
				wncol = Color.get(0, 5, 5, 5);
			}
		}
		
		if(rename) {
			//get all worldnames besides the one you're renaming
			for(int i = 0; i < worldnames.size(); i++) {
				if(!worldnames.get(i).equals(renamingworldname)) {
					namedworldnames.add(worldnames.get(i).toLowerCase());
				}
			}
			
			//check if worldname already exists
			if(namedworldnames.size() > 0) {
				for(int i = 0; i < namedworldnames.size(); i++) {
					if(name.toLowerCase().equals(namedworldnames.get(i).toLowerCase())) {
						wncol = Color.get(0, 500, 500, 500);
						break;
					}
					
					wncol = Color.get(0, 5, 5, 5);
				}
			} else {
				wncol = Color.get(0, 5, 5, 5);
			}
		}//end rename
		
		
		if(name.equals("")) //name cannot be blank
			wncol = Color.get(0, 500, 500, 500);
		
		if(input.backspace.clicked && name.length() > 0) //backspace support
			name = name.substring(0, name.length() - 1);
		
		if(name.length() < 36 && input.lastKeyTyped.length() > 0) {
			//ensure only valid characters are typed.
			java.util.regex.Pattern p = java.util.regex.Pattern.compile("[a-zA-Z0-9 ]");
			if(p.matcher(input.lastKeyTyped).matches())
				name += input.lastKeyTyped;
			input.lastKeyTyped = "";
		}
	}
}
	