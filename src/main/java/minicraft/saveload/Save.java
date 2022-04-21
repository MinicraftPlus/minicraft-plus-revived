package minicraft.saveload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.Arrow;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Spark;
import minicraft.entity.furniture.Chest;
import minicraft.entity.furniture.Crafter;
import minicraft.entity.furniture.DeathChest;
import minicraft.entity.furniture.DungeonChest;
import minicraft.entity.furniture.Lantern;
import minicraft.entity.furniture.Spawner;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.EnemyMob;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Sheep;
import minicraft.entity.particle.Particle;
import minicraft.entity.particle.TextParticle;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
import minicraft.screen.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tinylog.Logger;

public class Save {

	public String location = Game.gameDir;
	File folder;

	// Used to indent the .json files
	private static final int indent = 4;

	public static String extension = ".json";

	List<String> data;

	/**
	 * This is the main save method. Called by all Save() methods.
	 * @param worldFolder The folder of where to save
	 */
	private Save(File worldFolder) {
		data = new ArrayList<>();


		if (worldFolder.getParent().equals("saves")) {
			String worldName = worldFolder.getName();
			if (!worldName.toLowerCase().equals(worldName)) {
				Logger.debug("Renaming world in " + worldFolder + " to lowercase");
				String path = worldFolder.toString();
				path = path.substring(0, path.lastIndexOf(worldName));
				File newFolder = new File(path + worldName.toLowerCase());
				if (worldFolder.renameTo(newFolder))
					worldFolder = newFolder;
				else
					System.err.println("Failed to rename world folder " + worldFolder + " to " + newFolder);
			}
		}

		folder = worldFolder;
		location = worldFolder.getPath() + "/";

		folder.mkdirs();
	}

	/**
	 * This will save world options
	 * @param worldname The name of the world.
	 */
	public Save(String worldname) {
		this(new File(Game.gameDir+"/saves/" + worldname + "/"));

		writeGame("Game");
		writeWorld("Level");
		writePlayer("Player", Game.player);
		writeInventory("Inventory", Game.player);
		writeEntities("Entities");
		
		WorldSelectDisplay.updateWorlds();
		
		Updater.notifyAll("World Saved!");
		Updater.asTick = 0;
		Updater.saving = false;
	}

	/** This will save the settings in the settings menu. */
	public Save() {
		this(new File(Game.gameDir+"/"));
		Logger.debug("Writing preferences and unlocks...");
		writePrefs();
		writeUnlocks();
	}

	public Save(Player player, boolean writePlayer) {
		// This is simply for access to writeToFile.
		this(new File(Game.gameDir+"/saves/"+ WorldSelectDisplay.getWorldName() + "/"));
		if (writePlayer) {
			writePlayer("Player", player);
			writeInventory("Inventory", player);
		}
	}
	
	public static void writeFile(String filename, String[] lines) throws IOException {
		try (BufferedWriter br = new BufferedWriter(new FileWriter(filename))) {
			br.write(String.join(System.lineSeparator(), lines));
		}
	}
	
