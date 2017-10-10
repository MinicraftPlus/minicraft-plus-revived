package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.Player;

public class PlayerInvMenu extends Display {
	
	private Player player;
	
	public PlayerInvMenu(Player player) {
		super(new InventoryMenu(player.inventory.getItems()));
		this.player = player;
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		
		if(input.getKey("menu").clicked) {
			Game.setMenu(null);
			return;
		}
		
		InventoryMenu curMenu = (InventoryMenu) menus[selection];
		
		if(input.getKey("attack").clicked && curMenu != null && curMenu.getNumOptions() > 0) {
			player.activeItem = player.inventory.remove(curMenu.getSelection());
			Game.setMenu(null);
		}
	}
}
