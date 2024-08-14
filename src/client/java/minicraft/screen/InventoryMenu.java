package minicraft.screen;

import minicraft.core.Action;
import minicraft.core.io.InputHandler;
import minicraft.entity.Entity;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.screen.entry.ItemEntry;
import org.jetbrains.annotations.Nullable;

class InventoryMenu extends ItemListMenu {

	private final RelPos entryPos; // Used for copy constructor
	private final String title; // Used for copy constructor
	private final Inventory inv;
	private final Entity holder;
	private final boolean creativeInv;
	private final @Nullable Action onStackUpdateListener; // The length of the entry shown may change when there is an update to the stack.

	InventoryMenu(Entity holder, Inventory inv, String title, RelPos entryPos, @Nullable Action onStackUpdateListener) { this(holder, inv, title, entryPos, false, onStackUpdateListener); }
	InventoryMenu(Entity holder, Inventory inv, String title, RelPos entryPos, boolean creativeInv) { this(holder, inv, title, entryPos, creativeInv, null); }
	InventoryMenu(Entity holder, Inventory inv, String title, RelPos entryPos, boolean creativeInv, @Nullable Action onStackUpdateListener) {
		super(ItemListMenu.getBuilder(entryPos), ItemEntry.useItems(inv.getItems()), title);
		this.inv = inv;
		this.holder = holder;
		this.title = title;
		this.entryPos = entryPos;
		this.creativeInv = creativeInv;
		this.onStackUpdateListener = onStackUpdateListener;
	}

	InventoryMenu(InventoryMenu model) {
		super(ItemListMenu.getBuilder(model.entryPos), ItemEntry.useItems(model.inv.getItems()), model.title);
		this.inv = model.inv;
		this.holder = model.holder;
		this.creativeInv = model.creativeInv;
		this.title = model.title;
		this.entryPos = model.entryPos;
		this.onStackUpdateListener = model.onStackUpdateListener;
		setSelection(model.getSelection());
	}

	@Override
	public void tick(InputHandler input) {
		super.tick(input);

		boolean dropOne = input.inputPressed("drop-one");

		if (getNumOptions() > 0 && (dropOne || input.inputPressed("drop-stack"))) {
			ItemEntry entry = ((ItemEntry) getCurEntry());
			if (entry == null) return;
			Item invItem = entry.getItem();
			Item drop = invItem.copy();

			if (!creativeInv) {
				if (!dropOne || !(drop instanceof StackableItem) || ((StackableItem) drop).count <= 1) {
					// drop the whole item.
					removeSelectedEntry();
				} else {
					// just drop one from the stack
					((StackableItem) drop).count = 1;
					((StackableItem) invItem).count--;
				}

				if (onStackUpdateListener != null) {
					onStackUpdateListener.act();
				}
			}

			if (holder.getLevel() != null) {
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
