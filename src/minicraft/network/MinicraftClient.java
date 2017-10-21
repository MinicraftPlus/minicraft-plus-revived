package minicraft.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import minicraft.Game;
import minicraft.Settings;
import minicraft.entity.furniture.Bed;
import minicraft.entity.furniture.Chest;
import minicraft.entity.furniture.DeathChest;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.ItemEntity;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.level.Level;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.screen.DeadMenu;
import minicraft.screen.MultiplayerMenu;

/// This class is only used by the client runtime; the server runtime doesn't touch it.
public class MinicraftClient extends MinicraftConnection {
	
	private MultiplayerMenu menu;
	
	private enum State {
		LOGIN, LOADING, PLAY, DISCONNECTED
	}
	private State curState = State.DISCONNECTED;
	
	private boolean pingSuccessful = false; // this is more or less useless. -_-
	
	private HashMap<Integer, Long> entityRequests = new HashMap<>();
	
	private static Socket openSocket(String hostName, MultiplayerMenu menu) {
		InetAddress hostAddress;
		Socket socket;
		
		try {
			hostAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException ex) {
			System.err.println("Don't know about host " + hostName);
			menu.setError("host not found");
			ex.printStackTrace();
			return null;
		}
		
		try {
			socket = new Socket(hostAddress, PORT);
		} catch (IOException ex) {
			System.err.println("Problem connecting socket to server:");
			menu.setError(ex.getMessage().replace(" (Connection refused)", ""));
			ex.printStackTrace();
			return null;
		}
		
		return socket;
	}
	
	public MinicraftClient(String username, MultiplayerMenu menu, String hostName) {
		super("MinicraftClient", openSocket(hostName, menu));
		this.menu = menu;
		Game.ISONLINE = true;
		Game.ISHOST = false;
		
		if(super.isConnected()) {
			login(username);
			start();
		}
	}
	
	private void changeState(State newState) {
		curState = newState;
		
		switch(newState) {
			case LOGIN: sendData(InputType.LOGIN, ((RemotePlayer)Game.player).getUsername()+";"+Game.VERSION); break;
			
			case LOADING:
				Game.setMenu(menu);
				menu.setLoadingMessage("Tiles");
				sendData(InputType.LOAD, String.valueOf(Game.currentLevel));
				break;
			
			case PLAY:
				if (Game.debug) System.out.println("CLIENT: Begin game!");
				Game.levels[Game.currentLevel].add(Game.player);
				Game.readyToRenderGameplay = true;
				Game.setMenu(null);
				break;
		}
	}
	
	private void login(String username) {
		if (Game.debug) System.out.println("CLIENT: logging in to server...");
		
		try {
			Game.player = new RemotePlayer(Game.player, true, InetAddress.getLocalHost(), PORT);
			((RemotePlayer)Game.player).setUsername(username);
		} catch(UnknownHostException ex) {
			System.err.println("CLIENT could not get localhost address:");
			ex.printStackTrace();
			menu.setError("unable to get localhost address");
		}
		changeState(State.LOGIN);
		//return null;
	}
	
	private static String getPlayerData(Player player) {
		StringBuilder playerdata = new StringBuilder();
		List<String> sdata = new ArrayList<>();
		Save.writePlayer(player, sdata);
		if(sdata.size() > 0) // should always be the case
			playerdata.append(String.join(",", sdata.toArray(new String[0])));
		playerdata.append("\n");
		Save.writeInventory(player, sdata);
		if(sdata.size() == 0)
			playerdata.append("null");
		else
			playerdata.append(String.join(",", sdata.toArray(new String[0])));
		
		return playerdata.toString();
	}
	
