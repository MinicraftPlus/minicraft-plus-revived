package minicraft.screen;

import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.Entity;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.screen.entry.ItemEntry;

class InventoryMenu extends ItemListMenu {
	
	private Inventory inv;
	private Entity holder;
	
	InventoryMenu(Entity holder, Inventory inv, String title) {
		super(ItemEntry.useItems(inv.getItems()), title);
		this.inv = inv;
		this.holder = holder;
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		
		if(getNumOptions() > 0 && (input.getKey("drop-one").clicked || input.getKey("drop-stack").clicked)) {
			Item invItem = ((ItemEntry)getCurEntry()).getItem();
			Item drop = invItem.clone();
			
			if(input.getKey("drop-one").clicked && drop instanceof StackableItem && ((StackableItem)drop).count > 1) {
				// just drop one from the stack
				((StackableItem)drop).count = 1;
				((StackableItem)invItem).count--;
			} else {
				// drop the whole item.
				if(!Game.isMode("creative"))
					removeSelectedEntry();
			}
			
			if(holder.getLevel() != null) {
				if(Game.isValidClient())
					Game.client.dropItem(drop);
				else
					holder.getLevel().dropItem(holder.x, holder.y, drop);
			}
		}
	}
	
	@Override
	public void removeSelectedEntry() {
		inv.remove(getSelection());
		super.removeSelectedEntry();
	}
}
