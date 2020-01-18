package minicraft.core;

import minicraft.core.io.Localization;
import minicraft.core.io.Settings;
import minicraft.entity.furniture.Bed;
import minicraft.entity.mob.Player;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.saveload.Save;
import minicraft.screen.EndGameDisplay;
import minicraft.screen.LevelTransitionDisplay;
import minicraft.screen.PauseDisplay;
import minicraft.screen.PlayerDeathDisplay;
import minicraft.screen.WorldSelectDisplay;

public class Updater extends Game {
	private Updater() {}
	
	/// TIME AND TICKS
	
	public static final int normSpeed = 60; // measured in ticks / second.
	public static float gamespeed = 1; // measured in MULTIPLES OF NORMSPEED.
	public static boolean paused = true; // If the game is paused.
	
	public static int tickCount = 0; // The number of ticks since the beginning of the game day.
	static int time = 0; // Facilites time of day / sunlight.
	public static final int dayLength = 64800; //this value determines how long one game day is.
	public static final int sleepEndTime = dayLength/8; //this value determines when the player "wakes up" in the morning.
	public static final int sleepStartTime = dayLength/2+dayLength/8; //this value determines when the player allowed to sleep.
	//public static int noon = 32400; //this value determines when the sky switches from getting lighter to getting darker.
	
	public static int gameTime = 0; // This stores the total time (number of ticks) you've been playing your 
	public static boolean pastDay1 = true; // used to prevent mob spawn on surface on day 1.
	public static int scoreTime; // time remaining for score mode
	
	
	/// AUTOSAVE AND NOTIFICATIONS
	
	public static int notetick = 0; // "note"= notifications.
	
	private static final int astime = 7200; //stands for Auto-Save Time (interval)
	public static int asTick = 0; // The time interval between autosaves.
	public static boolean saving = false; // If the game is performing a save.
	public static int savecooldown; // Prevents saving many times too fast, I think.
	
	
	public enum Time {
		Morning (0),
		Day (dayLength/4),
		Evening (dayLength/2),
		Night (dayLength/4*3);
		
		public int tickTime;
		
		Time(int ticks) {
			tickTime = ticks;
		}
	}
	
