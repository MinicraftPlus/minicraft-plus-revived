package com.mojang.ld22.crafting;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;

public class ArrowRecipe extends Recipe {

	private Resource resource;
	static int number = 1;


	public ArrowRecipe(Resource resource, int amount) {
		super((new ResourceItem(resource, number)).addamount(amount));
		this.resource = resource;
		number = amount;
	}

	public void craft(Player player) {
		Game.ac += number;
	}
}
