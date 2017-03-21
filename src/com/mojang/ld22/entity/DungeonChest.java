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

	public Inventory inventory = new Inventory();
	public Random random = new Random();
	public boolean islocked = true;
	private static int openCol = Color.get(-1, 2, 115, 225);
	
	public DungeonChest() {
		super("Dungeon Chest");
		getInventory(inventory);
		
		sprite = 1;
		col = Color.get(-1, 222, 333, 555);
		
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
		}
	}
	
	public boolean use(Player player, int attackDir) {
		if(!islocked) {
			player.game.setMenu(new ContainerMenu(player, "Dungeon Chest", inventory));
			return true;
		} else {
			int aw;
			AirWizard wizard;
			
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
				player.game.setMenu(new ContainerMenu(player, "Dungeon Chest", inventory));
				col = col0 = col1 = col2 = col3 = openCol;
				
				level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
				level.add(new TextParticle("-1 key", x, y, Color.get(-1, 500, 500, 500)));
				level.chestcount--;
				if(level.chestcount == 0) {
					for(aw = 0; aw < 5; aw++) {
						level.add(new ItemEntity(new ResourceItem(Resource.goldapple), x, y));
					}
					
					Game.notifications.add("You hear a noise from the surface!");
					wizard = new AirWizard(true);
					wizard.x = Game.levels[3].w / 2;
					wizard.y = Game.levels[3].h / 2;
					Game.levels[3].add(wizard);
				}
			}
			
			return true;
			
			/*
			if(player.activeItem != null) {
				if(player.activeItem.getName().equals("Key")) {
					if(!ModeMenu.creative) {
						ResourceItem key = (ResourceItem)player.activeItem;
						key.count--;
					}
					
					islocked = false;
					player.game.setMenu(new ContainerMenu(player, "Dungeon Chest", inventory));
					col = col0 = col1 = col2 = col3 = openCol;
					
					level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
					level.add(new TextParticle("-1 key", x, y, Color.get(-1, 500, 500, 500)));
					level.chestcount--;
					if(level.chestcount == 0) {
						for(aw = 0; aw < 5; aw++) {
							level.add(new ItemEntity(new ResourceItem(Resource.goldapple), x, y));
						}

						Game.notifications.add("You hear a noise from the surface!");
						wizard = new AirWizard(true);
						wizard.x = Game.levels[3].w / 2;
						wizard.y = Game.levels[3].h / 2;
						Game.levels[3].add(wizard);
					}
				}
				return true;
			} else {
				if(player.inventory.hasResources(Resource.key, 1)) {
					islocked = false;
					player.game.setMenu(new ContainerMenu(player, "Dungeon Chest", inventory));
					col = col0 = col1 = col2 = col3 = openCol;
					
					level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
					level.add(new TextParticle("-1 key", x, y, Color.get(-1, 500, 500, 500)));
					level.chestcount--;
					if(level.chestcount == 0) {
						for(aw = 0; aw < 5; aw++) {
							level.add(new ItemEntity(new ResourceItem(Resource.goldapple), x, y));
						}

						Game.notifications.add("You hear a noise from the surface!");
						wizard = new AirWizard(true);
						wizard.x = Game.levels[3].w / 2;
						wizard.y = Game.levels[3].h / 2;
						Game.levels[3].add(wizard);
					}
					
					if(!ModeMenu.creative) {
						player.inventory.removeResource(Resource.key, 1);
					}
				}

				return true;
			}*/
		}
	}

	public void getInventory(Inventory itemlist) {
		if(random.nextInt(8) == 1) {
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
		}

		if(inventory.items.size() < 1) {
			inventory.items.add(new ResourceItem(Resource.steak, 6));
			inventory.items.add(new ResourceItem(Resource.timepotion, 1));
			inventory.items.add(new ToolItem(ToolType.hatchet, 4));
		}

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
