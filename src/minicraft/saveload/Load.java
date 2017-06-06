package minicraft.saveload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Base64;
import minicraft.Game;
import minicraft.entity.*;
import minicraft.item.ArmorItem;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.screen.LoadingMenu;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;

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
			loadFromFile(location + "/saves/" + worldname + "/Game" + extention);
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
			loadWorld("Level", game);
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
		
		File testFile = new File(location+"unlocks"+extention);
		if (testFile.exists()) {
			new LegacyLoad("unlocks", "Unlocks");
			testFile.delete();
		}
		testFile = new File(location+"Unlocks"+extention);
		if(!testFile.exists()) {
			try {
				testFile.createNewFile();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		loadUnlocks("Unlocks");
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
			
			/*if(worldVer != null && worldVer.compareTo(new Version("1.9.4-dev6")) >= 0 && filename.contains("Level") && !filename.contains("Data")) {
				total = new String(Base64.getDecoder().decode(total));
			}*/
			
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
	
	public void loadUnlocks(String filename) {
		loadFromFile(location + filename + extention);
		
		ModeMenu.unlockedtimes.clear();
		OptionsMenu.unlockedskin = false;
		
		for(String unlock: data) {
			if(unlock.equals("AirSkin"))
				OptionsMenu.unlockedskin = true;
			
			unlock = unlock.replace("HOURMODE", "H_ScoreTime").replace("MINUTEMODE", "M_ScoreTime");
			
			if(unlock.contains("_ScoreTime"))
				ModeMenu.unlockedtimes.add(unlock.substring(0, unlock.indexOf("_")));
		}
		
		ModeMenu.initTimeList();
	}
	
	public void loadGame(String filename, Game game) {
		loadFromFile(location + filename + extention);
		
		worldVer = new Version(data.get(0)); // gets the world version
		Game.setTime(Integer.parseInt(data.get(1)));
		
		if(worldVer.compareTo(new Version("1.9.3-dev2")) >= 0) {
			game.gameTime = Integer.parseInt(data.get(2));
			Game.pastDay1 = game.gameTime > 65000;
		} else {
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
	
	public void loadWorld(String filename, Game game) {
		for(int l = Game.levels.length-2; l>=0; l--) {
			//if(l == Game.levels.length-1) l = 4;
			//if(l == 0) l = Game.levels.length-1;
			
			loadFromFile(location + filename + l + extention);
			
			int lvlw = Integer.parseInt(data.get(0));
			int lvlh = Integer.parseInt(data.get(1));
			int lvldepth = Integer.parseInt(data.get(2));
			
			byte[] tiles = new byte[lvlw * lvlh];
			byte[] tdata = new byte[lvlw * lvlh];
			
			for(int x = 0; x < lvlw; x++) {
				for(int y = 0; y < lvlh; y++) {
					int tileArrIdx = /*worldVer.compareTo(new Version("1.9.3-dev3")) < 0 ?*/ y + x * lvlw;// : x + y * lvlw;
					int tileidx = x + y * lvlw; // the tiles are saved with x outer loop, and y inner loop, meaning that the list reads down, then right one, rather than right, then down one.
					String tilename = data.get(tileidx + 3);
					if(worldVer.compareTo(new Version("1.9.4-dev6")) < 0) {
						int tileID = Integer.parseInt(tilename); // they were id numbers, not names, at this point
						if(Tiles.oldids.get(tileID) != null)
							tilename = Tiles.oldids.get(tileID);
						else {
							System.out.println("tile list doesn't contain tile " + tileID);
							tilename = "grass";
						}
					}
					tiles[tileArrIdx] = Tiles.get(tilename).id;
					tdata[tileArrIdx] = Byte.parseByte(extradata.get(tileidx));
				}
			}
			
			Level parent = l == Game.levels.length-2 ? null : l == Game.levels.length-1 ? Game.levels[0] : Game.levels[l+1];
			Game.levels[l] = new Level(game, lvlw, lvlh, lvldepth, parent, false);
			
			Level curLevel = Game.levels[l];
			curLevel.tiles = tiles;
			curLevel.data = tdata;
			
			if(Game.debug) {
				//System.out.println("level depth=" + curLevel.depth + " -- parent depth=" + (parent==null?"null":parent.depth));
				
				curLevel.printTileLocs(Tiles.get("Stairs Down"));
			}
			
			if(parent == null) continue;
			/// comfirm that there are stairs in all the places that should have stairs.
			for(java.awt.Point p: parent.getMatchingTiles(Tiles.get("Stairs Down"))) {
				if(curLevel.getTile(p.x, p.y) != Tiles.get("Stairs Up")) {
					curLevel.printLevelLoc("INCONSISTENT STAIRS detected; placing stairsUp", p.x, p.y);
					curLevel.setTile(p.x, p.y, Tiles.get("Stairs Up"));
				}
			}
			for(java.awt.Point p: curLevel.getMatchingTiles(Tiles.get("Stairs Up"))) {
				if(parent.getTile(p.x, p.y) != Tiles.get("Stairs Down")) {
					parent.printLevelLoc("INCONSISTENT STAIRS detected; placing stairsDown", p.x, p.y);
					parent.setTile(p.x, p.y, Tiles.get("Stairs Down"));
				}
			}
			
			// fixes some parenting issues.
			if(l == 0) l = Game.levels.length;
			if(l == Game.levels.length-1) l = 0;
		}
	}
	
	public static void loadLevel(Level level, int[] lvlids, int[] lvldata) {
		int lvlw = lvlids[0];
		int lvlh = lvldata[0];
		
		byte[] tiles = new byte[lvlw * lvlh];
		byte[] tdata = new byte[lvlw * lvlh];
		
		int percentInc = 100 / (lvlw*lvlh);
		try {
			for(int i = 1; i < lvlw * lvlh-1; i++) {
				tiles[i] = (byte) lvlids[i];
				tdata[i] = (byte) lvldata[i];
				LoadingMenu.percentage += percentInc;
			}
		} catch (IndexOutOfBoundsException ex) {
			System.err.println("suspected: level id and data arrays do not have enough info for given world size.");
			ex.printStackTrace();
		}
		
		level.tiles = tiles;
		level.data = tdata;
	}
	
	public void loadPlayer(String filename, Player player) {
		loadFromFile(location + filename + extention);
		player.x = Integer.parseInt(data.get(0));
		player.y = Integer.parseInt(data.get(1));
		player.spawnx = Integer.parseInt(data.get(2));
		player.spawny = Integer.parseInt(data.get(3));
		player.health = Integer.parseInt(data.get(4));
		player.armor = Integer.parseInt(data.get(5));
		
		if(player.armor > 0) {
			player.armorDamageBuffer = Integer.parseInt(data.get(13));
			player.curArmor = (ArmorItem)Items.get(data.get(14));
		}
		
		player.score = Integer.parseInt(data.get(6));
		player.ac = Integer.parseInt(data.get(7));
		
		player.game.currentLevel = Integer.parseInt(data.get(8));
		Level level = Game.levels[player.game.currentLevel];
		level.add(player);
		Tile spawnTile = level.getTile(player.spawnx >> 4, player.spawny >> 4);
		//if(spawnTile.id != Tiles.get("grass").id && spawnTile.mayPass(level, player.spawnx >> 4, player.spawny >> 4, player))
			//player.bedSpawn = true; //A semi-advanced little algorithm to determine if the player has a bed save; and though if you sleep on a grass tile, this won't get set, it doesn't matter b/c you'll spawn there anyway!
		
		String modedata = data.get(9);
		int mode;
		if(modedata.contains(";")) {
			String[] modeinfo = modedata.split(";");
			mode = Integer.parseInt(modeinfo[0]);
			if (mode == 4) {
				Game.scoreTime = Integer.parseInt(modeinfo[1]);
				if(worldVer.compareTo(new Version("1.9.4")) >= 0)
					ModeMenu.setScoreTime(modeinfo[2]);
			}
		}
		else {
			mode = Integer.parseInt(modedata);
			if (mode == 4) Game.scoreTime = 300;
		}
		
		ModeMenu.updateModeBools(mode);
		
		if(!data.get(10).equals("PotionEffects[]")) {
			String[] effects = data.get(10).replace("PotionEffects[", "").replace("]", "").split(":");
			
			for(int i = 0; i < effects.length; i++) {
				String[] effect = effects[i].split(";");
				PotionType pName = Enum.valueOf(PotionType.class, effect[0]);
				PotionItem.applyPotion(player, pName, Integer.parseInt(effect[1]));
			}
		}
		
		if(worldVer.compareTo(new Version("1.9.4-dev4")) < 0) {
			String colors = data.get(11).replace("[", "").replace("]", "");
			String[] color = colors.split(";");
			int[] cols = new int[color.length];
			for(int i = 0; i < cols.length; i++)
				cols[i] = Integer.valueOf(color[i])/50;
			String col = ""+cols[0]+cols[1]+cols[2];
			System.out.println("getting color as " + col);
			player.shirtColor = Integer.parseInt(col);
		}
		else
			player.shirtColor = Integer.parseInt(data.get(11));
		
		player.skinon = Boolean.parseBoolean(data.get(12));
	}
	
	protected static String subOldName(String name, Version worldVer) {
		if(worldVer.compareTo(new Version("1.9.4-dev4")) < 0) {
			name = name.replace("Hatchet", "Axe").replace("Pick", "Pickaxe").replace("Pickaxeaxe", "Pickaxe").replace("Spade", "Shovel").replace("Pow glove", "Power Glove").replace("II", "").replace("W.Bucket", "Water Bucket").replace("L.Bucket", "Lava Bucket").replace("G.Apple", "Gold Apple").replace("St.", "Stone").replace("Ob.", "Obsidian").replace("I.Lantern", "Iron Lantern").replace("G.Lantern", "Gold Lantern").replace("BrickWall", "Wall").replace("Brick", " Brick").replace("Wall", " Wall").replace("  ", " ");
			if(name.equals("Bucket"))
				name = "Empty Bucket";
		}
		
		if(worldVer.compareTo(new Version("1.9.4")) < 0) {
			name = name.replace("I.Armor", "Iron Armor").replace("S.Armor", "Snake Armor").replace("L.Armor", "Leather Armor").replace("G.Armor", "Gold Armor").replace("BrickWall", "Wall");
		}
		
		return name;
	}
	
	public void loadInventory(String filename, Inventory inventory) {
		loadFromFile(location + filename + extention);
		inventory.clearInv();
		
		for(int i = 0; i < data.size(); i++) {
			String item = data.get(i);
			
			if(worldVer.compareTo(new Version("1.9.4")) < 0) {
				item = subOldName(item, worldVer);
			}
			
			//System.out.println("loading item: " + item);
			
			if(item.contains(";")) {
				List<String> curData = Arrays.asList(item.split(";"));
				String itemName = curData.get(0);
				
				Item newItem = Items.get(itemName);
				
				int count = Integer.parseInt(curData.get(1));
				
				if(newItem instanceof StackableItem) {
					((StackableItem)newItem).count = count;
					inventory.add(newItem);
				}
				else
					inventory.add(newItem, count);
			} else {
				Item toAdd = Items.get(item);
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
			if(!Crafter.names.contains(entityName)) {
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
			}
			
			Entity newEntity = getEntity(entityName, player, mobLvl);
			
			if(newEntity != null) { // the method never returns null, but...
				if(newEntity instanceof Mob) {
					Mob mob = (Mob)newEntity;
					mob.health = Integer.parseInt(info.get(2));
					newEntity = mob;
				} else if(newEntity instanceof Chest) {
					Chest chest = (Chest)newEntity;
					boolean isDeathChest = chest instanceof DeathChest;
					boolean isDungeonChest = chest instanceof DungeonChest;
					List<String> chestInfo = info.subList(2, info.size()-1);
					
					int endIdx = chestInfo.size()-(isDeathChest||isDungeonChest?1:0);
					for(int idx = 0; idx < endIdx; idx++) {
						String itemData = chestInfo.get(idx);
						if (itemData.contains(";")) {
							String[] aitemData = (itemData + ";1").split(";"); // this appends ";1" to the end, meaning one item, to everything; but if it was already there, then it becomes the 3rd element in the list, which is ignored.
							if(worldVer.compareTo(new Version("1.9.4")) < 0)
								aitemData[0] = subOldName(aitemData[0], worldVer);
							StackableItem stack = (StackableItem)Items.get(aitemData[0]);
							stack.count = Integer.parseInt(aitemData[1]);
							chest.inventory.add(stack);
						} else {
							if(worldVer.compareTo(new Version("1.9.4-dev4")) < 0)
								itemData = subOldName(itemData, worldVer);
							Item item = Items.get(itemData);
							chest.inventory.add(item);
						}
					}
					
					if (isDeathChest) {
						((DeathChest)chest).time = Integer.parseInt(chestInfo.get(chestInfo.size()-1));
					} else if (isDungeonChest) {
						((DungeonChest)chest).isLocked = Boolean.parseBoolean(chestInfo.get(chestInfo.size()-1));
					}
					
					if (chest instanceof DungeonChest)
						Game.levels[Integer.parseInt(info.get(info.size()-1))].chestcount++;
					newEntity = chest;
				}
				else if(newEntity instanceof Spawner) {
					newEntity = new Spawner((MobAi)getEntity(info.get(2), player, Integer.parseInt(info.get(3))));
					//egg.initMob((MobAi)getEntity(info.get(2), player, info.get(3)));
					//egg.lvl = Integer.parseInt(info.get(3));
					//newEntity = egg;
				}
				else if(newEntity instanceof Lantern && worldVer.compareTo(new Version("1.9.4")) >= 0 && info.size() > 3) {
					newEntity = new Lantern(Lantern.Type.values()[Integer.parseInt(info.get(2))]);
				}
				/*else if(newEntity instanceof Crafter && worldVer.compareTo(new Version("2.0.0-dev4")) >= 0) {
					System.out.println("");
					newEntity = new Crafter(Enum.valueOf(Crafter.Type.class, info.get(2)));
				}*/
				
				int currentlevel = Integer.parseInt(info.get(info.size()-1));
				Game.levels[currentlevel].add(newEntity, x, y);
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
			case "Spawner": return (Entity)(new Spawner(new Zombie(1)));
			case "Workbench": return (Entity)(new Crafter(Crafter.Type.Workbench));
			case "Chest": return (Entity)(new Chest());
			case "DeathChest": return (Entity)(new DeathChest());
			case "DungeonChest": return (Entity)(new DungeonChest());
			case "Anvil": return (Entity)(new Crafter(Crafter.Type.Anvil));
			case "Enchanter": return (Entity)(new Crafter(Crafter.Type.Enchanter));
			case "Loom": return (Entity)(new Crafter(Crafter.Type.Loom));
			case "Furnace": return (Entity)(new Crafter(Crafter.Type.Furnace));
			case "Oven": return (Entity)(new Crafter(Crafter.Type.Oven));
			case "Bed": return (Entity)(new Bed());
			case "Tnt": return (Entity)(new Tnt());
			case "Lantern": return (Entity)(new Lantern(Lantern.Type.NORM));
			//case "Iron Lantern": return (Entity)(new Lantern(Lantern.Type.IRON));
			//case "Gold Lantern": return (Entity)(new Lantern(Lantern.Type.GOLD));
			//case "Spark": return (Entity)(new Spark());
			default : /*if(Game.debug)*/ System.out.println("LOAD: unknown or outdated entity requested: " + string);
				return null;
		}
	}
}
