package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;

public class PauseMenu extends SelectMenu {

	private int selection; //selection is set when you press enter.
	Player player;

	private static final String[] options = {"Return to Game", "Options", "Change Key Bindings", "Save Game", "Load Game", "Main Menu"};

	public PauseMenu(Player player) {
		super(Arrays.asList(options), 8*11 - 35, 6, Color.get(-1, 555), Color.get(-1, 222));
		this.player = player;
		selection = -1; // set to main pause menu options.
	}

	public void tick() {
		if (input.getKey("pause").clicked) {
			game.setMenu(null);
			return;
		}
		
		super.tick(); // process key input
		
		//choice chosen; input here is at confirm menu
		if (input.getKey("select").clicked) {
			
			//this one is an EXCEPTION: no comfirm menu.
			if (selected == 1) {
				//I bet this is used when exiting options menu to decide whether to go to title screen, or pause menu:
				game.setMenu(new OptionsMenu(this));
			}
			
			if (selected == 2) {
				game.setMenu(new KeyInputMenu(this));
				//selected = 0;
				//selection = -1;
			}
			
			if (selection == 3) { //save game option
				game.setMenu((Menu) null);
				new Save(player, WorldSelectMenu.worldname);
			}
			
			if (selection == 4) { //load game option; can't return
				WorldSelectMenu.loadworld = true;
				game.setMenu(new WorldSelectMenu());
			}
			
			if (selection == 5) //title menu
				game.setMenu(new TitleMenu());
			
			if (selected != 1 && selected != 2) selection = selected;
		}
		
		if (input.getKey("exit").clicked || selection == 0) game.setMenu((Menu) null);
	}

	public void render(Screen screen) {
		renderFrame(screen, "", 4, 2, 32, 20); // draw the blue menu frame.
		
		if (selection == -1) { // still displaying main options menu.
			super.render(screen); // render the main options menu.
			Font.drawCentered("Paused", screen, 35, Color.get(-1, 550));
			Font.drawCentered(input.getMapping("up")+" and "+input.getMapping("down")+" to Scroll", screen, 140, Color.get(-1, 333));
			Font.drawCentered(input.getMapping("select")+": Choose", screen, 150, Color.get(-1, 333));
		} else {
			ArrayList<String> confirmDialog = new ArrayList<String>();
			
			if (selection == 3) // save game
				confirmDialog.addAll(Arrays.asList(Font.getLines("Save Game?\n\n\nTip: press \"R\" to save in-game", 28*8, 18*8, 2)));
			else if (selection == 4) // load game
				confirmDialog.addAll(Arrays.asList(Font.getLines("Load Game?\nCurrent game will\nnot be saved", 28*8, 18*8, 2)));
			else if (selection == 5) // back to menu
				confirmDialog.addAll(Arrays.asList(Font.getLines("Back to Main Menu?\nCurrent game will\nnot be saved", 28*8, 18*8, 2)));
			
			for(int i = 0; i < confirmDialog.size(); i++) { // draws each line from above; the first line is white, and all the following lines are red.
				int col = i == 0 ? Color.get(-1, 555) : selection == 3 ? Color.get(-1, 333) : Color.get(-1, 500);
				Font.drawCentered(confirmDialog.get(i), screen, 60+i*10, col); // draw it centered.
			}
			int ypos = 70 + confirmDialog.size()*10; // start 20 below the last element...
			Font.drawCentered(input.getMapping("select")+": Yes", screen, ypos, Color.get(-1, 555));
			Font.drawCentered(input.getMapping("exit")+": No", screen, ypos+15, Color.get(-1, 555));
		}
	}
}
