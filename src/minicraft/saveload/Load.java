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

import minicraft.core.Game;
import minicraft.core.Network;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.Arrow;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Spark;
import minicraft.entity.furniture.*;
import minicraft.entity.mob.*;
import minicraft.entity.particle.FireParticle;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.item.*;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.network.MinicraftServer;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.MultiplayerDisplay;

import org.jetbrains.annotations.Nullable;

public class Load {
	
	private String location = Game.gameDir;
	
	private static String extension = Save.extension;
	private float percentInc;
	
	private ArrayList<String> data;
	private ArrayList<String> extradata;
	
	private Version currentVer, worldVer;
	private boolean hasGlobalPrefs = false;
	
	{
		currentVer = new Version(Game.VERSION);
		worldVer = null;
		
		File testFile = new File(location + "/Preferences" + extension);
		hasGlobalPrefs = testFile.exists();
		
		data = new ArrayList<>();
		extradata = new ArrayList<>();
	}
	
	public Load(String worldname) {
		loadFromFile(location + "/saves/" + worldname + "/Game" + extension);
		if(data.get(0).contains(".")) worldVer = new Version(data.get(0));
		if(worldVer == null) worldVer = new Version("1.8");
		
		if(!hasGlobalPrefs)
			hasGlobalPrefs = worldVer.compareTo(new Version("1.9.2")) >= 0;
		
		if(worldVer.compareTo(new Version("1.9.2")) < 0)
			new LegacyLoad(worldname);
		else {
			location += "/saves/" + worldname + "/";
			
			percentInc = 5 + World.levels.length-1; // for the methods below, and world.
			
			percentInc = 100f / percentInc;
			
			LoadingDisplay.setPercentage(0);
			loadGame("Game"); // more of the version will be determined here
			loadWorld("Level");
			loadEntities("Entities");
			loadInventory("Inventory", Game.player.getInventory());
			loadPlayer("Player", Game.player);
			if(Game.isMode("creative"))
				Items.fillCreativeInv(Game.player.getInventory(), false);
		}
	}
	
	public Load(String worldname, MinicraftServer server) {
		location += "/saves/"+worldname+"/";
		File testFile = new File(location + "ServerConfig" + extension);
		if(testFile.exists())
			loadServerConfig("ServerConfig", server);
	}
	
	public Load() { this(new Version(Game.VERSION)); }
	public Load(Version worldVersion) {
		this(false);
		worldVer = worldVersion;
	}
	public Load(boolean loadConfig) {
		if(!loadConfig) {
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
			new LegacyLoad(testFile);
		}
		else if(!testFile.exists()) {
			try {
				testFile.createNewFile();
			} catch(IOException ex) {
				System.err.println("could not create Unlocks"+extension+":");
				ex.printStackTrace();
			}
		}
		
		loadUnlocks("Unlocks");
	}
	
	public Version getWorldVersion() { return worldVer; }
	
