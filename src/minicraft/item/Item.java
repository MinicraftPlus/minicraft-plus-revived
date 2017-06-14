package minicraft.item;

import minicraft.entity.Entity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.ListItem;

public abstract class Item implements ListItem {
	
	/* Note: Most of the stuff in the class is expanded upon in StackableItem/PowerGloveItem/FurnitureItem/etc */
	
	public String name;
	public Sprite sprite;
	
	protected Item(String name) {
		sprite = Sprite.missingTexture(1, 1);
		this.name = name;
	}
	protected Item(String name, Sprite sprite) {
		this.name = name;
		this.sprite = sprite;
	}
	
	/// TODO this method (and Menu.renderItemList) is actually slowly getting depricated; I just haven't gotten around to updating all the menus yet.
	/** Renders an item (sprite & name) in an inventory */
	public void renderInventory(Screen screen, int x, int y) { renderInventory(screen, x, y, true); }
	public void renderInventory(Screen screen, int x, int y, boolean ininv) {
		renderInventory(screen, x, y, ininv, name);}
	protected void renderInventory(Screen screen, int x, int y, boolean ininv, String name) {
		sprite.render(screen, x, y);
		if(ininv) {
			String shortname = name.length() > 20 ? name.substring(0, 20) : name;
			Font.draw(shortname, screen, x + 8, y, Color.get(-1, 555));
		}
		else
			Font.draw(name, screen, x + 8, y, Color.get(0, 555));
	}
	
	/** Determines what happens when the player interacts with an entity */
	// TODO I want to move this to the individual entity classes.
	public boolean interact(Player player, Entity entity, int attackDir) {
		return false;
	}
	
	/** Determines what happens when the player interacts with a tile */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		return false;
	}
	
	/** Returning true causes this item to be removed from the player's active item slot */
	public boolean isDepleted() {
		return false;
	}
	
	/** Returns if the item can attack mobs or not */
	public boolean canAttack() {
		return false;
	}
	
	/** Sees if an item matches another item */
	public boolean matches(Item item) {
		return item.getClass().equals(getClass()) && item.name.equals(name);
	}
	
	/** This returns a copy of this item, in all necessary detail. */
	public abstract Item clone();
	
	public String toString() {
		return name + "-Item";
	}
	
	public String getData() {
		return name;
	}
}
