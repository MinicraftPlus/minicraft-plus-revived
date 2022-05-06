package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;

public class PlayerInvDisplay extends Display {
	
	private final Player player;


	public PlayerInvDisplay(Player player) {
		super(new InventoryMenu(player, player.getInventory(), "Inventory"));
		this.player = player;
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		
		if(input.getKey("menu").clicked) {
			Game.exitDisplay();
			return;
		}
		
		if(input.getKey("attack").clicked && menus[0].getNumOptions() > 0) {
			player.activeItem = player.getInventory().remove(menus[0].getSelection());
			Game.exitDisplay();
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Searcher help text
		String text = "(" + Game.input.getMapping("SEARCHER-BAR") + ") " + Localization.getLocalized("to search.");
		
		// Position the search tip according to the aspect ratio
		if (OptionsMainMenuDisplay.originalAspectRatio == "16x9") {
			Font.draw(text, screen, 12, Screen.h/ 2 + 16, Color.WHITE);
		} else {
			Font.draw(text, screen, 12, Screen.h/ 2 - 10, Color.WHITE);
		}
		
	}
}
