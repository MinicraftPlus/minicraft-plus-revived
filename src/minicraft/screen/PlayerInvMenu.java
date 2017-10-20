package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.Player;

public class PlayerInvMenu extends Display {
	
	private Player player;
	
	public PlayerInvMenu(Player player) {
		super(new InventoryMenu(player, player.inventory, "Inventory"));
		this.player = player;
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		
		if(input.getKey("menu").clicked) {
			Game.setMenu(null);
			return;
		}
		
		if(input.getKey("attack").clicked && menus[0].getNumOptions() > 0) {
			player.activeItem = player.inventory.remove(menus[0].getSelection());
			Game.setMenu(null);
		}
	}
}
