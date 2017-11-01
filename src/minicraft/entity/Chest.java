package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.screen.ContainerMenu;

public class Chest extends Furniture {
	public Inventory inventory; // Inventory of the chest
	
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
	/** 
	 * This is what occurs when the player uses the "Menu" command near this.
	 */
	@Override
	public boolean use(Player player, int attackDir) {
		Game.setMenu(new ContainerMenu(player, this));
		return true;
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
				return true;
		}
		return false;
	}
}
