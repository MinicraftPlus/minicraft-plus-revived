package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.Game;
import minicraft.Settings;
import minicraft.gfx.Point;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class DeadMenu extends Display {
	//private int inputDelay = 60;
	// this is an IMPORTANT bool, determines if the user should respawn or not. :)
	public static boolean shouldRespawn;
	
	public DeadMenu() {
		super(false, false);
		
		ArrayList<ListEntry> entries = new ArrayList<>();
		entries.addAll(Arrays.asList(
			new StringEntry("Time: " + InfoDisplay.getTimeString()),
			new StringEntry("Score: " + Game.player.score),
			new BlankEntry(),
			new SelectEntry("Quit", () -> Game.setMenu(new TitleMenu()))
		));
		
		if(!Settings.get("mode").equals("hardcore")) {
			entries.add(new SelectEntry("Respawn", () -> {
				Game.resetGame();
				if (!Game.isValidClient())
					Game.setMenu(null); //sets the menu to nothing
			}));
		}
		
		menus = new Menu[]{
			new Menu.Builder(true, 0, RelPos.LEFT, entries)
				.setPositioning(new Point(SpriteSheet.boxWidth, SpriteSheet.boxWidth * 3), RelPos.BOTTOM_RIGHT)
				.setTitle("You died! Aww!")
				.setTitlePos(RelPos.TOP_LEFT)
				.createMenu()
		};
		
	}
}
