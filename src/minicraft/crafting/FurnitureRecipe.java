package minicraft.crafting;

import minicraft.entity.Furniture;
import minicraft.entity.Player;
import minicraft.item.FurnitureItem;

public class FurnitureRecipe extends Recipe {
	//private Class<? extends Furniture> clazz; // class of the furniture
	private Furniture furniture;
	
	public FurnitureRecipe(Furniture f) {
		super(new FurnitureItem(f.copy())); // assigns the furniture by class
		this.furniture = f; // assigns the class
	}
	
	public void craft(Player player) {
		// "crafts" the furniture item into the player's inventory:
		player.inventory.add(0, new FurnitureItem(furniture.copy()));
	}
}
