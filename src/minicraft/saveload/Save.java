package minicraft.saveload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import minicraft.Game;
import minicraft.entity.AirWizard;
import minicraft.entity.Chest;
import minicraft.entity.DeathChest;
import minicraft.entity.DungeonChest;
import minicraft.entity.EnemyMob;
import minicraft.entity.Entity;
import minicraft.entity.Inventory;
import minicraft.entity.ItemEntity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.entity.Spark;
import minicraft.entity.Spawner;
import minicraft.entity.particle.Particle;
import minicraft.item.Item;
import minicraft.item.ResourceItem;
import minicraft.screen.LoadingMenu;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;
import minicraft.screen.WorldGenMenu;

public class Save {

	String location = Game.gameDir;
	File folder;
	
	protected static String extention = ".miniplussave";
	
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
		
		writeGame("Game");
		//writePrefs("KeyPrefs");
		writeWorld("Level");
		writePlayer("Player", player);
		writeInventory("Inventory", player);
		writeEntities("Entities");
		
		Game.notifications.add("World Saved!");
		player.game.asTick = 0;
		player.game.saving = false;
	}
	
	// this saves global options
	public Save(Game game) {
		this(game, "/");
		
		writePrefs("Preferences");
	}
	
	public void writeToFile(String filename, List savedata) {
		BufferedWriter bufferedWriter = null;
		
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(filename));
			
			for(int ex = 0; ex < savedata.size(); ex++) {
				bufferedWriter.write((String)savedata.get(ex));
				bufferedWriter.write(",");
				if(filename.contains("Level5") && ex == savedata.size() - 1) {
					bufferedWriter.write(",");
				}
			}
			
			data.clear();
		} catch (FileNotFoundException var15) {
			var15.printStackTrace();
		} catch (IOException var16) {
			var16.printStackTrace();
		} finally {
			try {
				if(bufferedWriter != null) {
					LoadingMenu.percentage += 7;
					if(LoadingMenu.percentage > 100) {
						LoadingMenu.percentage = 100;
					}
					
					game.render();
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException var14) {
				var14.printStackTrace();
			}
			
		}
		
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
			
			for(int i = 0; i < Game.levels[l].w; i++) {
				for(int ii = 0; ii < Game.levels[l].h; ii++) {
					data.add(String.valueOf(Game.levels[l].getTile(i, ii).id));
				}
			}
			
			writeToFile(location + filename + l + extention, data);
		}
		
		for(int l = 0; l < Game.levels.length; l++) {
			for(int i = 0; i < Game.levels[l].w; i++) {
				for(int ii = 0; ii < Game.levels[l].h; ii++) {
					data.add(String.valueOf(Game.levels[l].getData(i, ii)));
				}
			}
			
			writeToFile(location + filename + l + "data" + extention, data);
		}
		
	}
	
	public void writePlayer(String filename, Player player) {
		data.add(String.valueOf(player.x));
		data.add(String.valueOf(player.y));
		data.add(String.valueOf(Player.spawnx));
		data.add(String.valueOf(Player.spawny));
		data.add(String.valueOf(player.health));
		data.add(String.valueOf(player.armor));
		data.add(String.valueOf(Player.score));
		data.add(String.valueOf(player.ac));
		data.add(String.valueOf(Game.currentLevel));
		data.add(ModeMenu.mode + (ModeMenu.score?";"+player.game.scoreTime:""));
		
		String subdata = "PotionEffects[";
		
		for(java.util.Map.Entry<String, Integer> potion: player.potioneffects.entrySet())
			subdata += potion.getKey() + ";" + potion.getValue() + ":";
		
		if(player.potioneffects.size() > 0)
			subdata = subdata.substring(0, subdata.length()-(1))+"]"; // cuts off extra ":" and appends "]"
		else subdata += "]";
		data.add(subdata);
		
		data.add("[" + player.r + ";" + player.g + ";" + player.b + "]");
		data.add(String.valueOf(Player.skinon));
		if(player.curArmor != null) {
			data.add(String.valueOf(player.armorDamageBuffer));
			data.add(String.valueOf(player.curArmor.name));
		}
		
		writeToFile(location + filename + extention, data);
	}
	
	public void writeInventory(String filename, Player player) {
		if(player.activeItem != null) {
			if(player.activeItem instanceof ResourceItem) {
				data.add(player.activeItem.getName() + ";" + ((ResourceItem)player.activeItem).count);
			} else {
				data.add(player.activeItem.getName());
			}
		}
		
		Inventory inventory = player.inventory;
		
		for(int i = 0; i < inventory.invSize(); i++) {
			if(inventory.get(i) instanceof ResourceItem) {
				data.add(((Item)inventory.get(i)).getName() + ";" + ((ResourceItem)inventory.get(i)).count);
			} else {
				data.add(((Item)inventory.get(i)).getName());
			}
		}
		
		writeToFile(location + filename + extention, data);
	}
	
	public void writeEntities(String filename) {
		for(int l = 0; l < Game.levels.length; l++) {
			for(int i = 0; i < Game.levels[l].entities.size(); i++) {
				Entity e = (Entity)Game.levels[l].entities.get(i);
				String name = e.getClass().getName().replace("minicraft.entity.", "");
				String extradata = "";
				
				if(e instanceof ItemEntity || e instanceof Particle || e instanceof Spark) continue; // don't even write ItemEntities or particle effects; Spark... will probably is saved, eventually; it presents an unfair cheat to remove the sparks by reloading the game.
				
				if(e instanceof Mob) {
					Mob m = (Mob)e;
					extradata = ":" + m.health;
					if(e instanceof EnemyMob)
					 	extradata += ":" + ((EnemyMob)m).lvl;
				}
				
				if(e instanceof Chest) {
					Chest chest = (Chest)e;
					
					for(int ii = 0; ii < chest.inventory.invSize(); ii++) {
						Item item = (Item)chest.inventory.get(ii);
						extradata += ":" + item.getName();
						if(item instanceof ResourceItem)
							extradata += ";" + chest.inventory.count(item);
					}
					
					if(chest instanceof DeathChest) extradata += ":" + ((DeathChest)chest).time;
					if(chest instanceof DungeonChest) extradata += ":" + ((DungeonChest)chest).isLocked;
				}
				
				if(e instanceof Spawner) {
					Spawner egg = (Spawner)e;
					extradata += ":" + egg.mob + ":" + egg.lvl;
				}
				
				data.add(name + "[" + e.x + ":" + e.y + extradata + ":" + l + "]");
			}
		}
		
		writeToFile(location + filename + extention, data);
	}
}
