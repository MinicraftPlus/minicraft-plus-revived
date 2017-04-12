package com.mojang.ld22.saveload;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.AirWizard;
import com.mojang.ld22.entity.Anvil;
import com.mojang.ld22.entity.Bed;
import com.mojang.ld22.entity.Chest;
import com.mojang.ld22.entity.Cow;
import com.mojang.ld22.entity.Creeper;
import com.mojang.ld22.entity.DeathChest;
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
import com.mojang.ld22.item.resource.ArmorResource;
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
	Version currentVer, worldVer;
	boolean oldSave = false;
	
	public Load(Game game, String worldname) {
		currentVer = new Version(Game.VERSION);
		folder = new File(location);
		extention = ".miniplussave";
		data = new ArrayList();
		extradata = new ArrayList();
		hasloadedbigworldalready = false;
		location += "/saves/" + worldname + "/";
		
		worldVer = null;
		File testFile = new File(location + "KeyPrefs" + extention);
		if(!testFile.exists()) {
			worldVer = new Version("1.8");
			oldSave = true;
		}
		
		/// TESTING
		//System.out.println( currentVer + " > 1.8: " + (currentVer.compareTo(new Version("1.8")) > 0) );
		//System.out.println( currentVer + " == 1.9.1: " + (currentVer.compareTo(new Version("1.9.1")) == 0) );
		//System.out.println(" 1.9.1-pre1 < 1.9.1: " + ((new Version("1.9.1-pre1")).compareTo(new Version("1.9.1")) < 0) );
		// end testing
		
		loadGame("Game", game); // more of the version will be determined here
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
	
	class Version implements Comparable {
		public Integer make, major, minor, dev;
		
		public Version(String version) {
			String[] nums = version.split("\\.");
			try {
				if(nums.length > 0) make = Integer.parseInt(nums[0]);
				else make = 0;
				
				if(nums.length > 1) major = Integer.parseInt(nums[1]);
				else major = 0;
				
				String min;
				if(nums.length > 2) min = nums[2];
				else min = "";
				
				if(min.contains("-")) {
					String[] mindev = min.split("-");
					minor = Integer.parseInt(mindev[0]);
					dev = Integer.parseInt(mindev[1].replace("pre", "").replace("dev", ""));
				} else {
					if(!min.equals("")) minor = Integer.parseInt(min);
					else minor = 0;
					dev = 0;
				}
			} catch(NumberFormatException ex) {
				System.out.println("INVALID version number: " + version);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		// the returned value of this method (-1, 0, or 1) is determined by whether this object is less than, equal to, or greater than the specified object.
		public int compareTo(Object other) throws NullPointerException, ClassCastException {
			if(other == null) throw new NullPointerException();
			if(other instanceof Version == false) { // if the passed object is not a Version...
				throw new ClassCastException("Cannot compare type Version with type " + other.getClass().getTypeName());
			}
			Version ov = (Version)other;
			
			if(make != ov.make) return make.compareTo(ov.make);
			else if(major != ov.major) return major.compareTo(ov.major);
			else if(minor != ov.minor) return minor.compareTo(ov.minor);
			else if(dev != ov.dev) {
				if(dev == 0) return 1; //0 is the last "dev" version, as it is not a dev.
				else if(ov.dev == 0) return -1;
				else return dev.compareTo(ov.dev);
			}
			else return 0; // the versions are equal.
		}
		
		public String toString() {
			return make + "." + major + "." + minor + (dev == 0 ? "" : "-dev" + dev);
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
		boolean hasVersion = ((String)data.get(0)).contains(".");
		if(hasVersion) {
			worldVer = new Version((String)data.get(0)); // gets the world version
			Game.setTime(Integer.parseInt((String)data.get(1)));
			Game.astime = Integer.parseInt((String)data.get(2));
			Game.autosave = Boolean.parseBoolean((String)data.get(3));
			OptionsMenu.isSoundAct = Boolean.parseBoolean((String)data.get(4));
		}
		else {
			if(data.size() == 5) {
				worldVer = new Version("1.9");
				Game.setTime(Integer.parseInt((String)data.get(0)));
				Game.astime = Integer.parseInt((String)data.get(1));
				//Game.gamespeed = Float.parseFloat((String)data.get(2));
				Game.autosave = Boolean.parseBoolean((String)data.get(3));
				OptionsMenu.isSoundAct = Boolean.parseBoolean((String)data.get(4));
			} else { // version == 1.8?
				if(!oldSave) {
					System.out.println("UNEXPECTED WORLD VERSION");
					worldVer = new Version("1.8.1");
				}
				// for backwards compatibility
				Game.astime = Integer.parseInt((String)this.data.get(1));
				//Game.gamespeed = Integer.parseInt((String)this.data.get(2));
				game.player.ac = Integer.parseInt((String)this.data.get(3));
				Game.tickCount = Integer.parseInt((String)this.data.get(0));
				Game.autosave = false;
			}
		}
	}
	
	public void loadPrefs(String filename, Game game) {
		if(oldSave) return;
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
		player.armor = Integer.parseInt((String)data.get(5));
		
		String modedata;
		if(!oldSave) {
			if(data.size() >= 14) {
				if(worldVer == null) worldVer = new Version("1.9.1-pre1");
				player.armorDamageBuffer = Integer.parseInt((String)data.get(13));
				player.curArmor = (ArmorResource)(((ResourceItem)ListItems.getItem((String)data.get(14))).resource);
			} else player.armor = 0;
			
			player.ac = Integer.parseInt((String)data.get(7));
			Game.currentLevel = Integer.parseInt((String)data.get(8));
			modedata = (String)data.get(9);
			
		} else {
			// old, 1.8 save.
			Game.currentLevel = Integer.parseInt((String)data.get(7));
			modedata = (String)data.get(8);
		}
		//else if(!oldSave) player.armor = 0;
		Player.score = Integer.parseInt((String)data.get(6));
		//if(!oldSave) player.ac = Integer.parseInt((String)data.get(7));
		//Game.currentLevel = Integer.parseInt((String)data.get(oldSave?7:8));
		player.game.level = Game.levels[Game.currentLevel];
		
		//String modedata = (String)data.get(oldSave?8:9);
		int mode;
		if(modedata.contains(";")) {
			mode = Integer.parseInt(modedata.substring(0, modedata.indexOf(";")));
			if (mode == 4)
				player.game.scoreTime = Integer.parseInt(modedata.substring(modedata.indexOf(";") + 1));
		}
		else {
			mode = Integer.parseInt(modedata);
			if (mode == 4) player.game.scoreTime = 300;
		}
		
		ModeMenu.updateModeBools(mode);
		
		boolean hasEffects;
		int potionIdx = 10;
		if(oldSave) {
			hasEffects = data.size() > 10 && ((String)data.get(data.size()-2)).contains("PotionEffects[");
			potionIdx = data.size() - 2;
		} else
			hasEffects = !((String)data.get(10)).equals("PotionEffects[]"); // newer save
		
		if(hasEffects) {
			String potiondata = ((String)data.get(potionIdx)).replace("PotionEffects[", "").replace("]", "");
			List effects = Arrays.asList(potiondata.split(":"));
			
			for(int i = 0; i < effects.size(); i++) {
				List effect = Arrays.asList(((String)effects.get(i)).split(";"));
				String pName = (String)effect.get(0);
				if(oldSave) pName = pName.replace("P.", "Potion");
				PotionResource.applyPotion(player, pName, Integer.parseInt((String)effect.get(1)));
			}
		}
		
		String colors = ((String)data.get(oldSave?data.size()-1:11)).replace("[", "").replace("]", "");
		List color = Arrays.asList(colors.split(";"));
		player.r = Integer.parseInt((String)color.get(0));
		player.g = Integer.parseInt((String)color.get(1));
		player.b = Integer.parseInt((String)color.get(2));
		
		if(!oldSave) Player.skinon = Boolean.parseBoolean((String)data.get(12));
		else Player.skinon = false;
	}
	
	public void loadInventory(String filename, Inventory inventory) {
		loadFromFile(location + filename + extention);
		inventory.clearInv();
		//inventory.playerinventory = true; // this is only called for the player inventory so I can do this
		
		for(int i = 0; i < data.size(); i++) {
			String item = (String)data.get(i);
			
			/*if(oldSave) {
				if(ListItems.getItem(item) instanceof ResourceItem) {
					
				} else {
					item = subOldItemName(item);
					Item toAdd = ListItems.getItem(item);
					inventory.add(toAdd);
				}
			}*/
			
			if(ListItems.getItem(item) instanceof ResourceItem) {
				if(oldSave && i == 0) item = item.replace(";0", ";1");
				List curData = Arrays.asList(item.split(";"));
				String itemName = (String)curData.get(0);
				if(oldSave) itemName = subOldItemName(itemName);
				
				Item newItem = ListItems.getItem(itemName);
				
				for(int ii = 0; ii < Integer.parseInt((String)curData.get(1)); ii++) {
					if(newItem instanceof ResourceItem) {
						ResourceItem resItem = new ResourceItem(((ResourceItem)newItem).resource);
						inventory.add(resItem);
					} else {
						inventory.add(newItem);
					}
				}
			} else {
				if(oldSave) item = subOldItemName(item);
				Item toAdd = ListItems.getItem(item);
				inventory.add(toAdd);
			}
		}
	}
	
	private String subOldItemName(String oldName) {
		if(oldName.contains(";")) oldName = oldName.substring(0, oldName.indexOf(";"));
		oldName = oldName.replace("P.", "Potion");
		
		switch(oldName) {
			case "Fish Rod": return "Fishing Rod";
			
			default: return oldName;
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
			if(newEntity != null) { // the method never returns null, but...
				newEntity.x = Integer.parseInt((String)info.get(0));
				newEntity.y = Integer.parseInt((String)info.get(1));
				int currentlevel;
				if(newEntity instanceof Mob) {
					Mob mob = (Mob)newEntity;
					mob.health = Integer.parseInt((String)info.get(2));
					mob.maxHealth = Integer.parseInt((String)info.get(3));
					mob.lvl = Integer.parseInt((String)info.get(4));
					//System.out.println("made mob, lvl " + mob.lvl);
					mob.level = Game.levels[Integer.parseInt((String)info.get(5))];
					currentlevel = Integer.parseInt((String)info.get(5));
					Game.levels[currentlevel].add(mob);
				} else if(newEntity instanceof Chest) {
					Chest chest = (Chest)newEntity;
					boolean isDeathChest = chest instanceof DeathChest;
					boolean isDungeonChest = chest instanceof DungeonChest;
					List<String> chestInfo = info.subList(2, info.size()-1);
					
					int endIdx = chestInfo.size()-(isDeathChest||isDungeonChest?1:0);
					for(int idx = 0; idx < endIdx; idx++) {
						String itemData = (String)chestInfo.get(idx);
						if(worldVer.compareTo(new Version("1.9.1")) < 0) // if this world is before 1.9.1
							if(itemData.equals("")) continue; // this skips any null items
						//if(Game.debug) System.out.println("fetching chest item "+(idx+1)+" of "+endIdx+": \"" + itemData + "\"");
						Item item = ListItems.getItem(itemData);
						if (item instanceof ResourceItem) {
							List<String> curData = Arrays.asList((itemData + ";1").split(";")); // this appends ";1" to the end, meaning one item, to everything; but if it was already there, then it becomes the 3rd element in the list, which is ignored.
							ResourceItem ri = (ResourceItem)ListItems.getItem(curData.get(0));
							ri.count = Integer.parseInt(curData.get(1));
							chest.inventory.add(ri);
						} else {//if(!item.getName().equals("")) {
							//addToChest(chest, item);
							/*if(item instanceof ResourceItem) {
								ResourceItem ri = (ResourceItem)item;
								chest.inventory.add(ri);
							} else*/ chest.inventory.add(item);
						}
						//else System.out.println("skipped NULL chest item: \"" + itemData + "\"");
					}
					//if(idx == chestInfo.size() - 2) {
					if (isDeathChest) {
						((DeathChest)chest).time = Integer.parseInt((chestInfo.get(chestInfo.size()-1)).replace("tl;", ""));//"tl;" is only for old save support
					} else if (isDungeonChest) {
						((DungeonChest)chest).isLocked = Boolean.parseBoolean(chestInfo.get(chestInfo.size()-1));
					}
					//}
					
					newEntity.level = Game.levels[Integer.parseInt((String)info.get(info.size() - 1))];
					currentlevel = Integer.parseInt((String)info.get(info.size() - 1));
					Game.levels[currentlevel].add(chest instanceof DeathChest ? (DeathChest)chest : chest instanceof DungeonChest ? (DungeonChest)chest : chest);
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
	
	/*private void addToChest(Furniture box, Item toAdd) {
		//if (toAdd == null || box == null) return;
		if(box instanceof Chest) {
			Chest chest = (Chest)box;
			if(toAdd instanceof ResourceItem) {
				ResourceItem item = (ResourceItem)toAdd;
				chest.inventory.add(item);
			} else {
				chest.inventory.add(toAdd);
			}
		}
		else if (box instanceof DungeonChest) {
			DungeonChest dChest = (DungeonChest)box;
			if(toAdd instanceof ResourceItem) {
				ResourceItem item = (ResourceItem)toAdd;
				dChest.inventory.add(item);
			} else
				dChest.inventory.add(toAdd);
		}
	}*/
	
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
			case "DeathChest": return (Entity)(new DeathChest());
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
			default : if(Game.debug) System.out.println("LOAD: UNKNOWN ENTITY");
				return new Entity();
		}
	}
}
