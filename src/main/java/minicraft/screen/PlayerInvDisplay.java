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
		Menu[] menus = new Menu[] {new InventoryMenu(player, player.getInventory(), "Inventory")};
		this.menus = menus;
		selection = 0;
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
		Font.draw(text, screen, 16, Screen.h / 2 + 2, Color.WHITE);
	}
}
