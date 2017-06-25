package minicraft.saveload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import minicraft.Game;
import minicraft.entity.*;
import minicraft.entity.particle.*;
import minicraft.item.Item;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
import minicraft.screen.LoadingMenu;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;
import minicraft.screen.WorldGenMenu;
import minicraft.screen.WorldSelectMenu;

public class Save {

	public String location = Game.gameDir;
	File folder;
	
	public static String extention = ".miniplussave";
	
	List<String> data;
	Game game;
	
	private Save(Game game, String dir) {
		data = new ArrayList<String>();
		
		this.game = game;
		
		location += dir;
		folder = new File(location);
		folder.mkdirs();
	}
	
	/// this saves world options
	public Save(Player player, String worldname) {
		this(player.game, "/saves/" + worldname + "/");
		
		if(Game.isValidClient()) {
			// clients are not allowed to save.
			Game.saving = false;
			return;
		}
		else if(Game.isValidServer()) {
			Game.server.broadcastData(Game.server.prependType(minicraft.network.MinicraftProtocol.InputType.SAVE, new byte[0])); // tell all the other clients to send their data over to be saved.
		}
		
		writeGame("Game");
		//writePrefs("KeyPrefs");
		writeWorld("Level");
		if(!Game.isValidServer()) { // this must be waited for on a server.
			writePlayer("Player", player);
			writeInventory("Inventory", player);
		}
		writeEntities("Entities");
		
		Game.notifyAll("World Saved!");
		Game.asTick = 0;
		Game.saving = false;
	}
	
	// this saves global options
	public Save(Game game) {
		this(game, "/");
		
		writePrefs("Preferences");
	}
	
	public Save(Player player) {
		// this is simply for access to writeToFile.
		this(player.game, "/saves/"+WorldSelectMenu.worldname + "/");
	}
	
	public void writeToFile(String filename, List<String> savedata) {
		try {
			writeToFile(filename, savedata.toArray(new String[0]), true);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		data.clear();
		
		LoadingMenu.percentage += 7;
		if(LoadingMenu.percentage > 100) {
			LoadingMenu.percentage = 100;
		}
		
		game.render(); // AH HA!!! HERE'S AN IMPORTANT STATEMENT!!!!
	}
	
	public static void writeToFile(String filename, String[] savedata, boolean isWorldSave) throws IOException {
		//BufferedWriter bufferedWriter = null;
		
		/*String content = "";
		for(String data: savedata) {
			content += data + ",";
		}
		if(filename.contains("Level5")) content += ",";
		*/
		/*if(base64Encode) {
			byte[] bytes = content.getBytes();
			content = Base64.getEncoder().encodeToString(bytes);
		}*/
		
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
		} catch (IOException ex) {
			throw ex;
		}/* finally {
			try {
				if(bufferedWriter != null) {
					
					
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}*/
	}
	
	public void writeGame(String filename) {
		data.add(String.valueOf(Game.VERSION));
		data.add(String.valueOf(Game.tickCount));
		data.add(String.valueOf(game.gameTime));
		data.add(String.valueOf(OptionsMenu.diff));
		data.add(String.valueOf(AirWizard.beaten));
		writeToFile(location + filename + extention, data);
	}
	
	public void writePrefs(String filename) {
		data.add(String.valueOf(OptionsMenu.isSoundAct));
		data.add(String.valueOf(OptionsMenu.autosave));
		
		String[] keyPrefs = game.input.getKeyPrefs();
		for(int i = 0; i < keyPrefs.length; i++)
			data.add(String.valueOf(keyPrefs[i]));
		
		writeToFile(location + filename + extention, data);
	}
	
	public void writeWorld(String filename) {
		for(int l = 0; l < Game.levels.length; l++) {
			data.add(String.valueOf(WorldGenMenu.getSize()));
			data.add(String.valueOf(WorldGenMenu.getSize()));
			data.add(String.valueOf(Game.levels[l].depth));
			
			for(int x = 0; x < Game.levels[l].w; x++) {
				for(int y = 0; y < Game.levels[l].h; y++) {
					data.add(String.valueOf(Game.levels[l].getTile(x, y).name));
				}
			}
			
			writeToFile(location + filename + l + extention, data);
		}
		
		for(int l = 0; l < Game.levels.length; l++) {
			for(int x = 0; x < Game.levels[l].w; x++) {
				for(int y = 0; y < Game.levels[l].h; y++) {
					data.add(String.valueOf(Game.levels[l].getData(x, y)));
				}
			}
			
			writeToFile(location + filename + l + "data" + extention, data);
		}
		
	}
	
