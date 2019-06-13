package minicraft.entity.furniture;

import java.util.List;

import minicraft.core.Game;
import minicraft.entity.ItemHolder;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.screen.ContainerDisplay;

public class Chest extends Furniture implements ItemHolder {
	private Inventory inventory; // Inventory of the chest
	
	/**
	 * Constructs a furniture with the name Chest and the chest sprite and color.
	 */
	public Chest() {
		this("Chest", Color.get(-1, 220, 331, 552));
	}
	
	/**
	 * Creates a chest with a custom name and color.
	 * @param name Name of chest.
	 * @param color Color of chest.
	 */
	public Chest(String name, int color) {
		super(name, new Sprite(2, 8, 2, 2, color), 3, 3); // Name of the chest
		
		inventory = new Inventory(); // initialize the inventory.
	}
	
	/** This is what occurs when the player uses the "Menu" command near this */
	public boolean use(Player player) {
		Game.setMenu(new ContainerDisplay(player, this));
		return true;
	}
	
	@Override
	public void take(Player player) {
		if(inventory.invSize() == 0)
			super.take(player);
	}
	
	@Override
	protected String getUpdateString() {
		String updates = super.getUpdateString()+";";
		updates += "inventory,"+inventory.getItemData();
		return updates;
	}
	
	@Override
	protected boolean updateField(String fieldName, String val) {
		if(super.updateField(fieldName, val)) return true;
		switch(fieldName) {
			case "inventory":
				inventory.updateInv(val);
				if(Game.getMenu() instanceof ContainerDisplay)
					((ContainerDisplay)Game.getMenu()).onInvUpdate(this);
				return true;
		}
		return false;
	}
	
	@Override
	public Inventory getInventory() {
		return inventory;
	}
	
	@Override
	public void die() {
		if(level != null) {
			List<Item> items = inventory.getItems();
			level.dropItem(x, y, items.toArray(new Item[items.size()]));
		}
		super.die();
	}
}
