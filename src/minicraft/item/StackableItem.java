package minicraft.item;

import java.util.ArrayList;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;

// some items are direct instances of this class; those instances are the true "items", like stone, wood, wheat, or coal; you can't do anything with them besides use them to make something else.

public class StackableItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
	
		items.add(new StackableItem("Wood", new Sprite(28, 4, Color.get(-1, 310, 532, 532))));
		items.add(new StackableItem("Stone", new Sprite(2, 4, Color.get(-1, 111, 333, 555))));
		items.add(new StackableItem("Leather", new Sprite(19, 4, Color.get(-1, 100, 211, 322))));
		items.add(new StackableItem("Wheat", new Sprite(6, 4, Color.get(-1, 110, 330, 550))));
		items.add(new StackableItem("Key", new Sprite(26, 4, Color.get(-1, -1, 444, 550))));
		items.add(new StackableItem("arrow", new Sprite(13, 5, Color.get(-1, 111, 222, 430))));
		items.add(new StackableItem("string", new Sprite(25, 4, Color.WHITE)));
		items.add(new StackableItem("Coal", new Sprite(10, 4, Color.get(-1, 000, 111, 111))));
		items.add(new StackableItem("Iron Ore", new Sprite(10, 4, Color.get(-1, 100, 322, 544))));
		items.add(new StackableItem("Lapis", new Sprite(10, 4, Color.get(-1, 005, 115, 115))));
		items.add(new StackableItem("Gold Ore", new Sprite(10, 4, Color.get(-1, 110, 440, 553))));
		items.add(new StackableItem("Iron", new Sprite(11, 4, Color.get(-1, 100, 322, 544))));
		items.add(new StackableItem("Gold", new Sprite(11, 4, Color.get(-1, 110, 330, 553))));
		items.add(new StackableItem("Rose", new Sprite(0, 4, Color.get(-1, 100, 300, 500))));
		items.add(new StackableItem("GunPowder", new Sprite(2, 4, Color.get(-1, 111, 222, 333))));
		items.add(new StackableItem("Slime", new Sprite(10, 4, Color.get(-1, 10, 30, 50))));
		items.add(new StackableItem("glass", new Sprite(12, 4, Color.WHITE)));
		items.add(new StackableItem("cloth", new Sprite(1, 4, Color.get(-1, 25, 252, 141))));
		items.add(new StackableItem("gem", new Sprite(13, 4, Color.get(-1, 101, 404, 545))));
		items.add(new StackableItem("Scale", new Sprite(22, 4, Color.get(-1, 10, 30, 20))));
		items.add(new StackableItem("Shard", new Sprite(23, 4, Color.get(-1, 222, 333, 444))));
		
		return items;
	}
	
	public int count;
	///public int maxCount; // TODO I want to implement this later.
	
	protected StackableItem(String name, Sprite sprite) {
		super(name, sprite);
		count = 1;
	}
	protected StackableItem(String name, Sprite sprite, int count) {
		this(name, sprite);
		this.count = count;
	}
	
	public boolean matches(Item other) {
		return super.matches(other) && other instanceof StackableItem;
	}
	
	/** Renders the icon, name, and count of the item. */
	public void renderInventory(Screen screen, int x, int y, boolean ininv) {
		// If the item count is above 999, then just render 999 (for spacing reasons)
		super.renderInventory(screen, x, y, ininv, (count>999?999:count)+" "+name);
	}
	
	/// this is used by (most) subclasses, to standardize the count decrement behavior. This is not the normal interactOn method.
	protected boolean interactOn(boolean subClassSuccess) {
		if(subClassSuccess && !Game.isMode("creative"))
			count--;
		return subClassSuccess;
	}
	
	/** Called to determine if this item should be removed from an inventory. */
	public boolean isDepleted() {
		return count <= 0;
	}
	
	public StackableItem clone() {
		return new StackableItem(name, sprite, count);
	}
	
	public String toString() {
		return super.toString() + "-Stack_Size:"+count;
	}
	
	public String getData() {
		return name+"_"+count;
	}
	
	@Override
	public String getDisplayName() {
		String extra = (count > 999 ? 999 : count) + " ";
		return " " + extra + name;
	}
}
