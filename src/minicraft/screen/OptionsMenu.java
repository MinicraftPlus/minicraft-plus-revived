package minicraft.screen;

import minicraft.Game;
import minicraft.Settings;
import minicraft.screen.entry.SelectEntry;

public class OptionsMenu extends Display {
	
	public OptionsMenu() {
		super(true, new Menu.Builder(false, 6, RelPos.LEFT,
				Settings.getEntry("diff"),
				Settings.getEntry("fps"),
				Settings.getEntry("sound"),
				Settings.getEntry("autosave"),
				Settings.getEntry("skinon"),
				new SelectEntry("Change Key Bindings", () -> Game.setMenu(new KeyInputMenu()))
			)
			.setTitle("Options")
			.createMenu()
		);
	}
}
