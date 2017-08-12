package minicraft.screen;

import minicraft.Game;
import minicraft.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;

public class OptionsMenu extends Menu {
	public static int easy = 0;
	public static int norm = 1;
	public static int hard = 2;
	public static int diff = 1;
	public static boolean isSoundAct = true;
	public static boolean autosave = false;
	public static boolean unlockedskin = false;
	
	private Menu parent;
	
	public OptionsMenu(Menu parent) {
		this.parent = parent;
	}
	
	public void tick() {
		if(!Game.isValidClient()) {
			
			int prevdiff = diff;
			if (input.getKey("left").clicked) diff--;
			if (input.getKey("right").clicked) diff++;
			
			if (diff != prevdiff) Sound.craft.play();
			
			if (diff > 2) diff = 0;
			if (diff < 0) diff = 2;
		}
		
		if (input.getKey("exit").clicked) {
			new Save(game);
			
			for(int i = 0; i < Game.levels.length; i++)
				if(Game.levels[i] != null)
					Game.levels[i].updateMobCap();
			
			game.setMenu(parent);
		}
		
		//toggles sound
		if (input.getKey("s").clicked) {
			Sound.craft.play();
			isSoundAct = !isSoundAct;
		}
		
		if (input.getKey("o").clicked && !Game.isValidClient()) {
			Sound.craft.play();
			autosave = !autosave;
		}
		
		if (!Game.isValidServer() && unlockedskin && input.getKey("w").clicked) game.player.skinon = !game.player.skinon;
	}

	public void render(Screen screen) {
		int textColor = Color.get(-1, 555);
		int onColor = Color.get(-1, 50);
		int offColor = Color.get(-1, 500);
		
		screen.clear(0);
		
		Font.draw("Difficulty:", screen, 11 * 6 + 4, 8 * 8, Color.get(-1, 555));
		String[] diffs = {"Easy", "Normal", "Hard"};
		Font.draw(diffs[diff], screen, 11 * 16 + 4, 8 * 8, Color.get(-1, 555));
		
		if(!Game.isValidClient()) {
			Font.draw("Press Left and Right", screen, 67, Screen.h - 150, textColor);
		}
		
		Font.draw((Game.isValidClient()?"Autosave":"Aut<o>save:"), screen, 80, Screen.h - 100, textColor);
		Font.draw((autosave?"On":"Off"), screen, 180, Screen.h - 100, (autosave?onColor:offColor));
		
		Font.draw("<S>ound:", screen, 80, Screen.h - 75, textColor);
		Font.draw((isSoundAct?"On":"Off"), screen, 180, Screen.h - 75, (isSoundAct?onColor:offColor));
		
		if (unlockedskin && !Game.isValidServer()) {
			Font.draw("<W>ear Suit:", screen, 80, Screen.h - 50, textColor);
			Font.draw((game.player.skinon?"On":"Off"), screen, 180, Screen.h - 50, (game.player.skinon?onColor:offColor));
		}

		Font.drawCentered("Press "+input.getMapping("exit")+" to return", screen, Screen.h - 25, textColor);
	}
}
