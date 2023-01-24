package minicraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import minicraft.screen.QuestsDisplay;
import org.jetbrains.annotations.Nullable;

import minicraft.entity.furniture.Furniture;
import minicraft.util.Logging;

public class Inventory {
	private final Random random = new Random();
	private final List<Item> items = new ArrayList<>(); // The list of items that is in the inventory.

	protected int maxItem = 27;
	protected boolean unlimited = false;

	public int getMaxSlots() {
		return maxItem;
	}

	/**
	 * Returns all the items which are in this inventory.
	 * @return ArrayList containing all the items in the inventory.
	 */
	public List<Item> getItems() { return new ArrayList<>(items); }
	public void clearInv() { items.clear(); }
	public int invSize() { return items.size(); }

	/**
	 * Get one item in this inventory.
	 * @param idx The index of the item in the inventory's item array.
	 * @return The specified item.
	 */
	public Item get(int idx) { return items.get(idx); }

	/**
	 * Remove an item in this inventory.
	 * @param idx The index of the item in the inventory's item array.
	 * @return The removed item.
	 */
	public Item remove(int idx) { return items.remove(idx); }

	/** Adds an item to the inventory */
	public int add(@Nullable Item item) {
		if (item != null)
			return add(items.size(), item);  // Adds the item to the end of the inventory list
		return 0;
	}

	/**
	 * Adds several copies of the same item to the end of the inventory.
	 * @param item Item to be added.
	 * @param num Amount of items to add.
	 */
	public int add(Item item, int num) {
		int total = 0;
		for (int i = 0; i < num; i++)
			total += add(item.clone());
		return total;
	}

	/**
	 * Adds an item to a specific spot in the inventory.
	 * @param slot Index to place item at.
	 * @param item Item to be added.
	 * @return The number of items added.
	 */
	public int add(int slot, Item item) {

		// Do not add to inventory if it is a PowerGlove
		if (item instanceof PowerGloveItem) {
			Logging.INVENTORY.warn("Tried to add power glove to inventory. stack trace:", new Exception());
			return 0;
		}

		if (item instanceof StackableItem) { // If the item is a item...
			StackableItem toTake = (StackableItem) item; // ...convert it into a StackableItem object.
			int total = toTake.count;

			for (Item value : items) {
				if (toTake.stacksWith(value)) {
					StackableItem stack = (StackableItem) value;

					if (!unlimited) {
						if (stack.count < stack.maxCount) {
							int r = stack.maxCount - stack.count;
							if (r >= toTake.count) {
								// Matching implies that the other item is stackable, too.
								stack.count += toTake.count;
								return total;
							} else {
								toTake.count -= r;
								stack.count += r;
							}
						}
					} else {
						stack.count += toTake.count;
						return total;
					}
				}
			}

			if (!unlimited) {
				if (items.size() < maxItem) {
					int c = (int) Math.ceil(toTake.count/100.0);
					for (int i = 0; i < c; i++) {
						StackableItem adding = toTake.clone();
						adding.count = i + 1 == c && toTake.count % 100 > 0 ? toTake.count % 100 : 100;
						if (adding.count == 0) break;
						if (items.size() == maxItem) return total - toTake.count;
						addItemToList(adding); // Add the item to the items list
						toTake.count -= adding.count;
					}
					return total;
				} else {
					return total - toTake.count;
				}
			} else {
				addItemToList(slot, toTake);
				return total;
			}
		}

		if (!unlimited) {
			if (items.size() < maxItem) {
				addItemToList(slot, item); // Add the item to the items list
				return 1;
			} else {
				return 0;
			}
		} else {
			addItemToList(slot, item);
			return 1;
		}
	}

	private void addItemToList(Item item) {
		resolveQuests(item);
		items.add(item);
	}
	private void addItemToList(int index, Item item) {
		resolveQuests(item);
		items.add(index, item);
	}

