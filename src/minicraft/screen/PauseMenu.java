package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import minicraft.Game;
import minicraft.gfx.*;
import minicraft.saveload.Save;
import minicraft.screen.entry.StringEntry;

public class PauseMenu extends Menu {

	private int selection; //selection is set when you press enter.
	private Menu parent;

	private static final List<StringEntry> alloptions = Arrays.asList(StringEntry.useStringArray("Return to Game", "Options", "Change Key Bindings", "Make World Multiplayer", "Save Game", "Load Game", "Main Menu"));
	
	private static StringEntry[] getOptions() {
		List<StringEntry> options = new ArrayList<StringEntry>();
		options.addAll(alloptions);
		
		if(Game.ISONLINE) {
			options.remove(3);
			
			if(!Game.isValidServer())
				options.remove(3); // is 4, but would be 3 after removing other one
		}
		
		return options.toArray(new StringEntry[options.size()]);
	}
	
	public PauseMenu() {
		super(getOptions());
		setFrames(new Frame("", new Rectangle(4, 2, 32, 20, Rectangle.CORNERS)));
		setLineSpacing(4);
		setTextStyle(new FontStyle(Color.get(-1, 222)).setYPos(SpriteSheet.boxWidth*11 - 35));
		//renderFrame(screen, "", 4, 2, 32, 20); // draw the blue menu frame.
		selection = -1; // set to main pause menu options.
	}

	public void tick() {
		if (input.getKey("pause").clicked) {
			Game.setMenu(parent);
			return;
		}
		
		super.tick(); // process key input
		
		//choice chosen; input here is at confirm menu
		if (input.getKey("select").clicked) {
			String chosen = ((StringEntry[])options)[selected].getText();
			String confirmed = selection >= 0 ? ((StringEntry[])options)[selection].getText() : "";
			
			switch(chosen) {
				case "Return to Game": Game.setMenu(parent); return;
				case "Options": Game.setMenu(Displays.options); return;
				case "Change Key Bindings": Game.setMenu(new KeyInputMenu(Game.main.input.getKeyPrefs())); return;
				default:
					selection = selected; // for any other choice, this progresses a choice to a confirmation.
			}
			
			switch(confirmed) {
				case "Save Game":
					Game.setMenu(null);
					if(!Game.isValidServer())
						new Save(Game.player, WorldSelectMenu.worldname);
					else
						Game.server.saveWorld();
				return;
				
				case "Load World":
					WorldSelectMenu.loadworld = true;
					Game.setMenu(new WorldSelectMenu());
				return;
				
				case "Make World Multiplayer":
					Game.setMenu(null);
					//new Save(Game.player, WorldSelectMenu.worldname);
					Game.startMultiplayerServer();
				return;
				
				case "Main Menu":
					Game.setMenu(new TitleMenu());
				return;
			}
		}
		
		if (input.getKey("exit").clicked)
			Game.setMenu(parent);
	}

	public void render(Screen screen) {
		if (selection == -1) { // still displaying main options menu.
			super.render(screen); // render the main options menu.
			Font.drawCentered("Paused", screen, 35, Color.get(-1, 550));
			Font.drawCentered(input.getMapping("up")+" and "+input.getMapping("down")+" to Scroll", screen, 140, Color.get(-1, 333));
			Font.drawCentered(input.getMapping("select")+": Choose", screen, 150, Color.get(-1, 333));
		} else {
			renderFrames(screen);
			ArrayList<String> confirmDialog = new ArrayList<String>();
			String selection = ((StringEntry[])options)[this.selection].getText();
			int msgColor = Color.get(-1, 500);
			
			switch (selection) {
				case "Save Game": // save game
					msgColor = Color.get(-1, 333);
					confirmDialog.addAll(Arrays.asList(Font.getLines("Save Game?\n\n\nTip: press \"R\" to save in-game", 28 * 8, 18 * 8, 2)));
					break;
				case "Load Game": // load game
					msgColor = Color.get(-1, 500);
					confirmDialog.addAll(Arrays.asList(Font.getLines("Load Game?\nUnsaved progress\nwill be lost", 28 * 8, 18 * 8, 2)));
					break;
				case "Make World Multiplayer":  // make world multiplayer
					msgColor = Color.get(-1, 440);
					confirmDialog.addAll(Arrays.asList(Font.getLines("Start Server?\n\nBe sure to\nsave first!", 28 * 8, 18 * 8, 2)));
					break;
				case "Main Menu": // back to menu
					msgColor = Color.get(-1, 500);
					confirmDialog.addAll(Arrays.asList(Font.getLines("Back to Main Menu?\nUnsaved progress\nwill be lost", 28 * 8, 18 * 8, 2)));
					break;
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
