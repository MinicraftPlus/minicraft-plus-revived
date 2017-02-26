package com.mojang.ld22.saveload;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Chest;
import com.mojang.ld22.entity.DungeonChest;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.Inventory;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.Spawner;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.screen.LoadingMenu;
import com.mojang.ld22.screen.ModeMenu;
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
		this.folder = new File(this.location);
		this.extention = ".miniplussave";
		this.data = new ArrayList();
		this.player = player;
		this.location += "/saves/" + worldname + "/";
		this.folder = new File(this.location);
		this.folder.mkdirs();
		this.writeGame("Game", player.game);
		this.writeWorld("Level");
		this.writePlayer("Player", player);
		this.writeInventory("Inventory", player.inventory);
		this.writeEntities("Entities");
		Game.savedtext = "Saved!";
		Game.notifications.add("World Saved!");
		player.game.tick = 0;
		player.game.saving = false;
	}

	public void writeToFile(String filename, List savedata) {
		BufferedWriter bufferedWriter = null;

		try {
			bufferedWriter = new BufferedWriter(new FileWriter(filename));

			for(int ex = 0; ex < savedata.size(); ++ex) {
				bufferedWriter.write((String)savedata.get(ex));
				bufferedWriter.write(",");
				if(filename.contains("Level5") && ex == savedata.size() - 1) {
					bufferedWriter.write(",");
				}
			}

			this.data.clear();
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

					this.player.game.render();
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException var14) {
				var14.printStackTrace();
			}

		}

	}

	public void writeGame(String filename, Game game) {
		this.data.add(String.valueOf(Game.tickCount));
		this.data.add(String.valueOf(Game.astime));
		this.data.add(String.valueOf(Game.gamespeed));
		this.data.add(String.valueOf(Game.ac));
		this.writeToFile(this.location + filename + this.extention, this.data);
	}

	public void writeWorld(String filename) {
		int l;
		int i;
		int ii;
		for(l = 0; l < Game.levels.length; ++l) {
			this.data.add(String.valueOf(WorldGenMenu.sized));
			this.data.add(String.valueOf(WorldGenMenu.sized));
			this.data.add(String.valueOf(Game.levels[l].depth));

			for(i = 0; i < Game.levels[l].w; ++i) {
				for(ii = 0; ii < Game.levels[l].h; ++ii) {
					this.data.add(String.valueOf(Game.levels[l].getTile(i, ii).id));
				}
			}

			this.writeToFile(this.location + filename + l + this.extention, this.data);
		}

		for(l = 0; l < Game.levels.length; ++l) {
			for(i = 0; i < Game.levels[l].w; ++i) {
				for(ii = 0; ii < Game.levels[l].h; ++ii) {
					this.data.add(String.valueOf(Game.levels[l].getData(i, ii)));
				}
			}

			this.writeToFile(this.location + filename + l + "data" + this.extention, this.data);
		}

	}

	public void writePlayer(String filename, Player player) {
		this.data.add(String.valueOf(player.x));
		this.data.add(String.valueOf(player.y));
		this.data.add(String.valueOf(Player.spawnx));
		this.data.add(String.valueOf(Player.spawny));
		this.data.add(String.valueOf(player.health));
		this.data.add(String.valueOf(player.maxArmor));
		this.data.add(String.valueOf(Player.score));
		this.data.add(String.valueOf(Game.currentLevel));
		if(!ModeMenu.score) {
			this.data.add(String.valueOf(ModeMenu.diff));
		} else {
			this.data.add(ModeMenu.diff + ";" + player.game.scoreTime);
		}
		
		/* potions yet be implemented
		if(player.potioneffects.size() > 0) {
			String subdata = "PotionEffects[";

			for(int i = 0; i < player.potioneffects.size(); ++i) {
				subdata = subdata + (String)player.potioneffects.get(i) + ";" + player.potioneffectstime.get(i);
				if(i != player.potioneffects.size() - 1) {
					subdata = subdata + ":";
				}
			}

			subdata = subdata + "]";
			this.data.add(subdata);
		}*/

		this.data.add("[" + player.r + ";" + player.g + ";" + player.b + "]");
		this.writeToFile(this.location + filename + this.extention, this.data);
	}

	public void writeInventory(String filename, Inventory inventory) {
		if(this.player.activeItem != null) {
			if(this.player.activeItem instanceof ResourceItem) {
				this.data.add(this.player.activeItem.getName() + ";" + inventory.count(this.player.activeItem));
			} else {
				this.data.add(this.player.activeItem.getName());
			}
		}

		for(int i = 0; i < inventory.items.size(); ++i) {
			if(inventory.items.get(i) instanceof ResourceItem) {
				this.data.add(((Item)inventory.items.get(i)).getName() + ";" + inventory.count((Item)inventory.items.get(i)));
			} else {
				this.data.add(((Item)inventory.items.get(i)).getName());
			}
		}

		this.writeToFile(this.location + filename + this.extention, this.data);
	}

	public void writeEntities(String filename) {
		for(int l = 0; l < Game.levels.length; ++l) {
			for(int i = 0; i < Game.levels[l].entities.size(); ++i) {
				Entity e = (Entity)Game.levels[l].entities.get(i);
				String name = e.getClass().getName().replace("com.mojang.ld22.entity.", "");
				String extradata = "";
				if(e.col1 == Color.get(-1, 0, 4, 46)) {
					name = e.getClass().getCanonicalName().replace("com.mojang.ld22.entity.", "") + "II";
				}

				if(e instanceof Mob) {
					Mob c = (Mob)e;
					extradata = ":" + c.health + ":" + c.maxHealth + ":" + c.lvl;
				}

				int ii;
				String var10;
				if(e instanceof Chest) {
					var10 = "";
					Chest c1 = (Chest)e;

					for(ii = 0; ii < c1.inventory.items.size(); ++ii) {
						if(c1.inventory.items.get(ii) instanceof ResourceItem) {
							var10 = var10 + ((Item)c1.inventory.items.get(ii)).getName() + ";" + c1.inventory.count((Item)c1.inventory.items.get(ii)) + ":";
						} else {
							var10 = var10 + ((Item)c1.inventory.items.get(ii)).getName() + ":";
						}
					}

					extradata = extradata + ":" + var10;
					if(c1.isdeathchest) {
						name = "DeathChest";
						extradata = extradata + ":" + "tl;" + c1.time;
					}
				}

				if(e instanceof DungeonChest) {
					var10 = "";
					DungeonChest var11 = (DungeonChest)e;

					for(ii = 0; ii < var11.inventory.items.size(); ++ii) {
						if(!((Item)var11.inventory.items.get(ii)).getName().equals("") || !((Item)var11.inventory.items.get(ii)).getName().equals(" ")) {
							if(var11.inventory.items.get(ii) instanceof ResourceItem) {
								var10 = var10 + ((Item)var11.inventory.items.get(ii)).getName() + ";" + var11.inventory.count((Item)var11.inventory.items.get(ii)) + ":";
							} else {
								var10 = var10 + ((Item)var11.inventory.items.get(ii)).getName() + ":";
							}
						}
					}

					extradata = extradata + ":" + var10 + ":" + var11.islocked;
				}

				if(e instanceof Spawner) {
					Spawner var12 = (Spawner)e;
					extradata = extradata + ":" + var12.mob.getClass().getCanonicalName().replace("com.mojang.ld22.entity.", "") + ":" + var12.lvl;
				}

				this.data.add(name + "[" + e.x + ":" + e.y + extradata + ":" + l + "]");
			}
		}

		this.writeToFile(this.location + filename + this.extention, this.data);
	}
}
