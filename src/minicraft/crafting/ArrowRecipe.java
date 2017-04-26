package minicraft.crafting;

import minicraft.entity.Player;
import minicraft.item.ResourceItem;
import minicraft.item.resource.Resource;

public class ArrowRecipe extends Recipe {
	private Resource resource;
	static int number = 1;
	
	public ArrowRecipe(Resource resource, int amount) {
		super((new ResourceItem(resource, number)).addamount(amount));
		this.resource = resource;
		number = amount;
	}
	
	public void craft(Player player) {
		player.ac += number;
	}
}
