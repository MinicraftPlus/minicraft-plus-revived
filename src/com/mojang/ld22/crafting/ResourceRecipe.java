package com.mojang.ld22.crafting;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;

public class ResourceRecipe extends Recipe {
	
	/** Adds a recipe to craft a resource */
	public ResourceRecipe(Resource resource, int number) {
		super(new ResourceItem(resource, number)); //this goes through Recipe.java to be put on a list.
		this.resource = resource; //resource to be added
	}
	public ResourceRecipe(Resource resource) {
		this(resource, 1);
	}
	
	/** Adds the resource to your inventory. */
	public void craft(Player player) {
		player.inventory.add(0, new ResourceItem(resource, ((ResourceItem)resultTemplate).count));
	}
}
