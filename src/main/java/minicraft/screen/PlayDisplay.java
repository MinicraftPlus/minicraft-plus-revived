package minicraft.screen;

import minicraft.core.Game;
import minicraft.gfx.Color;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;


public class PlayDisplay extends Display {
	public PlayDisplay() {
		super(true, true, new Menu.Builder(false, 2, RelPos.CENTER,
				new StringEntry("Game Mode", Color.YELLOW),
				new BlankEntry(),
				new BlankEntry(),
				new SelectEntry("Singleplayer", () -> {
					if (WorldSelectDisplay.getWorldNames().size() > 0)
						Game.setDisplay(new Display(true, new Menu.Builder(false, 2, RelPos.CENTER,
								new SelectEntry("Load World", () -> Game.setDisplay(new WorldSelectDisplay())),
								new SelectEntry("New World", () -> Game.setDisplay(new WorldGenDisplay()))
						).createMenu()));
					else Game.setDisplay(new WorldGenDisplay());
				}),
				new SelectEntry("Multiplayer", () -> Game.setDisplay(new MultiplayerDisplay()))
		).createMenu());
	}
}
