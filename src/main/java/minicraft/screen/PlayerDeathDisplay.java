package minicraft.screen;

import java.util.ArrayList;
import java.util.Arrays;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.core.io.Settings;
import minicraft.gfx.Point;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

public class PlayerDeathDisplay extends Display {
	// this is an IMPORTANT bool, determines if the user should respawn or not. :)
	public static boolean shouldRespawn = true;
	
	public PlayerDeathDisplay() {
		super(false, false);
		
		ArrayList<ListEntry> entries = new ArrayList<>();
		entries.addAll(Arrays.asList(
			new StringEntry("Time: " + InfoDisplay.getTimeString()),
			new StringEntry("Score: " + Game.player.getScore()),
			new BlankEntry()
		));
		
		if(!Settings.get("mode").equals("hardcore")) {
			entries.add(new SelectEntry("Respawn", () -> {
				World.resetGame();
				Game.setDisplay(null); //sets the menu to nothing
			}));
		}

		entries.add(new SelectEntry("Quit", () -> Game.setDisplay(new TitleDisplay())));
		
		menus = new Menu[]{
			new Menu.Builder(true, 0, RelPos.LEFT, entries)
				.setPositioning(new Point(SpriteSheet.boxWidth, SpriteSheet.boxWidth * 3), RelPos.BOTTOM_RIGHT)
				.setTitle("You died! Aww!")
				.setTitlePos(RelPos.TOP_LEFT)
				.createMenu()
		};
		
	}
}
