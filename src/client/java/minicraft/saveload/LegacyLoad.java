package minicraft.saveload;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Bed;
import minicraft.entity.furniture.Chest;
import minicraft.entity.furniture.Crafter;
import minicraft.entity.furniture.DeathChest;
import minicraft.entity.furniture.DungeonChest;
import minicraft.entity.furniture.Lantern;
import minicraft.entity.furniture.Spawner;
import minicraft.entity.furniture.Tnt;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Cow;
import minicraft.entity.mob.Creeper;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.MobAi;
import minicraft.entity.mob.Pig;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Sheep;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Snake;
import minicraft.entity.mob.Zombie;
import minicraft.item.ArmorItem;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.screen.LoadingDisplay;
import minicraft.util.Logging;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// This class is simply a way to seperate all the old, compatibility complications into a seperate file.
public class LegacyLoad {

	String location = Game.gameDir;

	private static final String extension = Save.extension;

	ArrayList<String> data;
	ArrayList<String> extradata;

	public boolean hasloadedbigworldalready;
	Version currentVer, worldVer;
	boolean oldSave = false;

	Game game = null;

	private DeathChest deathChest;

	{
		currentVer = Game.VERSION;
		worldVer = null;

		data = new ArrayList<>();
		extradata = new ArrayList<>();
		hasloadedbigworldalready = false;
	}

	public LegacyLoad(String worldname) {
		location += "/saves/" + worldname + "/";

		File testFile = new File(location + "KeyPrefs" + extension);
		if (!testFile.exists()) {
			worldVer = new Version("1.8");
			oldSave = true;
		} else
			testFile.delete(); // We don't care about it anymore anyway.

		// This is used in loadInventory().

		loadGame("Game"); // More of the version will be determined here
		loadWorld("Level");
		loadPlayer("Player", Game.player);
		loadInventory("Inventory", Game.player.getInventory());
		loadEntities("Entities", Game.player);
		LoadingDisplay.setPercentage(0); // reset

		if (deathChest != null && deathChest.getInventory().invSize() > 0) {
			Game.player.getLevel().add(deathChest, Game.player.x, Game.player.y);
			Logging.SAVELOAD.debug("Added DeathChest which contains exceed items.");
		}
	}

	protected LegacyLoad(File unlocksFile) {
		updateUnlocks(unlocksFile);
	}

