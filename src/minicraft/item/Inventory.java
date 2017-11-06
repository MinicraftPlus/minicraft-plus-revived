package minicraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import minicraft.entity.furniture.Furniture;

import org.jetbrains.annotations.Nullable;

public class Inventory {
	private Random random = new Random();
	private List<Item> items = new ArrayList<>(); // the list of items that is in the inventory.
	
	public List<Item> getItems() {
		List<Item> newItems = new ArrayList<>();
		newItems.addAll(items);
		return newItems;
	}
	public void clearInv() { items.clear(); }
	public int invSize() { return items.size(); }
	
	public Item get(int idx) { return items.get(idx); }
	
	public Item remove(int idx) { return items.remove(idx); }
	
	public boolean remove(Item item) {
		for(int i = 0; i < items.size(); i++) {
			if(items.get(i).equals(item)) {
				remove(i);
				return true;
			}
		}
		return false;
	}
	
	public void addAll(Inventory other) {
		for(Item i: other.getItems())
			add(i.clone());
	}
	
	/** Adds an item to the inventory */
	public void add(@Nullable Item item) {
		if(item != null)
			add(items.size(), item);  // adds the item to the end of the inventory list
	}
	
	public void add(Item item, int num) {
		for(int i = 0; i < num; i++)
			add(item.clone());
	}
	
	/** Adds an item to a specific spot in the inventory */
	public void add(int slot, Item item) {
		//if (Game.debug) System.out.println("adding item to an inventory: " + item);
		if(item instanceof PowerGloveItem) {
			System.out.println("WARNING: tried to add power glove to inventory. stack trace:");
			Thread.dumpStack();
			return; // do NOT add to inventory
		}
		
		if (item instanceof StackableItem) { // if the item is a item...
			StackableItem toTake = (StackableItem) item; // ...convert it into a StackableItem object.
			
			boolean added = false;
			for(int i = 0; i < items.size(); i++) {
				if(items.get(i).equals(toTake)) {
					// matching implies that the other item is stackable, too.
					((StackableItem)items.get(i)).count += toTake.count;
					added = true;
					break;
				}
			}
			
			if(!added) items.add(slot, toTake);
		} else {
			items.add(slot, item); // add the item to the items list
		}
	}
	
	/** Removes items from your inventory; looks for stacks, and removes from each until reached count. returns amount removed. */
	private int removeFromStack(StackableItem given, int count) {
		int removed = 0; // to keep track of amount removed.
		for(int i = 0; i < items.size(); i++) {
			if(!(items.get(i) instanceof StackableItem)) continue;
			StackableItem curItem = (StackableItem) items.get(i);
			if(!curItem.name.equals(given.name)) continue; // can't do equals, becuase that includes the stack size.
			// equals; and current item is stackable.
			int amountRemoving = Math.min(count-removed, curItem.count); // this is the number of items that are being removed from the stack this run-through.
			curItem.count -= amountRemoving;
			if(curItem.count == 0) { // remove the item from the inventory if its stack is empty.
				remove(i);
				i--;
			}
			removed += amountRemoving;
			if(removed == count) break;
			if(removed > count) { // just in case...
				System.out.println("SCREW UP while removing items from stack: " + (removed-count) + " too many.");
				break;
			}
			// if not all have been removed, look for another stack.
		}
		
		if(removed < count) System.out.println("Inventory: could not remove all items; " + (count-removed) + " left.");
		return removed;
	}
	
	/** Removes the item from the inventory entirely, whether it's a stack, or a lone item. */
	public void removeItem(Item i) {
		Item save = i.clone();
		//if (Game.debug) System.out.println("original item: " + i);
		if(i instanceof StackableItem)
			removeItems(i.clone(), ((StackableItem)i).count);
		else
			removeItems(i.clone(), 1);
	}
	
	/** removes items from this inventory. Note, if passed a stackable item, this will only remove a max of count from the stack. */
	public void removeItems(Item given, int count) {
		if(given instanceof StackableItem)
			count -= removeFromStack((StackableItem)given, count);
		else {
			for(int i = 0; i < items.size(); i++) {
				Item curItem = items.get(i);
				if(curItem.equals(given)) {
					remove(i);
					count--;
					if(count == 0) break;
				}
			}
		}
		
		if(count > 0)
			System.out.println("WARNING: could not remove " + count + " "+given+(count>1?"s":"")+" from inventory");
	}
	
	public boolean hasItem(Item given) {
		return items.contains(given);
	}
	
	/** Returns the how many of an item you have in the inventory. */
	public int count(Item given) {
		if (given == null) return 0; // null requests get no items. :)
		
		int found = 0; // initialize counting var
		for(int i = 0; i < items.size(); i++) { // loop though items in inv
			Item curItem = items.get(i); // assign current item
			if(!curItem.equals(given)) continue; // ignore it if it doesn't match the given item
			
			if (curItem instanceof StackableItem) // if the item can be a stack...
				found += ((StackableItem)curItem).count; // add however many items are in the stack.
			else
				found++; // otherwise, just add 1 to the found count.
		}
		
		return found;
	}
	
	public List<String> getItemNames() {
		List<String> names = new ArrayList<>();
		for(int i = 0; i < items.size(); i++)
			names.add(items.get(i).name);
		
		return names;
	}
	
	public String getItemData() {
		StringBuilder itemdata = new StringBuilder();
		for(Item i: items)
			itemdata.append(i.getData()).append(":");
		
		if(itemdata.length() > 0)
			itemdata = new StringBuilder(itemdata.substring(0, itemdata.length() - 1)); //remove extra ",".
		
		return itemdata.toString();
	}
	
	public void updateInv(String items) {
		clearInv();
		
		if(items.length() == 0) return; // there are no items to add.
		
		for(String item: items.split(":")) // this still generates a 1-item array when "items" is blank... [""].
			add(Items.get(item));
	}
	
	/** These functions (possibly) add Items and/or Tools to the inventory. */
	public void tryAdd(int chance, Item item, int num, boolean allOrNothing) {
		if(!allOrNothing || random.nextInt(chance) == 0)
			for(int i = 0; i < num; i++)
				if(allOrNothing || random.nextInt(chance) == 0)
					add(item.clone());
	}
	public void tryAdd(int chance, @Nullable Item item, int num) {
		if(item == null) return;
		if(item instanceof StackableItem) {
			((StackableItem)item).count *= num;
			tryAdd(chance, item, 1, true);
		} else
			tryAdd(chance, item, num, false);
	}
	public void tryAdd(int chance, @Nullable Item item) { tryAdd(chance, item, 1); }
	public void tryAdd(int chance, ToolType type, int lvl) {
		tryAdd(chance, new ToolItem(type, lvl));
	}
	public void tryAdd(int chance, Furniture type) {
		tryAdd(chance, new FurnitureItem(type));
	}
}
