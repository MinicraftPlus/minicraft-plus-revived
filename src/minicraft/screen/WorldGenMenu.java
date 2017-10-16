package minicraft.screen;

import java.util.List;
import java.util.Random;

import minicraft.Game;
import minicraft.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.SelectEntry;

public class WorldGenMenu extends Display {
	
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
		};
	}
	
	public WorldGenMenu() {
		super(true);
		
		InputEntry nameField = makeWorldNameInput("Enter World Name", WorldSelectMenu.getWorldNames(), "");
		worldSeed = new InputEntry("World Seed", "[0-9]+", 20) {
			@Override
			public boolean isValid() { return true; }
		};
		
		menus = new Menu[] {
			new Menu.Builder(false, 10, RelPos.LEFT,
				nameField,
				Settings.getEntry("mode"),
				Settings.getEntry("scoretime"),
				
				new SelectEntry("Create World", () -> {
					if(!nameField.isValid()) return;
					WorldSelectMenu.setWorldName(nameField.getUserInput());
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
