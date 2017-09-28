package minicraft.screen;

import java.util.ArrayList;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;

public class PlayerInfoMenu extends Display {
	
	public PlayerInfoMenu() {
		super();
	}
	
	@Override
	public Menu getMenu() {
		return new Menu(this, new Frame("Player Info", new Rectangle(1, 1, 30, 15, Rectangle.CORNERS)));
	}
	
	public void tick(InputHandler input) {
		if (input.getKey("select").clicked || input.getKey("Exit").clicked) {
			Game.setMenu((Menu)null);
		}
	}
	
	public void render(Screen screen) {
		//renderFrame(screen, "Player Info", 1, 1, 30, 15);
		
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
		
		Font.draw("General Stats:", screen, 8 * 8, 3 * 8, Color.get(-1, 324, 200, 150));
		Font.draw(" ----------------------------", screen, 1 * 8, 4 * 8, Color.get(-1, 540));
		
		ArrayList<String> stats = new ArrayList<>();
		
		stats.add("Time Played: " + timeString);
		stats.add("Current Score: " + Game.player.score);
		
		for(int i = 0; i < stats.size(); i++) {
			String[] split = stats.get(i).split(":");
			Font.draw(split[0]+":", screen, 2*8, (4+i)*8, Color.get(-1, 555));
			if(split.length==1) continue;
			StringBuilder data = new StringBuilder(split[1]);
			if(split.length > 2) {
				for(int idx = 2; idx < split.length; idx++)
					data.append(":").append(split[idx]);
			}
			Font.draw(data.toString(), screen, 2*8 + Font.textWidth(split[0]+":"), (4+i)*8, Color.get(-1, 550));
		}
		
		int y = 4 + stats.size();
		Font.draw(" ----------------------------", screen, 1 * 8, y * 8, Color.get(-1, 540));
		Font.draw(Game.input.getMapping("select")+":Exit", screen, 2 * 8, (y+2) * 8, Color.get(-1, 333));
	}
}
