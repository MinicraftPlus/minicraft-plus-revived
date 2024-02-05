package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Point;
import minicraft.screen.entry.StringEntry;

public class InfoDisplay extends Display {

	public InfoDisplay() {
		//noinspection SuspiciousNameCombination
		super(new Menu.Builder(true, 4, RelPos.LEFT, StringEntry.useLines(
				"----------------------------",
				Localization.getLocalized("minicraft.displays.info.display.time", getTimeString()),
				Localization.getLocalized("minicraft.displays.info.display.score", Game.player.getScore()),
				"----------------------------",
				Localization.getLocalized("minicraft.displays.info.display.exit_help", Game.input.getMapping("select"), Game.input.getMapping("exit"))
			))
				.setTitle("minicraft.displays.info.title")
				.setTitlePos(RelPos.TOP_LEFT)
				.setPositioning(new Point(MinicraftImage.boxWidth, MinicraftImage.boxWidth), RelPos.BOTTOM_RIGHT)
				.createMenu()
		);
	}

	@Override
	public void tick(InputHandler input) {
		if (input.inputPressed("select") || input.inputPressed("exit"))
			Game.exitDisplay();
	}

	public static String getTimeString() {
		int seconds = Updater.gameTime / Updater.normSpeed;
		int minutes = seconds / 60;
		int hours = minutes / 60;
		minutes %= 60;
		seconds %= 60;

		String timeString;
		if (hours > 0) {
			timeString = hours + "h" + (minutes < 10 ? "0" : "") + minutes + "m";
		} else {
			timeString = minutes + "m " + (seconds < 10 ? "0" : "") + seconds + "s";
		}

		return timeString;
	}
}
