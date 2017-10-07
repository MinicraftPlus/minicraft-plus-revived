package minicraft.screen;

import minicraft.Settings;

public class OptionsMenu extends Display {
	
	public OptionsMenu() {
		super(true, new Menu.Builder(false, 6, RelPos.LEFT,
				Settings.getEntry("diff"),
				Settings.getEntry("sound"),
				Settings.getEntry("autosave"),
				Settings.getEntry("skinon"),
				entryFactory("Change Key Bindings", new KeyInputMenu())
			)
			.setTitle("Options")
			.createMenu()
		);
	}
}
