package com.mojang.ld22.crafting;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;

public class ResourceRecipe extends Recipe {
	private Resource resource;
	static int number = 1;
	
	
	public ResourceRecipe(Resource resource) {
		super(new ResourceItem(resource, number));
		this.resource = resource;
		
	}
	
	public static int more(Resource resource){
		if (resource.name == "string"){
			number = 2;
		}
		if (resource.name == "Torch"){
			number = 2;
		}
		if (resource.name == "Plank"){
			number = 2;
		}
		if (resource.name == "St.Brick"){
			number = 2;
		}
		if (resource.name != "string" && resource.name != "Torch" && resource.name != "Plank" && resource.name != "St.Brick"){
			number = 1;
		}
		return number;
	}
	
	public void craft(Player player) {
		more(resource);
		player.inventory.add(0, new ResourceItem(resource, number));
	}
}
	