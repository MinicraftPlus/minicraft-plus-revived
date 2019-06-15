package minicraft.screen;

import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.*;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.screen.entry.BlankEntry;
import minicraft.screen.entry.ItemEntry;
import minicraft.screen.entry.ListEntry;

class InventoryMenu extends ItemListMenu {
	
	private Inventory inv;
	private Entity holder;
	
	InventoryMenu(Entity holder, Inventory inv, String title) {
		super(ItemEntry.useItems(inv.getItems()), title);
		this.inv = inv;
		this.holder = holder;
	}
	
	InventoryMenu(InventoryMenu model) {
		super(ItemEntry.useItems(model.inv.getItems()), model.getTitle());
		this.inv = model.inv;
		this.holder = model.holder;
		setSelection(model.getSelection());
	}
	
	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		
		boolean dropOne = input.getKey("drop-one").clicked && !(Game.getMenu() instanceof ContainerDisplay);
		
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

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// just for rendering the enchantment indicator
		int y = super.getBounds().getTop();
		for (int i = super.offset; i < super.offset + super.displayLength; i++) {

			int idx = i % getEntries().length;
			ListEntry entry = getEntries()[idx];
			if (!(entry instanceof BlankEntry)) {
				if (inv.get(idx) instanceof ToolItem && ((ToolItem) inv.get(idx)).ench == 1) {
					Point pos = entryPos.positionRect(new Dimension(entry.getWidth(), ListEntry.getHeight()), new Rectangle(entryBounds.getLeft(), y, entryBounds.getWidth(), ListEntry.getHeight(), Rectangle.CORNER_DIMS));
					screen.render(pos.x - 8, pos.y + 8, 6 + 10 * 32, Color.get(-1, 50, 40, 40), 0);
				}
			}

			y += ListEntry.getHeight() + spacing;
		}
	}
}
