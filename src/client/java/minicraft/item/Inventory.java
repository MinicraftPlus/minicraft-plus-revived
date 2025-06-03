package minicraft.item;

import minicraft.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Inventory {
	private final List<ItemStack> items = new ArrayList<>(); // The list of items that is in the inventory.

	protected int maxItem = 27;
	protected boolean unlimited = false;

	public int getMaxSlots() {
		return maxItem;
	}

	/**
	 * Returns all the items which are in this inventory.
	 * @return ArrayList containing all the items in the inventory.
	 */
	public List<ItemStack> getItems() {
		return new ArrayList<>(items);
	}

	public void clearInv() {
		items.clear();
	}

	public int invSize() {
		return items.size();
	}

	/**
	 * Get one item in this inventory.
	 * @param idx The index of the item in the inventory's item array.
	 * @return The specified item.
	 */
	public ItemStack get(int idx) {
		return items.get(idx);
	}

	/**
	 * Remove an item in this inventory.
	 * @param idx The index of the item in the inventory's item array.
	 * @return The removed item.
	 */
	public ItemStack remove(int idx) {
		return items.remove(idx);
	}

	/**
	 * Adds several copies of the same item to the end of the inventory.
	 * @param item Item to be added.
	 * @param num Amount of items to add.
	 * @return the remaining item not being added; empty if whole stack of items has been added successfully
	 */
	public List<ItemStack> add(@NotNull ItemStack item, int num) {
		ArrayList<ItemStack> remaining = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			ItemStack remain = add(item.copy());
			if (remain != null) remaining.add(remain);
		}
		return remaining;
	}

	/**
	 * Adds an item at the end of the inventory.
	 * @param item Item to be added.
	 * @return the remaining item not being added; {@code null} if whole stack of items has been added successfully
	 */
	public @Nullable ItemStack add(@Nullable ItemStack item) {
		if (item == null) return null;
		// Do not add to inventory if it is a PowerGlove
		if (item.isOf(PowerGloveItem.class)) {
			Logging.INVENTORY.warn("Tried to add power glove to inventory. stack trace:", new Exception());
			return null;
		}

		if (item.isStackable()) { // If the item is a item...
			ItemStack toTake = item; // ...convert it into a StackableItem object.
			for (ItemStack stack : items) {
				if (((StackableItem) toTake.getItem()).stacksWith(stack.getItem())) {
					if (!unlimited) {
						if (stack.getCount() < stack.getMaxCount()) {
							int r = stack.getMaxCount() - stack.getCount();
							if (r >= toTake.getCount()) {
								// Matching implies that the other item is stackable, too.
								stack.increment(toTake.getCount());
								return null;
							} else {
								toTake.decrement(r);
								stack.increment(r);
							}
						}
					} else {
						stack.increment(toTake.getCount());
						return null;
					}
				}
			}

			if (!unlimited) {
				if (items.size() < maxItem) {
					while (toTake.getCount() > 0) {
						if (items.size() == maxItem) return toTake;
						ItemStack adding = toTake.copy();
						adding.setCount(Math.min(toTake.getCount(), toTake.getMaxCount()));
						items.add(adding); // Add the item to the items list
						toTake.decrement(adding.getCount());
					}
					return null;
				} else {
					return toTake;
				}
			} else {
				items.add(toTake);
				return null;
			}
		}

		if (!unlimited) {
			if (items.size() < maxItem) {
				items.add(item); // Add the item to the items list
				return null;
			} else {
				return item;
			}
		} else {
			items.add(item);
			return null;
		}
	}

	/**
	 * Removes items from your inventory; looks for stacks, and removes from each until reached count. returns amount removed.
	 */
	private int removeFromStack(StackableItem given, int count) {
		int removed = 0; // To keep track of amount removed.
		for (int i = 0; i < items.size(); i++) {
			if (!(items.get(i).isStackable())) continue;
			ItemStack curItem = items.get(i);
			if (!((StackableItem) curItem.getItem()).stacksWith(given)) continue; // Can't do equals, becuase that includes the stack size.
			// equals; and current item is stackable.
			int amountRemoving = Math.min(count - removed, curItem.getCount()); // This is the number of items that are being removed from the stack this run-through.
			curItem.decrement(amountRemoving);
			if (curItem.getCount() == 0) { // Remove the item from the inventory if its stack is empty.
				remove(i);
				i--;
			}
			removed += amountRemoving;
			if (removed == count) break;
			if (removed > count) { // Just in case...
				Logging.INVENTORY.info("SCREW UP while removing items from stack: " + (removed - count) + " too many.");
				break;
			}
			// If not all have been removed, look for another stack.
		}

		if (removed < count)
			Logging.INVENTORY.info("Inventory: could not remove all items; " + (count - removed) + " left.");
		return removed;
	}

	/**
	 * Removes the item from the inventory entirely, whether it's a stack, or a lone item.
	 */
	public void removeItem(ItemStack i) {
		//if (Game.debug) System.out.println("original item: " + i);
		if (i.isStackable())
			removeItems(i.copy(), i.getCount());
		else
			removeItems(i.copy(), 1);
	}

	/**
	 * Removes items from this inventory. Note, if passed a stackable item, this will only remove a max of count from the stack.
	 * @param given Item to remove.
	 * @param count Max amount of the item to remove.
	 */
	public void removeItems(ItemStack given, int count) {
		if (given.isStackable())
			count -= removeFromStack((StackableItem) given.getItem(), count);
		else {
			for (int i = 0; i < items.size(); i++) {
				Item curItem = items.get(i).getItem();
				if (curItem.equals(given)) {
					remove(i);
					count--;
					if (count == 0) break;
				}
			}
		}

		if (count > 0)
			Logging.INVENTORY.warn("Could not remove " + count + " " + given + (count > 1 ? "s" : "") + " from inventory");
	}

	/**
	 * Returns the how many of an item you have in the inventory.
	 */
	public int count(ItemStack given) {
		if (given == null) return 0; // null requests get no items. :)

		int found = 0; // Initialize counting var
		// Assign current item
		for (ItemStack curItem : items) { // Loop though items in inv
			// If the item can be a stack...
			if (curItem.getItem() instanceof StackableItem && ((StackableItem) curItem.getItem()).stacksWith(given.getItem()))
				found += curItem.getCount(); // Add however many items are in the stack.
			else if (curItem.equals(given))
				found++; // Otherwise, just add 1 to the found count.
		}

		return found;
	}

	/**
	 * Generates a string representation of all the items in the inventory which can be sent
	 * over the network.
	 * @return String representation of all the items in the inventory.
	 */
	public String getItemData() {
		StringBuilder itemdata = new StringBuilder();
		for (ItemStack i : items)
			itemdata.append(i.getData()).append(":");

		if (itemdata.length() > 0)
			itemdata = new StringBuilder(itemdata.substring(0, itemdata.length() - 1)); // Remove extra ",".

		return itemdata.toString();
	}

	/**
	 * Replaces all the items in the inventory with the items in the string.
	 * @param items String representation of an inventory.
	 */
	public void updateInv(String items) {
		clearInv();

		if (items.length() == 0) return; // There are no items to add.

		for (String item : items.split(":")) // This still generates a 1-item array when "items" is blank... [""].
			add(Items.getStackOf(item));
	}

	/**
	 * Tries to add an item to the inventory.
	 * @param random The {@code Random} number generator.
	 * @param chance Chance for the item to be added.
	 * @param item Item to be added.
	 * @param num How many of the item.
	 * @param allOrNothing if true, either all items will be added or none, if false its possible to add
	 * 	between 0-num items.
	 */
	public void tryAdd(Random random, int chance, ItemStack item, int num, boolean allOrNothing) {
		if (!allOrNothing || random.nextInt(chance) == 0)
			for (int i = 0; i < num; i++)
				if (allOrNothing || random.nextInt(chance) == 0)
					add(item.copy());
	}

	public void tryAdd(Random random, int chance, @Nullable ItemStack item, int num) {
		if (item == null) return;
		if (item.isStackable()) {
			item.setCount(item.getCount() * num);
			tryAdd(random, chance, item, 1, true);
		} else
			tryAdd(random, chance, item, num, false);
	}
}
