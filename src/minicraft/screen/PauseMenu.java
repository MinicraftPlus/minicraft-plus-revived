package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;

public class PauseMenu extends SelectMenu {

	private int selection; //selection is set when you press enter.
	private Menu parent;

	private static final List<String> alloptions = Arrays.asList(new String[] {"Return to Game", "Options", "Change Key Bindings", "Make World Multiplayer", "Save Game", "Load Game", "Main Menu"});
	//private List<String> options = new ArrayList<String>();
	
	private static final List<String> getOptions() {
		List<String> options = new ArrayList<String>();
		options.addAll(alloptions);
		
		if(Game.ISONLINE) {
			options.remove("Make World Multiplayer");
			
			if(!Game.isValidServer())
				options.remove("Save Game");
		}
		
		return options;
	}
	
	public PauseMenu(Menu parent) {
		super(getOptions(), 8*11 - 35, 4, Color.get(-1, 555), Color.get(-1, 222));
		selection = -1; // set to main pause menu options.
		this.parent = parent;
	}

	public void tick() {
		if (input.getKey("pause").clicked) {
			game.setMenu(parent);
			return;
		}
		
		super.tick(); // process key input
		
		//choice chosen; input here is at confirm menu
		if (input.getKey("select").clicked) {
			String chosen = options.get(selected);
			String confirmed = selection >= 0 ? options.get(selection) : "";
			
			switch(chosen) {
				case "Return to Game": game.setMenu(parent); return;
				case "Options": game.setMenu(new OptionsMenu(this)); return;
				case "Change Key Bindings": game.setMenu(new KeyInputMenu(this)); return;
				default:
					selection = selected; // for any other choice, this progresses a choice to a confirmation.
			}
			
			switch(confirmed) {
				case "Save Game":
					game.setMenu(null);
					if(!Game.isValidServer())
						new Save(game.player, WorldSelectMenu.worldname);
					else
						Game.server.saveWorld();
				return;
				
				case "Load World":
					WorldSelectMenu.loadworld = true;
					game.setMenu(new WorldSelectMenu());
				return;
				
				case "Make World Multiplayer":
					game.setMenu(null);
					//new Save(game.player, WorldSelectMenu.worldname);
					game.startMultiplayerServer();
				return;
				
				case "Main Menu":
					game.setMenu(new TitleMenu());
				return;
			}
		}
		
		if (input.getKey("exit").clicked)
			game.setMenu(parent);
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
			String selection = options.get(this.selection);
			int msgColor = Color.get(-1, 500);
			
			if (selection.equals("Save Game")) {// save game
				msgColor = Color.get(-1, 333);
				confirmDialog.addAll(Arrays.asList(Font.getLines("Save Game?\n\n\nTip: press \"R\" to save in-game", 28*8, 18*8, 2)));
			} else if (selection.equals("Load Game")) {// load game
				msgColor = Color.get(-1, 500);
				confirmDialog.addAll(Arrays.asList(Font.getLines("Load Game?\nUnsaved progress\nwill be lost", 28*8, 18*8, 2)));
			} else if (selection.equals("Make World Multiplayer")) { // make world multiplayer
				msgColor = Color.get(-1, 440);
				confirmDialog.addAll(Arrays.asList(Font.getLines("Start Server?\n\nBe sure to\nsave first!", 28*8, 18*8, 2)));
			} else if (selection.equals("Main Menu")) {// back to menu
				msgColor = Color.get(-1, 500);
				confirmDialog.addAll(Arrays.asList(Font.getLines("Back to Main Menu?\nUnsaved progress\nwill be lost", 28*8, 18*8, 2)));
			}
			for(int i = 0; i < confirmDialog.size(); i++) { // draws each line from above; the first line is white, and all the following lines are color msgColor.
				int col = i == 0 ? Color.get(-1, 555) : msgColor;
				Font.drawCentered(confirmDialog.get(i), screen, 55+i*10, col); // draw it centered.
			}
			int ypos = 70 + confirmDialog.size()*10; // start 20 below the last element...
			Font.drawCentered(input.getMapping("select")+": Yes", screen, ypos, Color.get(-1, 555));
			Font.drawCentered(input.getMapping("exit")+": No", screen, ypos+15, Color.get(-1, 555));
		}
	}
}
