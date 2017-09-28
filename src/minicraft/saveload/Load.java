package minicraft.saveload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minicraft.Game;
import minicraft.Settings;
import minicraft.entity.*;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.item.ArmorItem;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.network.MinicraftServer;
import minicraft.screen2.LoadingDisplay;
import minicraft.screen2.MultiplayerMenu;

public class Load {
	
	String location = Game.gameDir;
	File folder;
	
	private static String extension = Save.extension;
	private float percentInc;
	
	ArrayList<String> data;
	ArrayList<String> extradata;
	
	public boolean hasloadedbigworldalready;
	Version currentVer, worldVer;
	boolean oldSave = false, hasGlobalPrefs = false;
	
	{
		currentVer = new Version(Game.VERSION);
		worldVer = null;
		
		File testFile = new File(location + "/Preferences" + extension);
		hasGlobalPrefs = testFile.exists();
		
		data = new ArrayList<>();
		extradata = new ArrayList<>();
		hasloadedbigworldalready = false;
	}
	
	public Load(String worldname) {
		loadFromFile(location + "/saves/" + worldname.toLowerCase() + "/Game" + extension);
		if(data.get(0).contains(".")) worldVer = new Version(data.get(0));
		if(worldVer == null) worldVer = new Version("1.8");
		
		if(!hasGlobalPrefs)
			hasGlobalPrefs = worldVer.compareTo(new Version("1.9.2")) >= 0;
		
		if(worldVer.compareTo(new Version("1.9.2")) < 0)
			new LegacyLoad(worldname);
		else {
			location += "/saves/" + worldname + "/";
			
			percentInc = 5 + Game.levels.length-1; // for the methods below, and world.
			// for entities...
			/*int nument = 0;
			for(Level level: Game.levels)
				if(level)
					nument += level.getEntityArray().length;
			percentInc += nument;*/
			percentInc = 100f / percentInc;
			
			LoadingDisplay.setPercentage(0);
			loadGame("Game"); // more of the version will be determined here
			loadWorld("Level");
			loadEntities("Entities");
			loadInventory("Inventory", Game.player.inventory);
			loadPlayer("Player", Game.player);
			if(Game.isMode("creative")) {
				Items.fillCreativeInv(Game.player.inventory, false);
			}
			//LoadingDisplay.setPercentage(0); // reset
		}
	}
	
	public Load(String worldname, MinicraftServer server) {
		location += "/saves/"+worldname.toLowerCase()+"/";
		File testFile = new File(location + "ServerConfig" + extension);
		if(testFile.exists())
			loadServerConfig("ServerConfig", server);
	}
	
	public Load() { this(false); }
	public Load(boolean loadStuff) {
		if(!loadStuff) {
			worldVer = currentVer;
			return;
		}
		
		location += "/";
		
		if(hasGlobalPrefs)
			loadPrefs("Preferences");
		else
			new Save();
		
		File testFileOld = new File(location+"unlocks"+extension);
		File testFile = new File(location+"Unlocks"+extension);
		if (testFileOld.exists() && !testFile.exists()) {
			testFileOld.renameTo(testFile);
			//testFileOld = new File(location+"unlocks"+extension);
			//testFileOld.delete()
			new LegacyLoad(testFile);
		}
		else if(!testFile.exists()) {
			try {
				testFile.createNewFile();
			} catch(IOException ex) {
				System.err.println("could not create Unlocks."+extension+":");
				ex.printStackTrace();
			}
		}
		//if(testFileOld.exists())
		//testFile = new File(location+"Unlocks"+extension);
		/*if(!testFile.exists()) {
			try {
				testFile.createNewFile();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}*/
		
		loadUnlocks("Unlocks");
	}
	
	/*public Load() {	
		worldVer = currentVer;
	}*/
	