	private void resolveQuests(Item item) {
		if (item.equals(Items.get("Wheat Seeds"))) {
			QuestsDisplay.questCompleted("minicraft.quest.tutorial.farming.get_seeds");
		} else if (item.equals(Items.get("Coal"))) {
			QuestsDisplay.questCompleted("minicraft.quest.tutorial.underground.get_coal");
		} else if (item.equals(Items.get("Iron Ore"))) {
			QuestsDisplay.questCompleted("minicraft.quest.tutorial.underground.get_iron_ore");
		} else if (item.equals(Items.get("Iron"))) {
			QuestsDisplay.questCompleted("minicraft.quest.tutorial.underground.get_iron");
		}
	}

	/** Removes items from your inventory; looks for stacks, and removes from each until reached count. returns amount removed. */
	private int removeFromStack(StackableItem given, int count) {
		int removed = 0; // To keep track of amount removed.
		for (int i = 0; i < items.size(); i++) {
			if (!(items.get(i) instanceof StackableItem)) continue;
			StackableItem curItem = (StackableItem) items.get(i);
			if (!curItem.stacksWith(given)) continue; // Can't do equals, becuase that includes the stack size.
			// equals; and current item is stackable.
			int amountRemoving = Math.min(count-removed, curItem.count); // This is the number of items that are being removed from the stack this run-through.
			curItem.count -= amountRemoving;
			if (curItem.count == 0) { // Remove the item from the inventory if its stack is empty.
				remove(i);
				i--;
			}
			removed += amountRemoving;
			if (removed == count) break;
			if (removed > count) { // Just in case...
				Logging.INVENTORY.info("SCREW UP while removing items from stack: " + (removed-count) + " too many.");
				break;
			}
			// If not all have been removed, look for another stack.
		}

		if (removed < count) Logging.INVENTORY.info("Inventory: could not remove all items; " + (count-removed) + " left.");
		return removed;
	}

	/**
	 * Removes the item from the inventory entirely, whether it's a stack, or a lone item.
	 */
	public void removeItem(Item i) {
		//if (Game.debug) System.out.println("original item: " + i);
		if (i instanceof StackableItem)
			removeItems(i.clone(), ((StackableItem)i).count);
		else
			removeItems(i.clone(), 1);
	}

	/**
	 * Removes items from this inventory. Note, if passed a stackable item, this will only remove a max of count from the stack.
	 * @param given Item to remove.
	 * @param count Max amount of the item to remove.
	 */
	public void removeItems(Item given, int count) {
		if (given instanceof StackableItem)
			count -= removeFromStack((StackableItem)given, count);
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
			Logging.INVENTORY.warn("Could not remove " + count + " " + given + (count>1?"s":"") + " from inventory");
	}

	/** Returns the how many of an item you have in the inventory. */
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
		for (Item i: items)
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

		for (String item: items.split(":")) // This still generates a 1-item array when "items" is blank... [""].
			add(Items.get(item));
	}

	/**
	 * Tries to add an item to the inventory.
	 * @param chance Chance for the item to be added.
	 * @param item Item to be added.
	 * @param num How many of the item.
	 * @param allOrNothing if true, either all items will be added or none, if false its possible to add
	 * between 0-num items.
	 */
	public void tryAdd(int chance, Item item, int num, boolean allOrNothing) {
		if (!allOrNothing || random.nextInt(chance) == 0)
			for (int i = 0; i < num; i++)
				if (allOrNothing || random.nextInt(chance) == 0)
					add(item.clone());
	}
	public void tryAdd(int chance, @Nullable Item item, int num) {
		if (item == null) return;
		if (item instanceof StackableItem) {
			((StackableItem)item).count *= num;
			tryAdd(chance, item, 1, true);
		} else
			tryAdd(chance, item, num, false);
	}
	public void tryAdd(int chance, @Nullable Item item) { tryAdd(chance, item, 1); }
	public void tryAdd(int chance, ToolType type, int lvl) {
		tryAdd(chance, new ToolItem(type, lvl));
	}

	/**
	 * Tries to add an Furniture to the inventory.
	 * @param chance Chance for the item to be added.
	 * @param type Type of furniture to add.
	 */
	public void tryAdd(int chance, Furniture type) {
		tryAdd(chance, new FurnitureItem(type));
	}
}
