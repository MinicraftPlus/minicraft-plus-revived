package minicraft.screen;

import java.util.Arrays;
import minicraft.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public class WorldGenMenu extends Display {
	//this the the "more world options" menu.
	
	private static final int[] sizes = {128, 256, 512};
	
	private static final String[] settings = {"Theme", "Type", "Size"};
	private static final String[][] choices = {
		{"Normal", "Forest", "Desert", "Plain", "Hell"}, //theme
		{"Island", "Box", "Mountain", "Irregular"}, //type
		{"Normal (128 x 128)", "Big (256 x 256)", "Huge (512 x 512)"} //size
	};
	
	private static int[] selections = new int[choices.length];
	
	public WorldGenMenu() {
		//super(Arrays.copyOf(settings, settings.length), 8 * 8 + 4, 8 * 8, 8, Color.get(-1, 555), Color.get(-1, 111));
	}
	
	public void tick() {
		if (input.getKey("exit").clicked) {
			game.setMenu(new ModeMenu());
			return;
		}
		
		super.tick();
		
		int prevSel = selections[selected];
		if (input.getKey("left").clicked) selections[selected]--;
		if (input.getKey("right").clicked) selections[selected]++;
		
		if(selections[selected] >= choices[selected].length) selections[selected] = 0;
		if(selections[selected] < 0) selections[selected] = choices[selected].length - 1;
		
		if(prevSel != selections[selected]) Sound.craft.play();
	}

	public void render(Screen screen) {
		screen.clear(0);
		
		for(int i = 0; i < settings.length; i++) {
			options.set(i, settings[i] + ": " + choices[i][selections[i]]);
		}
		
		super.render(screen);
		
		Font.drawCentered("World options", screen, 3 * 8, Color.get(-1, 555));
		Font.drawCentered(input.getMapping("up")+" and "+input.getMapping("down")+" to scroll", screen, 16 * 8, Color.get(-1, 555));
		Font.drawCentered("Press "+input.getMapping("exit")+" to exit", screen, 18 * 8, Color.get(-1, 555));
	}
	
	private static int getIdx(String query) {
		for(int i = 0; i < settings.length; i++) {
			if(settings[i].equals(query))
				return i;
		}
		return -1;
	}
	
	public static String get(String query) {
		int idx = getIdx(query);
		if(idx >= 0)
			return choices[idx][selections[idx]];
		return "";
	}
	
	public static int getSize() {
		return sizes[selections[getIdx("Size")]];
	}
	
	public static void setSize(int size) {
		for(int i = 0; i < sizes.length; i++) {
			if(sizes[i] == size) {
				selections[getIdx("Size")] = i;
				break;
			}
		}
	}
}
