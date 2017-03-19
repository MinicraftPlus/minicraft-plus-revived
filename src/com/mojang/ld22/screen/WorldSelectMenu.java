//new class; no comments
package com.mojang.ld22.screen;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.sound.Sound;
import java.io.File;
import java.util.ArrayList;

public class WorldSelectMenu extends Menu {

	private Menu parent;

	private int selected = 0; //index of action; load or create.
	private int worldselected = 0; //index of selected world?
	public static boolean loadworld = false;
	boolean createworld = false;
	boolean delete = false;
	boolean rename = false;
	boolean fw = false; //="found-world"; tells if there are any pre-existing worlds.
	String name = "";
	boolean hasworld = false;
	String renamingworldname = "";
	String location = Game.gameDir + "/saves/";
	File folder;
	ArrayList<String> worldnames;
	public static String worldname = "";
	private static final String[] options = new String[] {"Load World", "New World"};
	public int tick;
	int wncol;

	public WorldSelectMenu(Menu parent) {
		this.parent = parent; //Always TitleMenu?
		tick = 0;
		wncol = Color.get(0, 5, 5, 5);

		//find worlds:
		worldnames = new ArrayList<String>();
		folder = new File(location);
		folder.mkdirs();
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; ++i) {
			if (listOfFiles[i].isDirectory()) {
				String path = location + listOfFiles[i].getName() + "/";
				File folder2 = new File(path);
				folder2.mkdirs();
				String[] Files = folder2.list();
				if (Files.length > 0 && Files[0].endsWith(".miniplussave")) {
					worldnames.add(listOfFiles[i].getName());
					System.out.println("World found: " + listOfFiles[i].getName());
				}
			}
		}

