package minicraft.saveload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import minicraft.entity.mob.RemotePlayer;
import minicraft.entity.particle.Particle;
import minicraft.entity.particle.TextParticle;
import minicraft.item.*;
import minicraft.network.MinicraftServer;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.MultiplayerDisplay;
import minicraft.screen.WorldSelectDisplay;

public class Save {

	public String location = Game.gameDir;
	File folder;

	public static String extension = ".miniplussave";

	List<String> data;
	Game game;

	private Save(File worldFolder) {
		data = new ArrayList<>();


		if (worldFolder.getParent().equals("saves")) {
			String worldName = worldFolder.getName();
			if (!worldName.toLowerCase().equals(worldName)) {
				if (Game.debug) System.out.println("Renaming world in " + worldFolder + " to lowercase");
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

	// This saves world options
	public Save(String worldname) {
		this(new File(Game.gameDir+"/saves/" + worldname + "/"));

		if (Game.isValidClient()) {
			// clients are not allowed to save.
			Updater.saving = false;
			return;
		}

		writeGame("Game");
		writeWorld("Level");
		if (!Game.isValidServer()) { // this must be waited for on a server.
			writePlayer("Player", Game.player);
			writeInventory("Inventory", Game.player);
		}
		writeEntities("Entities");
		
		WorldSelectDisplay.refreshWorldNames();
		
		Updater.notifyAll("World Saved!");
		Updater.asTick = 0;
		Updater.saving = false;
	}
	
	// this saves server config options
	public Save(String worldname, MinicraftServer server) {
		this(new File(Game.gameDir+"/saves/" + worldname + "/"));
		
		if (Game.debug) System.out.println("Writing server config...");
		writeServerConfig("ServerConfig", server);
	}
	
	// this saves global options
	public Save() {
		this(new File(Game.gameDir+"/"));
		if (Game.debug) System.out.println("Writing preferences and unlocks...");
		writePrefs();
	}
	
	public Save(Player player, boolean writePlayer) {
		// this is simply for access to writeToFile.
		this(new File(Game.gameDir+"/saves/"+ WorldSelectDisplay.getWorldName() + "/"));
		if(writePlayer) {
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
		} catch(IOException ex) {
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
			for(int i = 0; i < savedata.length; i++) {
				bufferedWriter.write(savedata[i]);
				if(isWorldSave) {
					bufferedWriter.write(",");
					if(filename.contains("Level5") && i == savedata.length - 1) {
						bufferedWriter.write(",");
					}
				} else
					bufferedWriter.write("\n");
			}
		}
	}
	
	private void writeGame(String filename) {
		data.add(String.valueOf(Game.VERSION));
		data.add(Settings.getIdx("mode") + (Game.isMode("score")?";"+Updater.scoreTime+";"+Settings.get("scoretime"):""));
		data.add(String.valueOf(Updater.tickCount));
		data.add(String.valueOf(Updater.gameTime));
		data.add(String.valueOf(Settings.getIdx("diff")));
		data.add(String.valueOf(AirWizard.beaten));
		writeToFile(location + filename + extension, data);
	}
	
	private void writePrefs() {
		data.add(String.valueOf(Game.VERSION));
		data.add(String.valueOf(Settings.get("sound")));
		data.add(String.valueOf(Settings.get("autosave")));
		data.add(String.valueOf(Settings.get("fps")));
		data.add(MultiplayerDisplay.savedIP);
		data.add(MultiplayerDisplay.savedUUID);
		data.add(MultiplayerDisplay.savedUsername);
		data.add(Localization.getSelectedLanguage());
		
		List<String> keyPairs = new ArrayList<>();
		Collections.addAll(keyPairs, Game.input.getKeyPrefs());
		
		data.add(String.join(":", keyPairs.toArray(new String[keyPairs.size()])));
		
		writeToFile(location + "Preferences" + extension, data);
		
		if((boolean)Settings.get("unlockedskin"))
			data.add("AirSkin");
		
		if(Settings.getEntry("scoretime").getValueVisibility(10))
			data.add("10_ScoreTime");
		if(Settings.getEntry("scoretime").getValueVisibility(120))
			data.add("120_ScoreTime");
		
		writeToFile(location + "Unlocks" + extension, data);
	}
	
	private void writeServerConfig(String filename, MinicraftServer server) {
		data.add(String.valueOf(server.getPlayerCap()));
		
		writeToFile(location + filename + extension, data);
	}
	
	private void writeWorld(String filename) {
		LoadingDisplay.setMessage("Levels");
		for(int l = 0; l < World.levels.length; l++) {
			String worldSize = String.valueOf(Settings.get("size"));
			data.add(worldSize);
			data.add(worldSize);
			data.add(String.valueOf(World.levels[l].depth));
			
			for(int x = 0; x < World.levels[l].w; x++) {
				for(int y = 0; y < World.levels[l].h; y++) {
					data.add(String.valueOf(World.levels[l].getTile(x, y).name));
				}
			}
			
			writeToFile(location + filename + l + extension, data);
		}
		
		for(int l = 0; l < World.levels.length; l++) {
			for(int x = 0; x < World.levels[l].w; x++) {
				for(int y = 0; y < World.levels[l].h; y++) {
					data.add(String.valueOf(World.levels[l].getData(x, y)));
				}
			}
			
			writeToFile(location + filename + l + "data" + extension, data);
		}
		
	}
	
	private void writePlayer(String filename, Player player) {
		LoadingDisplay.setMessage("Player");
		writePlayer(player, data);
		writeToFile(location + filename + extension, data);
	}
	
	public static void writePlayer(Player player, List<String> data) {
		data.clear();
		data.add(String.valueOf(player.x));
		data.add(String.valueOf(player.y));
		data.add(String.valueOf(player.spawnx));
		data.add(String.valueOf(player.spawny));
		data.add(String.valueOf(player.health));
		data.add(String.valueOf(player.hunger));
		data.add(String.valueOf(player.armor));
		data.add(String.valueOf(player.armorDamageBuffer));
		data.add(String.valueOf(player.curArmor == null ? "NULL" : player.curArmor.getName()));
		data.add(String.valueOf(player.getScore()));
		data.add(String.valueOf(Game.currentLevel));
		
		StringBuilder subdata = new StringBuilder("PotionEffects[");
		
		for(java.util.Map.Entry<PotionType, Integer> potion: player.potioneffects.entrySet())
			subdata.append(potion.getKey()).append(";").append(potion.getValue()).append(":");
		
		if(player.potioneffects.size() > 0)
			subdata = new StringBuilder(subdata.substring(0, subdata.length() - (1)) + "]"); // cuts off extra ":" and appends "]"
		else subdata.append("]");
		data.add(subdata.toString());
		
		data.add(String.valueOf(player.shirtColor));
		data.add(String.valueOf(player.skinon));
	}
	
	private void writeInventory(String filename, Player player) {
		writeInventory(player, data);
		writeToFile(location + filename + extension, data);
	}
	public static void writeInventory(Player player, List<String> data) {
		data.clear();
		if(player.activeItem != null) {
			data.add(player.activeItem.getData());
		}
		
		Inventory inventory = player.getInventory();
		
		for(int i = 0; i < inventory.invSize(); i++) {
			data.add(inventory.get(i).getData());
		}
	}
	
	private void writeEntities(String filename) {
		LoadingDisplay.setMessage("Entities");
		for(int l = 0; l < World.levels.length; l++) {
			for(Entity e: World.levels[l].getEntitiesToSave()) {
				String saved = writeEntity(e, true);
				if(saved.length() > 0)
					data.add(saved);
			}
		}
		
		writeToFile(location + filename + extension, data);
	}
	
	public static String writeEntity(Entity e, boolean isLocalSave) {
		String name = e.getClass().getName();
		name = name.substring(name.lastIndexOf('.')+1);
		StringBuilder extradata = new StringBuilder();
		
		// don't even write ItemEntities or particle effects; Spark... will probably is saved, eventually; it presents an unfair cheat to remove the sparks by reloading the Game.
		
		//if(e instanceof Particle) return ""; // TODO I don't want to, but there are complications.
		
		if(isLocalSave && (e instanceof ItemEntity || e instanceof Arrow || e instanceof RemotePlayer || e instanceof Spark || e instanceof Particle)) // wirte these only when sending a world, not writing it. (RemotePlayers are saved separately, when their info is received.)
			return "";
		
		if(!isLocalSave)
			extradata.append(":").append(e.eid);
		
		if(!isLocalSave && e instanceof RemotePlayer) {
			RemotePlayer rp = (RemotePlayer)e;
			extradata.append(":").append(rp.getData());
		} // the "else" part is so that remote player, which is a mob, doesn't get the health thing.
		else if(e instanceof Mob) {
			Mob m = (Mob)e;
			extradata.append(":").append(m.health);
			if(e instanceof EnemyMob)
				extradata.append(":").append(((EnemyMob) m).lvl);
		}
		
		if(e instanceof Chest) {
			Chest chest = (Chest)e;
			
			for(int ii = 0; ii < chest.getInventory().invSize(); ii++) {
				Item item = chest.getInventory().get(ii);
				extradata.append(":").append(item.getData());
			}
			
			if(chest instanceof DeathChest) extradata.append(":").append(((DeathChest) chest).time);
			if(chest instanceof DungeonChest) extradata.append(":").append(((DungeonChest) chest).isLocked);
		}
		
		if(e instanceof Spawner) {
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
			if(e instanceof ItemEntity) extradata.append(":").append(((ItemEntity) e).getData());
			if(e instanceof Arrow) extradata.append(":").append(((Arrow) e).getData());
			if(e instanceof Spark) extradata.append(":").append(((Spark) e).getData());
			if(e instanceof TextParticle) extradata.append(":").append(((TextParticle) e).getData());
		}
		//else // is a local save
		
		int depth = 0;
		if(e.getLevel() == null)
			System.out.println("WARNING: Saving entity with no level reference: " + e + "; setting level to surface");
		else
			depth = e.getLevel().depth;
		
		extradata.append(":").append(World.lvlIdx(depth));
		
		return name + "[" + e.x + ":" + e.y + extradata + "]";
	}
}
