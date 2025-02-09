package minicraft.item;

import minicraft.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * An item storage space for general purposes, based on the maximum number of item stacks,
 * and the default item maximum count for stackable items.
 * <p>
 *     For special conditional storage space for items, like custom maximum count of items and
 *     storage space that is variable to other conditions, slots or a custom implementation
 *     is recommended instead.
 * </p>
 */
public abstract class Inventory {
	protected final List<Item> items = new ArrayList<>(); // The list of items that is in the inventory.

	/**
	 * Returns all the items which are in this inventory.
	 * @return a read-only view of the internal list containing all the items in the inventory.
	 */
	public List<Item> getItemsView() {
		return Collections.unmodifiableList(items);
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
	public Item get(int idx) {
		return items.get(idx);
	}

	/**
	 * Remove an item in this inventory.
	 * @param idx The index of the item in the inventory's item array.
	 * @return The removed item.
	 */
	public Item remove(int idx) {
		return items.remove(idx);
	}

	/**
	 * Adds several copies of the same item to the end of the inventory.
	 * @param item Item to be added.
	 * @param num Amount of items to add.
	 * @return the remaining item not being added; empty if whole stack of items has been added successfully
	 */
	public List<Item> add(@NotNull Item item, int num) {
		ArrayList<Item> remaining = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			Item remain = add(item.copy());
			if (remain != null) remaining.add(remain);
		}
		return remaining;
	}

	/**
	 * Adds an item at the end of the inventory.
	 * @param item Item to be added.
	 * @return the remaining item not being added; {@code null} if whole stack of items has been added successfully
	 */
	public abstract @Nullable Item add(@Nullable Item item);

	/**
	 * Removes items from your inventory; looks for stacks, and removes from each until reached count. returns amount removed.
	 */
	protected int removeFromStack(StackableItem given, int count) {
		int removed = 0; // To keep track of amount removed.
		for (int i = 0; i < items.size(); i++) {
			if (!(items.get(i) instanceof StackableItem)) continue;
			StackableItem curItem = (StackableItem) items.get(i);
			if (!curItem.stacksWith(given)) continue; // Can't do equals, becuase that includes the stack size.
			// equals; and current item is stackable.
			int amountRemoving = Math.min(count - removed, curItem.count); // This is the number of items that are being removed from the stack this run-through.
			curItem.count -= amountRemoving;
			if (curItem.count == 0) { // Remove the item from the inventory if its stack is empty.
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
	public void removeItem(Item i) {
		//if (Game.debug) System.out.println("original item: " + i);
		if (i instanceof StackableItem)
			removeItems(i.copy(), ((StackableItem) i).count);
		else
			removeItems(i.copy(), 1);
	}

	/**
	 * Removes items from this inventory. Note, if passed a stackable item, this will only remove a max of count from the stack.
	 * @param given Item to remove.
	 * @param count Max amount of the item to remove.
	 */
	public void removeItems(Item given, int count) {
		if (given instanceof StackableItem)
			count -= removeFromStack((StackableItem) given, count);
		else {
			for (int i = 0; i < items.size(); i++) {
				Item curItem = items.get(i);
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
	 * Returns how many of an item you have in the inventory.
	 */
	public int count(Item given) {
		if (given == null) return 0; // null requests get no items. :)

		int found = 0; // Initialize counting var
		// Assign current item
		for (Item curItem : items) { // Loop though items in inv
			// If the item can be a stack...
			if (curItem instanceof StackableItem && ((StackableItem) curItem).stacksWith(given))
				found += ((StackableItem) curItem).count; // Add however many items are in the stack.
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
		for (Item i : items)
			itemdata.append(i.getData()).append(":");

		if (itemdata.length() > 0)
			itemdata = new StringBuilder(itemdata.substring(0, itemdata.length() - 1)); // Remove extra ",".

		return itemdata.toString();
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
	public void tryAdd(Random random, int chance, Item item, int num, boolean allOrNothing) {
		if (!allOrNothing || random.nextInt(chance) == 0)
			for (int i = 0; i < num; i++)
				if (allOrNothing || random.nextInt(chance) == 0)
					add(item.copy());
	}

	public void tryAdd(Random random, int chance, @Nullable Item item, int num) {
		if (item == null) return;
		if (item instanceof StackableItem) {
			((StackableItem) item).count *= num;
			tryAdd(random, chance, item, 1, true);
		} else
			tryAdd(random, chance, item, num, false);
	}
}
