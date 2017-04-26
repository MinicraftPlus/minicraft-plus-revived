package minicraft.crafting;

import minicraft.entity.Furniture;
import minicraft.entity.Player;
import minicraft.item.BucketItem;
import minicraft.item.Item;

public class ItemRecipe extends Recipe {
	private Class<? extends Furniture> clazz;

	public ItemRecipe(Class<? extends Item> clazz)
			throws InstantiationException, IllegalAccessException {
		super(new BucketItem()); //bucket? is this only for buckets?
	}
	
	// craft that bucket, or whatever.
	public void craft(Player player) {
		try {
			player.inventory.add(0, new BucketItem());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