	/** This method is responsible for parsing all data received by the socket. */
	public synchronized boolean parsePacket(InputType inType, String alldata) {
		String[] data = alldata.split(";");
		
		switch(inType) {
			case INVALID:
				System.err.println("CLIENT received error: " + alldata);
				menu.setError(alldata);
				endConnection();
				return false;
			
			case PING:
				sendData(InputType.PING, alldata);
				return true;
			
			case LOGIN:
				System.err.println("Server tried to login...");
				return false;
			
			case DISCONNECT:
				if (Game.debug) System.out.println("CLIENT: received disconnect");
				menu.setError("Server Disconnected."); // this sets the menu back to the multiplayer menu, and tells the user what happened.
				endConnection();
				return true;
			
			case GAME:
				Settings.set("mode", data[0]);
				Game.setTime(Integer.parseInt(data[1]));
				Game.gamespeed = Float.parseFloat(data[2]);
				Game.pastDay1 = Boolean.parseBoolean(data[3]);
				Game.scoreTime = Integer.parseInt(data[4]);
				
				if(Game.isMode("creative"))
					Items.fillCreativeInv(Game.player.inventory, false);
				
				return true;
			
			case INIT:
				//if (Game.debug) System.out.println("CLIENT: received INIT packet");
				if(curState != State.LOGIN) {
					//System.out.println("WARNING: client received init packet in state " + curState + "; ignoring packet.");
					return false;
				}
				
				changeState(State.LOADING);
				//curState = State.LOADING; // I don't want to do the change state sequence quite yet.
				menu.setLoadingMessage("World");
				
				String[] infostrings = alldata.split(",");
				int[] info = new int[infostrings.length];
				for(int i = 0; i < info.length; i++)
					info[i] = Integer.parseInt(infostrings[i]);
				Game.player.eid = info[0];
				Game.lvlw = info[1];
				Game.lvlh = info[2];
				Game.currentLevel = info[3];
				Game.player.x = info[4];
				Game.player.y = info[5];
				return true;
			
			case TILES:
				if(curState != State.LOADING) { // ignore
					if (Game.debug) System.out.println("ignoring level tile data because client state is not LOADING: " + curState);
					return false;
				}
				if (Game.debug) System.out.println("CLIENT: received tiles");
				/// recieve tiles.
				Level level = Game.levels[Game.currentLevel];
				if(level == null) {
					int lvldepth = Game.idxToDepth[Game.currentLevel];
					Game.levels[Game.currentLevel] = level = new Level(Game.lvlw, Game.lvlh, lvldepth, Game.levels[Game.lvlIdx(lvldepth+1)], false);
				}
				
				/*byte[] tiledata = new byte[alldata.length()];
				for(int i = 0; i < alldata.length(); i++) {
					int tbit = (int) alldata.charAt(i);
					tbit--;
					if(tbit >= 128) tbit -= 256;
					tiledata[i] = (byte) tbit;
				}*/
				String[] tilestrs = alldata.split(",");
				byte[] tiledata = new byte[tilestrs.length];
				for(int i = 0; i < tiledata.length; i++)
					tiledata[i] = Byte.parseByte(tilestrs[i]);
				
				//System.out.println("TILE DATA ARRAY AS RECEIVED BY CLIENT, DECODED BACK TO NUMBERS (length="+tiledata.length+"):");
				//System.out.println(Arrays.toString(tiledata));
				
				if(tiledata.length / 2 > level.tiles.length) {
					System.err.println("CLIENT ERROR: received level tile data is too long for world size; level.tiles.length="+level.tiles.length+", tiles in data: " + (tiledata.length / 2) + ". Will truncate tile loading.");
				}
				
				for(int i = 0; i < tiledata.length/2 && i < level.tiles.length; i++) {
					level.tiles[i] = tiledata[i*2];
					level.data[i] = tiledata[i*2+1];
				}
				
				menu.setLoadingMessage("Entities");
				
				return true;
			
			case ENTITIES:
				if(curState != State.LOADING) {// ignore
					System.out.println("ignoring level entity data becuase client state is not LOADING: " + curState);
					return false;
				}
				
				if (Game.debug) System.out.println("CLIENT: received entities");
				Level curLevel = Game.levels[Game.currentLevel];
				Game.player.setLevel(curLevel, Game.player.x, Game.player.y); // so the shouldTrack() calls check correctly.
				
				String[] entities = alldata.split(",");
				for(String entityString: entities) {
					if(entityString.length() == 0) continue;
					
					if (Game.debug) System.out.println("CLIENT: loading entity: " + entityString);
					Load.loadEntity(entityString, false);
				}
				
				// ready to start game now.
				changeState(State.PLAY); // this will be set before the client receives any cached entities, so that should work out.
				return true;
			
			case MOVE:
				Game.player.x = Integer.parseInt(data[1]);
				Game.player.y = Integer.parseInt(data[2]);
				int newLvlDepth = Integer.parseInt(data[0]);
				if(Game.player.getLevel() == null || newLvlDepth != Game.player.getLevel().depth) {
					// switch to the other level.
					Game.changeLevel(Game.lvlIdx(newLvlDepth) - Game.currentLevel);
				}
				return true;
			
			case TILE:
				Level theLevel = Game.levels[Integer.parseInt(data[0])];
				if(theLevel == null)
					return false; // ignore, this is for an unvisited level.
				int pos = Integer.parseInt(data[1]);
				theLevel.tiles[pos] = Byte.parseByte(data[2]);
				theLevel.data[pos] = Byte.parseByte(data[3]);
				//if (Game.debug) System.out.println("CLIENT: updated tile on lvl " + theLevel.depth + " to " + Tiles.get(theLevel.tiles[pos]).name);
				return true;
			
			case ADD:
				if(curState == State.LOADING)
					System.out.println("CLIENT: received entity addition while loading level");
				
				//if (Game.debug) System.out.println("CLIENT: received entity addition: " + alldata);
				
				if(alldata.length() == 0) {
					System.err.println("CLIENT WARNING: received entity addition is blank...");
					return false;
				}
				
				Entity addedEntity = Load.loadEntity(alldata, false);
				if(addedEntity != null) {
					if(addedEntity.eid == Game.player.eid/* && Game.player.getLevel() == null*/) {
						if (Game.debug) System.out.println("CLIENT: added main game player back to level based on add packet");
						Game.levels[Game.currentLevel].add(Game.player);
						Bed.inBed = false;
					}
					
					if(entityRequests.containsKey(addedEntity.eid))
						entityRequests.remove(addedEntity.eid);
					//else if(Game.debug && addedEntity instanceof RemotePlayer)
						//System.out.println("CLIENT: added remote player from packet: " + addedEntity + "; game player has eid " + Game.player.eid + "; this player has eid " + addedEntity.eid + "; are equal: " + (Game.player.eid == addedEntity.eid));
				}
				
				return true;
			
			case REMOVE:
				if(curState == State.LOADING)
					System.out.println("CLIENT: received entity removal while loading level");
				
				int eid = Integer.parseInt(alldata);
				//if (Game.debug) System.out.println("CLIENT: received entity removal: " + eid);
				
				Entity toRemove = Game.getEntity(eid);
				if(toRemove != null) {
					toRemove.remove();
					return true;
				}
				return false;
			
			case ENTITY:
				// these shouldn't occur while loading, becuase the server caches them. But just in case, let's make sure.
				if(curState == State.LOADING)
					System.out.println("CLIENT received entity update while loading level");
				
				int entityid = Integer.parseInt(alldata.substring(0, alldata.indexOf(";")));
				//if (Game.debug) System.out.println("CLIENT: received entity update for: " + entityid);
				String updates = alldata.substring(alldata.indexOf(";")+1);
				Entity entity = Game.getEntity(entityid);
				if(entity == null) {
					//System.err.println("CLIENT: couldn't find entity specified to update: " + entityid + "; could not apply updates: " + updates);
					if(entityRequests.containsKey(entityid) && (System.nanoTime() - entityRequests.get(entityid))/1E8 > 15L) { // this will make it so that there has to be at least 1.5 seconds between each time a certain entity is requested. Also, it won't request the entity the first time around; it has to wait a bit after the first attempt before it will actually request it.
						sendData(InputType.ENTITY, String.valueOf(entityid));
						entityRequests.put(entityid, System.nanoTime());
					}
					else if(!entityRequests.containsKey(entityid))
						entityRequests.put(entityid, (long)(System.nanoTime() - 7L*1E8)); // should "advance" the time so that it only takes 0.8 seconds after the first attempt to issue the actual request.
					return false;
				}
				else if(!((RemotePlayer)Game.player).shouldSync(entity.x >> 4, entity.y >> 4, entity.getLevel())) {
					// the entity is out of sync range; but not necessarily out of the tracking range, so it's *not* removed from the level here.
					return false;
				}
				else if(!((RemotePlayer)Game.player).shouldTrack(entity.x >> 4, entity.y >> 4, entity.getLevel())) {
					// the entity is out of tracking range, and so may as well be removed from the level.
					entity.remove();
					return false;
				}
				entity.update(updates);
				return true;
			
			case PLAYER:
				//if (Game.debug) System.out.println("CLIENT: received player packet");
				/*if(setPlayer) {
					if (Game.debug) System.out.println("CLIENT: ignoring set player, already set");
					return false;
				}*/
				// use the contained data to load up the player object vars.
				//if(Game.debug) System.out.println("CLIENT: player data received: " + alldata);
				String[] playerparts = alldata.split("\\n");
				List<String> playerinfo = Arrays.asList(playerparts[0].split(","));
				List<String> playerinv = Arrays.asList(playerparts[1].split(","));
				Load load = new Load();
				if (Game.debug) System.out.println("CLIENT: setting player vars from packet...");
				//if(Game.isMode("creative")) {
					//if(Game.debug) System.out.println("CLIENT: in creative mode, filling creative inv...");
					
				if(!(playerinv.size() == 1 && playerinv.get(0).equals("null")))
					load.loadInventory(Game.player.inventory, playerinv);
				load.loadPlayer(Game.player, playerinfo);
				//setPlayer = true;
				if(Game.getMenu() instanceof DeadMenu) {
					Game.setMenu(null);
				}
				return true;
			
			case SAVE:
				if (Game.debug) System.out.println("CLIENT: received save request");
				// send back the player data.
				if (Game.debug) System.out.println("CLIENT: sending save data");
				sendData(InputType.SAVE, getPlayerData(Game.player));
				return true;
			
			case NOTIFY:
				if (Game.debug) System.out.println("CLIENT: received notification");
				if(curState != State.PLAY) return true; // ignoring for now
				int notetime = Integer.parseInt(alldata.substring(0, alldata.indexOf(";")));
				String note = alldata.substring(alldata.indexOf(";")+1);
				Game.notifications.add(note);
				Game.notetick = notetime;
				return true;
			
			case CHESTOUT:
				if(curState != State.PLAY) return false; // shouldn't happen.
				Item item = Items.get(alldata);
				//if (Game.debug) System.out.println("CLIENT: received chestout with item: " + item);
				if(!Game.isMode("creative")) {
					Game.player.inventory.add(0, item);
					//if(Game.getMenu() instanceof InventoryMenu)
					//	((InventoryMenu)Game.getMenu()).onInvUpdate(Game.player.inventory);
				}
				//if (Game.debug) System.out.println("CLIENT successfully took " + item + " from chest and added to inv.");
				return true;
			
			case INTERACT:
				// the server went through with the interaction, and has sent back the new activeItem.
				//Item holdItem = Items.get(alldata);
				//if(Game.debug) System.out.println("CLIENT: received interaction success; setting player item to " + holdItem);
				Game.player.activeItem = Items.get(alldata);
				Game.player.resolveHeldItem();
				return true;
			
			case PICKUP:
				if(curState != State.PLAY) return false; // shouldn't happen.
				int ieid = Integer.parseInt(alldata);
				//if (Game.debug) System.out.println("CLIENT: received pickup approval for: " + ieid);
				Entity ie = Game.getEntity(ieid);
				if(ie == null || !(ie instanceof ItemEntity)) {
					System.err.println("CLIENT error with PICKUP response: specified entity does not exist or is not an ItemEntity: " + ieid);
					return false;
				}
				Game.player.pickupItem((ItemEntity)ie);
				return true;
			
			case POTION:
				boolean addEffect = Boolean.parseBoolean(data[0]);
				int typeIdx = Integer.parseInt(data[1]);
				PotionItem.applyPotion(Game.player, PotionType.values[typeIdx], addEffect);
				return true;
			
			case HURT:
				// the player got attacked.
				//if(Game.debug) System.out.println("CLIENT: received hurt packet");
				int hurteid = Integer.parseInt(data[0]);
				int damage = Integer.parseInt(data[1]);
				Direction attackDir = Direction.values[Integer.parseInt(data[2])];
				Entity p = Game.getEntity(hurteid);
				if (p instanceof Player)
					((Player)p).hurt(damage, attackDir);
				return true;
		}
		
		//System.out.println("CLIENT: received unexpected packet type " + inType + "; ignoring packet.");
		return false; // this isn't reached by anything, unless it's some packet type we aren't looking for. So in that case, return false.
	}
	
