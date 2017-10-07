package minicraft.screen;

import java.util.Random;

import minicraft.Game;
import minicraft.Settings;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.SelectEntry;

public class WorldGenMenu extends Display {
	
	private static InputEntry worldSeed = new InputEntry("World Seed", "[0-9]", 20);
	
	public static long getSeed() {
		String seedStr = worldSeed.getUserInput();
		if(seedStr.length() == 0)
			return new Random().nextLong();
		else
			return Long.parseLong(seedStr);
	}
	
	private String[] takenNames = WorldSelectMenu.getWorldNames().toArray(new String[0]);
	
	public WorldGenMenu() {
		// TODO add world seed option
		super(true);
		InputEntry nameField = new InputEntry("Enter World Name", "[a-zA-Z0-9 ]+", 36) {
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
				}),
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
