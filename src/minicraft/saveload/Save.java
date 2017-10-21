package minicraft.saveload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import minicraft.Game;
import minicraft.Settings;
import minicraft.entity.*;
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
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
import minicraft.network.MinicraftServer;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.MultiplayerMenu;
import minicraft.screen.WorldSelectMenu;

public class Save {

	public String location = Game.gameDir;
	File folder;
	
	public static String extension = ".miniplussave";
	
	List<String> data;
	Game game;
	
	private Save(File worldFolder) {
		data = new ArrayList<>();
		
		
		if(worldFolder.getParent().equals("saves")) {
			String worldName = worldFolder.getName();
			if (!worldName.toLowerCase().equals(worldName)) {
				if (Game.debug) System.out.println("renaming world in " + worldFolder + " to lowercase");
				String path = worldFolder.toString();
				path = path.substring(0, path.lastIndexOf(worldName));
				File newFolder = new File(path + worldName.toLowerCase());
				if (worldFolder.renameTo(newFolder))
					worldFolder = newFolder;
				else
					System.err.println("failed to rename world folder " + worldFolder + " to " + newFolder);
			}
		}
		
		//location += dir;
		folder = worldFolder;
		location = worldFolder.getPath() + "/";
		folder.mkdirs();
	}
	
	/// this saves world options
	public Save(String worldname) {
		this(new File(Game.gameDir+"/saves/" + worldname + "/"));
		
		if(Game.isValidClient()) {
			// clients are not allowed to save.
			Game.saving = false;
			return;
		}
		
		writeGame("Game");
		//writePrefs("KeyPrefs");
		writeWorld("Level");
		if(!Game.isValidServer()) { // this must be waited for on a server.
			writePlayer("Player", Game.player);
			writeInventory("Inventory", Game.player);
		}
		writeEntities("Entities");
		
		Game.notifyAll("World Saved!");
		Game.asTick = 0;
		Game.saving = false;
	}
	
	/// this saves server config options
	public Save(String worldname, MinicraftServer server) {
		this(new File(Game.gameDir+"/saves/" + worldname + "/"));
		
		if (Game.debug) System.out.println("writing server config...");
		writeServerConfig("ServerConfig", server);
	}
	
	// this saves global options
	public Save() {
		this(new File(Game.gameDir+"/"));
		if(Game.debug) System.out.println("writing preferences...");
		writePrefs("Preferences");
	}
	
	public Save(Player player) {
		// this is simply for access to writeToFile.
		this(new File(Game.gameDir+"/saves/"+ WorldSelectMenu.getWorldName() + "/"));
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
		
		Game.render(); // AH HA!!! HERE'S AN IMPORTANT STATEMENT!!!!
	}
	
