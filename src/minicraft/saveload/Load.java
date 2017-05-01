package minicraft.saveload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import minicraft.Game;
import minicraft.entity.AirWizard;
import minicraft.entity.Anvil;
import minicraft.entity.Bed;
import minicraft.entity.Chest;
import minicraft.entity.Cow;
import minicraft.entity.Creeper;
import minicraft.entity.DeathChest;
import minicraft.entity.DungeonChest;
import minicraft.entity.Enchanter;
import minicraft.entity.Entity;
import minicraft.entity.Furnace;
import minicraft.entity.GoldLantern;
import minicraft.entity.Inventory;
import minicraft.entity.IronLantern;
import minicraft.entity.Knight;
import minicraft.entity.Lantern;
import minicraft.entity.Loom;
import minicraft.entity.Mob;
import minicraft.entity.Oven;
import minicraft.entity.Pig;
import minicraft.entity.Player;
import minicraft.entity.Sheep;
import minicraft.entity.Skeleton;
import minicraft.entity.Slime;
import minicraft.entity.Snake;
import minicraft.entity.Spawner;
import minicraft.entity.Tnt;
import minicraft.entity.Workbench;
import minicraft.entity.Zombie;
import minicraft.item.Item;
import minicraft.item.ListItems;
import minicraft.item.ResourceItem;
import minicraft.item.resource.ArmorResource;
import minicraft.item.resource.PotionResource;
import minicraft.level.tile.Tile;
import minicraft.screen.LoadingMenu;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;


// I may want to consider a "LegacyLoad class in the near future, simply to reduce the clutter and really allow me to "start fresh". :)
public class Load {
	
	String location = Game.gameDir;
	File folder;
	
	private static String extention = Save.extention;
	
	ArrayList<String> data;
	ArrayList<String> extradata;
	
	public boolean hasloadedbigworldalready;
	Version currentVer, worldVer;
	boolean oldSave = false, hasGlobalPrefs = false;
	
	private Load() {
		currentVer = new Version(Game.VERSION);
		worldVer = null;
		
		File testFile = new File(location + "/Preferences" + extention);
		hasGlobalPrefs = testFile.exists();
		
		data = new ArrayList<String>();
		extradata = new ArrayList<String>();
		hasloadedbigworldalready = false;
	}
	
	public Load(Game game, String worldname) {
		this();
		
		location += "/saves/" + worldname + "/";
		
		File testFile = new File(location + "KeyPrefs" + extention);
		if(!testFile.exists() && !hasGlobalPrefs) {
			worldVer = new Version("1.8");
			oldSave = true;
		}
		
		loadGame("Game", game); // more of the version will be determined here
		//loadPrefs("KeyPrefs", game);
		loadWorld("Level");
		loadPlayer("Player", game.player);
		loadInventory("Inventory", game.player.inventory);
		loadEntities("Entities", game.player);
		LoadingMenu.percentage = 0;
	}
	
