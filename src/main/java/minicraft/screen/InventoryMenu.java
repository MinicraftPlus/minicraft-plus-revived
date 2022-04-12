package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.screen.entry.ItemEntry;

class InventoryMenu extends ItemListMenu {
	
	private final Inventory inv;
	private final Entity holder;
	
	InventoryMenu(Entity holder, Inventory inv, String title) {
		super(ItemListMenu.getBuilder(), ItemEntry.useItems(inv.getItems()), title);
		this.inv = inv;
		this.holder = holder;
	}
	
	InventoryMenu(InventoryMenu model) {
		super(ItemListMenu.getBuilder(), ItemEntry.useItems(model.inv.getItems()), model.getTitle());
		this.inv = model.inv;
		this.holder = model.holder;
		setSelection(model.getSelection());
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		
		boolean dropOne = input.getKey("drop-one").clicked && !(Game.getDisplay() instanceof ContainerDisplay);
		
		if(getNumOptions() > 0 && (dropOne || input.getKey("drop-stack").clicked)) {
			ItemEntry entry = ((ItemEntry)getCurEntry());
			if(entry == null) return;
			Item invItem = entry.getItem();
			Item drop = invItem.clone();
			
			if(dropOne && drop instanceof StackableItem && ((StackableItem)drop).count > 1) {
				// just drop one from the stack
				((StackableItem)drop).count = 1;
				((StackableItem)invItem).count--;
			} else {
				// drop the whole item.
				if(!Game.isMode("creative") || !(holder instanceof Player))
					removeSelectedEntry();
			}
			
			if(holder.getLevel() != null) {
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