	public void writePlayer(String filename, Player player) {
		writePlayer(player, data);
		writeToFile(location + filename + extention, data);
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
		data.add(String.valueOf(player.game.currentLevel));
		data.add(ModeMenu.mode + (ModeMenu.score?";"+Game.scoreTime+";"+ModeMenu.getSelectedTime():""));
		
		String subdata = "PotionEffects[";
		
		for(java.util.Map.Entry<PotionType, Integer> potion: player.potioneffects.entrySet())
			subdata += potion.getKey() + ";" + potion.getValue() + ":";
		
		if(player.potioneffects.size() > 0)
			subdata = subdata.substring(0, subdata.length()-(1))+"]"; // cuts off extra ":" and appends "]"
		else subdata += "]";
		data.add(subdata);
		
		data.add(String.valueOf(player.shirtColor));
		data.add(String.valueOf(player.skinon));
		if(player.curArmor != null) {
			data.add(String.valueOf(player.armorDamageBuffer));
			data.add(String.valueOf(player.curArmor.name));
		}
	}
	
	public void writeInventory(String filename, Player player) {
		writeInventory(player, data);
		writeToFile(location + filename + extention, data);
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
		
		writeToFile(location + filename + extention, data);
	}
	
	public static String writeEntity(Entity e, boolean isLocalSave) {
		String name = e.getClass().getName().replace("minicraft.entity.", "");
		String extradata = "";
		
		// don't even write ItemEntities or particle effects; Spark... will probably is saved, eventually; it presents an unfair cheat to remove the sparks by reloading the game.
		else if(isLocalSave && (e instanceof ItemEntity || e instanceof Arrow || e instanceof RemotePlayer || e instanceof Spark || e instanceof Particle)) // wirte these only when sending a world, not writing it. (RemotePlayers are saved seperately, when their info is recieved.)
			return "";
		
		if(!isLocalSave)
			extradata += ":" + e.eid;
		
		if(!isLocalSave && e instanceof RemotePlayer) {
			RemotePlayer rp = (RemotePlayer)e;
			extradata += ":"+rp.username+":"+rp.ipAddress.getCanonicalHostName()+":"+rp.port;
		} // the "else" part is so that remote player, which is a mob, doesn't get the health thing.
		else if(e instanceof Mob) {
			Mob m = (Mob)e;
			extradata += ":" + m.health;
			if(e instanceof EnemyMob)
				extradata += ":" + ((EnemyMob)m).lvl;
		}
		
		if(e instanceof Chest) {
			Chest chest = (Chest)e;
			
			for(int ii = 0; ii < chest.inventory.invSize(); ii++) {
				Item item = (Item)chest.inventory.get(ii);
				extradata += ":" + item.name;
				if(item instanceof StackableItem)
					extradata += ";" + chest.inventory.count(item);
			}
			
			if(chest instanceof DeathChest) extradata += ":" + ((DeathChest)chest).time;
			if(chest instanceof DungeonChest) extradata += ":" + ((DungeonChest)chest).isLocked;
		}
		
		if(e instanceof Spawner) {
			Spawner egg = (Spawner)e;
			extradata += ":" + egg.mob.getClass().getName().replace("minicraft.entity.", "") + ":" + (egg.mob instanceof EnemyMob ? ((EnemyMob)egg.mob).lvl : 1);
		}
		
		if (e instanceof Lantern) {
			extradata += ":"+((Lantern)e).type.ordinal();
		}
		
		if (e instanceof Crafter) {
			name = ((Crafter)e).type.name();
		}
		
		if (!isLocalSave) {
			if(e instanceof ItemEntity) extradata += ":" + ((ItemEntity)e).getData();
			if(e instanceof Arrow) extradata += ":" + ((Arrow)e).getData();
			if(e instanceof Spark) extradata += ":" + ((Spark)e).getData();
			if(e instanceof TextParticle) extradata += ":" + ((TextParticle)e).getData();
		}
		//else // is a local save
			extradata += ":" + Game.lvlIdx(e.level.depth);
		
		return name + "[" + e.x + ":" + e.y + extradata + "]";
	}
}
