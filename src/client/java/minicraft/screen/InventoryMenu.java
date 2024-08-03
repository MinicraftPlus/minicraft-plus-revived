package minicraft.screen;

import minicraft.core.Action;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.entity.Entity;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.screen.entry.ItemEntry;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;

class InventoryMenu extends ItemListMenu {

	private final Inventory inv;
	private final Entity holder;
	private final boolean creativeInv;
	private final @Nullable Action onStackUpdateListener; // The length of the entry shown may change when there is an update to the stack.

	InventoryMenu(Entity holder, Inventory inv, Localization.LocalizationString title,
	              @MagicConstant(intValues = { ItemListMenu.POS_LEFT, ItemListMenu.POS_RIGHT }) int slot,
	              @Nullable Action onStackUpdateListener) {
		this(holder, inv, title, slot, false, onStackUpdateListener);
	}
	InventoryMenu(Entity holder, Inventory inv, Localization.LocalizationString title,
	              @MagicConstant(intValues = { ItemListMenu.POS_LEFT, ItemListMenu.POS_RIGHT }) int slot,
	              boolean creativeInv) {
		this(holder, inv, title, slot, creativeInv, null);
	}
	InventoryMenu(Entity holder, Inventory inv, Localization.LocalizationString title,
	              @MagicConstant(intValues = { ItemListMenu.POS_LEFT, ItemListMenu.POS_RIGHT }) int slot,
	              boolean creativeInv, @Nullable Action onStackUpdateListener) {
		super(ItemListMenu.getBuilder(slot), ItemEntry.useItems(inv.getItems()), title);
		this.inv = inv;
		this.holder = holder;
		this.creativeInv = creativeInv;
		this.onStackUpdateListener = onStackUpdateListener;

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

	public void refresh() {
		setEntries(ItemEntry.useItems(inv.getItems()));
		setSelection(getSelection());
	}

	@Override
	public void removeSelectedEntry() {
		inv.remove(getSelection());
		super.removeSelectedEntry();
	}
}
