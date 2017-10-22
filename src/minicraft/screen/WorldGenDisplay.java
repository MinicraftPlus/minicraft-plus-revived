package minicraft.screen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import minicraft.core.*;
import minicraft.core.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.SelectEntry;

public class WorldGenDisplay extends Display {
	
	private static final String worldNameRegex = "[a-zA-Z0-9 ]+";
	
	private static InputEntry worldSeed = new InputEntry("World Seed", "[0-9]", 20);
	
	public static long getSeed() {
		String seedStr = worldSeed.getUserInput();
		if(seedStr.length() == 0)
			return new Random().nextLong();
		else
			return Long.parseLong(seedStr);
	}
	
	public static InputEntry makeWorldNameInput(String prompt, List<String> takenNames, String initValue) {
		return new InputEntry(prompt, worldNameRegex, 36, initValue) {
			@Override
			public boolean isValid() {
				if(!super.isValid()) return false;
				String name = getUserInput();
				for(String other: takenNames)
					if(other.equalsIgnoreCase(name))
						return false;
				
				return true;
			}
			
			@Override
			public String getUserInput() {
				return super.getUserInput().toLowerCase(Locale.ENGLISH);
			}
		};
	}
	
	public WorldGenDisplay() {
		super(true);
		
		InputEntry nameField = makeWorldNameInput("Enter World Name", WorldSelectDisplay.getWorldNames(), "");
		
		SelectEntry nameHelp = new SelectEntry("Trouble with world name?", () -> Game.setMenu(new BookDisplay("by default, w and s move the cursor up and down. This can be changed in the key binding menu. To type the letter instead of moving the cursor, hold the shift key while typing the world name."))) {
			@Override
			public int getColor(boolean isSelected) {
				return Color.get(-1, 444);
			}
		};
		
		nameHelp.setVisible(false);
		
		HashSet<String> controls = new HashSet<>();
		controls.addAll(Arrays.asList(Game.input.getMapping("up").split("/")));
		controls.addAll(Arrays.asList(Game.input.getMapping("down").split("/")));
		for(String key: controls) {
			if(key.matches("^\\w$")) {
				nameHelp.setVisible(true);
				break;
			}
		}
		
		worldSeed = new InputEntry("World Seed", "[0-9]+", 20) {
			@Override
			public boolean isValid() { return true; }
		};
		
		menus = new Menu[] {
			new Menu.Builder(false, 10, RelPos.LEFT,
				nameField,
				nameHelp,
				Settings.getEntry("mode"),
				Settings.getEntry("scoretime"),
				
				new SelectEntry("Create World", () -> {
					if(!nameField.isValid()) return;
					WorldSelectDisplay.setWorldName(nameField.getUserInput(), false);
					Game.setMenu(new LoadingDisplay());
				}) {
					@Override
					public void render(Screen screen, int x, int y, boolean isSelected) {
						Font.draw(toString(), screen, x, y, Color.CYAN);
					}
				},
				
				Settings.getEntry("size"),
				Settings.getEntry("theme"),
				Settings.getEntry("type"),
				worldSeed
			)
				.setDisplayLength(5)
				.setScrollPolicies(0.8f, false)
				.setTitle("World Gen Options")
				.createMenu()
		};
	}
}
