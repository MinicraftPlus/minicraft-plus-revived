package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.particle.SmashParticle;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ContainerMenu;
import com.mojang.ld22.screen.ModeMenu;
import java.util.Random;

public class DungeonChest extends Furniture {

	public Inventory inventory;
	public Random random = new Random();
	public boolean islocked;
	private static int openCol = Color.get(-1, 2, 115, 225);
	
	public DungeonChest() {
		super("Dungeon Chest");
		populateInv();
		
		islocked = true;
		sprite = 1;
		col = Color.get(-1, 222, 333, 555);
		// this is always the case here; locked is set to true above, and canLight() in Entity.java returns false.
		col0 = Color.get(-1, 111, 222, 444);
		col1 = Color.get(-1, 222, 333, 555);
		col2 = Color.get(-1, 111, 222, 444);
		col3 = Color.get(-1, 0, 111, 333);
		/*
		if(canLight()) {
			if(islocked) // can light, and locked.
				col0 = col1 = col2 = col3 = col;
			else { // can light, unlocked.
				col0 = Color.get(-1, 111, 115, 225);
				col1 = Color.get(-1, 222, 115, 225);
				col2 = Color.get(-1, 111, 115, 225);
				col3 = Color.get(-1, 2, 115, 225);
			}
		} else if(!islocked) // can't light, unlocked.
			col0 = col1 = col2 = col3 = openCol;
		else { // can't light, locked.
			col0 = Color.get(-1, 111, 222, 444);
			col1 = Color.get(-1, 222, 333, 555);
			col2 = Color.get(-1, 111, 222, 444);
			col3 = Color.get(-1, 0, 111, 333);
		}*/
	}
	
	public boolean use(Player player, int attackDir) {
		if(!islocked) {
			player.game.setMenu(new ContainerMenu(player, name, inventory));
			return true;
		} else {
			boolean activeKey = player.activeItem != null && player.activeItem.getName().equals("Key");
			boolean invKey = player.inventory.hasResources(Resource.key, 1);
			if(activeKey || invKey) {
				if (!ModeMenu.creative) {
					if (activeKey) {
						ResourceItem key = (ResourceItem)player.activeItem;
						key.count--;
					} else {
						player.inventory.removeResource(Resource.key, 1);
					}
				}
				
				islocked = false;
				player.game.setMenu(new ContainerMenu(player, name, inventory));//use(player, attackDir);
				col = col0 = col1 = col2 = col3 = openCol;
				
				level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
				level.add(new TextParticle("-1 key", x, y, Color.get(-1, 500, 500, 500)));
				level.chestcount--;
				if(level.chestcount == 0) {
					for(int i = 0; i < 5; i++) {
						level.add(new ItemEntity(new ResourceItem(Resource.goldapple), x, y));
					}
					
					Game.notifications.add("You hear a noise from the surface!");
					AirWizard wizard = new AirWizard(true);
					wizard.x = Game.levels[3].w / 2;
					wizard.y = Game.levels[3].h / 2;
					Game.levels[3].add(wizard);
				}
			}
			
			return true;
		}
	}
	
	/*static int[] rChance = {8, 6, 5, 4, 3, 3, 4, 5, 5, 5, 5, 6, 4, 4, 2, 7, 7, 14, 14, 7, 5};
	static Resource[] rContents = {Resource.gemarmor, Resource.garmor, Resource.iarmor, Resource.speedpotion, Resource.lightpotion, Resource.potion, Resource.lightpotion, Resource.steak, Resource.cookedpork, Resource.gem, Resource.gem, Resource.speedpotion, Resource.wood, Resource.wool, Resource.coal, Resource.regenpotion, Resource.energypotion, Resource.timepotion, Resource.shieldpotion, Resource.lavapotion, Resource.hastepotion};
	static int[] rNumber = {1, 1, 2, 2, 2, 10, 3, 6, 6, 7, 8, 5, 20, 12, 4, 1, 1, 1, 1, 1, 3};
	static int[] tChance = {6, 4, 4, 6, 7, 7};
	static ToolType[] tContents = {ToolType.claymore, ToolType.sword, ToolType.claymore, ToolType.bow, ToolType.bow, ToolType.sword};
	static int[] tLevel = {2, 3, 1, 3, 4, 4};
	*/
	
