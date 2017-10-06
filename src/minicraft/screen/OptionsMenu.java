package minicraft.screen;

import minicraft.Settings;
import minicraft.gfx.Screen;

public class OptionsMenu extends Display {
	
	public OptionsMenu() {
		super(new Menu.Builder(6,
				Settings.getEntry("diff"),
				Settings.getEntry("sound"),
				Settings.getEntry("autosave"),
				Settings.getEntry("skinon"),
				entryFactory("Change Key Bindings", new KeyInputMenu())
			)
			.setCentering(RelPos.CENTER, RelPos.LEFT)
			.setTitle("Options")
			.createMenu()
		);
	}
	
	@Override
	public void render(Screen screen) {
		screen.clear(0);
		super.render(screen);
	}
}
