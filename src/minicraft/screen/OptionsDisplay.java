package minicraft.screen;

import minicraft.core.*;
import minicraft.core.Settings;
import minicraft.saveload.Save;
import minicraft.screen.entry.SelectEntry;

public class OptionsDisplay extends Display {
	
	public OptionsDisplay() {
		super(true, new Menu.Builder(false, 6, RelPos.LEFT,
				Settings.getEntry("diff"),
				Settings.getEntry("fps"),
				Settings.getEntry("sound"),
				Settings.getEntry("autosave"),
				Settings.getEntry("skinon"),
				new SelectEntry("Change Key Bindings", () -> Game.setMenu(new KeyInputDisplay()))
			)
			.setTitle("Options")
			.createMenu()
		);
	}
	
	@Override
	public void onExit() {
		new Save();
		Game.MAX_FPS = (int)Settings.get("fps");
	}
}
