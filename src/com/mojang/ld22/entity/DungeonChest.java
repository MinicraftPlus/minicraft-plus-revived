package com.mojang.ld22.entity;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.item.FurnitureItem;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ContainerMenu;

public class DungeonChest extends Furniture {
	public Inventory inventory = new Inventory();
	
	public DungeonChest() {
		super("Dungeon Chest");
		inventory.add(new ToolItem(ToolType.sword, 2));
		inventory.add(new ToolItem(ToolType.bow, 2));
		inventory.add(new ToolItem(ToolType.pickaxe, 4));
		inventory.add(new ResourceItem( Resource.larmor));
		inventory.add(new ResourceItem( Resource.larmor));
		inventory.add(new ResourceItem( Resource.larmor));
		inventory.add(new ToolItem(ToolType.hoe, 1));
		inventory.add(new FurnitureItem(new Oven()));
		inventory.add(new FurnitureItem(new Anvil()));
		
		int a = 0;
		int b = 0;
		int c = 0;
		
		while (a < 3){
			inventory.add(new ResourceItem( Resource.grassseeds));
			inventory.add(new ResourceItem( Resource.acorn));
			inventory.add(new ResourceItem( Resource.seeds));
			a++;
		}
		while (b < 6){
			inventory.add(new ResourceItem( Resource.dirt));
			b++;
		}
		while (c < 50){
			inventory.add(new ResourceItem( Resource.sbrick));
			c++;
		}
		
		if (canLight()) {
			col0 = Color.get(-1, 222, 333, 555);
			col1 = Color.get(-1, 222, 333, 555);
			col2 = Color.get(-1, 222, 333, 555);
			col3 = Color.get(-1, 222, 333, 555);
		}else{
			col0 = Color.get(-1, 111, 222, 444);
			col1 = Color.get(-1, 222, 333, 555);
			col2 = Color.get(-1, 111, 222, 444);	
		    col3 = Color.get(-1, 000, 111, 333);
		}
	    
		col = Color.get(-1, 222, 333, 555);
		sprite = 1;
		
		//  x = s.x * 41 + 0;
		//  y = s.y * 41 + 1;
		
	}
	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new ContainerMenu(player, "Dungeon Chest", inventory));
		return true;
	}
}