		if (worldnames.size() > 0) {
			fw = true;
		}
	}

	public void tick() {
		//loadworld, createworld, rename, and delete all start as false.

		//select action
		if (input.getKey("up").clicked) selected--;
		if (input.getKey("down").clicked) selected++;

		//automatically choose
		if (!fw) selected = 1;

		byte len = 2; //only two choices
		if (selected < 0) selected += len;

		if (selected >= len) selected -= len;

		if (loadworld) {
			if (input.getKey("up").clicked) worldselected--;
			if (input.getKey("down").clicked) worldselected++;

			if (worldselected < 0) worldselected = 0;

			//prevent scrolling past the last one
			if (worldselected > worldnames.size() - 1) worldselected = worldnames.size() - 1;
		}

		if (createworld) {
			typename(); //check for input to type worldname
			if (input.getKey("escape").clicked) {
				//cancel to title screen
				createworld = false;
				loadworld = false;
				game.setMenu(new TitleMenu());
			}

			if (input.getKey("enter").clicked && wncol == Color.get(0, 5, 5, 5)) {
				//proceed to mode selection
				worldname = name;
				name = "";
				game.setMenu(new ModeMenu());
			}
		}

		File world;
		if (loadworld && input.getKey("enter").clicked && !rename) {
			if (!delete) {
				//load the game
				worldname = worldnames.get(worldselected);
				Sound.test.play();
				game.resetstartGame();
				game.setMenu((Menu) null);
			} else {
				System.out.println("delete mode");
				//delete the world
				world = new File(location + "/" + worldnames.get(worldselected));
				System.out.println(world);
				File[] list = world.listFiles();

				for (int i = 0; i < list.length; i++) {
					list[i].delete();
				}

				world.delete();
				createworld = false;
				loadworld = false;
				if (worldnames.size() > 0) {
					game.setMenu(new WorldSelectMenu(parent));
				} else {
					game.setMenu(new TitleMenu());
				}
			}
		}

		if (input.getKey("escape").clicked && !rename && !createworld) {
			if (!delete && !rename) {
				//return to title screen
				createworld = false;
				loadworld = false;
				game.setMenu(new TitleMenu());
			} else if (delete) {
				//cancel delete
				delete = false;
			} else if (rename) {
				//cancel rename? never will happen...
				rename = false;
			}
		}

		if (input.getKey("d").clicked && !rename && !createworld) { //toggle delete world mode
			System.out.println("toggle delete");
			delete = !delete;
		}

		if (input.getKey("r").clicked && !rename && !createworld) {
			//toggle rename world mode
			if (!rename) {
				name = worldnames.get(worldselected);
				renamingworldname = name;
				rename = true;
			} else {
				rename = false;
			}
		}

		if (rename) {
			tick++;
			if (input.getKey("pause").clicked) {
				//cancel renaming
				tick = 0;
				rename = false;
			}

			if (input.getKey("enter").clicked && wncol == Color.get(0, 5, 5, 5)) {
				//user hits enter with a vaild new name; name is set here:
				worldname = name;
				name = "";
				world = new File(location + "/" + worldnames.get(worldselected));
				world.renameTo(new File(location + "/" + worldname));
				game.setMenu(new WorldSelectMenu(parent));
				tick = 0;
				rename = false;
			}

			if (tick > 1) { //currently renaming a world
				typename();
			}
		}

		if (!createworld && !loadworld) {
			//this executes at first, before you choose load or save
			if (input.getKey("enter").clicked) {
				System.out.println(selected);
				if (selected == 0) {
					loadworld = true;
					createworld = false;
				}

				if (selected == 1) {
					name = "";
					createworld = true;
					loadworld = false;
				}
			}

			if (input.getKey("escape").clicked) {
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
		if (!createworld && !loadworld) {
			byte var6 = 2;

			for (col = 0; col < var6; col++) {
				if (fw || col != 0) {
					String var7 = options[col];
					int col1 = Color.get(0, 222, 222, 222);
					if (col == selected) {
						var7 = "> " + var7 + " <";
						col1 = Color.get(0, 555, 555, 555);
					}

					if (!fw) {
						drawCentered(var7, screen, 80, col1);
					} else {
						drawCentered(var7, screen, 80 + col * 12, col1);
					}
				}
			}

			drawCentered("Arrow keys to move", screen, screen.h - 170, Color.get(0, 444, 444, 444));
			drawCentered("Enter to confirm", screen, screen.h - 60, Color.get(0, 444, 444, 444));
			drawCentered(
					"Esc to go back to the title screen", screen, screen.h - 40, Color.get(0, 444, 444, 444));
		} else {
			String msg;
			if (createworld && !loadworld) {
				msg = "Name of New World";
				col = Color.get(-1, 555, 555, 555);
				drawCentered(msg, screen, 20, col);
				drawCentered(name, screen, 50, wncol);
				drawCentered("A-Z, 0-9, up to 36 Characters", screen, 80, col);
				drawCentered("(Space + Backspace as well)", screen, 92, col);
				if (wncol == Color.get(0, 500, 500, 500)) {
					if (!name.equals("")) {
						drawCentered("Cannot have 2 worlds", screen, 120, wncol);
						drawCentered(" with the same name!", screen, 132, wncol);
					} else {
						drawCentered("Name cannot be blank!", screen, 125, wncol);
					}
				}

				drawCentered("Press Enter to create", screen, 162, col);
				drawCentered("Press Esc to cancel", screen, 172, col);
			} else if (!createworld && loadworld) {
				msg = "Load World";
				col = Color.get(-1, 555, 555, 555);
				int col2 = Color.get(-1, 222, 222, 222);
				if (delete) {
					msg = "Delete World!";
					col = Color.get(-1, 500, 500, 500);
				}

				if (worldnames.size() > 0) {
					drawCentered(msg, screen, 20, col);
					drawCentered(worldnames.get(worldselected), screen, 80, col);
					if (worldselected > 0) drawCentered(worldnames.get(worldselected - 1), screen, 70, col2);

					if (worldselected > 1) drawCentered(worldnames.get(worldselected - 2), screen, 60, col2);

					if (worldselected > 2) drawCentered(worldnames.get(worldselected - 3), screen, 50, col2);

					if (worldselected < worldnames.size() - 1)
						drawCentered(worldnames.get(worldselected + 1), screen, 90, col2);

					if (worldselected < worldnames.size() - 2)
						drawCentered(worldnames.get(worldselected + 2), screen, 100, col2);

					if (worldselected < worldnames.size() - 3)
						drawCentered(worldnames.get(worldselected + 3), screen, 110, col2);

				} else {
					game.setMenu(new TitleMenu());
				}

				if (!delete && !rename) {
					drawCentered("Arrow keys to move", screen, screen.h - 44, Color.get(0, 444, 444, 444));
					drawCentered("Enter to confirm", screen, screen.h - 32, Color.get(0, 444, 444, 444));
					drawCentered(
							"Esc to go back to the title screen",
							screen,
							screen.h - 20,
							Color.get(0, 444, 444, 444));
					drawCentered("D to delete a world", screen, screen.h - 70, Color.get(0, 400, 400, 400));
					drawCentered("R to rename world", screen, screen.h - 60, Color.get(0, 40, 40, 40));
				} else if (delete) {
					drawCentered("Enter to delete", screen, screen.h - 48, Color.get(0, 444, 444, 444));
					drawCentered("Esc to cancel", screen, screen.h - 36, Color.get(0, 444, 444, 444));
				} else if (rename) {
					screen.clear(0);
					drawCentered("Rename World", screen, 20, col);
					drawCentered(name, screen, 50, wncol);
					drawCentered("A-Z, 0-9, up to 36 Characters", screen, 80, col);
					drawCentered("(Space + Backspace as well)", screen, 92, col);

					if (wncol == Color.get(0, 500, 500, 500)) {
						if (!name.equals("")) {
							drawCentered("Cannot have 2 worlds", screen, 120, wncol);
							drawCentered(" with the same name!", screen, 132, wncol);
						} else {
							drawCentered("Name cannot be blank!", screen, 125, wncol);
						}
					}

					drawCentered("Press Enter to rename", screen, 162, col);
					drawCentered("Press Esc to cancel", screen, 172, col);
				}
			}
		}
	}

	public void typename() {
		ArrayList<String> namedworldnames = new ArrayList<String>();
		if (createworld) {
			//typing name of new world
			if (worldnames.size() > 0) {
				for (int i = 0; i < worldnames.size(); i++) {
					if (!name.equals(worldnames.get(i).toLowerCase())) {
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

		if (rename) {
			//get all worldnames besides the one you're renaming
			for (int i = 0; i < worldnames.size(); i++) {
				if (!worldnames.get(i).equals(renamingworldname)) {
					namedworldnames.add(worldnames.get(i).toLowerCase());
				}
			}

			//check if worldname already exists
			if (namedworldnames.size() > 0) {
				for (int i = 0; i < namedworldnames.size(); i++) {
					if (name.toLowerCase().equals(namedworldnames.get(i).toLowerCase())) {
						wncol = Color.get(0, 500, 500, 500);
						break;
					}

					wncol = Color.get(0, 5, 5, 5);
				}
			} else {
				wncol = Color.get(0, 5, 5, 5);
			}
		} //end rename

		if (name.equals("")) //name cannot be blank
		wncol = Color.get(0, 500, 500, 500);

		if (input.getKey("backspace").clicked && name.length() > 0) //backspace support
		name = name.substring(0, name.length() - 1);

		if (name.length() < 36 && input.lastKeyTyped.length() > 0) {
			//ensure only valid characters are typed.
			java.util.regex.Pattern p = java.util.regex.Pattern.compile("[a-zA-Z0-9 ]");
			if (p.matcher(input.lastKeyTyped).matches()) name += input.lastKeyTyped;
			input.lastKeyTyped = "";
		}
	}
}