	public void writeToFile(String filename, List<String> savedata) {
		try {
			writeToFile(filename, savedata.toArray(new String[0]), true);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		data.clear();
		
		LoadingDisplay.progress(7);
		if(LoadingDisplay.getPercentage() > 100) {
			LoadingDisplay.setPercentage(100);
		}
		
		Renderer.render(); // AH HA!!! HERE'S AN IMPORTANT STATEMENT!!!!
	}
	
	public static void writeToFile(String filename, String[] savedata, boolean isWorldSave) throws IOException {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename))) {
			for (int i = 0; i < savedata.length; i++) {
				bufferedWriter.write(savedata[i]);
				if (isWorldSave) {
					bufferedWriter.write(",");
					if (filename.contains("Level5") && i == savedata.length - 1) {
						bufferedWriter.write(",");
					}
				} else
					bufferedWriter.write("\n");
			}
		}
	}

	public static void writeJSONToFile(String filename, String json) throws IOException {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename))) {
			bufferedWriter.write(json);
		}
	}
	
	private void writeGame(String filename) {
		JSONObject data = new JSONObject();
		data.put("version", Game.VERSION.toString());
		data.put("mode", Settings.getIdx("mode"));
		if (Game.isMode("score")) data.put("score", Updater.scoreTime + ";" + Settings.get("scoretime"));
		data.put("tickCount", Updater.tickCount);
		data.put("gameTime", Updater.gameTime);
		data.put("difficulty", Settings.getIdx("diff"));
		data.put("wizardBeaten", AirWizard.beaten);
		try {
			writeJSONToFile(location + filename + extension, data.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writePrefs() {
		JSONObject json = new JSONObject();

		json.put("version", String.valueOf(Game.VERSION));
		json.put("diff", Settings.get("diff"));
		json.put("sound", String.valueOf(Settings.get("sound")));
		json.put("autosave", String.valueOf(Settings.get("autosave")));
		json.put("fps", String.valueOf(Settings.get("fps")));
		json.put("lang", Localization.getSelectedLocale().toLanguageTag());
		json.put("skinIdx", String.valueOf(SkinDisplay.getSelectedSkinIndex()));
		json.put("savedIP", MultiplayerDisplay.savedIP);
		json.put("savedUUID", MultiplayerDisplay.savedUUID);
		json.put("savedUsername", MultiplayerDisplay.savedUsername);
		json.put("keymap", new JSONArray(Game.input.getKeyPrefs()));
		json.put("resourcePack", ResourcePackDisplay.getLoadedPack());
		json.put("mainSDTversion", String.valueOf(new Version("0.0.1")/*Game.mainSDTversion*/));
		json.put("levelSDTversion", String.valueOf(new Version("0.0.1")/*Game.levelSDTversion*/));

		// Save json
		try {
			writeJSONToFile(location + "Preferences.json", json.toString(indent));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeUnlocks() {
		JSONObject json = new JSONObject();

		json.put("unlockedAirWizardSuit", (boolean) Settings.get("unlockedskin"));

		JSONArray scoretimes = new JSONArray();
		if (Settings.getEntry("scoretime").getValueVisibility(10))
			scoretimes.put(10);
		if (Settings.getEntry("scoretime").getValueVisibility(120))
			scoretimes.put(120);
		json.put("visibleScoreTimes", scoretimes);

		json.put("unlockedAchievements", new JSONArray(AchievementsDisplay.getUnlockedAchievements()));

		try {
			writeJSONToFile(location + "Unlocks.json", json.toString(indent));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeWorld(String filename) {
		LoadingDisplay.setMessage("Levels");
		for (int l = 0; l < World.levels.length; l++) {
			JSONObject data = new JSONObject();
			int worldSize = (Integer) Settings.get("size");
			data.put("width", worldSize);
			data.put("height", worldSize);
			data.put("seed", World.levels[l].getSeed());
			data.put("depth", World.levels[l].depth);
			
			JSONArray tiles = new JSONArray();
			for (int x = 0; x < World.levels[l].w; x++) {
				for (int y = 0; y < World.levels[l].h; y++) {
					JSONObject tile = new JSONObject();
					tile.put("name", World.levels[l].getTile(x, y).name);
					tile.put("data", World.levels[l].data[x+y*World.levels[l].w]);
					tiles.put(tile);
				}
			}
			data.put("tiles", tiles);
			
			try {
				writeJSONToFile(location + filename + l + extension, data.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void writePlayer(String filename, Player player) {
		LoadingDisplay.setMessage("Player");
		JSONObject data = new JSONObject();
		data.put("x", player.x);
		data.put("y", player.y);
		data.put("spawnX", player.spawnx);
		data.put("spawnY", player.spawny);
		data.put("health", player.health);
		data.put("hunger", player.hunger);
		data.put("armor", player.armor);
		data.put("armorDamageBuffer", player.armorDamageBuffer);
		data.put("curArmor", player.curArmor == null ? "NULL" : player.curArmor.getName());
		data.put("score", player.getScore());
		data.put("level", Game.currentLevel);
		
		JSONArray potions = new JSONArray();
		for (java.util.Map.Entry<PotionType, Integer> potion: player.potioneffects.entrySet())
			potions.put(potion.getKey()+":"+potion.getValue());
		
		data.put("potions", potions);
		data.put("shirtColor", player.shirtColor);
		data.put("suitOn", player.suitOn);
		try {
			writeJSONToFile(location + filename + extension, data.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeInventory(String filename, Player player) {
		try {
			ArrayList<Item> inv = writeInventory(player);
			JSONArray items = new JSONArray();
			for (Item item : inv) {
				JSONObject obj = new JSONObject();
				obj.put("name", item.getName());
				obj.put("data", item.data);
				if (item instanceof StackableItem) obj.put("count", ((StackableItem)item).count);
				items.put(obj);
			}
			writeJSONToFile(location + filename + extension, items.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static ArrayList<Item> writeInventory(Player player) {
		ArrayList<Item> items = new ArrayList<>();
		if (player.activeItem != null) {
			items.add(player.activeItem);
		}
		
		Inventory inventory = player.getInventory();
		
		for (int i = 0; i < inventory.invSize(); i++) {
			items.add(inventory.get(i));
		}
		return items;
	}
	
	private void writeEntities(String filename) {
		LoadingDisplay.setMessage("Entities");
		for (int l = 0; l < World.levels.length; l++) {
			for (Entity e: World.levels[l].getEntitiesToSave()) {
				String saved = writeEntity(e, true);
				if (saved.length() > 0)
					data.add(saved);
			}
		}
		
		writeToFile(location + filename + extension, data);
	}
	
	public static String writeEntity(Entity e, boolean isLocalSave) {
		String name = e.getClass().getName();
		name = name.substring(name.lastIndexOf('.')+1);
		StringBuilder extradata = new StringBuilder();
		
		// Don't even write ItemEntities or particle effects; Spark... will probably is saved, eventually; it presents an unfair cheat to remove the sparks by reloading the Game.
		
		if (isLocalSave && (e instanceof ItemEntity || e instanceof Arrow || e instanceof Spark || e instanceof Particle)) // Write these only when sending a world, not writing it. (RemotePlayers are saved separately, when their info is received.)
			return "";
		
		if (!isLocalSave)
			extradata.append(":").append(e.eid);
		
		if (e instanceof Mob) {
			Mob m = (Mob)e;
			extradata.append(":").append(m.health);
			if (e instanceof EnemyMob)
				extradata.append(":").append(((EnemyMob) m).lvl);
			else if (e instanceof Sheep)
				extradata.append(":").append(((Sheep) m).cut); // Saves if the sheep is cut. If not, we could reload the save and the wool would regenerate.
		}
		
		if (e instanceof Chest) {
			Chest chest = (Chest)e;
			
			for(int ii = 0; ii < chest.getInventory().invSize(); ii++) {
				Item item = chest.getInventory().get(ii);
				extradata.append(":").append(item.getData());
			}
			
			if(chest instanceof DeathChest) extradata.append(":").append(((DeathChest) chest).time);
			if(chest instanceof DungeonChest) extradata.append(":").append(((DungeonChest) chest).isLocked());
		}
		
		if (e instanceof Spawner) {
			Spawner egg = (Spawner)e;
			String mobname = egg.mob.getClass().getName();
			mobname = mobname.substring(mobname.lastIndexOf(".")+1);
			extradata.append(":").append(mobname).append(":").append(egg.mob instanceof EnemyMob ? ((EnemyMob) egg.mob).lvl : 1);
		}
		
		if (e instanceof Lantern) {
			extradata.append(":").append(((Lantern) e).type.ordinal());
		}
		
		if (e instanceof Crafter) {
			name = ((Crafter)e).type.name();
		}
		
		if (!isLocalSave) {
			if (e instanceof ItemEntity) extradata.append(":").append(((ItemEntity) e).getData());
			if (e instanceof Arrow) extradata.append(":").append(((Arrow) e).getData());
			if (e instanceof Spark) extradata.append(":").append(((Spark) e).getData());
			if (e instanceof TextParticle) extradata.append(":").append(((TextParticle) e).getData());
		}
		//else // is a local save
		
		int depth = 0;
		if (e.getLevel() == null)
			System.out.println("WARNING: Saving entity with no level reference: " + e + "; setting level to surface");
		else
			depth = e.getLevel().depth;
		
		extradata.append(":").append(World.lvlIdx(depth));
		
		return name + "[" + e.x + ":" + e.y + extradata + "]";
	}
}
