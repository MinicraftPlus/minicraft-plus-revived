package minicraft.saveload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import minicraft.Game;
import minicraft.entity.*;
import minicraft.item.ArmorItem;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.screen.LoadingMenu;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;

/// this class is simply a way to seperate all the old, compatibility complications into a seperate file.
public class LegacyLoad {
	
	String location = Game.gameDir;
	File folder;
	
	private static String extention = Save.extention;
	
	ArrayList<String> data;
	ArrayList<String> extradata;
	
	public boolean hasloadedbigworldalready;
	Load.Version currentVer, worldVer;
	boolean oldSave = false;
	
	{
		currentVer = new Load.Version(Game.VERSION);
		worldVer = null;
		
		data = new ArrayList<String>();
		extradata = new ArrayList<String>();
		hasloadedbigworldalready = false;
	}
	
	public LegacyLoad(Game game, String worldname) {
		location += "/saves/" + worldname + "/";
		
		File testFile = new File(location + "KeyPrefs" + extention);
		if(!testFile.exists()) {
			worldVer = new Load.Version("1.8");
			oldSave = true;
		} else
			testFile.delete(); // we don't care about it anymore anyway.
		
		loadGame("Game", game); // more of the version will be determined here
		loadWorld("Level");
		loadPlayer("Player", game.player);
		loadInventory("Inventory", game.player.inventory);
		loadEntities("Entities", game.player);
		LoadingMenu.percentage = 0; // reset
	}
	
	protected LegacyLoad(String oldName, String newName) {
		updateUnlocks(location+"/" + oldName + extention, location+"/" + newName + extention);
	}
	
