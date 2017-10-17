package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.Player;
import minicraft.screen.entry.ItemEntry;
import minicraft.screen.entry.ListEntry;

public class PlayerInvMenu extends Display {
	
	private Player player;
	
	public PlayerInvMenu(Player player) {
		super();
		
		menus = new Menu[] {new InventoryMenu(player.inventory.getItems()) {
			@Override
			public void removeSelectedEntry() {
				player.inventory.remove(selection);
				super.removeSelectedEntry();
			}
			
			@Override
			public void updateSelectedEntry(ListEntry newEntry) {
				if(newEntry instanceof ItemEntry) { // should ALWAYS be true
					player.inventory.removeItems(getSelectedItem(), 1);
				}
				super.updateSelectedEntry(newEntry);
			}
		}};
		
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
