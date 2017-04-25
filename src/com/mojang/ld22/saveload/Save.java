package com.mojang.ld22.saveload;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.AirWizard;
import com.mojang.ld22.entity.Chest;
import com.mojang.ld22.entity.DeathChest;
import com.mojang.ld22.entity.DungeonChest;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.Inventory;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.EnemyMob;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.Spawner;
import com.mojang.ld22.entity.particle.Particle;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.screen.LoadingMenu;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;
import com.mojang.ld22.screen.WorldGenMenu;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Save {

	String location = Game.gameDir;
	File folder;
	String extention;
	List data;
	Player player;
	
	public Save(Player player, String worldname) {
		folder = new File(location);
		extention = ".miniplussave";
		data = new ArrayList();
		this.player = player;
		location += "/saves/" + worldname + "/";
		folder = new File(location);
		folder.mkdirs();
		writeGame("Game", player.game);
		writePrefs("KeyPrefs");
		writeWorld("Level");
		writePlayer("Player", player);
		writeInventory("Inventory", player.inventory);
		writeEntities("Entities");
		Game.notifications.add("World Saved!");
		player.game.asTick = 0;
		player.game.saving = false;
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
					
					player.game.render();
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException var14) {
				var14.printStackTrace();
			}
			
		}
		
	}
	
	public void writeGame(String filename, Game game) {
		data.add(String.valueOf(Game.VERSION));
		data.add(String.valueOf(Game.tickCount));
		data.add(String.valueOf(Game.astime));
		data.add(String.valueOf(Game.autosave));
		data.add(String.valueOf(OptionsMenu.isSoundAct));
		writeToFile(location + filename + extention, data);
	}
	
	public void writePrefs(String filename) {
		String[] keyPrefs = player.game.input.getKeyPrefs();
		for(int i = 0; i < keyPrefs.length; i++)
			data.add(String.valueOf(keyPrefs[i]));
		writeToFile(location + filename + extention, data);
	}
	
	public void writeWorld(String filename) {
		int l;
		int i;
		int ii;
		for(l = 0; l < Game.levels.length; l++) {
			data.add(String.valueOf(WorldGenMenu.sized));
			data.add(String.valueOf(WorldGenMenu.sized));
			data.add(String.valueOf(Game.levels[l].depth));
			
			for(i = 0; i < Game.levels[l].w; i++) {
				for(ii = 0; ii < Game.levels[l].h; ii++) {
					data.add(String.valueOf(Game.levels[l].getTile(i, ii).id));
				}
			}
			
			writeToFile(location + filename + l + extention, data);
		}
		
		for(l = 0; l < Game.levels.length; l++) {
			for(i = 0; i < Game.levels[l].w; i++) {
				for(ii = 0; ii < Game.levels[l].h; ii++) {
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
	
	public void writeInventory(String filename, Inventory inventory) {
		if(player.activeItem != null) {
			if(player.activeItem instanceof ResourceItem) {
				data.add(player.activeItem.getName() + ";" + ((ResourceItem)player.activeItem).count);
			} else {
				data.add(player.activeItem.getName());
			}
		}
		
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
				String name = e.getClass().getName().replace("com.mojang.ld22.entity.", "");
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