	public static class Version implements Comparable {
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
				System.out.println("INVALID version number: \"" + version + "\"");
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		// the returned value of this method (-1, 0, or 1) is determined by whether this object is less than, equal to, or greater than the specified object.
		public int compareTo(Object other) throws NullPointerException, ClassCastException {
			if(other == null) throw new NullPointerException();
			if(!(other instanceof Version)) { // if the passed object is not a Version...
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
	
	public static ArrayList<String> loadFile(String filename) throws IOException {
		ArrayList<String> lines = new ArrayList<>();
		
		InputStream fileStream = Load.class.getResourceAsStream(filename);
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fileStream))) {
			
			String line;
			while((line = br.readLine()) != null)
				lines.add(line);
			
		}
		
		return lines;//.toArray(new String[lines.size()]);
	}
	
	public void loadFromFile(String filename) {
		data.clear();
		extradata.clear();
		
		String total;
		try {
			total = loadFromFile(filename, true);
			data.addAll(Arrays.asList(total.split(",")));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		if(filename.contains("Level")) {
			try {
				total = Load.loadFromFile(filename.substring(0, filename.lastIndexOf("/") + 7) + "data" + extension, true);
				extradata.addAll(Arrays.asList(total.split(",")));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		LoadingDisplay.progress(percentInc);
		/*if(LoadingDisplay.getPercentage() > 100) {
			LoadingDisplay.setPercentage(100);
		}*/
	}
	
	public static String loadFromFile(String filename, boolean isWorldSave) throws IOException {
		StringBuilder total = new StringBuilder();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			
			String curLine;
			//ArrayList<String> curData;
			while((curLine = br.readLine()) != null)
				total.append(curLine).append(isWorldSave ? "" : "\n");
			
			/*if(worldVer != null && worldVer.compareTo(new Version("1.9.4-dev6")) >= 0 && filename.contains("Level") && !filename.contains("Data")) {
				total = new String(Base64.getDecoder().decode(total));
			}*/
			
		}/* catch (IOException ex) {
			*//*if(br != null) {
				br.close();
			}*//*
			throw ex;
		}*//* finally {
			try {
				
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}*/
		
		return total.toString();
	}
	
	public void loadUnlocks(String filename) {
		loadFromFile(location + filename + extension);
		
		//ModeMenu.unlockedtimes.clear();
		//SettingEntry<Integer> scoreTimes = (SettingEntry<Integer>) Displays.worldGen.getEntry("scoreTime");
		// TODO fix this
		
		//BooleanEntry skinUnlocked = (BooleanEntry) Displays.options.getEntry("unlockedskin"); 
		//skinUnlocked.setValue(Boolean.FALSE);
		Settings.set("unlockedskin", false);
		//Settings.set("wear suit", false;)
		
		for(String unlock: data) {
			if(unlock.equals("AirSkin"))
				Settings.set("unlockedskin", true);
			
			unlock = unlock.replace("HOURMODE", "H_ScoreTime").replace("MINUTEMODE", "M_ScoreTime");
			
//			if(unlock.contains("_ScoreTime"))
//				ModeMenu.unlockedtimes.add(unlock.substring(0, unlock.indexOf("_")));
		}
		
		//ModeMenu.initTimeList();
	}
	
	public void loadGame(String filename) {
		loadFromFile(location + filename + extension);
		
		worldVer = new Version(data.get(0)); // gets the world version
		Game.setTime(Integer.parseInt(data.get(1)));
		
		if(worldVer.compareTo(new Version("1.9.3-dev2")) >= 0) {
			Game.gameTime = Integer.parseInt(data.get(2));
			Game.pastDay1 = Game.gameTime > 65000;
		} else {
			Game.gameTime = 65000; // prevents time cheating.
		}
		
		int diffIdx = Integer.parseInt(data.get(3));
		if(worldVer.compareTo(new Version("1.9.3-dev3")) < 0)
			diffIdx--; // account for change in difficulty
		
		Settings.setIdx("diff", diffIdx);
		
		AirWizard.beaten = Boolean.parseBoolean(data.get(4));
	}
	
	public void loadPrefs(String filename) {
		loadFromFile(location + filename + extension);
		
		Version prefVer = new Version("2.0.2"); // the default, b/c this doesn't really matter much being specific past this if it's not set below.
		
		if(!data.get(2).contains(";")) // signifies that this file was last written to by a version after 2.0.2.
			prefVer = new Version(data.remove(0));
		
		Settings.set("sound", Boolean.parseBoolean(data.get(0)));
		Settings.set("autosave", Boolean.parseBoolean(data.get(1)));
		
		List<String> subdata;
		
		if(prefVer.compareTo(new Version("2.0.3-dev1")) < 0) {
			subdata = data.subList(2, data.size());
		} else {
			MultiplayerMenu.savedIP = data.get(2);
			if(prefVer.compareTo(new Version("2.0.3-dev3")) > 0) {
				MultiplayerMenu.savedUUID = data.remove(3);
				MultiplayerMenu.savedUsername = data.remove(3);
			}
			String keyData = data.get(3);
			subdata = Arrays.asList(keyData.split(":"));
		}
		
		for (String keymap : subdata) {
			String[] map = keymap.split(";");
			Game.input.setKey(map[0], map[1]);
		}
	}
	
	private void loadServerConfig(String filename, MinicraftServer server) {
		loadFromFile(location + filename + extension);
		
		server.setPlayerCap(Integer.parseInt(data.get(0)));
	}
	
	public void loadWorld(String filename) {
		for(int l = Game.maxLevelDepth; l >= Game.minLevelDepth; l--) {
			//if(l == Game.levels.length-1) l = 4;
			//if(l == 0) l = Game.levels.length-1;
			int lvlidx = Game.lvlIdx(l);
			loadFromFile(location + filename + lvlidx + extension);
			
			int lvlw = Integer.parseInt(data.get(0));
			int lvlh = Integer.parseInt(data.get(1));
			//int lvldepth = Integer.parseInt(data.get(2));
			
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
					if(l == Game.minLevelDepth+1 && tilename.equalsIgnoreCase("LAPIS") && worldVer.compareTo(new Version("2.0.3-dev6")) < 0) {
						if(Math.random() < 0.8) // don't replace *all* the lapis
							tilename = "Gem Ore";
					}
					tiles[tileArrIdx] = Tiles.get(tilename).id;
					tdata[tileArrIdx] = Byte.parseByte(extradata.get(tileidx));
				}
			}
			
			Level parent = Game.levels[Game.lvlIdx(l+1)];
			Game.levels[lvlidx] = new Level(lvlw, lvlh, l, parent, false);
			
			Level curLevel = Game.levels[lvlidx];
			curLevel.tiles = tiles;
			curLevel.data = tdata;
			
			if(Game.debug) {
				//System.out.println("level depth=" + curLevel.depth + " -- parent depth=" + (parent==null?"null":parent.depth));
				
				curLevel.printTileLocs(Tiles.get("Stairs Down"));
			}
			
			//LoadingDisplay.progress(percentInc);
			
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
			//if(l == 0) l = Game.levels.length;
			//if(l == Game.levels.length-1) l = 0;
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
				//LoadingDisplay.progress(percentInc);
			}
		} catch (IndexOutOfBoundsException ex) {
			System.err.println("suspected: level id and data arrays do not have enough info for given world size.");
			ex.printStackTrace();
		}
		