	private void populateInv() {
		inventory = new Inventory(this);
		tryAdd(8, Resource.gemarmor, 1);
		tryAdd(6, Resource.garmor, 1);
		tryAdd(5, Resource.iarmor, 2);
		tryAdd(4, Resource.speedpotion, 2);
		tryAdd(3, Resource.lightpotion, 2);
		tryAdd(3, Resource.potion, 10);
		tryAdd(4, Resource.lightpotion, 3);
		tryAdd(5, Resource.steak, 6);
		tryAdd(5, Resource.cookedpork, 6);
		tryAdd(5, Resource.gem, 7);
		tryAdd(5, Resource.gem, 8);
		tryAdd(6, Resource.speedpotion, 5);
		tryAdd(4, Resource.wood, 20);
		tryAdd(4, Resource.wool, 12);
		tryAdd(2, Resource.coal, 4);
		tryAdd(7, Resource.regenpotion, 1);
		tryAdd(7, Resource.energypotion, 1);
		tryAdd(14, Resource.timepotion, 1);
		tryAdd(14, Resource.shieldpotion, 1);
		tryAdd(7, Resource.lavapotion, 1);
		tryAdd(5, Resource.hastepotion, 3);
		
		tryAdd(6, ToolType.claymore, 2);
		tryAdd(4, ToolType.sword, 3);
		tryAdd(4, ToolType.claymore, 1);
		tryAdd(6, ToolType.bow, 3);
		tryAdd(7, ToolType.bow, 4);
		tryAdd(7, ToolType.sword, 4);
		/*
		for(int i = 0; i < rContents.length; i++) {
			if(random.nextInt(rChance[i]) == 1)
				inventory.add(new ResourceItem(rContents[i], rNumber[i]));
		}
		for(int i = 0; i < tContents.length; i++) {
			if(random.nextInt(tChance[i]) == 1)
				inventory.add(new ToolItem(tContents[i], tLevel[i]));
		}
		*/
		if(inventory.invSize() < 1) {
			inventory.add(new ResourceItem(Resource.steak, 6));
			inventory.add(new ResourceItem(Resource.timepotion, 1));
			inventory.add(new ToolItem(ToolType.hatchet, 4));
		}
		/*if(random.nextInt(8) == 1) {
			itemlist.add(new ResourceItem(Resource.gemarmor));
		}

		if(random.nextInt(6) == 1) {
			itemlist.add(new ResourceItem(Resource.garmor));
		}

		if(random.nextInt(5) == 1) {
			itemlist.add(new ResourceItem(Resource.iarmor, 2));
		}

		if(random.nextInt(4) == 1) {
			itemlist.add(new ResourceItem(Resource.speedpotion, 2));
		}

		if(random.nextInt(3) == 1) {
			itemlist.add(new ResourceItem(Resource.lightpotion, 2));
		}

		if(random.nextInt(3) == 1) {
			itemlist.add(new ResourceItem(Resource.potion, 10));
		}

		if(random.nextInt(4) == 1) {
			itemlist.add(new ResourceItem(Resource.lightpotion, 3));
		}

		if(random.nextInt(5) == 1) {
			itemlist.add(new ResourceItem(Resource.steak, 6));
		}

		if(random.nextInt(5) == 1) {
			itemlist.add(new ResourceItem(Resource.cookedpork, 6));
		}

		if(random.nextInt(6) == 1) {
			itemlist.add(new ToolItem(ToolType.claymore, 2));
		}

		if(random.nextInt(4) == 1) {
			itemlist.add(new ToolItem(ToolType.sword, 3));
		}

		if(random.nextInt(4) == 1) {
			itemlist.add(new ToolItem(ToolType.claymore, 1));
		}

		if(random.nextInt(6) == 1) {
			itemlist.add(new ToolItem(ToolType.bow, 3));
		}

		if(random.nextInt(7) == 1) {
			itemlist.add(new ToolItem(ToolType.bow, 4));
		}

		if(random.nextInt(7) == 1) {
			itemlist.add(new ToolItem(ToolType.sword, 4));
		}

		if(random.nextInt(5) == 1) {
			itemlist.add(new ResourceItem(Resource.gem, 7));
		}

		if(random.nextInt(5) == 1) {
			itemlist.add(new ResourceItem(Resource.gem, 8));
		}

		if(random.nextInt(6) == 1) {
			itemlist.add(new ResourceItem(Resource.speedpotion, 5));
		}

		if(random.nextInt(4) == 1) {
			itemlist.add(new ResourceItem(Resource.wood, 20));
		}

		if(random.nextInt(4) == 1) {
			itemlist.add(new ResourceItem(Resource.wool, 12));
		}

		if(random.nextInt(2) == 1) {
			itemlist.add(new ResourceItem(Resource.coal, 4));
		}

		if(random.nextInt(7) == 1) {
			itemlist.add(new ResourceItem(Resource.regenpotion, 1));
		}

		if(random.nextInt(7) == 1) {
			itemlist.add(new ResourceItem(Resource.energypotion, 1));
		}

		if(random.nextInt(14) == 1) {
			itemlist.add(new ResourceItem(Resource.timepotion, 1));
		}

		if(random.nextInt(14) == 1) {
			itemlist.add(new ResourceItem(Resource.shieldpotion, 1));
		}

		if(random.nextInt(7) == 1) {
			itemlist.add(new ResourceItem(Resource.lavapotion, 1));
		}

		if(random.nextInt(5) == 1) {
			itemlist.add(new ResourceItem(Resource.hastepotion, 3));
		}*/
	}
	
	private void tryAdd(int chance, Resource item, int num) {
		if(random.nextInt(chance) == 1)
			inventory.add(new ResourceItem(item, num));
	}
	private void tryAdd(int chance, ToolType type, int lvl) {
		if(random.nextInt(chance) == 1)
			inventory.add(new ToolItem(type, lvl));
	}

	protected void touchedBy(Entity entity) {
		if(entity instanceof Player && pushTime == 0 && !islocked) {
			pushDir = ((Player)entity).dir;
			pushTime = 10;
		}

	}

	public void take(Player player) {
		if(!islocked) {
			shouldTake = player;
		}

	}
}
