package com.mojang.ld22.crafting;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;

public class ResourceRecipe extends Recipe {
	private Resource resource; //The resource used in this recipe
	static int number = 1; // the number of items you get per craft.
	
	/** Adds a recipe to craft a resource */
	public ResourceRecipe(Resource resource) {
		super(new ResourceItem(resource, number)); //this goes through Recipe.java to be put on a list.
		this.resource = resource; //resource to be added
	}
	
	/** find the number of items given per craft. */
	public static int more(Resource resource) {
		if (resource.name == "string" || resource.name == "Torch" || resource.name == "Plank" || resource.name == "St.Brick") {
			number = 2;
		}
		else number = 1;
		return number;
	}
	
	/** Adds the resource to your inventory. */
	public void craft(Player player) {
		more(resource);
		player.inventory.add(0, new ResourceItem(resource, number));
	}
}
