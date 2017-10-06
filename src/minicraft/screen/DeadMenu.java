package minicraft.screen;

import java.util.ArrayList;

import minicraft.Game;
import minicraft.Settings;
import minicraft.gfx.Point;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class DeadMenu extends Display {
	// TODO implement an optional input delay variable for Displays
	private int inputDelay = 60;
	// this is an IMPORTANT bool, determines if the user should respawn or not. :)
	public static boolean shouldRespawn;
	
	public DeadMenu() {
		super(false);
		
		ArrayList<ListEntry> entries = new ArrayList<>();
		entries.add(new StringEntry("Time: " + InfoDisplay.getTimeString()));
		entries.add(new StringEntry("Score: " + Game.player.score));
		entries.add(entryFactory("Quit", new TitleMenu()));
		if(!Settings.get("mode").equals("hardcore")) {
			entries.add(new SelectEntry("Respawn", () -> {
				Game.resetGame();
				if (!Game.isValidClient())
					Game.setMenu(null); //sets the menu to nothing
			}));
		}
		
		menus = new Menu[] {
			new Menu.Builder(true, 0, RelPos.LEFT, entries)
				.setPositioning(new Point(SpriteSheet.boxWidth, SpriteSheet.boxWidth*3), RelPos.BOTTOM_RIGHT)
				.setTitle("You died! Aww!")
				.setTitlePos(RelPos.TOP_LEFT)
				.createMenu()
		};
		
	}
	
	/*@Override
	public Menu getMenu() {
		return new Menu(this, new Frame("", new Rectangle(1, 3, 18, 10, Rectangle.CORNERS)));
	}*/
	
	/*@Override
	public void tick(InputHandler input) {
		if (inputDelay > 0) {
			inputDelay--;
		} else if (input.getKey("exit").clicked) {
			Game.setMenu(new TitleMenu());
			shouldRespawn = false;
		}
		//This is so that if the user presses x @ respawn menu, they respawn (what a concept)
		//if (!Game.isMode("hardcore")) {
			if (input.getKey("select").clicked) {
				//This makes it so the player respawns
				shouldRespawn = true;
				//reset game function
				Game.resetGame();
				if(!Game.isValidClient()) {
					//sets the menu to nothing
					Game.setMenu(null);
				}
			}
		//}
	}*/
	
	/*@Override
	public void render(Screen screen) {
		//renderFrame(screen, "", 1, 3, 18, 10); // Draws a box frame based on 4 points. You can include a title as well.
		Font.draw("You died! Aww!", screen, 16, 32, Color.WHITE);
		
		// the current time elapsed in the Game.
		int seconds = Game.gameTime / Game.normSpeed;
		int minutes = seconds / 60;
		int hours = minutes / 60;
		minutes %= 60;
		seconds %= 60;

		String timeString; //Full text of time.
		if (hours > 0) {
			timeString = hours + "h" + (minutes < 10 ? "0" : "") + minutes + "m"; // If over an hour has passed, then it will show hours and minutes.
		} else {
			timeString = minutes + "m " + (seconds < 10 ? "0" : "") + seconds + "s"; // If under an hour has passed, then it will show minutes and seconds.
		}

		Font.draw("Time:", screen, 2 * 8, 5 * 8, Color.WHITE);
		Font.draw(timeString, screen, (2 + 5) * 8, 5 * 8, Color.YELLOW);
		Font.draw("Score:", screen, 2 * 8, 6 * 8, Color.WHITE);
		Font.draw("" + Game.player.score, screen, (2 + 6) * 8, 6 * 8, Color.YELLOW);
		Font.draw(Game.input.getMapping("exit")+" = lose", screen, 2 * 8, 8 * 8, Color.GRAY);
		*//*if (!Game.isMode("hardcore")) //respawn only if not on hardcore mode
			Font.draw(Game.input.getMapping("select")+" = respawn", screen, 2 * 8, 9 * 8, Color.GRAY);*//*
	}*/
}
