package minicraft.item;

import java.util.ArrayList;
import minicraft.entity.ItemEntity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.ModeMenu;

// some items are direct instances of this class; those instances are the true "items", like stone, wood, wheat, or coal; you can't do anything with them besides use them to make something else.

public class StackableItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
	
		items.add(new StackableItem("Wood", new Sprite(1, 4, Color.get(-1, 200, 531, 430))));
		items.add(new StackableItem("Stone", new Sprite(2, 4, Color.get(-1, 111, 333, 555))));
		items.add(new StackableItem("Leather", new Sprite(19, 4, Color.get(-1, 100, 211, 322))));
		items.add(new StackableItem("Wheat", new Sprite(6, 4, Color.get(-1, 110, 330, 550))));
		items.add(new StackableItem("Key", new Sprite(26, 4, Color.get(-1, -1, 444, 550))));
		items.add(new StackableItem("arrow", new Sprite(13, 5, Color.get(-1, 111, 222, 430))));
		items.add(new StackableItem("string", new Sprite(25, 4, Color.get(-1, 555))));
		items.add(new StackableItem("Coal", new Sprite(10, 4, Color.get(-1, 000, 111, 111))));
		items.add(new StackableItem("Iron Ore", new Sprite(10, 4, Color.get(-1, 100, 322, 544))));
		items.add(new StackableItem("Lapis", new Sprite(10, 4, Color.get(-1, 005, 115, 115))));
		items.add(new StackableItem("Gold Ore", new Sprite(10, 4, Color.get(-1, 110, 440, 553))));
		items.add(new StackableItem("Iron", new Sprite(11, 4, Color.get(-1, 100, 322, 544))));
		items.add(new StackableItem("Gold", new Sprite(11, 4, Color.get(-1, 110, 330, 553))));
		items.add(new StackableItem("Rose", new Sprite(0, 4, Color.get(-1, 100, 300, 500))));
		items.add(new StackableItem("GunPowder", new Sprite(2, 4, Color.get(-1, 111, 222, 333))));
		items.add(new StackableItem("Slime", new Sprite(10, 4, Color.get(-1, 10, 30, 50))));
		items.add(new StackableItem("glass", new Sprite(12, 4, Color.get(-1, 555))));
		items.add(new StackableItem("cloth", new Sprite(1, 4, Color.get(-1, 25, 252, 141))));
		items.add(new StackableItem("gem", new Sprite(13, 4, Color.get(-1, 101, 404, 545))));
		items.add(new StackableItem("Scale", new Sprite(22, 4, Color.get(-1, 10, 30, 20))));
		items.add(new StackableItem("Shard", new Sprite(23, 4, Color.get(-1, 222, 333, 444))));
		
		return items;
	}
	
	public int count;
	///public int maxCount; // TODO I want to implement this later.
	
	//public int level = 0;
	//public int amount = 1; // The amount of items
	
	public StackableItem(String name, Sprite sprite) {
		super(name, sprite);
		count = 1;
	}
	public StackableItem(String name, Sprite sprite, int count) {
		this(name, sprite);
		this.count = count;
	}
	/*
	public StackableItem addamount(int amount) {
		this.amount = amount;
		return this;
	}*/
	
	public boolean matches(Item other) {
		return super.matches(other) && other instanceof StackableItem;
	}
	
	/** Renders the icon, name, and count of the item. */
	public void renderInventory(Screen screen, int x, int y) { renderInventory(screen, x, y, true); }
	public void renderInventory(Screen screen, int x, int y, boolean ininv) {
		sprite.render(screen, x, y);//screen.render(x, y, item.sprite, item.color, 0); // renders the icon
		//String name = item.name; // draws the name of the item
		if(name.length() > 11 && !ininv) { // only draw part of the name if it's too long to the black bar. (not in the inventory)
			Font.draw(name.substring(0, 11), screen, x + 32, y, Color.get(-1, 555));
		} else {
			Font.draw(name, screen, x + 32, y, Color.get(-1, 555));
		}
		
		int cc = count; // count of the item
		if(cc > 999) { // If the item count is above 999, then just render 999 (for spacing reasons)
			cc = 999;
		}

		Font.draw(""+cc, screen, x + 8, y, Color.get(-1, 444)); // draws the item count
	}
	
	/*public String getName() {
		return name;
	}*/
	
	/** What happens when you try to use this item on a tile. */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		/*if (interactOn(tile, level, xt, yt, player, attackDir)) { // Calls the item's 'interactOn()' method as a check
			if (!ModeMenu.creative)
				count--; // interaction was successful, meaning the item was used; so remove it.
			return true;
		}*/
		return false;
	}
	
	/** Called to determine if this item should be removed from an inventory. */
	public boolean isDepleted() {
		return count <= 0;
	}
	
	public StackableItem clone() {
		return new StackableItem(name, sprite, count);
	}
}
