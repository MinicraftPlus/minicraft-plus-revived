package minicraft.screen;

import minicraft.Game;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.saveload.Save;
import minicraft.sound.Sound;

public class OptionsMenu extends Menu {
	public static int easy = 1;
	public static int norm = 2;
	public static int hard = 3;
	public static int diff = 2;
	public static boolean isSoundAct = true;
	public static boolean autosave = false;
	public static boolean unlockedskin = false;
	public static boolean hasSetDiff = false;
	
	private Menu parent;
	
	public OptionsMenu(Menu parent) {
		this.parent = parent;
	}
	
	public void tick() {
		if (input.getKey("left").clicked) {
			diff--;
			Sound.craft.play();
		}
		if (input.getKey("right").clicked) {
			diff++;
			Sound.craft.play();
		}

		if (diff > 3) diff = 1;
		if (diff < 1) diff = 3;

		if (input.getKey("escape").clicked) {
			hasSetDiff = true;
			new Save(game);
			game.setMenu(parent);
		}
		
		//toggles sound
		if (input.getKey("s").clicked) {
			Sound.craft.play();
			isSoundAct = !isSoundAct;
		}

		if (input.getKey("a").clicked) {
			Sound.craft.play();
			autosave = !autosave;
		}
		
		if (unlockedskin && input.getKey("w").clicked) Player.skinon = !Player.skinon;
	}

	public void render(Screen screen) {
		int textColor = Color.get(0, 555, 555, 555);
		int onColor = Color.get(0, 50, 50, 50);
		int offColor = Color.get(0, 500, 500, 500);
		
		screen.clear(0);
		
		Font.draw("Difficulty:", screen, 11 * 6 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		String[] diffs = {"Easy", "Normal", "Hard"};
		Font.draw(diffs[diff - 1], screen, 11 * 16 + 4, 8 * 8, Color.get(-1, 555, 555, 555));
		
		Font.draw("Press Left and Right", screen, 67, screen.h - 150, textColor);
		
		Font.draw("<A>utosave:", screen, 80, screen.h - 100, textColor);
		Font.draw((autosave?"On":"Off"), screen, 180, screen.h - 100, (autosave?onColor:offColor));
		
		Font.draw("<S>ound:", screen, 80, screen.h - 75, textColor);
		Font.draw((isSoundAct?"On":"Off"), screen, 180, screen.h - 75, (isSoundAct?onColor:offColor));
		
		if (unlockedskin) {
			Font.draw("<W>ear Suit:", screen, 80, screen.h - 50, textColor);
			Font.draw((Player.skinon?"On":"Off"), screen, 180, screen.h - 50, (Player.skinon?onColor:offColor));
		}

		Font.draw("Press Esc to return", screen, 80, screen.h - 25, textColor);
	}
}
