package com.mojang.ld22.entity;

import com.mojang.ld22.item.BucketLavaItem;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import java.util.ArrayList;
import java.util.List;

public class Inventory {
	public List<Item> items = new ArrayList<Item>(); // the list of items that is in the inventory.
	public static List<Item> itemss = new ArrayList<Item>(); // a static list, of the last inventory that was added to with add(Item).
	public boolean playerinventory = false; // if this is a player inventory.
	
	public Inventory() {}
		
	public Inventory(Player player) {
		this.playerinventory = true;
	}
	
	/** Adds an item to the inventory */
	public void add(Item item) {
		add(items.size(), item);  // adds the item to the end of the inventory list
		itemss = items; // sets static inv. to this one.
	}
	
	/** Adds an item to a specific spot in the inventory */
	public void add(int slot, Item item) {
		if (item instanceof ResourceItem) { // if the item is a resource...
			ResourceItem toTake = (ResourceItem) item; // ...convert it into a ResourceItem object.
			ResourceItem has = findResource(toTake.resource); // finds if the resourceItem is already in their inventory (looking for an instance of ResourceItem with the same .resource)
			if (has == null) { // if the owner of this inventory doesn't have the resource
				items.add(slot, toTake); // add the resource in the items list
			} else {
				has.count += toTake.count; // else add to the count of the item resource already there.
			}
		} else {
			items.add(slot, item); // add the item to the items list
		}
	}
	
	/** Finds a resource in your inventory */
	private ResourceItem findResource(Resource resource) {
		/// this works becuase all resources are simply references to the static ones in Resource.java; so, multiple ResourceItems SHOULD just have different references to the same Resource object.
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i) instanceof ResourceItem) {
				ResourceItem has = (ResourceItem) items.get(i);
				if (has.resource == resource) return has; // returns if the loop has found a ResourceItem with a matching resource in your inventory
			}
		}
		return null; // else it will return null
	}
	
	/** this is findResourceStatic, not multiple. It's like the one above. */
	private static ResourceItem findResources(Resource resource) {
		for (int i = 0; i < itemss.size(); i++) {
			if (itemss.get(i) instanceof ResourceItem) {
				ResourceItem has = (ResourceItem) itemss.get(i);
				if (has.resource == resource) return has;
			}
		}
		return null;
	}
	
	/** like findResource, but for other items. */
	private Item findItem(Item item) {
		for(int i = 0; i < this.items.size(); i++) {
			Item has = (Item)this.items.get(i);
			if(has.getName().equals(item.getName())) { // compares item name for match.
				return has;
			}
		}
		
		return null;
	}
	
	/** Same as above for tools. */
	private ToolItem findtool(ToolType type, int level, boolean matchHigher) { // may match if higher quality.
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i) instanceof ToolItem) {
				ToolItem hass = (ToolItem) items.get(i);
				if (hass.type == type && (hass.level == level || hass.level > level && matchHigher) return hass; // compares ToolType and level for match.
			}
		}
		return null;
	}
	
	/** Returns true if the player has the resource, and has equal or more than the amount given */
	public boolean hasResources(Resource r, int count) {
		ResourceItem ri = findResource(r); // finds the matching ResourceItem in your inv, if it exists.
		if (ri == null) return false; // if the player doesn't have the ResourceItem, then return false.
		return ri.count >= count; // otherwise, return whether the inventory has the given amount of the resource.
	}
	
	/** true if this inv contains the given tool. */
	public boolean hasTools(ToolType t, int level) {
		ToolItem ti = findtool(t, level, false);
		if (ti == null) return false;
		return true; // tool was found.
	}
	
	/** true if this inv contains the given item. */
	public boolean hasItem(Item i) {
		Item ti = this.findItem(i);
		return ti != null;
	}
	
	/** Removes resources from your inventory */
	public boolean removeResource(Resource r, int count) {
		ResourceItem ri = findResource(r); // finds the matching ResourceItem in your inv, if it exists.
		if (ri == null) return false; // can't remove if you don't have that resource
		if (ri.count < count) return false; // can't remove if you don't have the given amount of that resource
		ri.count -= count; // remove the given amount
		if (ri.count <= 0) items.remove(ri); // remove the ResourceItem from the inv if there are no more of that Resource.
		return true; // removal successful, at this point.
	}
	
	/**  */
	public boolean removeTool(ToolType t, int level) {
		ToolItem ti = findtool(t, level, false); // find the exact tool.
		if (ti == null) return false;
		//if (ti.level < level) return false; //
		//ti.level -= level;
		//if (ti.level <= 0) items.remove(ti); // remove if the item was entirely used up.
		items.remove(ti);
		return true;
	}
	
	/** removes item from this inv. */
	public boolean removeItem(Item i) { // This was expected to only be a lava bucket, it seems.
		ResourceItem ri = findItem(i);
		if(ri == null) return false;
		else items.remove(ri); // remove the item.
		
		return true;
	}
	
	/** that gets the score for items...? Really, all it's doing is counting the given resource... */
	public static int scored(Resource r) {
		int lscore = 0;
		ResourceItem ri = findResources(r);
		if (ri == null) {
			lscore = 0;
		} else if (ri != null) {
			lscore = ri.count;
		}
		return lscore;
	}
	
	/** Returns the how many of an item you have in the inventory */
	public int count(Item item) {
		if (item instanceof ResourceItem) { // if the item is a resource...
			ResourceItem ri = findResource(((ResourceItem) item).resource); // find the ResourceItem in your inv
			if (ri != null) return ri.count; // if the ResourceItem was found, return the stored amount.
		} else { // if the item is NOT a resource...
			int count = 0;
			for (int i = 0; i < items.size(); i++) { // loop through inv
				if (items.get(i).matches(item)) count++; // increment count if the current item matches the one given
			}
			return count; // return count
		}
		return 0; // should never reach here.
	}
}
