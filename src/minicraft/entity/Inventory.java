package minicraft.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import minicraft.item.FurnitureItem;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PowerGloveItem;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;

public class Inventory {
	private Random random = new Random();
	private List<Item> items = new ArrayList<Item>(); // the list of items that is in the inventory.
	
	public List<Item> getItems() {
		List<Item> newItems = new ArrayList<Item>();
		newItems.addAll(items);
		return newItems;
	}
	public void clearInv() {items.clear();}
	public int invSize() {return items.size();}
	
	public Item get(int idx) {return items.get(idx);}
	
	public Item remove(int idx) {
		return items.remove(idx);
	}
	
	public boolean remove(Item item) {
		for(Item i: items) {
			if(i.matches(item)) {
				items.remove(i);
				return true;
			}
		}
		return false;
	}
	
	/** Adds an item to the inventory */
	public void add(Item item) {
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
				if(items.get(i).matches(toTake)) {
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
	
	/*
	/// Finds a item in your inventory.
	private StackableItem findStack(StackableItem item) {
		/// this works becuase all items are simply references to the static ones in Items.get("java"); so, multiple StackableItems SHOULD just have different references to the same Item object.
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i) instanceof StackableItem) {
				StackableItem has = (StackableItem) items.get(i);
				if (has.matches(item)) return has; // returns if the loop has found a StackableItem with a matching item in your inventory
			}
		}
		// TO-DO should I check the activeItem too? Seems like a good idea, but I should check the code for anything that may take advantake of the absence of this feature, before I just put it in.
		
		return null; // else it will return null
	}
	
	/// like findItem, but for other items.
	public Item findItem(Item item) {
		if(item instanceof StackableItem)
			return findStack((StackableItem)item);
		if(item instanceof ToolItem) {
			ToolItem ti = (ToolItem)item;
			return findTool(ti.type, ti.level, false);
		}
		
		for(int i = 0; i < items.size(); i++) {
			if(items.get(i).matches(item)) { // compares items for match.
				return items.get(i);
			}
		}
		
		return null;
	}
	
	/// Same as above for tools.
	private ToolItem findTool(ToolType type, int level, boolean matchHigher) { // may match if higher quality.
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i) instanceof ToolItem) {
				ToolItem has = (ToolItem) items.get(i);
				if (has.type == type && (has.level == level || has.level > level && matchHigher)) return has; // compares ToolType and level for match.
			}
		}
		return null;
	}*/
	
	/*
	/// Returns true if the player has the item, and has equal or more than the amount given.
	public boolean hasItems(Item i, int count) {
		int itemsFound = 0;
		Item found = findItem(i);
		
		while(itemsFound < count && found != null) {
			if(found instanceof StackableItem)
		}
		
		return itemsFound < count;
		
		if(i instanceof StackableItem) {
			StackableItem i = findItem(i); // finds the matching StackableItem in your inv, if it exists.
		}
		if (i == null) return false; // if the player doesn't have the StackableItem, then return false.
		return i.count >= count; // otherwise, return whether the inventory has the given amount of the item.
	}
	
	/// true if this inv contains the given tool.
	public boolean hasTools(ToolType t, int level) {
		ToolItem ti = findTool(t, level, false);
		if (ti == null) return false;
		return true; // tool was found.
	}
	
	/// true if this inv contains the given item.
	public boolean hasItem(Item i) {
		Item ti = this.findItem(i);
		return ti != null;
	}*/
	
	/** Removes items from your inventory; looks for stacks, and removes from each until reached count. returns amount removed. */
	public int removeFromStack(StackableItem given, int count) {
		int removed = 0; // to keep track of amount removed.
		for(int i = 0; i < items.size(); i++) {
			if(!(items.get(i) instanceof StackableItem)) continue;
			StackableItem curItem = (StackableItem) items.get(i);
			if(!curItem.name.equals(given.name)) continue; // can't do matches, becuase that includes the stack size.
			// matches; and current item is stackable.
			int amountRemoving = Math.min(count-removed, curItem.count); // this is the number of items that are being removed from the stack this run-through.
			curItem.count -= amountRemoving;
			if(curItem.count == 0) { // remove the item from the inventory if its stack is empty.
				items.remove(curItem);
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
		/*
		StackableItem stack = findStack(given); // finds the matching StackableItem in your inv, if it exists.
		if (stack == null) return false; // can't remove if you don't have that item
		if (stack.count < count) return false; // can't remove if you don't have the given amount of that item
		stack.count -= count; // remove the given amount
		if (stack.count <= 0) items.remove(stack); // remove the StackableItem from the inv if there are no more of that Item.
		return true; // removal successful, at this point.
		*/
	}
	
	/**  *//*
	public boolean removeTool(ToolType t, int level) {
		ToolItem ti = findTool(t, level, false); // find the exact tool.
		if (ti == null) return false;
		items.remove(ti);
		return true;
	}*/
	
	/** Removes the item from the inventory entirely, whether it's a stack, or a lone item. */
	public void removeItem(Item i) {
		Item save = i.clone();
		//if (Game.debug) System.out.println("original item: " + i);
		if(i instanceof StackableItem)
			removeItems(i.clone(), ((StackableItem)i).count);
		else
			removeItems(i.clone(), 1);
		/// the clone is so that the item reference is not damaged, since in this case, it never really needs to be altered.
		//if (Game.debug) System.out.println("end item: " + i + "; save = " + save);
		//i = save;
	}
	/** removes items from this inventory. Note, if passed a stackable item, this will only remove a max of count from the stack. */
	public void removeItems(Item given, int count) {
		//while(count > 0) {
			if(given instanceof StackableItem)
				count -= removeFromStack((StackableItem)given, count);
			else {
				for(int i = 0; i < items.size(); i++) {
					Item curItem = items.get(i);
					if(curItem.matches(given)) {
						items.remove(curItem);
						count--;
						if(count == 0) break;
					}
				}
			}
		//}
		
		if(count > 0)
			System.out.println("WARNING: could not remove " + count + " "+given+(count>1?"s":"")+" from inventory");
		
		/*
		if(given instanceof StackableItem)
			return removeFromStack(given, count);
		
		Item i = findItem(given);
		if(i == null) return false;
		while(count > 0 && i != null) {
			items.remove(i);
			count--;
			i = findItem(given);
		}
		Item item = findItem(i);
		if(item == null) return false;
		else items.remove(item); // remove the item.
		
		return true;*/
	}
	
	/** Returns the how many of an item you have in the inventory. */
	public int count(Item given) {
		if (given == null) return 0; // null requests get no items. :)
		
		int found = 0; // initialize counting var
		for(int i = 0; i < items.size(); i++) { // loop though items in inv
			Item curItem = items.get(i); // assign current item
			if(!curItem.matches(given)) continue; // ignore it if it doesn't match the given item
			
			if (curItem instanceof StackableItem) // if the item can be a stack...
				found += ((StackableItem)curItem).count; // add however many items are in the stack.
			else
				found++; // otherwise, just add 1 to the found count.
		}
		
		return found;
	}
	
	public List<String> getItemNames() {
		List<String> names = new ArrayList<String>();
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
		
		//if (Game.debug) System.out.println(Game.onlinePrefix()+"updating inventory " + this + " with itemstring: " + items);
		
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
	public void tryAdd(int chance, Item item, int num) {
		if(item instanceof StackableItem) {
			((StackableItem)item).count *= num;
			tryAdd(chance, item, 1, true);
		} else
			tryAdd(chance, item, num, false);
	}
	public void tryAdd(int chance, Item item) { tryAdd(chance, item, 1); }
	public void tryAdd(int chance, ToolType type, int lvl) {
		tryAdd(chance, new ToolItem(type, lvl));
	}
	public void tryAdd(int chance, Furniture type) {
		tryAdd(chance, new FurnitureItem(type));
	}
}
