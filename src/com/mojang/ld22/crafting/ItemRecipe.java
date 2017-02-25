package com.mojang.ld22.crafting;

import com.mojang.ld22.entity.Furniture;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.item.BucketItem;
import com.mojang.ld22.item.FurnitureItem;
import com.mojang.ld22.item.Item;

public class ItemRecipe extends Recipe {
	private Class<? extends Furniture> clazz;

	public ItemRecipe(Class<? extends Item > clazz) throws InstantiationException, IllegalAccessException {
		super(new BucketItem());
	}


	public void craft(Player player) {
		try {
			player.inventory.add(0, new BucketItem());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