	public Load(Game game) {
		this();
		
		location += "/";
		
		if(hasGlobalPrefs)
			loadPrefs("Preferences", game);
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
			
			String curLine, total = "";
			ArrayList<String> curData;
			while((curLine = br.readLine()) != null)
				total += curLine;
			data.addAll(Arrays.asList(total.split(",")));
			
			if(filename.contains("Level")) {
				total = "";
				br2 = new BufferedReader(new FileReader(filename.substring(0, filename.lastIndexOf("/") + 7) + "data" + extention));
				
				while((curLine = br2.readLine()) != null)
					total += curLine;
				extradata.addAll(Arrays.asList(total.split(",")));
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
		boolean hasVersion = data.get(0).contains(".");
		if(hasVersion) {
			worldVer = new Version(data.get(0)); // gets the world version
			Game.setTime(Integer.parseInt(data.get(1)));
			Game.astime = Integer.parseInt(data.get(2));
			if(worldVer.compareTo(new Version("1.9.2")) < 0) {
				OptionsMenu.autosave = Boolean.parseBoolean(data.get(3));
				OptionsMenu.isSoundAct = Boolean.parseBoolean(data.get(4));
				if(worldVer.compareTo(new Version("1.9.2-dev2")) >= 0)
					AirWizard.beaten = Boolean.parseBoolean(data.get(5));
			}
			else { // this is dev4 or after
				OptionsMenu.diff = Integer.parseInt(data.get(3));
				AirWizard.beaten = Boolean.parseBoolean(data.get(4));
			}
		}
		else {
			if(data.size() == 5) {
				worldVer = new Version("1.9");
				Game.setTime(Integer.parseInt(data.get(0)));
				Game.astime = Integer.parseInt(data.get(1));
				OptionsMenu.autosave = Boolean.parseBoolean(data.get(3));
				OptionsMenu.isSoundAct = Boolean.parseBoolean(data.get(4));
			} else { // version == 1.8?
				if(!oldSave) {
					System.out.println("UNEXPECTED WORLD VERSION");
					worldVer = new Version("1.8.1");
				}
				// for backwards compatibility
				Game.tickCount = Integer.parseInt(data.get(0));
				Game.astime = Integer.parseInt(data.get(1));
				game.player.ac = Integer.parseInt(data.get(3));
				OptionsMenu.autosave = false;
			}
		}
	}
	
	public void loadPrefs(String filename, Game game) {
		loadFromFile(location + filename + extention);
		
		OptionsMenu.isSoundAct = Boolean.parseBoolean(data.get(0));
		OptionsMenu.autosave = Boolean.parseBoolean(data.get(1));
		
		List<String> subdata = data.subList(2, data.size());
		
		Iterator<String> keys = subdata.iterator();
		while(keys.hasNext()) {
			String[] map = keys.next().split(";");
			game.input.setKey(map[0], map[1]);
		}
	}
	
	public void loadWorld(String filename) {
		for(int l = 0; l < Game.levels.length; l++) {
			loadFromFile(location + filename + l + extention);
			Game.levels[l].w = Integer.parseInt(data.get(0));
			Game.levels[l].h = Integer.parseInt(data.get(1));
			Game.levels[l].depth = Integer.parseInt(data.get(2));
			
			for(int x = 0; x < Game.levels[l].w - 1; x++) {
				for(int y = 0; y < Game.levels[l].h - 1; y++) {
					Game.levels[l].setTile(y, x, Tile.tiles[Integer.parseInt(data.get(x + y * Game.levels[l].w + 3))], Integer.parseInt(extradata.get(x + y * Game.levels[l].w)));
				}
			}
		}
		
	}
	
	public void loadPlayer(String filename, Player player) {
		loadFromFile(location + filename + extention);
		player.x = Integer.parseInt(data.get(0));
		player.y = Integer.parseInt(data.get(1));
		Player.spawnx = Integer.parseInt(data.get(2));
		Player.spawny = Integer.parseInt(data.get(3));
		player.health = Integer.parseInt(data.get(4));
		player.armor = Integer.parseInt(data.get(5));
		
		String modedata;
		if(!oldSave) {
			if(data.size() >= 14) {
				if(worldVer == null) worldVer = new Version("1.9.1-pre1");
				player.armorDamageBuffer = Integer.parseInt(data.get(13));
				player.curArmor = (ArmorResource)(((ResourceItem)ListItems.getItem(data.get(14))).resource);
			} else player.armor = 0;
			
			player.ac = Integer.parseInt(data.get(7));
			Game.currentLevel = Integer.parseInt(data.get(8));
			modedata = data.get(9);
			
		} else {
			// old, 1.8 save.
			Game.currentLevel = Integer.parseInt(data.get(7));
			modedata = data.get(8);
		}
		
		Player.score = Integer.parseInt(data.get(6));
		Game.levels[Game.currentLevel].add(player);
		
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
			hasEffects = data.size() > 10 && data.get(data.size()-2).contains("PotionEffects[");
			potionIdx = data.size() - 2;
		} else
			hasEffects = !data.get(10).equals("PotionEffects[]"); // newer save
		
		if(hasEffects) {
			String[] effects = data.get(potionIdx).replace("PotionEffects[", "").replace("]", "").split(":");
			
			for(int i = 0; i < effects.length; i++) {
				String[] effect = effects[i].split(";");
				String pName = effect[0];
				if(oldSave) pName = pName.replace("P.", "Potion");
				PotionResource.applyPotion(player, pName, Integer.parseInt(effect[1]));
			}
		}
		
		String colors = data.get(oldSave?data.size()-1:11).replace("[", "").replace("]", "");
		String[] color = colors.split(";");
		player.r = Integer.parseInt(color[0]);
		player.g = Integer.parseInt(color[1]);
		player.b = Integer.parseInt(color[2]);
		
		if(!oldSave) Player.skinon = Boolean.parseBoolean(data.get(12));
		else Player.skinon = false;
	}
	
	public void loadInventory(String filename, Inventory inventory) {
		loadFromFile(location + filename + extention);
		inventory.clearInv();
		
		for(int i = 0; i < data.size(); i++) {
			String item = data.get(i);
			
			if(ListItems.getItem(item) instanceof ResourceItem) {
				if(oldSave && i == 0) item = item.replace(";0", ";1");
				List<String> curData = Arrays.asList(item.split(";"));
				String itemName = curData.get(0);
				if(oldSave) itemName = subOldName(itemName);
				
				Item newItem = ListItems.getItem(itemName);
				
				for(int ii = 0; ii < Integer.parseInt(curData.get(1)); ii++) {
					if(newItem instanceof ResourceItem) {
						ResourceItem resItem = new ResourceItem(((ResourceItem)newItem).resource);
						inventory.add(resItem);
					} else {
						inventory.add(newItem);
					}
				}
			} else {
				if(oldSave) item = subOldName(item);
				Item toAdd = ListItems.getItem(item);
				inventory.add(toAdd);
			}
		}
	}
	
	private String subOldName(String oldName) {
		//System.out.println("old name: " + oldName);
		String newName = oldName.replace("P.", "Potion").replace("Fish Rod", "Fishing Rod").replace("bed", "Bed");
		//System.out.println("new name: " + newName);
		return newName;
	}
	