	public void loadFromFile(String filename) {
		data.clear();
		extradata.clear();
		BufferedReader br = null;
		BufferedReader br2 = null;
		
		try {
			br = new BufferedReader(new FileReader(filename));
			
			String curLine, total = "";
			ArrayList<String> curData;
			while((curLine = br.readLine()) != null)
				total += curLine;
			data.addAll(Arrays.asList(total.split(",")));
			
			if(filename.contains("Level")) {
				total = "";
				br2 = new BufferedReader(new FileReader(filename.substring(0, filename.lastIndexOf("/") + 7) + "data" + extention));
				
				while((curLine = br2.readLine()) != null)
					total += curLine;
				extradata.addAll(Arrays.asList(total.split(",")));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				LoadingMenu.percentage += 13;
				if(LoadingMenu.percentage > 100) {
					LoadingMenu.percentage = 100;
				}
				
				if(br != null) {
					br.close();
				}
				
				if(br2 != null) {
					br2.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
	}
	
	protected void updateUnlocks(String oldfilename, String newfilename) {
		loadFromFile(oldfilename);
		
		for(int i = 0; i < data.size(); i++) {
			if(data.get(i).length() == 0) {
				data.remove(i);
				i--;
				continue;
			}
			data.set(i, data.get(i).replace("HOURMODE", "H_ScoreTime").replace("MINUTEMODE", "M_ScoreTime"));
		}
		
		try {
			java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(newfilename));
			for(String unlock: data) {
				writer.write(","+unlock);
			}
			writer.flush();
			writer.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void loadGame(String filename, Game game) {
		loadFromFile(location + filename + extention);
		boolean hasVersion = data.get(0).contains(".");
		if(hasVersion) {
			worldVer = new Load.Version(data.get(0)); // gets the world version
			Game.setTime(Integer.parseInt(data.get(1)));
			Game.astime = Integer.parseInt(data.get(2));
			game.gameTime = 65000; // prevents time cheating.
			
			if(worldVer.compareTo(new Load.Version("1.9.2")) < 0) {
				OptionsMenu.autosave = Boolean.parseBoolean(data.get(3));
				OptionsMenu.isSoundAct = Boolean.parseBoolean(data.get(4));
				if(worldVer.compareTo(new Load.Version("1.9.2-dev2")) >= 0)
					AirWizard.beaten = Boolean.parseBoolean(data.get(5));
			} else { // this is 1.9.2 official or after
				OptionsMenu.diff = Integer.parseInt(data.get(3));
				AirWizard.beaten = Boolean.parseBoolean(data.get(4));
			}
		}
		else {
			if(data.size() == 5) {
				worldVer = new Load.Version("1.9");
				Game.setTime(Integer.parseInt(data.get(0)));
				Game.astime = Integer.parseInt(data.get(1));
				OptionsMenu.autosave = Boolean.parseBoolean(data.get(3));
				OptionsMenu.isSoundAct = Boolean.parseBoolean(data.get(4));
			} else { // version == 1.8?
				if(!oldSave) {
					System.out.println("UNEXPECTED WORLD VERSION");
					worldVer = new Load.Version("1.8.1");
				}
				// for backwards compatibility
				Game.tickCount = Integer.parseInt(data.get(0));
				Game.astime = Integer.parseInt(data.get(1));
				game.player.ac = Integer.parseInt(data.get(3));
				OptionsMenu.autosave = false;
			}
		}
	}
	
	public void loadWorld(String filename) {
		for(int l = Game.levels.length-1; l>=0; l--) {
			loadFromFile(location + filename + l + extention);
			
			int lvlw = Integer.parseInt(data.get(0));
			int lvlh = Integer.parseInt(data.get(1));
			int lvldepth = Integer.parseInt(data.get(2));
			
			byte[] tiles = new byte[lvlw * lvlh];
			byte[] tdata = new byte[lvlw * lvlh];
			
			for(int x = 0; x < lvlw - 1; x++) {
				for(int y = 0; y < lvlh - 1; y++) {
					int tileArrIdx = /*worldVer.compareTo(new Version("1.9.3-dev3")) < 0 ?*/ y + x * lvlw;// : x + y * lvlw;
					int tileidx = x + y * lvlw; // the tiles are saved with x outer loop, and y inner loop, meaning that the list reads down, then right one, rather than right, then down one.
					tiles[tileArrIdx] = (byte) Tiles.get(Tiles.oldids.get(Integer.parseInt(data.get(tileidx + 3)))).id;
					tdata[tileArrIdx] = Byte.parseByte(extradata.get(tileidx));
				}
			}
			
			Level parent = l == Game.levels.length-1 ? null : Game.levels[l+1];
			Game.levels[l] = new Level(lvlw, lvlh, lvldepth, parent, false);
			Game.levels[l].tiles = tiles;
			Game.levels[l].data = tdata;
		}
	}
	
	public void loadPlayer(String filename, Player player) {
		loadFromFile(location + filename + extention);
		player.x = Integer.parseInt(data.get(0));
		player.y = Integer.parseInt(data.get(1));
		player.spawnx = Integer.parseInt(data.get(2));
		player.spawny = Integer.parseInt(data.get(3));
		player.health = Integer.parseInt(data.get(4));
		player.armor = Integer.parseInt(data.get(5));
		
		String modedata;
		if(!oldSave) {
			if(data.size() >= 14) {
				if(worldVer == null) worldVer = new Load.Version("1.9.1-pre1");
				player.armorDamageBuffer = Integer.parseInt(data.get(13));
				player.curArmor = (ArmorItem)Items.get(data.get(14));
			} else player.armor = 0;
			
			player.ac = Integer.parseInt(data.get(7));
			Game.currentLevel = Integer.parseInt(data.get(8));
			modedata = data.get(9);
			
		} else {
			// old, 1.8 save.
			Game.currentLevel = Integer.parseInt(data.get(7));
			modedata = data.get(8);
		}
		
		player.score = Integer.parseInt(data.get(6));
		Game.levels[Game.currentLevel].add(player);
		
		int mode;
		if(modedata.contains(";")) {
			mode = Integer.parseInt(modedata.substring(0, modedata.indexOf(";")));
			if (mode == 4)
				player.game.scoreTime = Integer.parseInt(modedata.substring(modedata.indexOf(";") + 1));
		}
		else {
			mode = Integer.parseInt(modedata);
			if (mode == 4) player.game.scoreTime = 300;
		}
		
		ModeMenu.updateModeBools(mode);
		
		boolean hasEffects;
		int potionIdx = 10;
		if(oldSave) {
			hasEffects = data.size() > 10 && data.get(data.size()-2).contains("PotionEffects[");
			potionIdx = data.size() - 2;
		} else
			hasEffects = !data.get(10).equals("PotionEffects[]"); // newer save
		
		if(hasEffects) {
			String[] effects = data.get(potionIdx).replace("PotionEffects[", "").replace("]", "").split(":");
			
			for(int i = 0; i < effects.length; i++) {
				String[] effect = effects[i].split(";");
				String pName = effect[0];
				if(oldSave) pName = pName.replace("P.", "Potion");
				PotionItem.applyPotion(player, Enum.valueOf(PotionType.class, pName), Integer.parseInt(effect[1]));
			}
		}
		
		String colors = data.get(oldSave?data.size()-1:11).replace("[", "").replace("]", "");
		String[] color = colors.split(";");
		player.shirtColor = Integer.parseInt(color[0]+color[1]+color[2]);
		
		if(!oldSave) player.skinon = Boolean.parseBoolean(data.get(12));
		else player.skinon = false;
	}
	
	public void loadInventory(String filename, Inventory inventory) {
		loadFromFile(location + filename + extention);
		inventory.clearInv();
		
		for(int i = 0; i < data.size(); i++) {
			String item = data.get(i);
			
			if(Items.get(item) instanceof StackableItem) {
				if(oldSave && i == 0) item = item.replace(";0", ";1");
				List<String> curData = Arrays.asList(item.split(";"));
				String itemName = curData.get(0);
				if(oldSave) itemName = subOldName(itemName);
				
				Item newItem = Items.get(itemName);
				
				for(int ii = 0; ii < Integer.parseInt(curData.get(1)); ii++) {
					inventory.add(newItem);
				}
			} else {
				if(oldSave) item = subOldName(item);
				Item toAdd = Items.get(item);
				inventory.add(toAdd);
			}
		}
	}
	
	private String subOldName(String oldName) {
		//System.out.println("old name: " + oldName);
		String newName = oldName.replace("P.", "Potion").replace("Fish Rod", "Fishing Rod").replace("bed", "Bed");
		newName = Load.subOldName(newName, worldVer);
		//System.out.println("new name: " + newName);
		return newName;
	}
	
	public void loadEntities(String filename, Player player) {
		loadFromFile(location + filename + extention);
		
		for(int i = 0; i < Game.levels.length; i++) {
			Game.levels[i].entities.clear();
		}
		
		for(int i = 0; i < data.size(); i++) {
			List<String> info = Arrays.asList(data.get(i).substring(data.get(i).indexOf("[") + 1, data.get(i).indexOf("]")).split(":")); // this gets everything inside the "[...]" after the entity name.
			
			String entityName = data.get(i).substring(0, data.get(i).indexOf("[")).replace("bed", "Bed").replace("II", ""); // this gets the text before "[", which is the entity name.
			int x = Integer.parseInt(info.get(0));
			int y = Integer.parseInt(info.get(1));
			
			int mobLvl = 0;
			try {
				if(Class.forName("EnemyMob").isAssignableFrom(Class.forName(entityName)))
					mobLvl = Integer.parseInt(info.get(info.size()-2));
			} catch(ClassNotFoundException ex) {}
			
			Entity newEntity = getEntity(entityName, player, mobLvl);
			
			if(newEntity != null) { // the method never returns null, but...
				int currentlevel;
				if(newEntity instanceof Mob) {
					Mob mob = (Mob)newEntity;
					mob.health = Integer.parseInt(info.get(2));
					currentlevel = Integer.parseInt(info.get(info.size()-1));
					Game.levels[currentlevel].add(mob, x, y);
				} else if(newEntity instanceof Chest) {
					Chest chest = (Chest)newEntity;
					boolean isDeathChest = chest instanceof DeathChest;
					boolean isDungeonChest = chest instanceof DungeonChest;
					List<String> chestInfo = info.subList(2, info.size()-1);
					
					int endIdx = chestInfo.size()-(isDeathChest||isDungeonChest?1:0);
					for(int idx = 0; idx < endIdx; idx++) {
						String itemData = chestInfo.get(idx);
						if(worldVer.compareTo(new Load.Version("1.9.1")) < 0) // if this world is before 1.9.1
							if(itemData.equals("")) continue; // this skips any null items
						if(oldSave) itemData = subOldName(itemData);
						Item item = Items.get(itemData);
						if (item instanceof StackableItem) {
							String[] aitemData = (itemData + ";1").split(";"); // this appends ";1" to the end, meaning one item, to everything; but if it was already there, then it becomes the 3rd element in the list, which is ignored.
							StackableItem stack = (StackableItem)Items.get(aitemData[0]);
							stack.count = Integer.parseInt(aitemData[1]);
							chest.inventory.add(stack);
						} else {
							chest.inventory.add(item);
						}
					}
					
					if (isDeathChest) {
						((DeathChest)chest).time = Integer.parseInt(chestInfo.get(chestInfo.size()-1).replace("tl;", "")); // "tl;" is only for old save support
					} else if (isDungeonChest) {
						((DungeonChest)chest).isLocked = Boolean.parseBoolean(chestInfo.get(chestInfo.size()-1));
					}
					
					
					currentlevel = Integer.parseInt(info.get(info.size() - 1));
					Game.levels[currentlevel].add(chest instanceof DeathChest ? (DeathChest)chest : chest instanceof DungeonChest ? (DungeonChest)chest : chest, x, y);
				}
				else if(newEntity instanceof Spawner) {
					Spawner egg = new Spawner((MobAi)getEntity(info.get(2), player, Integer.parseInt(info.get(3))));
					//egg.lvl = Integer.parseInt(info.get(3));
					//egg.initMob((MobAi)getEntity(info.get(2), player, info.get(3)));
					currentlevel = Integer.parseInt((String)info.get(info.size() - 1));
					Game.levels[currentlevel].add(egg, x, y);
				}
				else {
					currentlevel = Integer.parseInt(info.get(2));
					Game.levels[currentlevel].add(newEntity, x, y);
				}
			} // end of entity not null conditional
		}
	}
	
	public Entity getEntity(String string, Player player, int moblvl) {
		switch(string) {
			case "Player": return (Entity)(player);
			case "Cow": return (Entity)(new Cow());
			case "Sheep": return (Entity)(new Sheep());
			case "Pig": return (Entity)(new Pig());
			case "Zombie": return (Entity)(new Zombie(moblvl));
			case "Slime": return (Entity)(new Slime(moblvl));
			case "Creeper": return (Entity)(new Creeper(moblvl));
			case "Skeleton": return (Entity)(new Skeleton(moblvl));
			case "Knight": return (Entity)(new Knight(moblvl));
			case "Snake": return (Entity)(new Snake(moblvl));
			case "AirWizard": return (Entity)(new AirWizard(moblvl>1));
			case "Spawner": return (Entity)(new Spawner(new Zombie(1)));
			case "Workbench": return (Entity)(new Crafter(Crafter.Type.Workbench));
			case "Chest": return (Entity)(new Chest());
			case "DeathChest": return (Entity)(new DeathChest());
			case "DungeonChest": return (Entity)(new DungeonChest());
			case "Anvil": return (Entity)(new Crafter(Crafter.Type.Anvil));
			case "Enchanter": return (Entity)(new Crafter(Crafter.Type.Enchanter));
			case "Loom": return (Entity)(new Crafter(Crafter.Type.Loom));
			case "Furnace": return (Entity)(new Crafter(Crafter.Type.Furnace));
			case "Oven": return (Entity)(new Crafter(Crafter.Type.Oven));
			case "Bed": return (Entity)(new Bed());
			case "Tnt": return (Entity)(new Tnt());
			case "Lantern": return (Entity)(new Lantern(Lantern.Type.NORM));
			case "IronLantern": return (Entity)(new Lantern(Lantern.Type.IRON));
			case "GoldLantern": return (Entity)(new Lantern(Lantern.Type.GOLD));
			//case "Spark": return (Entity)(new Spark());
			default : /*if(Game.debug)*/ System.out.println("LEGACYLOAD: unknown or outdated entity requested: " + string);
				return null;
		}
	}
}