	public void loadFromFile(String filename) {
		data.clear();
		extradata.clear();
		BufferedReader br = null;
		BufferedReader br2 = null;

		try {
			br = new BufferedReader(new FileReader(filename));

			String curLine;
			StringBuilder total = new StringBuilder();
			ArrayList<String> curData;
			while ((curLine = br.readLine()) != null)
				total.append(curLine);
			data.addAll(Arrays.asList(total.toString().split(",")));

			if (filename.contains("Level")) {
				total = new StringBuilder();
				br2 = new BufferedReader(new FileReader(filename.substring(0, filename.lastIndexOf("/") + 7) + "data" + extension));

				while ((curLine = br2.readLine()) != null)
					total.append(curLine);
				extradata.addAll(Arrays.asList(total.toString().split(",")));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				LoadingDisplay.progress(13);
				if (LoadingDisplay.getPercentage() > 100) {
					LoadingDisplay.setPercentage(100);
				}

				if (br != null) {
					br.close();
				}

				if (br2 != null) {
					br2.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}

	protected void updateUnlocks(File file) {
		String path = file.getPath();
		loadFromFile(path);

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).length() == 0) {
				data.remove(i);
				i--;
				continue;
			}
			data.set(i, data.get(i).replace("HOURMODE", "H_ScoreTime").replace("MINUTEMODE", "M_ScoreTime"));
		}

		file.delete();

		try {
			java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(path));
			for (String unlock : data) {
				writer.write("," + unlock);
			}
			writer.flush();
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private int playerac = 0; // This is a temp storage var for use to restore player arrow count.

	public void loadGame(String filename) {
		loadFromFile(location + filename + extension);
		boolean hasVersion = data.get(0).contains(".");
		if (hasVersion) {
			worldVer = new Version(data.get(0)); // Gets the world version
			Updater.setTime(Integer.parseInt(data.get(1)));
			Updater.gameTime = 65000; // Prevents time cheating.

			if (worldVer.compareTo(new Version("1.9.2")) < 0) {
				Settings.set("autosave", Boolean.parseBoolean(data.get(3)));
				Settings.set("sound", Boolean.parseBoolean(data.get(4)));
				if (worldVer.compareTo(new Version("1.9.2-dev2")) >= 0)
					AirWizard.beaten = Boolean.parseBoolean(data.get(5));
			} else { // This is 1.9.2 official or after
				Settings.setIdx("diff", Integer.parseInt(data.get(3)));
				AirWizard.beaten = Boolean.parseBoolean(data.get(4));
			}
		} else {
			if (data.size() == 5) {
				worldVer = new Version("1.9");
				Updater.setTime(Integer.parseInt(data.get(0)));
				Settings.set("autosave", Boolean.parseBoolean(data.get(3)));
				Settings.set("sound", Boolean.parseBoolean(data.get(4)));
			} else { // version == 1.8?
				if (!oldSave) {
					Logging.SAVELOAD.warn("UNEXPECTED WORLD VERSION");
					worldVer = new Version("1.8.1");
				}
				// For backwards compatibility
				Updater.tickCount = Integer.parseInt(data.get(0));
				playerac = Integer.parseInt(data.get(3));
				Settings.set("autosave", false);
			}
		}
	}

	public void loadWorld(String filename) {
		for (int l = 0; l < World.levels.length; l++) {
			loadFromFile(location + filename + l + extension);

			int lvlw = Integer.parseInt(data.get(0));
			int lvlh = Integer.parseInt(data.get(1));
			int lvldepth = Integer.parseInt(data.get(2));
			Settings.set("size", lvlw);

			short[] tiles = new short[lvlw * lvlh];
			short[] tdata = new short[lvlw * lvlh];

			for (int x = 0; x < lvlw - 1; x++) {
				for (int y = 0; y < lvlh - 1; y++) {
					int tileArrIdx = y + x * lvlw;
					int tileidx = x + y * lvlw; // The tiles are saved with x outer loop, and y inner loop, meaning that the list reads down, then right one, rather than right, then down one.
					Load.loadTile(worldVer, tiles, tdata, tileArrIdx, Tiles.oldids.get(Integer.parseInt(data.get(tileidx + 3))),
						extradata.get(tileidx));
				}
			}

			World.levels[l] = new Level(lvlw, lvlh, lvldepth, null, false);
			World.levels[l].tiles = tiles;
			World.levels[l].data = tdata;
		}
	}

	public void loadPlayer(String filename, Player player) {
		loadFromFile(location + filename + extension);
		player.x = Integer.parseInt(data.get(0));
		player.y = Integer.parseInt(data.get(1));
		player.spawnx = Integer.parseInt(data.get(2));
		player.spawny = Integer.parseInt(data.get(3));
		player.health = Integer.parseInt(data.get(4));
		player.armor = Integer.parseInt(data.get(5));

		String modedata;
		if (!oldSave) {
			if (data.size() >= 14) {
				if (worldVer == null) worldVer = new Version("1.9.1-pre1");
				player.armorDamageBuffer = Integer.parseInt(data.get(13));
				player.curArmor = (ArmorItem) Items.get(data.get(14));
			} else player.armor = 0;

			Game.currentLevel = Integer.parseInt(data.get(8));
			modedata = data.get(9);

		} else {
			// Old, 1.8 save.
			Game.currentLevel = Integer.parseInt(data.get(7));
			modedata = data.get(8);
		}

		player.setScore(Integer.parseInt(data.get(6)));
		World.levels[Game.currentLevel].add(player);

		int mode;
		if (modedata.contains(";")) {
			mode = Integer.parseInt(modedata.substring(0, modedata.indexOf(";")));
			if (mode == 4)
				Updater.scoreTime = Integer.parseInt(modedata.substring(modedata.indexOf(";") + 1));
		} else {
			mode = Integer.parseInt(modedata);
			if (mode == 4) Updater.scoreTime = 300;
		}

		Settings.setIdx("mode", mode);

		boolean hasEffects;
		int potionIdx = 10;
		if (oldSave) {
			hasEffects = data.size() > 10 && data.get(data.size() - 2).contains("PotionEffects[");
			potionIdx = data.size() - 2;
		} else
			hasEffects = !data.get(10).equals("PotionEffects[]"); // Newer save

		if (hasEffects) {
			String[] effects = data.get(potionIdx).replace("PotionEffects[", "").replace("]", "").split(":");

			for (int i = 0; i < effects.length; i++) {
				String[] effect = effects[i].split(";");
				String pName = effect[0];
				if (oldSave) pName = pName.replace("P.", "Potion");
				PotionItem.applyPotion(player, Enum.valueOf(PotionType.class, pName), Integer.parseInt(effect[1]));
			}
		}

		String colors = data.get(oldSave ? data.size() - 1 : 11).replace("[", "").replace("]", "");
		String[] color = colors.split(";");
		player.shirtColor = Integer.parseInt(color[0] + color[1] + color[2]);
	}

	public void loadInventory(String filename, Inventory inventory) {
		deathChest = new DeathChest();
		loadFromFile(location + filename + extension);
		inventory.clearInv();

		for (int i = 0; i < data.size(); i++) {
			String item = data.get(i);
			if (i == 0 && oldSave && item.contains(";")) item = item.replace(";0", ";1");
			loadItemToInventory(item, inventory);
		}

		if (playerac > 0 && inventory == Game.player.getInventory()) {
			for (int i = 0; i < playerac; i++)
				loadItem(inventory, Items.get("arrow"));
			playerac = 0;
		}
	}

	public void loadItemToInventory(String item, Inventory inventory) {
		if (item.contains(";")) {
			String[] curData = item.split(";");
			String itemName = curData[0];
			if (oldSave) itemName = subOldName(itemName);

			//System.out.println("Item to fetch: " + itemName + "; count=" + curData[1]);
			Item newItem = Items.get(itemName);
			int count = Integer.parseInt(curData[1]);
			for (int i = 0; i < count; i++)
				loadItem(inventory, newItem);
		} else {
			if (oldSave) item = subOldName(item);
			Item toAdd = Items.get(item);
			loadItem(inventory, toAdd);
		}
	}

	private void loadItem(Inventory inventory, Item item) {
		if (inventory.add(item) != null) {
			deathChest.getInventory().add(item.copy());
		}
	}

	private String subOldName(String oldName) {
		//System.out.println("Old name: " + oldName);
		String newName = oldName.replace("P.", "Potion").replace("Fish Rod", "Fishing Rod").replace("bed", "Bed");
		newName = Load.subOldName(newName, worldVer);
		//System.out.println("New name: " + newName);
		return newName;
	}

	public void loadEntities(String filename, Player player) {
		loadFromFile(location + filename + extension);

		for (int i = 0; i < World.levels.length; i++) {
			World.levels[i].clearEntities();
		}

		for (int i = 0; i < data.size(); i++) {
			List<String> info = Arrays.asList(data.get(i).substring(data.get(i).indexOf("[") + 1, data.get(i).indexOf("]")).split(":")); // This gets everything inside the "[...]" after the entity name.

			String entityName = data.get(i).substring(0, data.get(i).indexOf("[")).replace("bed", "Bed").replace("II", ""); // This gets the text before "[", which is the entity name.
			int x = Integer.parseInt(info.get(0));
			int y = Integer.parseInt(info.get(1));

			int mobLvl = 0;
			try {
				if (Class.forName("EnemyMob").isAssignableFrom(Class.forName(entityName)))
					mobLvl = Integer.parseInt(info.get(info.size() - 2));
			} catch (ClassNotFoundException ignored) {
			}

			Entity newEntity = getEntity(entityName, player, mobLvl);

			if (newEntity != null) { // the method never returns null, but...
				int currentlevel;
				if (newEntity instanceof Mob) {
					Mob mob = (Mob) newEntity;
					mob.health = Integer.parseInt(info.get(2));
					currentlevel = Integer.parseInt(info.get(info.size() - 1));
					World.levels[currentlevel].add(mob, x, y);
				} else if (newEntity instanceof Chest) {
					Chest chest = (Chest) newEntity;
					boolean isDeathChest = chest instanceof DeathChest;
					boolean isDungeonChest = chest instanceof DungeonChest;
					List<String> chestInfo = info.subList(2, info.size() - 1);

					int endIdx = chestInfo.size() - (isDeathChest || isDungeonChest ? 1 : 0);
					for (int idx = 0; idx < endIdx; idx++) {
						String itemData = chestInfo.get(idx);
						if (worldVer.compareTo(new Version("1.9.1")) < 0) // if this world is before 1.9.1
							if (itemData.equals("")) continue; // this skips any null items
						loadItemToInventory(itemData, chest.getInventory());
					}

					if (isDeathChest) {
						((DeathChest) chest).time = Integer.parseInt(chestInfo.get(chestInfo.size() - 1).replace("tl;", "")); // "tl;" is only for old save support
					} else if (isDungeonChest) {
						((DungeonChest) chest).setLocked(Boolean.parseBoolean(chestInfo.get(chestInfo.size() - 1)));
					}

					currentlevel = Integer.parseInt(info.get(info.size() - 1));
					World.levels[currentlevel].add(chest instanceof DeathChest ? chest : chest instanceof DungeonChest ? (DungeonChest) chest : chest, x, y);
				} else if (newEntity instanceof Spawner) {
					MobAi mob = (MobAi) getEntity(info.get(2), player, Integer.parseInt(info.get(3)));
					currentlevel = Integer.parseInt(info.get(info.size() - 1));
					if (mob != null)
						World.levels[currentlevel].add(new Spawner(mob), x, y);
				} else {
					currentlevel = Integer.parseInt(info.get(2));
					World.levels[currentlevel].add(newEntity, x, y);
				}
			} // End of entity not null conditional
		}
	}

	public Entity getEntity(String string, Player player, int mobLevel) {
		switch (string) {
			case "Player":
				return player;
			case "Cow":
				return new Cow();
			case "Sheep":
				return new Sheep();
			case "Pig":
				return new Pig();
			case "Zombie":
				return new Zombie(mobLevel);
			case "Slime":
				return new Slime(mobLevel);
			case "Creeper":
				return new Creeper(mobLevel);
			case "Skeleton":
				return new Skeleton(mobLevel);
			case "Knight":
				return new Knight(mobLevel);
			case "Snake":
				return new Snake(mobLevel);
			case "AirWizard":
				if (mobLevel > 1) return null;
				return new AirWizard();
			case "Spawner":
				return new Spawner(new Zombie(1));
			case "Workbench":
				return new Crafter(Crafter.Type.Workbench);
			case "Chest":
				return new Chest();
			case "DeathChest":
				return new DeathChest();
			case "DungeonChest":
				return new DungeonChest(null);
			case "Anvil":
				return new Crafter(Crafter.Type.Anvil);
			case "Enchanter":
				return new Crafter(Crafter.Type.Enchanter);
			case "Loom":
				return new Crafter(Crafter.Type.Loom);
			case "Furnace":
				return new Crafter(Crafter.Type.Furnace);
			case "Oven":
				return new Crafter(Crafter.Type.Oven);
			case "Bed":
				return new Bed();
			case "Tnt":
				return new Tnt();
			case "Lantern":
				return new Lantern(Lantern.Type.NORM);
			case "IronLantern":
				return new Lantern(Lantern.Type.IRON);
			case "GoldLantern":
				return new Lantern(Lantern.Type.GOLD);
			default:
				Logger.tag("SaveLoad/LegacyLoad").warn("Unknown or outdated entity requested: " + string);
				return null;
		}
	}
}
