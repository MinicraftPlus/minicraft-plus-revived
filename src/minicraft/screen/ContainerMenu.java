package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.Chest;
import minicraft.entity.Inventory;
import minicraft.entity.Player;
import minicraft.gfx.Screen;

public class ContainerMenu extends Display {
	
	private static final int padding = 10;
	
	private Inventory pInv, cInv;
	
	public ContainerMenu(Player player, Chest chest) {
		super(new InventoryMenu(chest.inventory.getItems(), chest.name), new InventoryMenu(player.inventory.getItems()));
		pInv = player.inventory;
		cInv = chest.inventory;
		
		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
	}
	
	@Override
	protected void onSelectionChange(int oldSel, int newSel) {
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
			Inventory from = selection == 0 ? cInv : pInv;
			Inventory to = from == cInv ? pInv : cInv;
			
			int toSel = menus[otherIdx].getSelection();
			int fromSel = curMenu.getSelection();
			
			to.add(toSel, from.remove(fromSel));
			
			menus[selection] = new InventoryMenu(from.getItems(), menus[selection].getTitle());
			menus[otherIdx] = new InventoryMenu(to.getItems(), menus[otherIdx].getTitle());
			menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
			onSelectionChange(0, selection);
			menus[selection].setSelection(fromSel);
			menus[otherIdx].setSelection(toSel);
		}
	}
}
