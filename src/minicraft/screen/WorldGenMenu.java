package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.sound.Sound;
import java.util.Arrays;

public class WorldGenMenu extends SelectMenu {
	//this the the "more world options" menu.
	
	public static final int[] sizes = {128, 256, 512};
	
	public static final String[] options = {"Theme", "Type", "Size"};
	public static final String[][] optionSets = {
		{"Normal", "Forest", "Desert", "Plain", "Hell"}, //theme
		{"Island", "Box", "Mountain", "Irregular"}, //type
		{"Normal (128 x 128)", "Big (256 x 256)", "Huge (512 x 512)"} //size
	};
	
	public static int[] selections = new int[optionSets.length];
	
	public WorldGenMenu() {
		super(Arrays.copyOf(options, options.length), 8 * 8 + 4, 8 * 8, 12);
	}
	
	public void tick() {
		if (input.getKey("escape").clicked) {
			game.setMenu(new ModeMenu());
			return;
		}
		
		super.tick();
		
		int prevSel = selections[selected];
		if (input.getKey("left").clicked) selections[selected]--;
		if (input.getKey("right").clicked) selections[selected]++;
		
		if(selections[selected] >= optionSets[selected].length) selections[selected] = 0;
		if(selections[selected] < 0) selections[selected] = optionSets[selected].length - 1;
		
		if(prevSel != selections[selected]) Sound.craft.play();
	}

	public void render(Screen screen) {
		screen.clear(0);
		
		for(int i = 0; i < options.length; i++) {
			choices[i] = options[i] + ": " + optionSets[i][selections[i]];
		}
		
		super.render(screen);
		
		Font.drawCentered("World options", screen, 3 * 8, Color.get(0, 555, 555, 555));
		Font.drawCentered("Arrow keys to scroll", screen, 16 * 8, Color.get(-1, 555, 555, 555));
		Font.drawCentered("Press Esc to exit", screen, 18 * 8, Color.get(-1, 555, 555, 555));
	}
	
	private static int getIdx(String query) {
		for(int i = 0; i < options.length; i++) {
			if(options[i].equals(query))
				return i;
		}
		return -1;
	}
	public static String get(String query) {
		int idx = getIdx(query);
		if(idx >= 0)
			return optionSets[idx][selections[idx]];
		return "";
	}
	
	public static int getSize() {
		return sizes[selections[getIdx("Size")]];
	}
}
