package minicraft.screen;

import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.sound.Sound;
import java.util.Arrays;

public class WorldGenMenu extends SelectMenu {
	//this the the "more world options" menu.
	
	//private Menu parent;
	
	/*public static String Theme {Normal, Forest, Desert, Plain, Hell};
	public static enum Type {Island, Box, Mount, Irreg};
	public static enum Size {
		Normal(128), Big(256), Huge(512);
		public int size;
		private Sizes(int size) {this.size = size;}
	}*/
	
	/*public static Theme theme = Theme.Normal;
	public static Type type = Type.Island;
	public static Size size = Size.Normal;
	*//*
	public static final String[] themes = {"Normal", "Forest", "Desert", "Plain", "Hell"};
	public static int normal = 10;
	public static int forest = 11;
	public static int desert = 12;
	public static int plain = 13;
	public static int hell = 14;
	
	public static final String[] types = {"Island", "Box", "Mountain", "Irregular"};
	public static int island = 0;
	public static int box = 1;
	public static int mount = 2;
	public static int irreg = 3;
	
	public static final String[] sizeNames = {"Normal", "Big", "Huge"};
	public static final int[] sizes = {128, 256, 512};
	public static int sizeNorm = 128;
	public static int sizeBig = 256;
	public static int sizeHuge = 512;
	
	public static int type = 0;
	public static int theme = 10;
	public static int size = 0;
	public static int sized = 128;
	*/
	public static final int[] sizes = {128, 256, 512};
	
	public static final String[] options = {"Theme", "Type", "Size"};
	public static final String[][] optionSets = {
		{"Normal", "Forest", "Desert", "Plain", "Hell"}, //theme
		{"Island", "Box", "Mountain", "Irregular"}, //type
		{"Normal (128 x 128)", "Big (256 x 256)", "Huge (512 x 512)"} //size
	};
	//public static int op = 0;
	
	public static int[] selections = new int[optionSets.length];
	/*static {
		selections = new int[optionSets[0].length];
		Arrays.fill(selections, 0);
	}*/
	/*private static int selected = theme;
	private static int selectedlr = type;
	private static int selecteds = size;
	*/
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
		/*
		if (op == 0) {
			if (input.getKey("left").clicked) selected--;
			if (input.getKey("right").clicked) selected++;
			if (input.getKey("left").clicked) theme--;
			if (input.getKey("right").clicked) theme++;
		}
		
		if (op == 1) {
			if (input.getKey("left").clicked) selectedlr--;
			if (input.getKey("right").clicked) selectedlr++;
			if (input.getKey("left").clicked) type--;
			if (input.getKey("right").clicked) type++;
		}
		
		if (op == 2) {
			if (input.getKey("left").clicked) selecteds--;
			if (input.getKey("right").clicked) selecteds++;
			if (input.getKey("left").clicked) size--;
			if (input.getKey("right").clicked) size++;
		}
		
		if (input.getKey("left").clicked) Sound.craft.play();
		if (input.getKey("right").clicked) Sound.craft.play();
		if (input.getKey("up").clicked) Sound.craft.play();
		if (input.getKey("down").clicked) Sound.craft.play();
		
		if (op > 2) op = 0;
		if (op < 0) op = 2;
		
		if (size > 2) size = 0;
		if (size < 0) size = 2;
		
		if (selected > 14) selected = 10;
		if (selected < 10) selected = 14;
		if (selectedlr > 3) selectedlr = 0;
		if (selectedlr < 0) selectedlr = 3;
		
		if (theme > 14) theme = 10;
		if (theme < 10) theme = 14;
		if (type > 3) type = 0;
		if (type < 0) type = 3;
		
		if (selected == 10) {
			theme = normal;
		} else if (selected == 11) {
			theme = forest;
		} else if (selected == 12) {
			theme = desert;
		} else if (selected == 13) {
			theme = plain;
		} else if (selected == 14) {
			theme = hell;
		}

		if (size == 0) {
			sized = sizeNorm;
		} else if (size == 1) {
			sized = sizeBig;
		} else if (size == 2) {
			sized = sizeHuge;
		}

		if (type == 0) {
			type = island;
		} else if (type == 1) {
			type = box;
		} else if (type == 2) {
			type = mount;
		} else if (type == 3) {
			type = irreg;
		}

		if (theme == 10) {
			theme = normal;
		} else if (theme == 11) {
			theme = forest;
		} else if (theme == 12) {
			theme = desert;
		} else if (theme == 13) {
			theme = plain;
		} else if (theme == 14) {
			theme = hell;
		}
		if (selectedlr == 0) {
			type = island;
		} else if (selectedlr == 1) {
			type = box;
		} else if (selectedlr == 2) {
			type = mount;
		} else if (selectedlr == 3) {
			type = irreg;
		}

		if (size == 0) {
			sized = sizeNorm;
		} else if (size == 1) {
			sized = sizeBig;
		} else if (size == 2) {
			sized = sizeHuge;
		}*/
	}

	public void render(Screen screen) {
		screen.clear(0);
		
		for(int i = 0; i < options.length; i++) {
			choices[i] = options[i] + ": " + optionSets[i][selections[i]];
		}
		
		super.render(screen);
		/*
		Font.draw("Theme: " + themes[selections[0]], screen, 11 * 8 + 4, 10 * 8, (op==1?Color.get(-1, 555):Color.get(-1, 111)));
		Font.draw("Type: " + types[selections[1]], screen, 11 * 8 + 4, 8 * 8, (op==2?Color.get(-1, 555):Color.get(-1, 111)));
		Font.draw("Size: " + sizeNames[selections[2]], screen, 11 * 8 + 4, 12 * 8, (op==0?Color.get(-1, 555):Color.get(-1, 111)));
		
		if (op == 0) {
			if (theme == 10)
				Font.draw("Normal", screen, 11 * 14 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (theme == 12)
				Font.draw("Desert", screen, 11 * 14 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (theme == 11)
				Font.draw("Forest", screen, 11 * 14 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (theme == 13)
				Font.draw("Plain", screen, 11 * 14 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (theme == 14)
				Font.draw("Hell", screen, 11 * 14 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
		} else if (op == 1) {
			if (type == 0) Font.draw("Island", screen, 11 * 13 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (type == 1)
				Font.draw("Box", screen, 11 * 13 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (type == 2)
				Font.draw("Mountain", screen, 11 * 13 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			else if (type == 3)
				Font.draw("Irregular", screen, 11 * 13 + 4, 10 * 8, Color.get(-1, 555, 555, 555));

		} else if (op == 2) {
			if (size == 0) {
				Font.draw("Normal (128 x 128)", screen, 11 * 12 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			} else if (size == 1) {
				Font.draw("Big (256 x 256)", screen, 11 * 12 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			} else if (size == 2) {
				Font.draw("Huge (512 x 512)", screen, 11 * 12 + 4, 10 * 8, Color.get(-1, 555, 555, 555));
			}
		}*/

		Font.drawCentered("World options", screen, 3 * 8, Color.get(0, 555, 555, 555));
		Font.drawCentered("Arrow keys to scroll", screen, 16 * 8, Color.get(-1, 555, 555, 555));
		Font.drawCentered("Press Esc to exit", screen, 18 * 8, Color.get(-1, 555, 555, 555));
		//This is debug info to see if the numbers are working correctly.
		//Font.draw("" + type, screen, 11 * 7 + 4, 21 * 8, Color.get(-1, 555, 555, 555));
		//Font.draw("" + theme, screen, 11 * 8 + 4, 21 * 8, Color.get(-1, 555, 555, 555));
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
