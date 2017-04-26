package minicraft.crafting;

import minicraft.entity.Furniture;
import minicraft.entity.Player;
import minicraft.item.FurnitureItem;

public class FurnitureRecipe extends Recipe {
	private Class<? extends Furniture> clazz; // class of the furniture

	public FurnitureRecipe(Class<? extends Furniture> clazz)
			throws InstantiationException, IllegalAccessException {
		super(new FurnitureItem(clazz.newInstance())); // assigns the furniture by class
		this.clazz = clazz; // assigns the class
	}

	public void craft(Player player) {
		try {
			// "crafts" the furniture item into the player's inventory:
			player.inventory.add(0, new FurnitureItem(clazz.newInstance()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