	// VERY IMPORTANT METHOD!! Makes everything keep happening.
	// In the end, calls menu.tick() if there's a menu, or level.tick() if no menu.
	public static void tick() {
		if(newMenu != menu) {
			if(menu != null && (newMenu == null || newMenu.getParent() != menu))
				menu.onExit();
			
			//if(debug) System.out.println("Setting menu from " + newMenu + " to " + display);
			
			if (newMenu != null && (menu == null || newMenu != menu.getParent()))
				newMenu.init(menu);
			
			menu = newMenu;
		}
		
		if(isValidClient())
			Game.client.checkConnection();
		
		Level level = levels[currentLevel];
		if (Bed.sleeping() && !isValidClient()) {
			// IN BED
			if(gamespeed != 20) {
				gamespeed = 20;
				if(isValidServer()) {
					if (debug) System.out.println("SERVER: Setting time for bed");
					server.updateGameVars();
				}
			}
			if(tickCount > sleepEndTime) {
				if(Game.debug) System.out.println(Network.onlinePrefix()+"passing midnight in bed");
				pastDay1 = true;
				tickCount = 0;
				if(isValidServer())
					server.updateGameVars();
			}
			if (tickCount <= sleepStartTime && tickCount >= sleepEndTime) { // it has reached morning.
				if(Game.debug) System.out.println(Network.onlinePrefix()+"reached morning, getting out of bed");
				gamespeed = 1;
				if(isValidServer())
					server.updateGameVars();
				Bed.restorePlayers();
			}
		}
		
		//auto-save tick; marks when to do autosave.
		if(!paused || isValidServer())
			asTick++;
		if (asTick > astime) {
			if ((boolean) Settings.get("autosave") && !gameOver && (isValidServer() || player.health > 0) ) {
				if(!ISONLINE)
					new Save(WorldSelectDisplay.getWorldName());
				else if(isValidServer())
					server.saveWorld();
			}
			
			asTick = 0;
		}
		
		// Increment tickCount if the game is not paused
		if (!paused || isValidServer()) setTime(tickCount+1);
		
		/// SCORE MODE ONLY
		
		if (isMode("score") && (!paused || isValidServer() && !gameOver)) {
			if (scoreTime <= 0) { // GAME OVER
				gameOver = true;
				setMenu(new EndGameDisplay(player));
			}
			
			scoreTime--;
		}
		
		boolean hadMenu = menu != null;
		if(isValidServer()) {
			/// this is to keep the game going while online, even with an unfocused window.
			input.tick();
			for (Level floor : levels) {
				if (floor == null) continue;
				floor.tick(true);
			}
			
			Tile.tickCount++;
		}
		
		// This is the general action statement thing! Regulates menus, mostly.
		if (!Renderer.canvas.hasFocus() && HAS_GUI) {
			input.releaseAll();
		}
		if(Renderer.canvas.hasFocus() || ISONLINE || !HAS_GUI) {
			if ((isValidServer() || !player.isRemoved()) && !gameOver) {
				gameTime++;
			}
			
			if(!isValidServer() || menu != null && !hadMenu)
				input.tick(); // INPUT TICK; no other class should call this, I think...especially the *Menu classes.
			
			if(isValidClient() && Renderer.readyToRenderGameplay) {
				for(int i = 0; i < levels.length; i++)
					if(levels[i] != null)
						levels[i].tick(i == currentLevel);
			}
			
			if (menu != null) {
				//a menu is active.
				if(player != null)
					player.tick(); // it is CRUCIAL that the player is ticked HERE, before the menu is ticked. I'm not quite sure why... the menus break otherwise, though.
				menu.tick(input);
				paused = true;
			} else {
				//no menu, currently.
				paused = false;
				
				if(!isValidServer()) {
					//if player is alive, but no level change, nothing happens here.
					if (player.isRemoved() && Renderer.readyToRenderGameplay && !Bed.inBed(player)) {
						//makes delay between death and death menu.
						World.playerDeadTime++;
						if (World.playerDeadTime > 60) {
							setMenu(new PlayerDeathDisplay());
						}
					} else if (World.pendingLevelChange != 0) {
						setMenu(new LevelTransitionDisplay(World.pendingLevelChange));
						World.pendingLevelChange = 0;
					}
					
					player.tick(); // ticks the player when there's no menu.
					if(isValidClient() && Bed.inBed(player) && !Bed.sleeping() && input.getKey("exit").clicked)
						Game.client.sendBedExitRequest();
					
					if(level != null) {
						if(!isValidClient())
							level.tick(true);
						Tile.tickCount++;
					}
				}
				else if(isValidServer()) {
					// here is where I should put things like select up/down, backspace to boot, esc to open pause menu, etc.
					if(input.getKey("pause").clicked)
						setMenu(new PauseDisplay());
				}
				
				if(menu == null && input.getKey("F3").clicked) { // shows debug info in upper-left
					Renderer.showinfo = !Renderer.showinfo;
				}
				
				//for debugging only
				if (debug && HAS_GUI) {
					
					if(input.getKey("ctrl-p").clicked) {
						// print all players on all levels, and their coordinates.
						System.out.println("Printing players on all levels "+Network.onlinePrefix());
						for(int i = 0; i < levels.length; i++) {
							if(levels[i] == null) continue;
							levels[i].printEntityLocs(Player.class);
						}
					}
					
					if(!ISONLINE || isValidServer()) {
						/// host-only cheats.
						if (input.getKey("Shift-r").clicked && !isValidServer())
							World.initWorld(); // for single-player use only.
						
						if (input.getKey("1").clicked) changeTimeOfDay(Time.Morning);
						if (input.getKey("2").clicked) changeTimeOfDay(Time.Day);
						if (input.getKey("3").clicked) changeTimeOfDay(Time.Evening);
						if (input.getKey("4").clicked) changeTimeOfDay(Time.Night);
						
						String prevMode = (String)Settings.get("mode");
						if (input.getKey("creative").clicked) {
							Settings.set("mode", "creative");
							if(!ISONLINE)
								Items.fillCreativeInv(player.getInventory(), false);
						}
						if (input.getKey("survival").clicked) Settings.set("mode", "survival");
						if (input.getKey("shift-t").clicked) Settings.set("mode", "score");
						if(!Settings.get("mode").equals(prevMode) && isValidServer())
							server.updateGameVars(); // gamemode changed
						
						if (isMode("score") && input.getKey("ctrl-t").clicked) {
							scoreTime = normSpeed * 5; // 5 seconds
							if(isValidServer()) server.updateGameVars();
						}
						
						float prevSpeed = gamespeed;
						if (input.getKey("shift-0").clicked)
							gamespeed = 1;
						
						if (input.getKey("shift-equals").clicked) {
							if(gamespeed < 1) gamespeed *= 2;
							else if(normSpeed*gamespeed < 2000) gamespeed++;
						}
						if (input.getKey("shift-minus").clicked) {
							if(gamespeed > 1) gamespeed--;
							else if(normSpeed*gamespeed > 5) gamespeed /= 2;
						}
						if(gamespeed != prevSpeed && isValidServer())
							server.updateGameVars();
					}
					
					
					if(!ISONLINE || isValidClient()) {
						/// client-only cheats, since they are player-specific.
						
						if (input.getKey("shift-g").clicked) // this should not be needed, since the inventory should not be altered.
							Items.fillCreativeInv(player.getInventory());
						
						if(input.getKey("ctrl-h").clicked) player.health--;
						if(input.getKey("ctrl-b").clicked) player.hunger--;
						
						if (input.getKey("0").clicked) player.moveSpeed = 1;
						if (input.getKey("equals").clicked) player.moveSpeed++;
						if (input.getKey("minus").clicked && player.moveSpeed > 1) player.moveSpeed--;// -= 0.5D;
						
						if(input.getKey("shift-u").clicked) {
							levels[currentLevel].setTile(player.x>>4, player.y>>4, Tiles.get("Stairs Up"));
						}
						if(input.getKey("shift-d").clicked) {
							levels[currentLevel].setTile(player.x>>4, player.y>>4, Tiles.get("Stairs Down"));
						}
						
						if(isConnectedClient() && input.getKey("alt-t").clicked) {
							// update the tile with the server's value for it.
							client.requestTile(player.getLevel(), player.x >> 4, player.y >> 4);
						}
					}
				} // end debug only cond.
			} // end "menu-null" conditional
		} // end hasfocus conditional
	} // end tick()
	
	
	/// this is the proper way to change the tickCount.
	public static void setTime(int ticks) {
		if (ticks < Time.Morning.tickTime) ticks = 0; // error correct
		if (ticks < Time.Day.tickTime) time = 0; // morning
		else if (ticks < Time.Evening.tickTime) time = 1; // day
		else if (ticks < Time.Night.tickTime) time = 2; // evening
		else if (ticks < dayLength) time = 3; // night
		else { // back to morning
			time = 0;
			ticks = 0;
			pastDay1 = true;
		}
		tickCount = ticks;
	}
	
	/// this is the proper way to change the time of day.
	public static void changeTimeOfDay(Time t) {
		setTime(t.tickTime);
		if(isValidServer())
			server.updateGameVars();
	}
	// this one works too.
	public static void changeTimeOfDay(int t) {
		Time[] times = Time.values();
		if(t > 0 && t < times.length)
			changeTimeOfDay(times[t]); // it just references the other one.
		else
			System.out.println("Time " + t + " does not exist.");
	}
	
	public static Time getTime() {
		Time[] times = Time.values();
		return times[time];
	}
	
	/** This adds a notification to all player games. */
	public static void notifyAll(String msg) {
		notifyAll(msg, 0);
	}
	public static void notifyAll(String msg, int notetick) {
		msg = Localization.getLocalized(msg);
		notifications.add(msg);
		Updater.notetick = notetick;
		if(isValidServer())
			server.broadcastNotification(msg, notetick);
		else if(isConnectedClient())
			client.sendNotification(msg, notetick);
	}
}