	public void loadEntities(String filename, Player player) {
		loadFromFile(location + filename + extention);
		
		for(int i = 0; i < Game.levels.length; i++) {
			Game.levels[i].entities.clear();
		}
		
		for(int i = 0; i < data.size(); i++) {
			List<String> info = Arrays.asList(data.get(i).substring(data.get(i).indexOf("[") + 1, data.get(i).indexOf("]")).split(":")); // this gets everything inside the "[...]" after the entity name.
			
			String entityName = data.get(i).substring(0, data.get(i).indexOf("[")).replace("bed", "Bed").replace("II", ""); // this gets the text before "[", which is the entity name.
			int x = Integer.parseInt(info.get(0));
			int y = Integer.parseInt(info.get(1));
			
			int mobLvl = 0;
			try {
				if(Class.forName("EnemyMob").isAssignableFrom(Class.forName(entityName)))
					mobLvl = Integer.parseInt(info.get(info.size()-2));
			} catch(ClassNotFoundException ex) {}
			
			Entity newEntity = getEntity(entityName, player, mobLvl);
			
			if(newEntity != null) { // the method never returns null, but...
				int currentlevel;
				if(newEntity instanceof Mob) {
					Mob mob = (Mob)newEntity;
					mob.health = Integer.parseInt(info.get(2));
					currentlevel = Integer.parseInt(info.get(info.size()-1));
					Game.levels[currentlevel].add(mob, x, y);
				} else if(newEntity instanceof Chest) {
					Chest chest = (Chest)newEntity;
					boolean isDeathChest = chest instanceof DeathChest;
					boolean isDungeonChest = chest instanceof DungeonChest;
					List<String> chestInfo = info.subList(2, info.size()-1);
					
					int endIdx = chestInfo.size()-(isDeathChest||isDungeonChest?1:0);
					for(int idx = 0; idx < endIdx; idx++) {
						String itemData = chestInfo.get(idx);
						if(worldVer.compareTo(new Version("1.9.1")) < 0) // if this world is before 1.9.1
							if(itemData.equals("")) continue; // this skips any null items
						if(oldSave) itemData = subOldName(itemData);
						Item item = ListItems.getItem(itemData);
						if (item instanceof ResourceItem) {
							String[] resourceData = (itemData + ";1").split(";"); // this appends ";1" to the end, meaning one item, to everything; but if it was already there, then it becomes the 3rd element in the list, which is ignored.
							ResourceItem ri = (ResourceItem)ListItems.getItem(resourceData[0]);
							ri.count = Integer.parseInt(resourceData[1]);
							chest.inventory.add(ri);
						} else {
							chest.inventory.add(item);
						}
					}
					
					if (isDeathChest) {
						((DeathChest)chest).time = Integer.parseInt(chestInfo.get(chestInfo.size()-1).replace("tl;", "")); // "tl;" is only for old save support
					} else if (isDungeonChest) {
						((DungeonChest)chest).isLocked = Boolean.parseBoolean(chestInfo.get(chestInfo.size()-1));
					}
					
					
					currentlevel = Integer.parseInt(info.get(info.size() - 1));
					Game.levels[currentlevel].add(chest instanceof DeathChest ? (DeathChest)chest : chest instanceof DungeonChest ? (DungeonChest)chest : chest, x, y);
				}
				else if(newEntity instanceof Spawner) {
					Spawner egg = (Spawner)newEntity;
					egg.lvl = Integer.parseInt(info.get(3));
					egg.setMob(info.get(2));
					currentlevel = Integer.parseInt((String)info.get(info.size() - 1));
					Game.levels[currentlevel].add(egg, x, y);
				}
				else {
					currentlevel = Integer.parseInt(info.get(2));
					Game.levels[currentlevel].add(newEntity, x, y);
				}
			} // end of entity not null conditional
		}
	}
	
	public Entity getEntity(String string, Player player, int moblvl) {
		switch(string) {
			case "Player": return (Entity)(player);
			case "Cow": return (Entity)(new Cow());
			case "Sheep": return (Entity)(new Sheep());
			case "Pig": return (Entity)(new Pig());
			case "Zombie": return (Entity)(new Zombie(moblvl));
			case "Slime": return (Entity)(new Slime(moblvl));
			case "Creeper": return (Entity)(new Creeper(moblvl));
			case "Skeleton": return (Entity)(new Skeleton(moblvl));
			case "Knight": return (Entity)(new Knight(moblvl));
			case "Snake": return (Entity)(new Snake(moblvl));
			case "AirWizard": return (Entity)(new AirWizard(moblvl>1));
			case "Spawner": return (Entity)(new Spawner("Zombie", 1));
			case "Workbench": return (Entity)(new Workbench());
			case "Chest": return (Entity)(new Chest());
			case "DeathChest": return (Entity)(new DeathChest());
			case "DungeonChest": return (Entity)(new DungeonChest());
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
			//case "Spark": return (Entity)(new Spark());
			default : /*if(Game.debug)*/ System.out.println("LOAD: unknown or outdated entity requested: " + string);
				return null;
		}
	}
}
