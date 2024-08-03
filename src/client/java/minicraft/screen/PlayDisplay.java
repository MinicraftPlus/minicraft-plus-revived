package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.gfx.Color;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.StringEntry;

/**
 * @deprecated This class is not used as this is replaced by an anonymous class in {@link TitleDisplay}.
 */
@Deprecated
public class PlayDisplay extends Display {
	public PlayDisplay() {
		super(true, true, new Menu.Builder(false, 2, RelPos.CENTER,
				new StringEntry(new Localization.LocalizationString("Game Mode"), Color.YELLOW),
				new BlankEntry(),
				new BlankEntry(),
				new SelectEntry(new Localization.LocalizationString("Singleplayer"), () -> {
//					if (WorldSelectDisplay.hasWorld())
//						Game.setDisplay(new Display(true, new Menu.Builder(false, 2, RelPos.CENTER,
//								new SelectEntry("Load World", () -> Game.setDisplay(new WorldSelectDisplay())),
//								new SelectEntry("New World", () -> Game.setDisplay(new WorldGenDisplay()))
//						).createMenu()));
//					else Game.setDisplay(new WorldGenDisplay());
				}),
				new SelectEntry(new Localization.LocalizationString("Multiplayer"),
					() -> Game.setDisplay(new MultiplayerDisplay()))
		).createMenu());
	}
}
