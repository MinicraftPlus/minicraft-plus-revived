package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.SpriteSheet;
import minicraft.screen.entry.StringEntry;

public class InfoDisplay extends Display {
	
	public InfoDisplay() {
		//noinspection SuspiciousNameCombination
		super(new Menu.Builder(4, StringEntry.useLines(
			"----------------------------",
			"Time Played: " + getTimeString(),
			"Current Score: " + Game.player.score,
			"----------------------------",
			Game.input.getMapping("select")+"/"+Game.input.getMapping("exit")+":Exit"
			))
			.setTitle("General Stats")
			.setTitlePos(RelPos.TOP_LEFT)
			.setFrame(true)
			.setAnchor(SpriteSheet.boxWidth, SpriteSheet.boxWidth)
			.setCentering(RelPos.BOTTOM_RIGHT, RelPos.LEFT)
			.createMenu()
		);
	}
	
	/*@Override
	public Menu getMenu() {
		return new Menu(this, new Frame("Player Info", new Rectangle(1, 1, 30, 15, Rectangle.CORNERS)));
	}*/
	
	@Override
	public void tick(InputHandler input) {
		if (input.getKey("select").clicked || input.getKey("exit").clicked)
			Game.exitMenu();
	}
	
	/*public void render(Screen screen) {
		//renderFrame(screen, "Player Info", 1, 1, 30, 15);
		
		Font.draw("General Stats:", screen, 8 * 8, 3 * 8, Color.get(-1, 324, 200, 150));
		Font.draw(" ----------------------------", screen, 1 * 8, 4 * 8, Color.get(-1, 540));
		
		ArrayList<String> stats = new ArrayList<>();
		
		stats.add("Time Played: " + timeString);
		stats.add("Current Score: " + Game.player.score);
		
		for(int i = 0; i < stats.size(); i++) {
			String[] split = stats.get(i).split(":");
			Font.draw(split[0]+":", screen, 2*8, (4+i)*8, Color.WHITE);
			if(split.length==1) continue;
			StringBuilder data = new StringBuilder(split[1]);
			if(split.length > 2) {
				for(int idx = 2; idx < split.length; idx++)
					data.append(":").append(split[idx]);
			}
			Font.draw(data.toString(), screen, 2*8 + Font.textWidth(split[0]+":"), (4+i)*8, Color.YELLOW);
		}
		
		int y = 4 + stats.size();
		Font.draw(" ----------------------------", screen, 1 * 8, y * 8, Color.get(-1, 540));
		Font.draw(Game.input.getMapping("select")+":Exit", screen, 2 * 8, (y+2) * 8, Color.GRAY);
	}*/
	
	public static String getTimeString() {
		int seconds = Game.gameTime / Game.normSpeed;
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
