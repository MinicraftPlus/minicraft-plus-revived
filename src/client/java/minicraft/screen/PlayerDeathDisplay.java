package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.core.io.Localization;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.saveload.Save;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

import java.util.ArrayList;
import java.util.Arrays;

public class PlayerDeathDisplay extends Display {
	// this is an IMPORTANT bool, determines if the user should respawn or not. :)
	public static boolean shouldRespawn = true;

	public PlayerDeathDisplay() {
		super(false, false);

		ArrayList<ListEntry> entries = new ArrayList<>(Arrays.asList(
			new StringEntry(Localization.getLocalized("minicraft.displays.player_death.display.time", InfoDisplay.getTimeString())),
			new StringEntry(Localization.getLocalized("minicraft.displays.player_death.display.score", Game.player.getScore())),
			new BlankEntry()
		));

		if (!Game.isMode("minicraft.settings.mode.hardcore")) {
			entries.add(new SelectEntry("minicraft.displays.player_death.respawn", () -> {
				World.resetGame();
				Game.setDisplay(null);
			}));
		}

		entries.add(new SelectEntry("minicraft.displays.player_death.save_quit", () -> {
			new Save(WorldSelectDisplay.getWorldName());
			Game.setDisplay(new TitleDisplay());
		}));
		entries.add(new SelectEntry("minicraft.displays.player_death.quit", () -> Game.setDisplay(new TitleDisplay())));

		menus = new Menu[]{
			new Menu.Builder(true, 0, RelPos.LEFT, entries)
				.setPositioning(new Point(MinicraftImage.boxWidth, MinicraftImage.boxWidth * 3), RelPos.BOTTOM_RIGHT)
				.setTitle("minicraft.displays.player_death.title")
				.setTitlePos(RelPos.TOP_LEFT)
				.createMenu()
		};
	}
}
