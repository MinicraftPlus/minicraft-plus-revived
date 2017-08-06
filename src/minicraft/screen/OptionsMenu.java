package minicraft.screen;

import minicraft.Game;
import minicraft.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;

// TODO this class is going to extend the same class as WorldGenMenu, the one where the options aren't things you select, but things you configure. Each option has a number of possibilities, rather than just being an option. This new class will probably extend ScrollingMenu, just so that it can scroll if need be.
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
		super((new String[0]), null);
		this.parent = parent;
	}
	
	public void tick() {
		
		
		boolean hasDelta = false;
		
		if(!Game.isValidClient()) {
			
			int prevdiff = diff;
			if (input.getKey("left").clicked) diff--;
			if (input.getKey("right").clicked) diff++;
			
			if (diff != prevdiff) hasDelta = true;
			
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
			isSoundAct = !isSoundAct;
			hasDelta = true;
		}
		
		if (input.getKey("o").clicked && !Game.isValidClient()) {
			autosave = !autosave;
			hasDelta = true;
		}
		
		if (!Game.isValidServer() && unlockedskin && input.getKey("w").clicked) {
			game.player.skinon = !game.player.skinon;
			hasDelta = true;
		}
		
		if(hasDelta) {
			Sound.craft.play();
			// update the options list
			
		}
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
			Font.draw("Press Left and Right", screen, 67, screen.h - 150, textColor);
		}
		
		Font.draw((Game.isValidClient()?"Autosave":"Aut<o>save:"), screen, 80, screen.h - 100, textColor);
		Font.draw((autosave?"On":"Off"), screen, 180, screen.h - 100, (autosave?onColor:offColor));
		
		Font.draw("<S>ound:", screen, 80, screen.h - 75, textColor);
		Font.draw((isSoundAct?"On":"Off"), screen, 180, screen.h - 75, (isSoundAct?onColor:offColor));
		
		if (unlockedskin && !Game.isValidServer()) {
			Font.draw("<W>ear Suit:", screen, 80, screen.h - 50, textColor);
			Font.draw((game.player.skinon?"On":"Off"), screen, 180, screen.h - 50, (game.player.skinon?onColor:offColor));
		}

		Font.drawCentered("Press "+input.getMapping("exit")+" to return", screen, screen.h - 25, textColor);
	}
}