	/// the below methods are all about sending data to the server, *not* setting any game values.
	
	public void move(Player player) {
		//if(Game.debug) System.out.println("CLIENT: sending player movement to ("+player.x+","+player.y+"): " + player);
		String movedata = player.x+";"+player.y+";"+player.dir.ordinal()+";"+Game.lvlIdx(player.getLevel().depth);
		sendData(InputType.MOVE, movedata);
	}
	
	/** This is called when the player.attack() method is called. */
	public void requestInteraction(Player player) {
		/// I don't think the player parameter is necessary, but it doesn't harm anything.
		String itemString = player.activeItem != null ? player.activeItem.getData() : "null";
		sendData(InputType.INTERACT, itemString+";"+player.inventory.count(Items.arrowItem));
	}
	
	public void requestTile(Level level, int xt, int yt) {
		if (level == null) return;
		sendData(InputType.TILE, level.depth+";"+xt+";"+yt);
	}
	
	public void dropItem(Item drop) { sendData(InputType.DROP, drop.getData()); }
	
	public void sendPlayerUpdate(Player player) {
		if(player.getUpdates().length() > 0) {
			sendData(InputType.PLAYER, player.getUpdates());
			player.flushUpdates();
		}
	}
	
	public void sendPlayerDeath(Player player, DeathChest dc) {
		if(player != Game.player && Game.player != null) return; // this is client is not responsible for that player.
		Level level = Game.levels[Game.currentLevel];
		level.add(dc);
		dc.eid = -1;
		String chestData = Save.writeEntity(dc, false);
		level.remove(dc);
		sendData(InputType.DIE, chestData);
	}
	
