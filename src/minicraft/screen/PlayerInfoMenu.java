package minicraft.screen;

import java.util.ArrayList;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public class PlayerInfoMenu extends Menu {
	
	public void tick() {
		if (input.getKey("select").clicked || input.getKey("Exit").clicked) {
			game.setMenu(null);
		}
	}
	
	public void render(Screen screen) {
		renderFrame(screen, "Player Info", 1, 1, 30, 15);
		
		int seconds = game.gameTime / game.normSpeed;
		int minutes = seconds / 60;
		int hours = minutes / 60;
		minutes %= 60;
		seconds %= 60;
		//int cStep = game.player.stepCount;
		//int cHung = game.player.hungerStamCnt;
		
		String timeString = "";
		if (hours > 0) {
			timeString = hours + "h" + (minutes < 10 ? "0" : "") + minutes + "m";
		} else {
			timeString = minutes + "m " + (seconds < 10 ? "0" : "") + seconds + "s";
		}
		
		Font.draw("General Stats:", screen, 8 * 8, 3 * 8, Color.get(-1, 324, 200, 150));
		Font.draw(" ----------------------------", screen, 1 * 8, 4 * 8, Color.get(-1, 540));
		
		ArrayList<String> stats = new ArrayList<String>();
		
		stats.add("Time Played: " + timeString);
		stats.add("Current Score: " + game.player.score);
		
		/*
		Font.draw("Current Time: " + timeString, screen, 2 * 8, 5 * 8, Color.get(-1, 555));
		Font.draw(timeString, screen, (2 + 13) * 8, 5 * 8, Color.get(-1, 550));
		Font.draw("Current Score:", screen, 2 * 8, 6 * 8, Color.get(-1, 555));
		Font.draw("" + game.player.score, screen, (2 + 14) * 8, 6 * 8, Color.get(-1, 550));
		*/
		
		/*Font.draw("Health:", screen, 2 * 8, 7 * 8, Color.get(-1, 555));
		if (ModeMenu.creative)
			Font.draw("inf", screen, (2 + 7) * 8, 7 * 8, Color.get(-1, 550));
		else
			Font.draw("" + game.player.health, screen, (2 + 7) * 8, 7 * 8, Color.get(-1, 550));
		*//*Font.draw("Hunger:", screen, 2 * 8, 8 * 8, Color.get(-1, 555));
		if (ModeMenu.creative)
			Font.draw("inf", screen, (2 + 7) * 8, 8 * 8, Color.get(-1, 550));
		else
			Font.draw(""+game.player.hunger, screen, (2 + 7) * 8, 8 * 8, Color.get(-1, 550));
		*//*Font.draw("Stamina till hunger:", screen, 2 * 8, 9 * 8, Color.get(-1, 555));
		if (OptionsMenu.diff == OptionsMenu.easy) cHung = 10 - cHung;
		if (OptionsMenu.diff == OptionsMenu.norm) cHung = 7 - cHung;
		if (OptionsMenu.diff == OptionsMenu.hard) cHung = 5 - cHung;
		if (ModeMenu.creative)
			Font.draw("inf", screen, (2 + 20) * 8, 9 * 8, Color.get(-1, 550));
		else
			Font.draw("" + cHung, screen, (2 + 20) * 8, 9 * 8, Color.get(-1, 550));
		*//*Font.draw("Steps till hunger:", screen, 2 * 8, 10 * 8, Color.get(-1, 555));
		
		if (OptionsMenu.diff == OptionsMenu.norm) {
			cStep = 10000 - cStep;
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			cStep = 5000 - cStep;
		}

		if (OptionsMenu.diff == OptionsMenu.easy || ModeMenu.creative) {
			Font.draw("inf", screen, (2 + 18) * 8, 10 * 8, Color.get(-1, 550));
		
		} else {
			Font.draw("" + cStep, screen, (2 + 18) * 8, 10 * 8, Color.get(-1, 550));
		}*/
		
		for(int i = 0; i < stats.size(); i++) {
			String[] split = stats.get(i).split(":");
			Font.draw(split[0]+":", screen, 2*8, (4+i)*8, Color.get(-1, 555));
			if(split.length==1) continue;
			String data = split[1];
			if(split.length > 2) {
				for(int idx = 2; idx < split.length; idx++)
					data += ":"+split[idx];
			}
			Font.draw(data, screen, 2*8 + Font.textWidth(split[0]+":"), (4+i)*8, Color.get(-1, 550));
		}
		
		int y = 4 + stats.size();
		Font.draw(" ----------------------------", screen, 1 * 8, y * 8, Color.get(-1, 540));
		Font.draw(input.getMapping("select")+":Exit", screen, 2 * 8, (y+2) * 8, Color.get(-1, 333));
	}
}
