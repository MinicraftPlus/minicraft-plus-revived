package minicraft.screen;

import minicraft.core.*;
import minicraft.core.InputHandler;
import minicraft.entity.furniture.Chest;
import minicraft.item.Inventory;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;

public class ContainerDisplay extends Display {
	
	private static final int padding = 10;
	
	private Player player;
	private Chest chest;
	
	public ContainerDisplay(Player player, Chest chest) {
		super(new InventoryMenu(chest, chest.inventory, chest.name), new InventoryMenu(player, player.inventory, "Inventory"));
		//pInv = player.inventory;
		//cInv = chest.inventory;
		this.player = player;
		this.chest = chest;
		
		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
		
		if(menus[0].getNumOptions() == 0) onSelectionChange(0, 1);
	}
	
	@Override
	protected void onSelectionChange(int oldSel, int newSel) {
		super.onSelectionChange(oldSel, newSel);
		if(oldSel == newSel) return; // this also serves as a protection against access to menus[0] when such may not exist.
		int shift = 0;
		if(newSel == 0) shift = padding - menus[0].getBounds().getLeft();
		if(newSel == 1) shift = (Screen.w - padding) - menus[1].getBounds().getRight();
		for(Menu m: menus)
			m.translate(shift, 0);
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		
		if(input.getKey("menu").clicked) {
			Game.setMenu(null);
			return;
		}
		
		Menu curMenu = menus[selection];
		int otherIdx = (selection+1) % 2;
		
		if(input.getKey("attack").clicked && curMenu.getNumOptions() > 0) {
			// switch inventories
			Inventory from, to;
			if(selection == 0) {
				from = chest.inventory;
				to = player.inventory;
			} else {
				from = player.inventory;
				to = chest.inventory;
			}
			
			int toSel = menus[otherIdx].getSelection();
			int fromSel = curMenu.getSelection();
			
			to.add(toSel, from.remove(fromSel));
			
			menus[selection] = new InventoryMenu((InventoryMenu)menus[selection]);
			menus[otherIdx] = new InventoryMenu((InventoryMenu)menus[otherIdx]);
			//menus[otherIdx] = new InventoryMenu(selection==0?player:chest, to, menus[otherIdx].getTitle());
			menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
			onSelectionChange(0, selection);
			//menus[selection].setSelection(fromSel);
			//menus[otherIdx].setSelection(toSel);
		}
	}
}