	public static ArrayList<String> loadFile(String filename) throws IOException {
		ArrayList<String> lines = new ArrayList<>();
		
		InputStream fileStream = Load.class.getResourceAsStream(filename);
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fileStream))) {
			
			String line;
			while((line = br.readLine()) != null)
				lines.add(line);
			
		}
		
		return lines;
	}
	
	private void loadFromFile(String filename) {
		data.clear();
		extradata.clear();
		
		String total;
		try {
			total = loadFromFile(filename, true);
			if(total.length() > 0)
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
	}
	
	public static String loadFromFile(String filename, boolean isWorldSave) throws IOException {
		StringBuilder total = new StringBuilder();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String curLine;
			while((curLine = br.readLine()) != null)
				total.append(curLine).append(isWorldSave ? "" : "\n");
		}
		
		return total.toString();
	}
	
	private void loadUnlocks(String filename) {
		loadFromFile(location + filename + extension);
		
		for(String unlock: data) {
			if(unlock.equals("AirSkin"))
				Settings.set("unlockedskin", true);
			
			unlock = unlock.replace("HOURMODE", "H_ScoreTime").replace("MINUTEMODE", "M_ScoreTime").replace("M_ScoreTime", "_ScoreTime").replace("2H_ScoreTime", "120_ScoreTime");
			
			if(unlock.contains("_ScoreTime"))
				Settings.getEntry("scoretime").setValueVisibility(Integer.parseInt(unlock.substring(0, unlock.indexOf("_"))), true);
		}
	}
	
	private void loadGame(String filename) {
		loadFromFile(location + filename + extension);
		
		worldVer = new Version(data.remove(0)); // gets the world version
		if(worldVer.compareTo(new Version("2.0.4-dev8")) >= 0)
			loadMode(data.remove(0));
		
		Updater.setTime(Integer.parseInt(data.remove(0)));
		
		Updater.gameTime = Integer.parseInt(data.remove(0));
		if(worldVer.compareTo(new Version("1.9.3-dev2")) >= 0) {
			Updater.pastDay1 = Updater.gameTime > 65000;
		} else {
			Updater.gameTime = 65000; // prevents time cheating.
		}
		
		int diffIdx = Integer.parseInt(data.remove(0));
		if(worldVer.compareTo(new Version("1.9.3-dev3")) < 0)
			diffIdx--; // account for change in difficulty
		
		Settings.setIdx("diff", diffIdx);
		
		AirWizard.beaten = Boolean.parseBoolean(data.remove(0));
	}
	
	private void loadMode(String modedata) {
		int mode;
		if(modedata.contains(";")) {
			String[] modeinfo = modedata.split(";");
			mode = Integer.parseInt(modeinfo[0]);
			if(worldVer.compareTo(new Version("2.0.3")) <= 0)
				mode--; // we changed the min mode idx from 1 to 0.
			if(mode == 3) {
				Updater.scoreTime = Integer.parseInt(modeinfo[1]);
				if(worldVer.compareTo(new Version("1.9.4")) >= 0)
					Settings.set("scoretime", modeinfo[2]);
			}
		} else {
			mode = Integer.parseInt(modedata);
			if(worldVer.compareTo(new Version("2.0.3")) <= 0)
				mode--; // we changed the min mode idx from 1 to 0.
			
			if(mode == 3) Updater.scoreTime = 300;
		}
		
		Settings.setIdx("mode", mode);
	}
	
	private void loadPrefs(String filename) {
		loadFromFile(location + filename + extension);
		
		Version prefVer = new Version("2.0.2"); // the default, b/c this doesn't really matter much being specific past this if it's not set below.
		
		// TODO reformat the preferences file so that it uses key-value pairs. or json. JSON would be good.
		// TODO then, allow multiple saved accounts.
		// TODO do both of these in the same version (likely 2.0.5-dev1) because I also want to Make another iteration of LegacyLoad.
		
		if(!data.get(2).contains(";")) // signifies that this file was last written to by a version after 2.0.2.
			prefVer = new Version(data.remove(0));
		
		Settings.set("sound", Boolean.parseBoolean(data.remove(0)));
		Settings.set("autosave", Boolean.parseBoolean(data.remove(0)));
		
		if(prefVer.compareTo(new Version("2.0.4-dev2")) >= 0)
			Settings.set("fps", Integer.parseInt(data.remove(0)));
		
		List<String> subdata;
		
		if(prefVer.compareTo(new Version("2.0.3-dev1")) < 0) {
			subdata = data;
		} else {
			MultiplayerDisplay.savedIP = data.remove(0);
			if(prefVer.compareTo(new Version("2.0.3-dev3")) > 0) {
				MultiplayerDisplay.savedUUID = data.remove(0);
				MultiplayerDisplay.savedUsername = data.remove(0);
			}
			
			if(prefVer.compareTo(new Version("2.0.4-dev3")) >= 0) {
				String lang = data.remove(0);
				Settings.set("language", lang);
				Localization.changeLanguage(lang);
			}
			
			String keyData = data.get(0);
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
	
	private void loadWorld(String filename) {
		for(int l = World.maxLevelDepth; l >= World.minLevelDepth; l--) {
			int lvlidx = World.lvlIdx(l);
			loadFromFile(location + filename + lvlidx + extension);
			
			int lvlw = Integer.parseInt(data.get(0));
			int lvlh = Integer.parseInt(data.get(1));
			
			byte[] tiles = new byte[lvlw * lvlh];
			byte[] tdata = new byte[lvlw * lvlh];
			
			for(int x = 0; x < lvlw; x++) {
				for(int y = 0; y < lvlh; y++) {
					int tileArrIdx = y + x * lvlw;
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
					if(l == World.minLevelDepth+1 && tilename.equalsIgnoreCase("LAPIS") && worldVer.compareTo(new Version("2.0.3-dev6")) < 0) {
						if(Math.random() < 0.8) // don't replace *all* the lapis
							tilename = "Gem Ore";
					}
					tiles[tileArrIdx] = Tiles.get(tilename).id;
					tdata[tileArrIdx] = Byte.parseByte(extradata.get(tileidx));
				}
			}
			
			Level parent = World.levels[World.lvlIdx(l+1)];
			World.levels[lvlidx] = new Level(lvlw, lvlh, l, parent, false);
			
			Level curLevel = World.levels[lvlidx];
			curLevel.tiles = tiles;
			curLevel.data = tdata;
			
			if(Game.debug) curLevel.printTileLocs(Tiles.get("Stairs Down"));
			
			if(parent == null) continue;
			/// confirm that there are stairs in all the places that should have stairs.
			for(minicraft.gfx.Point p: parent.getMatchingTiles(Tiles.get("Stairs Down"))) {
				if(curLevel.getTile(p.x, p.y) != Tiles.get("Stairs Up")) {
					curLevel.printLevelLoc("INCONSISTENT STAIRS detected; placing stairsUp", p.x, p.y);
					curLevel.setTile(p.x, p.y, Tiles.get("Stairs Up"));
				}
			}
			for(minicraft.gfx.Point p: curLevel.getMatchingTiles(Tiles.get("Stairs Up"))) {
				if(parent.getTile(p.x, p.y) != Tiles.get("Stairs Down")) {
					parent.printLevelLoc("INCONSISTENT STAIRS detected; placing stairsDown", p.x, p.y);
					parent.setTile(p.x, p.y, Tiles.get("Stairs Down"));
				}
			}
		}
	}
	
	public void loadPlayer(String filename, Player player) {
		loadFromFile(location + filename + extension);
		loadPlayer(player, data);
	}
	public void loadPlayer(Player player, List<String> origData) {
		List<String> data = new ArrayList<>(origData);
		player.x = Integer.parseInt(data.remove(0));
		player.y = Integer.parseInt(data.remove(0));
		player.spawnx = Integer.parseInt(data.remove(0));
		player.spawny = Integer.parseInt(data.remove(0));
		player.health = Integer.parseInt(data.remove(0));
		if(worldVer.compareTo(new Version("2.0.4-dev7")) >= 0)
			player.hunger = Integer.parseInt(data.remove(0));
		player.armor = Integer.parseInt(data.remove(0));
		
		if(player.armor > 0) {
			if(worldVer.compareTo(new Version("2.0.4-dev7")) < 0) {
				player.armorDamageBuffer = Integer.parseInt(data.remove(data.size()-1));
				player.curArmor = (ArmorItem) Items.get(data.remove(data.size()-1));
			}
			else {
				player.armorDamageBuffer = Integer.parseInt(data.remove(0));
				player.curArmor = (ArmorItem) Items.get(data.remove(0));
			}
		}
		player.score = Integer.parseInt(data.remove(0));
		
		if(worldVer.compareTo(new Version("2.0.4-dev7")) < 0) {
			int arrowCount = Integer.parseInt(data.remove(0));
			if(worldVer.compareTo(new Version("2.0.1-dev1")) < 0)
				player.getInventory().add(Items.get("arrow"), arrowCount);
		}
		
		Game.currentLevel = Integer.parseInt(data.remove(0));
		Level level = World.levels[Game.currentLevel];
		if(!player.isRemoved()) player.remove(); // removes the user player from the level, in case they would be added twice.
		if(level != null && !(Game.isValidServer() && player == Game.player))
			level.add(player);
		else if(Game.debug) System.out.println(Network.onlinePrefix()+"game level to add player " + player + " to is null.");
		
		if(worldVer.compareTo(new Version("2.0.4-dev8")) < 0) {
			String modedata = data.remove(0);
			if(player == Game.player)
				loadMode(modedata); // only load if you're loading the main player
		}
		
		String potioneffects = data.remove(0);
		if(!potioneffects.equals("PotionEffects[]")) {
			String[] effects = potioneffects.replace("PotionEffects[", "").replace("]", "").split(":");
			
			for(int i = 0; i < effects.length; i++) {
				String[] effect = effects[i].split(";");
				PotionType pName = Enum.valueOf(PotionType.class, effect[0]);
				PotionItem.applyPotion(player, pName, Integer.parseInt(effect[1]));
			}
		}
		
		if(worldVer.compareTo(new Version("1.9.4-dev4")) < 0) {
			String colors = data.remove(0).replace("[", "").replace("]", "");
			String[] color = colors.split(";");
			int[] cols = new int[color.length];
			for(int i = 0; i < cols.length; i++)
				cols[i] = Integer.valueOf(color[i])/50;
			String col = ""+cols[0]+cols[1]+cols[2];
			System.out.println("getting color as " + col);
			player.shirtColor = Integer.parseInt(col);
		}
		else
			player.shirtColor = Integer.parseInt(data.remove(0));
		
		player.skinon = Boolean.parseBoolean(data.remove(0));
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
			if(item.length() == 0) {
				System.err.println("loadInventory: item in data list is \"\", skipping item");
				continue;
			}
			
			if(worldVer.compareTo(new Version("1.9.4")) < 0) {
				item = subOldName(item, worldVer);
			}
			
			if (item.contains("Power Glove")) continue; // just pretend it doesn't exist. Because it doesn't. :P
			
			//System.out.println("loading item: " + item);
			
			if(item.contains(";")) {
				String[] curData = item.split(";");
				String itemName = curData[0];
				
				Item newItem = Items.get(itemName);
				
				int count = Integer.parseInt(curData[1]);
				
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
	
	private void loadEntities(String filename) {
		loadFromFile(location + filename + extension);
		
		for(int i = 0; i < World.levels.length; i++) {
			World.levels[i].clearEntities();
		}
		for(int i = 0; i < data.size(); i++) {
			if(data.get(i).equalsIgnoreCase("player")) continue;
			loadEntity(data.get(i), worldVer, true);
		}
		
		for(int i = 0; i < World.levels.length; i++) {
			World.levels[i].checkChestCount();
			World.levels[i].checkAirWizard();
		}
	}
	
	@Nullable
	public static Entity loadEntity(String entityData, boolean isLocalSave) {
		if(isLocalSave) System.out.println("warning: assuming version of save file is current while loading entity: " + entityData);
		return Load.loadEntity(entityData, (new Version(Game.VERSION)), isLocalSave);
	}
	@Nullable
	public static Entity loadEntity(String entityData, Version worldVer, boolean isLocalSave) {
		entityData = entityData.trim();
		if(entityData.length() == 0) return null;
		
		List<String> info = new ArrayList<>(); // this gets everything inside the "[...]" after the entity name.
		//System.out.println("loading entity:" + entityData);
		String[] stuff = entityData.substring(entityData.indexOf("[") + 1, entityData.indexOf("]")).split(":");
		info.addAll(Arrays.asList(stuff));
		
		String entityName = entityData.substring(0, entityData.indexOf("[")); // this gets the text before "[", which is the entity name.
		
		if(entityName.equals("Player") && Game.debug && Game.isValidClient())
			System.out.println("CLIENT WARNING: loading regular player: " + entityData);
		
		int x = Integer.parseInt(info.get(0));
		int y = Integer.parseInt(info.get(1));
		
		int eid = -1;
		if(!isLocalSave) {
			eid = Integer.parseInt(info.remove(2));
			
			/// If I find an entity that is loaded locally, but on another level in the entity data provided, then I ditch the current entity and make a new one from the info provided.
			Entity existing = Network.getEntity(eid);
			int entityLevel = Integer.parseInt(info.get(info.size()-1));
			
			if(existing != null) {
				// existing one is out of date; replace it.
				existing.remove();
				Game.levels[Game.currentLevel].add(existing);
				return null;
			}
			
			if(Game.isValidClient()) {
				if(eid == Game.player.eid)
					return Game.player; // don't reload the main player via an entity addition, though do add it to the level (will be done elsewhere)
				if(Game.player instanceof RemotePlayer &&
					!((RemotePlayer)Game.player).shouldTrack(x >> 4, y >> 4, World.levels[entityLevel])
				) {
					// the entity is too far away to bother adding to the level.
					if(Game.debug) System.out.println("CLIENT: entity is too far away to bother loading: " + eid);
					Entity dummy = new Cow();
					dummy.eid = eid;
					return dummy; /// we need a dummy b/c it's the only way to pass along to entity id.
				}
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
				if(Game.debug) System.out.println("Prob CLIENT: Loaded remote player");
			} catch(java.net.UnknownHostException ex) {
				System.err.println("LOAD could not read ip address of remote player in file.");
				ex.printStackTrace();
			}
		}
		else if(entityName.equals("Spark") && !isLocalSave) {
			int awID = Integer.parseInt(info.get(2));
			Entity sparkOwner = Network.getEntity(awID);
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
					c = Class.forName("minicraft.entity.mob."+entityName);
				} catch(ClassNotFoundException ignored) {}
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
					if (!(stack instanceof UnknownItem)) {
						stack.count = Integer.parseInt(aitemData[1]);
						chest.getInventory().add(stack);
					} else {
						System.err.println("LOAD ERROR: encountered invalid item name, expected to be stackable: " + aitemData[0] + "; stack trace:");
						Thread.dumpStack();
					}
				} else {
					Item item = Items.get(itemData);
					chest.getInventory().add(item);
				}
			}
			
			if (isDeathChest) {
				((DeathChest)chest).time = Integer.parseInt(chestInfo.get(chestInfo.size()-1));
			} else if (isDungeonChest) {
				((DungeonChest)chest).isLocked = Boolean.parseBoolean(chestInfo.get(chestInfo.size()-1));
				if(((DungeonChest)chest).isLocked) World.levels[Integer.parseInt(info.get(info.size()-1))].chestCount++;
			}
			
			newEntity = chest;
		}
		else if(newEntity instanceof Spawner) {
			MobAi mob = (MobAi) getEntity(info.get(2).substring(info.get(2).lastIndexOf(".")+1), Integer.parseInt(info.get(3)));
			if(mob != null)
				newEntity = new Spawner(mob);
		} else if(newEntity instanceof Lantern && worldVer.compareTo(new Version("1.9.4")) >= 0 && info.size() > 3)
			newEntity = new Lantern(Lantern.Type.values()[Integer.parseInt(info.get(2))]);
		
		if(!isLocalSave) {
			if(newEntity instanceof Arrow) {
				int ownerID = Integer.parseInt(info.get(2));
				Mob m = (Mob)Network.getEntity(ownerID);
				if(m != null) {
					Direction dir = Direction.values[Integer.parseInt(info.get(3))];
					int dmg = Integer.parseInt(info.get(5));
					newEntity = new Arrow(m, x, y, dir, dmg);
				}
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
		if(World.levels[curLevel] != null) {
			World.levels[curLevel].add(newEntity, x, y);
			if(Game.debug && newEntity instanceof RemotePlayer)
				World.levels[curLevel].printEntityStatus("Loaded ", newEntity, "mob.RemotePlayer");
		} else if(newEntity instanceof RemotePlayer && Game.isValidClient())
			System.out.println("CLIENT: remote player not added b/c on null level");
		
		return newEntity;
	}
	
	@Nullable
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
			case "Arrow": return new Arrow(new Skeleton(0), 0, 0, Direction.NONE, 0);
			case "ItemEntity": return new ItemEntity(Items.get("unknown"), 0, 0);
			//case "Spark": return (Entity)(new Spark());
			case "FireParticle": return new FireParticle(0, 0);
			case "SmashParticle": return new SmashParticle(0, 0);
			case "TextParticle": return new TextParticle("", 0, 0, 0);
			default : /*if(Game.debug)*/ System.err.println("LOAD ERROR: unknown or outdated entity requested: " + string);
				return null;
		}
	}
}
