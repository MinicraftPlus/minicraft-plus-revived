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
import minicraft.entity.Inventory;
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
import minicraft.level.Level;
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
		//testFile = new File(location);
		//hasGlobalPrefs = hasGlobalPrefs || ;
		
		data = new ArrayList<String>();
		extradata = new ArrayList<String>();
		hasloadedbigworldalready = false;
	}
	
	public Load(Game game, String worldname) {
		this();
		
		if(!hasGlobalPrefs) {
			loadFromFile(location + "/saves/" + worldname + "/Game" + extension);
			Version wVer = null;
			if(data.get(0).contains(".")) wVer = new Version(data.get(0));
			if(wVer == null) wVer = new Version("1.8");
			hasGlobalPrefs = wVer.compareTo(new Version("1.9.2")) >= 0;
		}
		
		if(!hasGlobalPrefs)
			new LegacyLoad(game, worldname);
		else {
			location += "/saves/" + worldname + "/";
			
			loadGame("Game", game); // more of the version will be determined here
			loadWorld("Level");
			loadPlayer("Player", game.player);
			loadInventory("Inventory", game.player.inventory);
			loadEntities("Entities", game.player);
			LoadingMenu.percentage = 0; // reset
		}
	}
	
	public Load(Game game) {
		this();
		
		location += "/";
		
		if(hasGlobalPrefs)
			loadPrefs("Preferences", game);
	}
	
	protected static class Version implements Comparable {
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
		
		worldVer = new Version(data.get(0)); // gets the world version
		Game.setTime(Integer.parseInt(data.get(1)));
		
		if(worldVer.compareTo(new Version("1.9.3-dev2")) >= 0) {
			game.gameTime = Integer.parseInt(data.get(2));
			Game.pastDay1 = game.gameTime > 65000;
		} else {
			Game.astime = Integer.parseInt(data.get(2));
			game.gameTime = 65000; // prevents time cheating.
		}
		
		OptionsMenu.diff = Integer.parseInt(data.get(3));
		if(worldVer.compareTo(new Version("1.9.3-dev3")) < 0)
			OptionsMenu.diff--; // account for change in difficulty
		
		AirWizard.beaten = Boolean.parseBoolean(data.get(4));
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
		for(int l = Game.levels.length-1; l>=0; l--) {
			loadFromFile(location + filename + l + extention);
			
			int lvlw = Integer.parseInt(data.get(0));
			int lvlh = Integer.parseInt(data.get(1));
			int lvldepth = Integer.parseInt(data.get(2));
			
			byte[] tiles = new byte[lvlw * lvlh];
			byte[] tdata = new byte[lvlw * lvlh];
			
			for(int x = 0; x < lvlw - 1; x++) {
				for(int y = 0; y < lvlh - 1; y++) {
					int tileArrIdx = /*worldVer.compareTo(new Version("1.9.3-dev3")) < 0 ?*/ y + x * lvlw;// : x + y * lvlw;
					int tileidx = x + y * lvlw; // the tiles are saved with x outer loop, and y inner loop, meaning that the list reads down, then right one, rather than right, then down one.
					int tileID = Integer.parseInt(data.get(tileidx + 3));
					//System.out.println("reading tile on level "+l+"; save idx=" + (tileidx+3) + ", old id:" + tileID);
					if(worldVer.compareTo(new Version("1.9.4-dev3")) < 0) {
						if(Tile.oldids.containsKey(tileID))
							tileID = Tile.oldids.get(tileID);
						else System.out.println("tile list doesn't contain tile " + tileID);
					}
					//System.out.println("new id: " + tileID);
					byte id = (byte) tileID;
					if(id < 0)
						tiles[tileArrIdx] = minicraft.level.tile.TorchTile.getTorchTile(Tile.tiles[id+128]).id;
					else
						tiles[tileArrIdx] = id;
					if(tiles[tileArrIdx] == Tile.stairsUp.id) System.out.println("stairs up at: x="+x+" y="+y);
					tdata[tileArrIdx] = Byte.parseByte(extradata.get(tileidx));
				}
			}
			
			Level parent = l == Game.levels.length-1 ? null : Game.levels[l+1];
			Game.levels[l] = new Level(lvlw, lvlh, lvldepth, parent, false);
			Game.levels[l].tiles = tiles;
			Game.levels[l].data = tdata;
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
		
		if(player.armor > 0) {
			player.armorDamageBuffer = Integer.parseInt(data.get(13));
			player.curArmor = (ArmorResource)(((ResourceItem)ListItems.getItem(data.get(14))).resource);
		}
		
		Player.score = Integer.parseInt(data.get(6));
		player.ac = Integer.parseInt(data.get(7));
		
		Game.currentLevel = Integer.parseInt(data.get(8));
		Level level = Game.levels[Game.currentLevel];
		level.add(player);
		Tile spawnTile = level.getTile(player.spawnx >> 4, player.spawny >> 4);
		if(spawnTile.id != Tile.grass.id && spawnTile.mayPass(level, player.spawnx >> 4, player.spawny >> 4, player))
			player.bedSpawn = true; //A semi-advanced little algorithm to determine if the player has a bed save; and though if you sleep on a grass tile, this won't get set, it doesn't matter b/c you'll spawn there anyway!
		
		String modedata = data.get(9);
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
		
		if(!data.get(10).equals("PotionEffects[]")) {
			String[] effects = data.get(10).replace("PotionEffects[", "").replace("]", "").split(":");
			
			for(int i = 0; i < effects.length; i++) {
				String[] effect = effects[i].split(";");
				String pName = effect[0];
				PotionResource.applyPotion(player, pName, Integer.parseInt(effect[1]));
			}
		}
		
		String colors = data.get(11).replace("[", "").replace("]", "");
		String[] color = colors.split(";");
		player.r = Integer.parseInt(color[0]);
		player.g = Integer.parseInt(color[1]);
		player.b = Integer.parseInt(color[2]);
		
		Player.skinon = Boolean.parseBoolean(data.get(12));
	}
	
	public void loadInventory(String filename, Inventory inventory) {
		loadFromFile(location + filename + extention);
		inventory.clearInv();
		
		for(int i = 0; i < data.size(); i++) {
			String item = data.get(i);
			
			if(ListItems.getItem(item) instanceof ResourceItem) {
				List<String> curData = Arrays.asList(item.split(";"));
				String itemName = curData.get(0);
				
				if(worldVer.compareTo(new Version("1.9.4-dev4")) < 0) {
					itemName = itemName.replace("Hatchet", "Axe").replace("Pick\\b", "Pickaxe").replace("Spade", "Shovel");
				}
				
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
				if(worldVer.compareTo(new Version("1.9.4-dev4")) < 0) {
					item = item.replace("Water Bucket", "Bucket " + Tile.water.id)
							.replace("Lava Bucket", "Bucket " + Tile.lava.id);
				}
				Item toAdd = ListItems.getItem(item);
				inventory.add(toAdd);
			}
		}
	}
	
	public void loadEntities(String filename, Player player) {
		loadFromFile(location + filename + extention);
		
		for(int i = 0; i < Game.levels.length; i++) {
			Game.levels[i].entities.clear();
		}
		
		for(int i = 0; i < data.size(); i++) {
			List<String> info = Arrays.asList(data.get(i).substring(data.get(i).indexOf("[") + 1, data.get(i).indexOf("]")).split(":")); // this gets everything inside the "[...]" after the entity name.
			
			String entityName = data.get(i).substring(0, data.get(i).indexOf("[")); // this gets the text before "[", which is the entity name.
			int x = Integer.parseInt(info.get(0));
			int y = Integer.parseInt(info.get(1));
			
			int mobLvl = 1;
			try {
				if(Class.forName("minicraft.entity.EnemyMob").isAssignableFrom(Class.forName("minicraft.entity."+entityName)))
					mobLvl = Integer.parseInt(info.get(info.size()-2));
			} catch(ClassNotFoundException ex) {
				ex.printStackTrace();
			}
			
			if(mobLvl == 0) {
				if(Game.debug) System.out.println("level 0 mob: " + entityName);
				mobLvl = 1;
			}
			
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
						((DeathChest)chest).time = Integer.parseInt(chestInfo.get(chestInfo.size()-1));
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
			case "Lantern": return (Entity)(new Lantern(Lantern.Type.NORM));
			case "Iron Lantern": return (Entity)(new Lantern(Lantern.Type.IRON));
			case "Gold Lantern": return (Entity)(new Lantern(Lantern.Type.GOLD));
			//case "Spark": return (Entity)(new Spark());
			default : /*if(Game.debug)*/ System.out.println("LOAD: unknown or outdated entity requested: " + string);
				return null;
		}
	}
}
