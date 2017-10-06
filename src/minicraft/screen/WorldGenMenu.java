package minicraft.screen;

import minicraft.Game;
import minicraft.Settings;
import minicraft.gfx.Screen;
import minicraft.screen.entry.InputEntry;
import minicraft.screen.entry.SelectEntry;

public class WorldGenMenu extends Display {
	
	public WorldGenMenu() {
		// TODO add world seed option
		super(new Menu.Builder(false, 10, RelPos.LEFT,
				new InputEntry("Enter World Name", "[a-zA-Z0-9 ]", 36),
				Settings.getEntry("mode"),
				Settings.getEntry("scoretime"),
				new SelectEntry("Create World", () -> {
					// TODO set world name
					Game.setMenu(new LoadingDisplay());
				}),
				Settings.getEntry("size"),
				Settings.getEntry("theme"),
				Settings.getEntry("type")
			)
			.setDisplayLength(5)
			.setScrollPolicies(0.8f, false)
			.setTitle("World Gen Options")
			.createMenu()
		);
	}
	
	@Override
	public void render(Screen screen) {
		screen.clear(0);
		super.render(screen);
	}
}