		level.tiles = tiles;
		level.data = tdata;
	}
	
	public void loadPlayer(String filename, Player player) {
		loadFromFile(location + filename + extension);
		loadPlayer(player, data);
	}
	public void loadPlayer(Player player, List<String> data) {
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
		if(worldVer.compareTo(new Version("2.0.1-dev1")) < 0)
			player.inventory.add(Items.get("arrow"), Integer.parseInt(data.get(7)));
		
		Game.currentLevel = Integer.parseInt(data.get(8));
		Level level = Game.levels[Game.currentLevel];
		if(Game.player != null)
			Game.player.remove(); // removes the user player from the level, in case they would be added twice.
		if(level != null)
			level.add(player);
		else if(Game.debug) System.out.println(Game.onlinePrefix()+"game level to add player " + player + " to is null.");
		//Tile spawnTile = level.getTile(player.spawnx >> 4, player.spawny >> 4);
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
					Settings.set("scoretime", modeinfo[2]);
			}
		}
		else {
			mode = Integer.parseInt(modedata);
			if (mode == 4) Game.scoreTime = 300;
		}
		
		Settings.setIdx("mode", mode);
		
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
		loadFromFile(location + filename + extension);
		loadInventory(inventory, data);
	}
	public void loadInventory(Inventory inventory, List<String> data) {
		inventory.clearInv();
		
		for(int i = 0; i < data.size(); i++) {
			String item = data.get(i);
			
			if(worldVer.compareTo(new Version("1.9.4")) < 0) {
				item = subOldName(item, worldVer);
			}
			
			if (item.contains("Power Glove")) continue; // just pretend it doesn't exist. Because it doesn't. :P
			
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
	
	public void loadEntities(String filename) {
		loadFromFile(location + filename + extension);
		
		for(int i = 0; i < Game.levels.length; i++) {
			Game.levels[i].clearEntities();
		}
		
		for(int i = 0; i < data.size(); i++) {
			loadEntity(data.get(i), worldVer, true);
			//LoadingDisplay.progress(percentInc);
		}
	}
	
	public static Entity loadEntity(String entityData, boolean isLocalSave) {
		if(isLocalSave) System.out.println("warning: assuming version of save file is current while loading entity: " + entityData);
		return Load.loadEntity(entityData, (new Version(Game.VERSION)), isLocalSave);
	}
	public static Entity loadEntity(String entityData, Version worldVer, boolean isLocalSave) {
		entityData = entityData.trim();
		if(entityData.length() == 0) return null;
		
		List<String> info = new ArrayList<>(); // this gets everything inside the "[...]" after the entity name.
		//System.out.println("loading entity:" + entityData);
		String[] stuff = entityData.substring(entityData.indexOf("[") + 1, entityData.indexOf("]")).split(":");
		info.addAll(Arrays.asList(stuff));
		
		String entityName = entityData.substring(0, entityData.indexOf("[")); // this gets the text before "[", which is the entity name.
		
		if(entityName.equals("Player") && Game.debug && Game.isValidClient())
			System.out.println("CLIENT note: loading regular player");
			//return;// null;
		
		int x = Integer.parseInt(info.get(0));
		int y = Integer.parseInt(info.get(1));
		
		int eid = -1;
		if(!isLocalSave) {
			eid = Integer.parseInt(info.remove(2));
			
			/// If I find an entity that is loaded locally, but on another level in the entity data provided, then I ditch the current entity and make a new one from the info provided.
			Entity existing = Game.getEntity(eid);
			int entityLevel = Integer.parseInt(info.get(info.size()-1));
			
			if(existing != null) {
				// the entity loaded is now out of date; remove it.
				if(/*existing instanceof Player && */Game.debug)
					System.out.println(Game.onlinePrefix()+"received entity data matches a loaded entity: " + existing + "; removing from level " + existing.getLevel());
				
				existing.remove();
			}
			
			/*if(existing == null && Game.isValidClient() && Game.player.eid == eid) {
				existing = Game.player;
				//int playerLevel = Integer.parseInt(info.get(info.size()-1));
				//if(Game.levels[playerLevel] != null)
				//Game.levels[playerLevel].add(existing, x, y);
			}
			if(existing != null) {
				System.out.println(Game.onlinePrefix()+"already loaded entity with eid " + eid + "; returning that one");
				return existing;
			}*/
			
			if(Game.isValidClient() && Game.player instanceof RemotePlayer && 
				!((RemotePlayer)Game.player).shouldTrack(x >> 4, y >> 4, Game.levels[entityLevel])
				) {
				// the entity is too far away to bother adding to the level.
				if(Game.debug) System.out.println("CLIENT: entity is too far away to bother loading: " + eid);
				Entity dummy = new Cow();
				dummy.eid = eid;
				return dummy; /// we need a dummy b/c it's the only way to pass along to entity id.
			}
			
			if(Game.isValidClient() && existing != null && existing.eid == Game.player.eid) {
				System.out.println("CLIENT WARNING: asked to reload main player from server; ignoring.");
				return Game.player; // don't load the main player
			}
		}
		
		Entity newEntity = null;
		
		if(entityName.equals("RemotePlayer")) {
			if(isLocalSave) {
				System.err.println("remote player found in local save file.");
				return null; // don't load them; in fact, they shouldn't be here.
			}
			String username = info.get(2);
			java.net.InetAddress ip;
			try {
				ip = java.net.InetAddress.getByName(info.get(3));
				int port = Integer.parseInt(info.get(4));
				newEntity = new RemotePlayer(null, ip, port);
				((RemotePlayer)newEntity).setUsername(username);
				//rp.eid = eid;
				if(Game.debug) System.out.println("Prob CLIENT: Loaded remote player");
				//return rp;
			} catch(java.net.UnknownHostException ex) {
				System.err.println("LOAD could not read ip address of remote player in file.");
				ex.printStackTrace();
			}
			//return null;
		}
		else if(entityName.equals("Spark") && !isLocalSave) {
			int awID = Integer.parseInt(info.get(2));
			Entity sparkOwner = Game.getEntity(awID);
			if(sparkOwner != null && sparkOwner instanceof AirWizard)
				newEntity = new Spark((AirWizard)sparkOwner, x, y);
			else {
				System.err.println("failed to load spark; owner id doesn't point to a correct entity");
				return null;
			}
		}
		else {
			int mobLvl = 1;
			Class c = null;
			if(!Crafter.names.contains(entityName)) {
				try {
					c = Class.forName("minicraft.entity."+entityName);
				} catch(ClassNotFoundException ex) {
					ex.printStackTrace();
				}
			}
			if(c != null && EnemyMob.class.isAssignableFrom(c))
				mobLvl = Integer.parseInt(info.get(info.size()-2));
			
			if(mobLvl == 0) {
				if(Game.debug) System.out.println("level 0 mob: " + entityName);
				mobLvl = 1;
			}
			
			newEntity = getEntity(entityName.substring(entityName.lastIndexOf(".")+1), mobLvl);
		}
		
		if(newEntity == null)
			return null;
		
		if(newEntity instanceof Mob && !(newEntity instanceof RemotePlayer)) {
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
				if(worldVer.compareTo(new Version("1.9.4-dev4")) < 0)
					itemData = subOldName(itemData, worldVer);
				
				if(itemData.contains("Power Glove")) continue; // ignore it.
				
				if (itemData.contains(";")) {
					String[] aitemData = itemData.split(";");
					StackableItem stack = (StackableItem)Items.get(aitemData[0]);
					if (stack != null) {
						stack.count = Integer.parseInt(aitemData[1]);
						chest.inventory.add(stack);
					} else {
						System.err.println("LOAD ERROR: encountered invalid item name, expected to be stackable: " + aitemData[0] + "; stack trace:");
						Thread.dumpStack();
					}
				} else {
					Item item = Items.get(itemData);
					chest.inventory.add(item);
				}
			}
			
			if (isDeathChest) {
				((DeathChest)chest).time = Integer.parseInt(chestInfo.get(chestInfo.size()-1));
			} else if (isDungeonChest) {
				((DungeonChest)chest).isLocked = Boolean.parseBoolean(chestInfo.get(chestInfo.size()-1));
				Game.levels[Integer.parseInt(info.get(info.size()-1))].chestcount++;
			}
			
			newEntity = chest;
		}
		else if(newEntity instanceof Spawner)
			newEntity = new Spawner((MobAi)getEntity(info.get(2), Integer.parseInt(info.get(3))));
		else if(newEntity instanceof Lantern && worldVer.compareTo(new Version("1.9.4")) >= 0 && info.size() > 3)
			newEntity = new Lantern(Lantern.Type.values()[Integer.parseInt(info.get(2))]);
		
		/*else if(newEntity instanceof Crafter && worldVer.compareTo(new Version("2.0.0-dev4")) >= 0) {
			System.out.println("");
			newEntity = new Crafter(Enum.valueOf(Crafter.Type.class, info.get(2)));
		}*/
		/*else if(newEntity instanceof Spark && worldVer.compareTo(new Version("2.0.1-dev2")) >= 0) {
			Spark sp = (Spark)newEntity;
			sp.xa = info.get(2);
			sp.ya = info.get(3);
		}*/
		
		if(!isLocalSave) {
			if(newEntity instanceof Arrow) {
				int ownerID = Integer.parseInt(info.get(2));
				int dirx = Integer.parseInt(info.get(3));
				int diry = Integer.parseInt(info.get(4));
				int dmg = Integer.parseInt(info.get(5));
				newEntity = new Arrow((Mob)Game.getEntity(ownerID), x, y, dirx, diry, dmg);
			}
			if(newEntity instanceof ItemEntity) {
				Item item = Items.get(info.get(2));
				double zz = Double.parseDouble(info.get(3));
				int lifetime = Integer.parseInt(info.get(4));
				int timeleft = Integer.parseInt(info.get(5));
				double xa = Double.parseDouble(info.get(6));
				double ya = Double.parseDouble(info.get(7));
				double za = Double.parseDouble(info.get(8));
				newEntity = new ItemEntity(item, x, y, zz, lifetime, timeleft, xa, ya, za);
			}
			if(newEntity instanceof TextParticle) {
				int textcol = Integer.parseInt(info.get(3));
				newEntity = new TextParticle(info.get(2), x, y, textcol);
				//if (Game.debug) System.out.println("loaded text particle; color: "+Color.toString(textcol)+", text: " + info.get(2));
			}
		}
		
		newEntity.eid = eid; // this will be -1 unless set earlier, so a new one will be generated when adding it to the level.
		if(newEntity instanceof ItemEntity && eid == -1)
			System.out.println("Warning: item entity was loaded with no eid");
		
		int curLevel = Integer.parseInt(info.get(info.size()-1));
		if(Game.levels[curLevel] != null) {
			Game.levels[curLevel].add(newEntity, x, y);
			if(Game.debug && newEntity instanceof RemotePlayer)
				Game.levels[curLevel].printEntityStatus("Loaded ", newEntity, "RemotePlayer");
		} else if(newEntity instanceof RemotePlayer && Game.isValidClient())
			System.out.println("CLIENT: remote player not added b/c on null level");
		
		return newEntity;
	}
	
	private static Entity getEntity(String string, int moblvl) {
		switch(string) {
			case "Player": return null;
			case "RemotePlayer": return null;
			case "Cow": return new Cow();
			case "Sheep": return new Sheep();
			case "Pig": return new Pig();
			case "Zombie": return new Zombie(moblvl);
			case "Slime": return new Slime(moblvl);
			case "Creeper": return new Creeper(moblvl);
			case "Skeleton": return new Skeleton(moblvl);
			case "Knight": return new Knight(moblvl);
			case "Snake": return new Snake(moblvl);
			case "AirWizard": return new AirWizard(moblvl>1);
			case "Spawner": return new Spawner(new Zombie(1));
			case "Workbench": return new Crafter(Crafter.Type.Workbench);
			case "Chest": return new Chest();
			case "DeathChest": return new DeathChest();
			case "DungeonChest": return new DungeonChest();
			case "Anvil": return new Crafter(Crafter.Type.Anvil);
			case "Enchanter": return new Crafter(Crafter.Type.Enchanter);
			case "Loom": return new Crafter(Crafter.Type.Loom);
			case "Furnace": return new Crafter(Crafter.Type.Furnace);
			case "Oven": return new Crafter(Crafter.Type.Oven);
			case "Bed": return new Bed();
			case "Tnt": return new Tnt();
			case "Lantern": return new Lantern(Lantern.Type.NORM);
			//case "Iron Lantern": return (Entity)(new Lantern(Lantern.Type.IRON));
			//case "Gold Lantern": return (Entity)(new Lantern(Lantern.Type.GOLD));
			case "Arrow": return new Arrow(null, 0, 0, 0, 0, 0);
			case "ItemEntity": return new ItemEntity(null, 0, 0);
			//case "Spark": return (Entity)(new Spark());
			case "FireParticle": return new FireParticle(0, 0);
			case "SmashParticle": return new SmashParticle(0, 0);
			case "TextParticle": return new TextParticle("", 0, 0, 0);
			default : /*if(Game.debug)*/ System.err.println("LOAD ERROR: unknown or outdated entity requested: " + string);
				return null;
		}
	}
}
