package minicraft.screen;

import minicraft.core.Action;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Sound;
import minicraft.entity.Entity;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.screen.entry.ItemEntry;
import org.jetbrains.annotations.Nullable;

class InventoryMenu extends ItemListMenu {

	private final Inventory inv;
	private final Entity holder;
	private final boolean creativeInv;
	private final boolean sortable; // Both sortable and stack merge-able
	private final @Nullable Action onStackUpdateListener; // The length of the entry shown may change when there is an update to the stack.

	InventoryMenu(Entity holder, Inventory inv, String title, RelPos entryPos, @Nullable Action onStackUpdateListener) { this(holder, inv, title, entryPos, false, true, onStackUpdateListener); }
	InventoryMenu(Entity holder, Inventory inv, String title, RelPos entryPos, boolean creativeInv, boolean sortable) { this(holder, inv, title, entryPos, creativeInv, sortable, null); }
	InventoryMenu(Entity holder, Inventory inv, String title, RelPos entryPos, boolean creativeInv, boolean sortable, @Nullable Action onStackUpdateListener) {
		super(ItemListMenu.getBuilder(entryPos), ItemEntry.useItems(inv.getItems()), title);
		this.inv = inv;
		this.holder = holder;
		this.creativeInv = creativeInv;
		this.sortable = sortable;
		this.onStackUpdateListener = onStackUpdateListener;
	}

	InventoryMenu(InventoryMenu model) {
		super(ItemListMenu.getBuilder(), ItemEntry.useItems(model.inv.getItems()), model.getTitle());
		this.inv = model.inv;
		this.holder = model.holder;
		this.creativeInv = model.creativeInv;
		this.sortable = model.sortable;
		this.onStackUpdateListener = model.onStackUpdateListener;
		setSelection(model.getSelection());
	}

	@Override
	public void tick(InputHandler input) {
		if (sortable) {
			// Item slot movement; preventing up and down being proceeded before this
			if (input.getKey("SHIFT").down && input.getKey("CURSOR-UP").clicked) {
				int sel = getSelection();
				if (sel > 0) { // If there is an entry above.
					inv.swapSlots(sel, --sel);
					setSelection(sel);
					Sound.play("select");
					refreshEntries();
				}
				return;
			} else if (input.getKey("SHIFT").down && input.getKey("CURSOR-DOWN").clicked) {
				int sel = getSelection();
				if (getNumOptions() > sel + 1) { // If there is an entry below.
					inv.swapSlots(sel, ++sel);
					setSelection(sel);
					Sound.play("select");
					refreshEntries();
				}
				return;

			} else if (input.getKey("CTRL").down && input.getKey("CURSOR-UP").clicked) { // StackableItem slot stacking
				int sel = getSelection();
				if (sel > 0) { // If there is an entry above.
					Item item = inv.get(sel);
					Item toItem = inv.get(sel - 1);
					if (item instanceof StackableItem && ((StackableItem) item).stacksWith(toItem)) {
						if (((StackableItem) toItem).count < ((StackableItem) toItem).maxCount) {
							if (((StackableItem) toItem).count + ((StackableItem) item).count <= ((StackableItem) toItem).maxCount) {
								((StackableItem) toItem).count += ((StackableItem) item).count;
								inv.remove(sel);
							} else {
								((StackableItem) item).count -= ((StackableItem) toItem).maxCount - ((StackableItem) toItem).count;
								((StackableItem) toItem).count = ((StackableItem) toItem).maxCount;
							}

							setSelection(sel - 1);
							Sound.play("select");
							if (onStackUpdateListener != null) {
								onStackUpdateListener.act();
							}
						}
					}
				}
				return;
			} else if (input.getKey("CTRL").down && input.getKey("CURSOR-DOWN").clicked) { // StackableItem slot stacking
				int sel = getSelection();
				if (getNumOptions() > sel + 1) { // If there is an entry below.
					Item item = inv.get(sel);
					Item toItem = inv.get(sel + 1);
					if (item instanceof StackableItem && ((StackableItem) item).stacksWith(toItem)) {
						if (((StackableItem) toItem).count < ((StackableItem) toItem).maxCount) {
							if (((StackableItem) toItem).count + ((StackableItem) item).count <= ((StackableItem) toItem).maxCount) {
								((StackableItem) toItem).count += ((StackableItem) item).count;
								inv.remove(sel);
							} else {
								((StackableItem) item).count -= ((StackableItem) toItem).maxCount - ((StackableItem) toItem).count;
								((StackableItem) toItem).count = ((StackableItem) toItem).maxCount;
								setSelection(sel + 1);
							}

							Sound.play("select");
							if (onStackUpdateListener != null) {
								onStackUpdateListener.act();
							}
						}
					}
				}
				return;
			}
		}

		super.tick(input);

		boolean dropOne = input.inputPressed("drop-one");

		if(getNumOptions() > 0 && (dropOne || input.inputPressed("drop-stack"))) {
			ItemEntry entry = ((ItemEntry)getCurEntry());
			if(entry == null) return;
			Item invItem = entry.getItem();
			Item drop = invItem.copy();

			if (!creativeInv) {
				if (dropOne && drop instanceof StackableItem && ((StackableItem)drop).count > 1) {
					// just drop one from the stack
					((StackableItem)drop).count = 1;
					((StackableItem)invItem).count--;
				} else {
					// drop the whole item.
					removeSelectedEntry();
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

	// This does not reconstruct and recalculate the frame.
	private void refreshEntries() {
		setEntries(ItemEntry.useItems(inv.getItems()));
	}
}