	public static void writeToFile(String filename, String[] savedata, boolean isWorldSave) throws IOException {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename))) {
			//bufferedWriter.write(content);
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
	
	public void writeGame(String filename) {
		data.add(String.valueOf(Game.VERSION));
		data.add(String.valueOf(Game.tickCount));
		data.add(String.valueOf(Game.gameTime));
		data.add(String.valueOf(Settings.getIdx("diff")));
		data.add(String.valueOf(AirWizard.beaten));
		writeToFile(location + filename + extension, data);
	}
	
	public void writePrefs(String filename) {
		data.add(Game.VERSION);
		data.add(String.valueOf(Settings.get("sound")));
		data.add(String.valueOf(Settings.get("autosave")));
		data.add(String.valueOf(Settings.get("fps")));
		data.add(MultiplayerMenu.savedIP);
		data.add(MultiplayerMenu.savedUUID);
		data.add(MultiplayerMenu.savedUsername);
		
		List<String> keyPairs = new ArrayList<>();
		Collections.addAll(keyPairs, Game.input.getKeyPrefs());
		
		data.add(String.join(":", keyPairs.toArray(new String[keyPairs.size()])));
		
		writeToFile(location + filename + extension, data);
	}
	
	private void writeServerConfig(String filename, MinicraftServer server) {
		data.add(String.valueOf(server.getPlayerCap()));
		//data.add(String.join(":", server.getOpNames().toArray(new String[0])));
		
		writeToFile(location + filename + extension, data);
	}
	
	public void writeWorld(String filename) {
		for(int l = 0; l < Game.levels.length; l++) {
			String worldSize = String.valueOf(Settings.get("size"));
			data.add(worldSize);
			data.add(worldSize);
			data.add(String.valueOf(Game.levels[l].depth));
			
			for(int x = 0; x < Game.levels[l].w; x++) {
				for(int y = 0; y < Game.levels[l].h; y++) {
					data.add(String.valueOf(Game.levels[l].getTile(x, y).name));
				}
			}
			
			writeToFile(location + filename + l + extension, data);
		}
		
		for(int l = 0; l < Game.levels.length; l++) {
			for(int x = 0; x < Game.levels[l].w; x++) {
				for(int y = 0; y < Game.levels[l].h; y++) {
					data.add(String.valueOf(Game.levels[l].getData(x, y)));
				}
			}
			
			writeToFile(location + filename + l + "data" + extension, data);
		}
		
	}
	
	public void writePlayer(String filename, Player player) {
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
		data.add(String.valueOf(player.armor));
		data.add(String.valueOf(player.score));
		//data.add(String.valueOf(player.ac));
		data.add("25"); // TODO filler; remove this, but make sure not to break the Load class's LoadPlayer() method while doing so.
		data.add(String.valueOf(Game.currentLevel));
		data.add(Settings.getIdx("mode") + (Game.isMode("score")?";"+Game.scoreTime+";"+Settings.get("scoretime"):""));
		
		StringBuilder subdata = new StringBuilder("PotionEffects[");
		
		for(java.util.Map.Entry<PotionType, Integer> potion: player.potioneffects.entrySet())
			subdata.append(potion.getKey()).append(";").append(potion.getValue()).append(":");
		
		if(player.potioneffects.size() > 0)
			subdata = new StringBuilder(subdata.substring(0, subdata.length() - (1)) + "]"); // cuts off extra ":" and appends "]"
		else subdata.append("]");
		data.add(subdata.toString());
		
		data.add(String.valueOf(player.shirtColor));
		data.add(String.valueOf(player.skinon));
		if(player.curArmor != null) {
			data.add(String.valueOf(player.armorDamageBuffer));
			data.add(String.valueOf(player.curArmor.name));
		}
	}
	
	public void writeInventory(String filename, Player player) {
		writeInventory(player, data);
		writeToFile(location + filename + extension, data);
	}
	public static void writeInventory(Player player, List<String> data) {
		data.clear();
		if(player.activeItem != null) {
			if(player.activeItem instanceof StackableItem) {
				data.add(player.activeItem.name + ";" + ((StackableItem)player.activeItem).count);
			} else {
				data.add(player.activeItem.name);
			}
		}
		
		Inventory inventory = player.inventory;
		
		for(int i = 0; i < inventory.invSize(); i++) {
			if(inventory.get(i) instanceof StackableItem) {
				data.add(inventory.get(i).name + ";" + ((StackableItem)inventory.get(i)).count);
			} else {
				data.add(inventory.get(i).name);
			}
		}
	}
	
	public void writeEntities(String filename) {
		for(int l = 0; l < Game.levels.length; l++) {
			for(Entity e: Game.levels[l].getEntityArray()) {
				String saved = writeEntity(e, true);
				if(saved.length() > 0)
					data.add(saved);
			}
		}
		
		writeToFile(location + filename + extension, data);
	}
	
	public static String writeEntity(Entity e, boolean isLocalSave) {
		String name = e.getClass().getName().replace("minicraft.entity.", "");
		//name = name.substring(name.lastIndexOf(".")+1);
		StringBuilder extradata = new StringBuilder();
		
		// don't even write ItemEntities or particle effects; Spark... will probably is saved, eventually; it presents an unfair cheat to remove the sparks by reloading the Game.
		
		//if(e instanceof Particle) return ""; // TODO I don't want to, but there are complications.
		
		if(isLocalSave && (e instanceof ItemEntity || e instanceof Arrow || e instanceof RemotePlayer || e instanceof Spark || e instanceof Particle)) // wirte these only when sending a world, not writing it. (RemotePlayers are saved seperately, when their info is received.)
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
			
			for(int ii = 0; ii < chest.inventory.invSize(); ii++) {
				Item item = chest.inventory.get(ii);
				extradata.append(":").append(item.name);
				if(item instanceof StackableItem)
					extradata.append(";").append(chest.inventory.count(item));
			}
			
			if(chest instanceof DeathChest) extradata.append(":").append(((DeathChest) chest).time);
			if(chest instanceof DungeonChest) extradata.append(":").append(((DungeonChest) chest).isLocked);
		}
		
		if(e instanceof Spawner) {
			Spawner egg = (Spawner)e;
			extradata.append(":").append(egg.mob.getClass().getName().replace("minicraft.entity.", "")).append(":").append(egg.mob instanceof EnemyMob ? ((EnemyMob) egg.mob).lvl : 1);
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
			System.out.println("WARNING: saving entity with no level reference: " + e + "; setting level to surface");
		else
			depth = e.getLevel().depth;
		
		extradata.append(":").append(Game.lvlIdx(depth));
		
		return name + "[" + e.x + ":" + e.y + extradata + "]";
	}
}
