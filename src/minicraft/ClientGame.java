package minicraft;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*; // TODO these .*'s are unnecessary.
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import minicraft.entity.Bed;
import minicraft.entity.Entity;
import minicraft.entity.Furniture;
import minicraft.entity.Lantern;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.entity.RemotePlayer;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteSheet;
import minicraft.item.Items;
import minicraft.item.PotionType;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.network.MinicraftServer;
import minicraft.network.MinicraftServerThread;
import minicraft.network.MinicraftClient;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.screen.*;

public class ClientGame extends Game {
	
	private static Random random = new Random();
	
	public MinicraftServerThread connection;
	
	/** This constructor is used for each remote player, server-side only. */
	public ClientGame(MinicraftServerThread connection) {
		super();
		input = new InputHandler(this, false);
		this.connection = connection;
		init();
	}
	
	/** This method is used when respawning, and by initWorld to reset the vars. It does not generate any new terrain. */
	public void resetGame() {
		playerDeadTime = 0;
		currentLevel = 3;
		asTick = 0;
		notifications.clear();
		
		// adds a new player
		player = new RemotePlayer(this);
		
		// "shouldRespawn" is false on hardcore, or when making a new world.
		if (DeadMenu.shouldRespawn) { // respawn, don't regenerate level.
			//if (debug) System.out.println("Current Level = " + currentLevel);
			
			Level level = Game.levels[currentLevel];
			player.respawn(level);
			//if (debug) System.out.println("respawned player in current world");
			level.add(player); // adds the player to the current level (always surface here)
		}
		
		if(WorldGenMenu.get("Theme").equals("Hell")) {
			player.inventory.add(Items.get("lava potion"));
		}
	}
	
	public void initWorld() { resetGame(); }
	
	
	private boolean hadMenu = true;
	// VERY IMPORTANT METHOD!! Makes everything keep happening.
	// In the end, calls menu.tick() if there's a menu, or level.tick() if no menu.
	public void tick() {
		if(connection == null || !connection.isConnected()) {
			System.out.println("socket is disconnected from client");
			if(connection != null)
				connection.endConnection();
			else {
				Game.levels[currentLevel].remove(player);
				Game.server.threadList.remove(connection); // really is just remove(null)
			}
			return;
		}
		
		Level level = levels[currentLevel];
		
		if (gameOver)
			setMenu(new WonMenu(player));
		else if (ModeMenu.score) {
			if (!paused && multiplier > 1) {
				if (multipliertime != 0) multipliertime--;
				if (multipliertime == 0) setMultiplier(1);
			}
			if (multiplier > 50) multiplier = 50;
		}
		
		//This is the general action statement thing! Regulates menus, mostly.
		//input.tick(); //INPUT TICK; no other class should call this, I think...especially the *Menu classes.
		synchronized ("lock") {
			//System.out.println("processing unread key presses from client "+connection.getClientName()+", to remote player input");
			for(String keystate: connection.currentInput) {
				String[] parts = keystate.split("=");
				String key = parts[0];
				boolean pressed = Boolean.parseBoolean(parts[1]);
				//System.out.println("toggling key " + key + " on remote player " + connection.getClientName() + " to " + pressed);
				input.pressKey(key, pressed);
			}
			/*for(String keyname: input.getAllPressedKeys())
				if(!connection.currentInput.contains(keyname))
					input.pressKey(keyname, false);
			*/
			connection.currentInput.clear();
		}
		
		if(!hadMenu && menu != null)
			input.tick();
		
		if (menu != null) {
			//a menu is active.
			menu.tick();
			hadMenu = true;
			paused = true;
		} else {
			//no menu, currently.
			hadMenu = false;
			paused = false;
			
			//if player is in a level, but no level change, nothing happens here.
			if (player.removed) {
				//makes delay between death and death menu.
				playerDeadTime++;
				if (playerDeadTime > 60) {
					setMenu(new DeadMenu());
				}
			} else if (pendingLevelChange != 0) {
				setMenu(new LevelTransitionMenu(pendingLevelChange));
				pendingLevelChange = 0;
			}
			
			// I'd better leave this up to the main Game class, just so I don't tick a level twice.
			//level.tick();
			
			//for debugging only; for this class, I think if the host is in debug mode, then everyone is.
			if (Game.debug) {
				if (input.getKey("Shift-0").clicked)
					initWorld();
				
				// this should not be needed, since the inventory should not be altered.
				if (input.getKey("shift-g").clicked) {
					Items.fillCreativeInv(player.inventory);
				}
				
				if(input.getKey("ctrl-h").clicked) player.health--;
				
				if (input.getKey("equals").clicked) player.moveSpeed++;//= 0.5D;
				if (input.getKey("minus").clicked && player.moveSpeed > 1) player.moveSpeed--;// -= 0.5D;
				
				if(input.getKey("shift-space").clicked) {
					int tx = player.x >> 4;
					int ty = player.y >> 4;
					System.out.println("current tile: " + levels[currentLevel].getTile(tx, ty).name);
				}
			} // end debug only cond.
		} // end "menu-null" conditional
	} // end tick()
	
	// this shouldn't be run.
	public void run() {}
}