	public void requestRespawn() { sendData(InputType.RESPAWN, ""); }
	
	public void addToChest(Chest chest, Item item) {
		if(chest == null || item == null) return;
		sendData(InputType.CHESTIN, chest.eid+";"+item.getData());
	}
	
	public void removeFromChest(Chest chest, int index, boolean wholeStack) {
		if(chest == null) return;
		sendData(InputType.CHESTOUT, chest.eid+";"+index+";"+wholeStack);
	}
	
	public void pushFurniture(Furniture f, Direction pushDir) { sendData(InputType.PUSH, String.valueOf(f.eid)); }
	
	public void pickupItem(ItemEntity ie) {
		if(ie == null) return;
		sendData(InputType.PICKUP, String.valueOf(ie.eid));
	}
	
	public void sendBedRequest(Player player, Bed bed) { sendData(InputType.BED, String.valueOf(bed.eid)); }
	
	public void requestLevel(int lvlidx) {
		Game.currentLevel = lvlidx; // just in case.
		changeState(State.LOADING);
	}
	
	public void endConnection() {
		if(isConnected() && curState == State.PLAY)
			sendData(InputType.SAVE, getPlayerData(Game.player)); // try to make sure that the player's info is saved before they leave.
		
		super.endConnection();
		
		curState = State.DISCONNECTED;
		
		// one may end the connection without an error; any errors should be set before calling this method, so there's no need to say anything here.
		if(Game.debug) System.out.println("client has ended its connection.");
	}
	
	public boolean isConnected() { return super.isConnected() && curState != State.DISCONNECTED; }
	
	public String toString() { return "CLIENT"; }
}
