package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.screen.entry.StringEntry;

public class PlayerInvDisplay extends Display {

	private final Player player;

	private String itemDescription = "";
	private Menu.Builder builder;

	public PlayerInvDisplay(Player player) {
		builder = new Menu.Builder(true, 3, RelPos.TOP_LEFT);
		selection = 0;
		this.player = player;
		Menu[] menus = new Menu[2];
		menus[0] = new InventoryMenu(player, player.getInventory(), "Inventory");
		itemDescription = player.getInventory().get(menus[0].getSelection()).getDescription();
		menus[1] = builder.setPositioning(new Point(9, menus[0].getBounds().getBottom()+27), RelPos.BOTTOM_RIGHT)
			.setEntries(StringEntry.useLines(Color.WHITE, itemDescription.split("\n")))
			.setSelectable(false)
			.createMenu();
		this.menus = menus;
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
		itemDescription = player.getInventory().get(menus[0].getSelection()).getDescription();
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		menus[1] = builder.setEntries(StringEntry.useLines(Color.WHITE, itemDescription.split("\n")))
			.createMenu(); // This resizes menu
		// Searcher help text
		String text = "(" + Game.input.getMapping("SEARCHER-BAR") + ") " + Localization.getLocalized("to search.");

		Font.draw(text, screen, 12, Screen.h/ 2 + 16, Color.WHITE);
	}
}
