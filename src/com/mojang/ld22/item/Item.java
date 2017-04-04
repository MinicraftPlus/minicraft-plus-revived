package com.mojang.ld22.item;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.ListItem;

public class Item implements ListItem {
	
	/* Note: Most of the stuff in the class is expanded upon in ResourceItem/PowerGloveItem/FurnitureItem/etc */
	
	/** called to add this item to the item list in ListItems. */
	public Item addItem() {
		if (!ListItems.items.contains(this)) { // if this Item isn't already part of the list...
			ListItems.items.add(this); // add it.
		}
		
		return this;
	}

	public int getColor() {
		return 0;
	}

	public int getSprite() {
		return 0;
	}
	
	/** What happens when you pick up the item off the ground */
	public void onTake(ItemEntity itemEntity) {}
	
	/** Renders an item (sprite & name) in an inventory */
	public void renderInventory(Screen screen, int x, int y) {}
	
	/** Determines what happens when the player interacts with a entity */
	public boolean interact(Player player, Entity entity, int attackDir) {
		return false;
	}
	
	/** Renders the icon of the Item */
	public void renderIcon(Screen screen, int x, int y) {}
	
	/** Determines what happens when you use a item in a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		return false;
	}
	
	/** Returns if the item is depleted or not */
	public boolean isDepleted() {
		return false;
	}
	
	/** Returns if the item can attack mobs or not */
	public boolean canAttack() {
		return false;
	}
	
	/** Gets the attack bonus from an item/tool (sword/axe) */
	public int getAttackDamageBonus(Entity e) {
		return 0;
	}
	
	/** Gets the name of the tool */
	public String getName() {
		return "";
	}
	
	/** Sees if an item matches another item */
	public boolean matches(Item item) {
		return item.getClass() == getClass();
	}
}
