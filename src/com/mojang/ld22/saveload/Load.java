package com.mojang.ld22.saveload;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.AirWizard;
import com.mojang.ld22.entity.Anvil;
import com.mojang.ld22.entity.Bed;
import com.mojang.ld22.entity.Chest;
import com.mojang.ld22.entity.Cow;
import com.mojang.ld22.entity.Creeper;
import com.mojang.ld22.entity.DungeonChest;
import com.mojang.ld22.entity.Enchanter;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.Furnace;
import com.mojang.ld22.entity.Furniture;
import com.mojang.ld22.entity.GoldLantern;
import com.mojang.ld22.entity.Inventory;
import com.mojang.ld22.entity.IronLantern;
import com.mojang.ld22.entity.Knight;
import com.mojang.ld22.entity.Lantern;
import com.mojang.ld22.entity.Loom;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Oven;
import com.mojang.ld22.entity.Pig;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.Sheep;
import com.mojang.ld22.entity.Skeleton;
import com.mojang.ld22.entity.Slime;
import com.mojang.ld22.entity.Snake;
import com.mojang.ld22.entity.Spawner;
import com.mojang.ld22.entity.Tnt;
import com.mojang.ld22.entity.Workbench;
import com.mojang.ld22.entity.Zombie;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ListItems;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.PotionResource;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.LoadingMenu;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Load {
	
	String location = Game.gameDir;
	File folder;
	String extention;
	List data;
	List extradata;
	public boolean hasloadedbigworldalready;
	
	public Load(Game game, String worldname) {
		folder = new File(location);
		extention = ".miniplussave";
		data = new ArrayList();
		extradata = new ArrayList();
		hasloadedbigworldalready = false;
		location += "/saves/" + worldname + "/";
		loadGame("Game", game);
		loadPrefs("KeyPrefs", game);
		loadWorld("Level");
		loadPlayer("Player", game.player);
		loadInventory("Inventory", game.player.inventory);
		loadEntities("Entities", game.player);
		LoadingMenu.percentage = 0;
		ArrayList itemDatas = new ArrayList();
		
		for(int i = 0; i < ListItems.items.size(); i++) {
			if(!itemDatas.contains(ListItems.items.get(i))) {
				itemDatas.add(((Item)ListItems.items.get(i)).getName());
			}
		}
		
	}
	
	public void loadFromFile(String filename) {
		data.clear();
		extradata.clear();
		BufferedReader br = null;
		BufferedReader br2 = null;
		
		try {
			br = new BufferedReader(new FileReader(filename));
			
			String curLine;
			List curData;
			String item;
			Iterator lineIter;
			while((curLine = br.readLine()) != null) {
				curData = Arrays.asList(curLine.split(","));
				lineIter = curData.iterator();
				
				while(lineIter.hasNext()) {
					item = (String)lineIter.next();
					data.add(item);
				}
			}
			
			if(filename.contains("Level")) {
				br2 = new BufferedReader(new FileReader(filename.substring(0, filename.lastIndexOf("/") + 7) + "data" + extention));
				
				while((curLine = br2.readLine()) != null) {
					curData = Arrays.asList(curLine.split(","));
					lineIter = curData.iterator();
					
					while(lineIter.hasNext()) {
						item = (String)lineIter.next();
						extradata.add(item);
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				LoadingMenu.percentage += 13;
				if(LoadingMenu.percentage > 100) {
					LoadingMenu.percentage = 100;
				}
				
				if(br != null) {
					br.close();
				}
				
				if(br2 != null) {
					br2.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
		
	}
	
	public void loadGame(String filename, Game game) {
		loadFromFile(location + filename + extention);
		Game.astime = Integer.parseInt((String)data.get(1));
		Game.gamespeed = Float.parseFloat((String)data.get(2));
		Game.ac = Integer.parseInt((String)data.get(3));
		Game.autosave = Boolean.parseBoolean((String)data.get(4));
		OptionsMenu.isSoundAct = Boolean.parseBoolean((String)data.get(5));
		Game.tickCount = Integer.parseInt((String)data.get(0));
		if(Game.tickCount > -1 && Game.tickCount < 7200) {
			Game.changeTime(0);
		} else if(Game.tickCount > 7199 && Game.tickCount < 32000) {
			Game.changeTime(1);
		} else if(Game.tickCount > 31999 && Game.tickCount < '\u9c40') {
			Game.changeTime(2);
		} else if(Game.tickCount > '\u9c3f' && Game.tickCount < '\uf3c0') {
			Game.changeTime(3);
		}
		
	}
	
	public void loadPrefs(String filename, Game game) {
		loadFromFile(location + filename + extention);
		Iterator keys = data.iterator();
		while(keys.hasNext()) {
			String[] map = String.valueOf(keys.next()).split(";");
			game.input.setKey(map[0], map[1]);
		}
	}
	
	public void loadWorld(String filename) {
		for(int l = 0; l < Game.levels.length; l++) {
			loadFromFile(location + filename + l + extention);
			Game.levels[l].w = Integer.parseInt((String)data.get(0));
			Game.levels[l].h = Integer.parseInt((String)data.get(1));
			Game.levels[l].depth = Integer.parseInt((String)data.get(2));
			
			for(int x = 0; x < Game.levels[l].w - 1; x++) {
				for(int y = 0; y < Game.levels[l].h - 1; y++) {
					Game.levels[l].setTile(y, x, Tile.tiles[Integer.parseInt((String)data.get(x + y * Game.levels[l].w + 3))], Integer.parseInt((String)extradata.get(x + y * Game.levels[l].w)));
				}
			}
		}
		
	}
	
	public void loadPlayer(String filename, Player player) {
		loadFromFile(location + filename + extention);
		
		player.x = Integer.parseInt((String)data.get(0));
		player.y = Integer.parseInt((String)data.get(1));
		Player.spawnx = Integer.parseInt((String)data.get(2));
		Player.spawny = Integer.parseInt((String)data.get(3));
		player.health = Integer.parseInt((String)data.get(4));
		player.maxArmor = Integer.parseInt((String)data.get(5));
		Player.score = Integer.parseInt((String)data.get(6));
		
		Game.currentLevel = Integer.parseInt((String)data.get(7));
		player.game.level = Game.levels[Game.currentLevel];
		
		String modedata = (String)data.get(8);
		int mode;
		if(modedata.contains(";"))
			mode = Integer.parseInt(modedata.substring(0, modedata.indexOf(";")));
		else
			mode = Integer.parseInt(modedata);
		
		ModeMenu.updateModeBools(mode);
		
		if(mode == 4) { //score mode
			if(modedata.length() > 1)
				player.game.scoreTime = Integer.parseInt(modedata.substring(modedata.indexOf(";") + 1));
			else
				player.game.scoreTime = 300;
		}
		
		String colors = ((String)data.get(data.size() - 1)).replace("[", "").replace("]", "");
		List color = Arrays.asList(colors.split(";"));
		player.r = Integer.parseInt((String)color.get(0));
		player.g = Integer.parseInt((String)color.get(1));
		player.b = Integer.parseInt((String)color.get(2));
		
		if(data.size() > 10 && ((String)data.get(data.size() - 2)).contains("PotionEffects[")) {
			String potiondata = ((String)data.get(data.size() - 2)).replace("PotionEffects[", "").replace("]", "");
			List effects = Arrays.asList(potiondata.split(":"));
			
			for(int i = 0; i < effects.size(); i++) {
				List effect = Arrays.asList(((String)effects.get(i)).split(";"));
				PotionResource.applyPotion(player, (String)effect.get(0), Integer.parseInt((String)effect.get(1)));
			}
		}
		
	}
	
	public void loadInventory(String filename, Inventory inventory) {
		loadFromFile(location + filename + extention);
		inventory.items.clear();
		
		for(int i = 0; i < data.size(); i++) {
			String item = (String)data.get(i);
			if(item.contains(";")) {
				item = item.substring(0, item.lastIndexOf(";"));
			}
			
			if(ListItems.getItem(item) instanceof ResourceItem) {
				String name = (String)data.get(i) + ";0";
				List curData = Arrays.asList(name.split(";"));
				Item newItem = ListItems.getItem((String)curData.get(0));
				
				for(int ii = 0; ii < Integer.parseInt((String)curData.get(1)); ii++) {
					if(newItem instanceof ResourceItem) {
						ResourceItem resItem = new ResourceItem(((ResourceItem)newItem).resource);
						inventory.add(resItem);
					} else {
						inventory.items.add(newItem);
					}
				}
			} else {
				inventory.items.add(ListItems.getItem((String)data.get(i)));
			}
		}
		
	}
	
	public void loadEntities(String filename, Player player) {
		loadFromFile(location + filename + extention);
		
		for(int i = 0; i < Game.levels.length; i++) {
			Game.levels[i].entities.clear();
		}
		
		for(int i = 0; i < data.size(); i++) {
			Entity newEntity = getEntity(((String)data.get(i)).substring(0, ((String)data.get(i)).indexOf("[")), player);
			List info = Arrays.asList(((String)data.get(i)).substring(((String)data.get(i)).indexOf("[") + 1, ((String)data.get(i)).indexOf("]")).split(":"));
			if(newEntity != null) {
				newEntity.x = Integer.parseInt((String)info.get(0));
				newEntity.y = Integer.parseInt((String)info.get(1));
				int currentlevel;
				if(newEntity instanceof Mob) {
					Mob mob = (Mob)newEntity;
					mob.health = Integer.parseInt((String)info.get(2));
					mob.maxHealth = Integer.parseInt((String)info.get(3));
					mob.lvl = Integer.parseInt((String)info.get(4));
					mob.level = Game.levels[Integer.parseInt((String)info.get(5))];
					currentlevel = Integer.parseInt((String)info.get(5));
					Game.levels[currentlevel].add(mob);
				} else if(newEntity instanceof Chest || newEntity instanceof DungeonChest) {
					Furniture chest = (Furniture)newEntity;
					
					for(int idx = 2; idx < info.size(); idx++) {
						String itemData = (String)info.get(idx);
						Item item = ListItems.getItem(itemData);
						if (item instanceof ResourceItem) {
							List curData = Arrays.asList((itemData + ";0").split(";"));
							Item newItem = ListItems.getItem((String)curData.get(0));
							
							for(int ii = 0; ii < Integer.parseInt((String)curData.get(1)); ii++) {
								if(newItem instanceof ResourceItem) {
									ResourceItem resItem = new ResourceItem(((ResourceItem)newItem).resource);
									addToChest(chest, resItem);
								} else if(!item.getName().equals("")) {
									addToChest(chest, item);
								}
							}
						} else if(!item.getName().equals("")) {
							addToChest(chest, item);
						}
						
						if(idx == info.size() - 2) {
							if (chest instanceof Chest && itemData.contains("tl;")) {
								((Chest)chest).time = Integer.parseInt(itemData.replace("tl;", ""));
							} if (chest instanceof DungeonChest && (itemData.contains("true") || itemData.contains("false"))) {
								((DungeonChest)chest).islocked = Boolean.parseBoolean(itemData);
							}
						}
					}
					
					if (chest instanceof DungeonChest) {
						DungeonChest dChest = (DungeonChest)chest;
						ArrayList<String> contents = new ArrayList<String>();
						for(int ii = 0; ii < dChest.inventory.items.size(); ii++) {
							if(!((Item)dChest.inventory.items.get(ii)).getName().equals(" ") && !((Item)dChest.inventory.items.get(ii)).getName().equals("") && !((Item)dChest.inventory.items.get(ii)).getName().equals("	")) {
								contents.add(((Item)dChest.inventory.items.get(ii)).getName());
							} else {
								dChest.inventory.items.remove(ii);
							}
						}
						contents.add("/x=" + dChest.x / 16 + "/y=" + dChest.y / 16);
					}
					
					newEntity.level = Game.levels[Integer.parseInt((String)info.get(info.size() - 1))];
					currentlevel = Integer.parseInt((String)info.get(info.size() - 1));
					Game.levels[currentlevel].add((chest instanceof Chest ? (Chest)chest : (DungeonChest)chest));
				}
				else if(newEntity instanceof Spawner) {
					Spawner egg = (Spawner)newEntity;
					egg.x = Integer.parseInt((String)info.get(0));
					egg.y = Integer.parseInt((String)info.get(1));
					egg.lvl = Integer.parseInt((String)info.get(3));
					egg.setMob((String)info.get(2));
					currentlevel = Integer.parseInt((String)info.get(info.size() - 1));
					Game.levels[currentlevel].add(egg);
				} else {
					newEntity.level = Game.levels[Integer.parseInt((String)info.get(2))];
					currentlevel = Integer.parseInt((String)info.get(2));
					Game.levels[currentlevel].add(newEntity);
				}
			} // end of entity not null conditional
		}
	}
	
	private void addToChest(Furniture box, Item toAdd) {
		if(box instanceof Chest) {
			Chest chest = (Chest)box;
			if(toAdd instanceof ResourceItem) {
				ResourceItem item = (ResourceItem)toAdd;
				chest.inventory.items.add(item);
			} else
				chest.inventory.items.add(toAdd);
		}
		else if (box instanceof DungeonChest) {
			DungeonChest dChest = (DungeonChest)box;
			if(toAdd instanceof ResourceItem) {
				ResourceItem item = (ResourceItem)toAdd;
				dChest.inventory.items.add(item);
			} else
				dChest.inventory.items.add(toAdd);
		}
	}
	
	public Entity getEntity(String string, Player player) {
		switch(string) {
			case "Zombie": return (Entity)(new Zombie(0));
			case "Slime": return (Entity)(new Slime(0));
			case "Cow": return (Entity)(new Cow(0));
			case "Sheep": return (Entity)(new Sheep(0));
			case "Pig": return (Entity)(new Pig(0));
			case "Creeper": return (Entity)(new Creeper(0));
			case "Skeleton": return (Entity)(new Skeleton(0));
			case "Workbench": return (Entity)(new Workbench());
			case "AirWizard": return (Entity)(new AirWizard(false));
			case "AirWizardII": return (Entity)(new AirWizard(true));
			case "Chest": return (Entity)(new Chest());
			case "DeathChest": return (Entity)(new Chest(true));
			case "DungeonChest": return (Entity)(new DungeonChest());
			case "Spawner": return (Entity)(new Spawner("Zombie", 1));
			case "Anvil": return (Entity)(new Anvil());
			case "Enchanter": return (Entity)(new Enchanter());
			case "Loom": return (Entity)(new Loom());
			case "Furnace": return (Entity)(new Furnace());
			case "Oven": return (Entity)(new Oven());
			case "Bed": return (Entity)(new Bed());
			case "Tnt": return (Entity)(new Tnt());
			case "Lantern": return (Entity)(new Lantern());
			case "IronLantern": return (Entity)(new IronLantern());
			case "GoldLantern": return (Entity)(new GoldLantern());
			case "Player": return (Entity)(player);
			case "Knight": return (Entity)(new Knight(0));
			case "Snake": return (Entity)(new Snake(0));
			default : return new Entity();
		}
	}
}
