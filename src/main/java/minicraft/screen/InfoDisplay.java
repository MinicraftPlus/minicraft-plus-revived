package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.InputHandler;
import minicraft.gfx.Point;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.StringEntry;

public class InfoDisplay extends Display {
	
	public InfoDisplay() {
		//noinspection SuspiciousNameCombination
		super(new Menu.Builder(true, 4, RelPos.LEFT, StringEntry.useLines(
			"----------------------------",
			"Time Played: " + getTimeString(),
			"Current Score: " + Game.player.getScore(),
			"----------------------------",
			Game.input.getMapping("select")+"/"+Game.input.getMapping("exit")+":Exit"
			))
			.setTitle("Player Stats")
			.setTitlePos(RelPos.TOP_LEFT)
			.setPositioning(new Point(SpriteSheet.boxWidth, SpriteSheet.boxWidth), RelPos.BOTTOM_RIGHT)
			.createMenu()
		);
	}
	
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("select").clicked || input.getKey("exit").clicked)
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
