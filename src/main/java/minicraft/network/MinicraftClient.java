package minicraft.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import minicraft.core.Game;
import minicraft.core.Network;
import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.Settings;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.furniture.Bed;
import minicraft.entity.furniture.Chest;
import minicraft.entity.furniture.DeathChest;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.level.Level;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.saveload.Version;
import minicraft.screen.ContainerDisplay;
import minicraft.screen.MultiplayerDisplay;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import org.jetbrains.annotations.Nullable;

// This class is only used by the client runtime; the server runtime doesn't touch it.
public class MinicraftClient extends MinicraftConnection {
	
	public static final int DEFAULT_CONNECT_TIMEOUT = 5_000; // in milliseconds
	
	private MultiplayerDisplay menu;
	
	private enum State {
		LOGIN, LOADING, PLAY, RESPAWNING, DISCONNECTED
	}
	private State curState = State.DISCONNECTED;
	
	private HashMap<Integer, Long> entityRequests = new HashMap<>();
	
	private int serverPlayerCount = 0;
	
	@Nullable
	private static Socket openSocket(String hostName, MultiplayerDisplay menu, int connectTimeout) {
		InetAddress hostAddress;
		Socket socket;
		
		socket = connectWithSrv(hostName, menu, connectTimeout);
		if(socket != null)
			return socket;
		
		if (Game.debug) System.out.println("Getting host address from host name \"" + hostName + "\"...");

		// check if there's a custom port to connect on
		int port = MinicraftProtocol.PORT;
		String[] splitHostName = hostName.split(":");
		if (splitHostName.length > 1) {
			hostName = splitHostName[0];
			try {
				port = Integer.parseInt(splitHostName[1]);
			} catch (NumberFormatException exception) {
				System.err.println("Invalid port: " + splitHostName[1]);
				menu.setError("Invalid port");
				exception.printStackTrace();
				return null;
			}
		}

		try {
			hostAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException ex) {
			System.err.println("Don't know about host " + hostName);
			menu.setError("host not found");
			ex.printStackTrace();
			return null;
		}
		
		if (Game.debug) System.out.println("Host found. Attempting to open socket...");
		
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(hostAddress, port), connectTimeout);
		} catch (IOException ex) {
			System.err.println("Problem connecting socket to server:");
			menu.setError(ex.getMessage().replace(" (Connection refused)", ""));
			ex.printStackTrace();
			return null;
		}
		
		if (Game.debug) System.out.println("Successfully connected to game server. Returning socket...");
		
		return socket;
	}
	
	@Nullable
	private static Socket connectWithSrv(String hostName, MultiplayerDisplay menu, int connectTimeout) {
		// Perform an SRV lookup to determine if any exist
		String query = "_minicraft._tcp." + hostName;
		Record[] records;
		
		try {
			records = new Lookup(query, Type.SRV).run();
		} catch(TextParseException e) {
			if(Game.debug) System.err.println("Error running SRV lookup on '"+hostName+"', skipping.");
			return null;
		}
		
		if(records == null) {
			// no records
			if(Game.debug) System.out.println("No SRV records found.");
			return null;
		}
		
		// record-sorting queue
		PriorityQueue<SRVRecord> queue = new PriorityQueue<>((rec1, rec2) -> {
			// get smaller priority
			int pComp = Integer.compare(rec1.getPriority(), rec2.getPriority());
			if(pComp != 0)
				return pComp;
			// get larger weight
			int wComp = Integer.compare(rec2.getWeight(), rec1.getWeight());
			if(wComp != 0)
				return wComp;
			
			// else equal
			return 0;
		});
		
		// menu.setWaitMessage("Trying available servers");
		
		// run through each record in the right order and try to connect
		for(Record record: records) {
			if(Game.debug) System.out.println("Found SRV record: "+record.toString());
			SRVRecord srec = (SRVRecord) record;
			if(Game.debug) {
				System.out.println("SRV record data:");
				System.out.println("Target: '"+srec.getTarget()+"'");
				System.out.println("Port: '"+srec.getPort()+"'");
				System.out.println("Priority: '"+srec.getPriority()+"'");
				System.out.println("Weight: '"+srec.getWeight()+"'");
			}
			
			queue.add(srec);
		}
		
		// attempt to connect to each record in order of their precedence
		int serverIdx = 1;
		while(!queue.isEmpty()) {
			SRVRecord srec = queue.poll();
			
			// remove trailing .
			String address = srec.getTarget().toString();
			int port = srec.getPort();
			
			menu.setWaitMessage("Trying server "+serverIdx+"/"+records.length);
			try {
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(address, port), connectTimeout);
				return socket;
			} catch (IOException ex) {
				System.err.println("Failed to connect to server "+serverIdx+"/"+records.length+": "+ex.getMessage());
			}
			serverIdx++;
		}
		
		menu.setWaitMessage("Trying main server");
		
		return null;
	}
	
	public MinicraftClient(String username, MultiplayerDisplay menu, String hostName) { this(username, menu, hostName, DEFAULT_CONNECT_TIMEOUT); }
	public MinicraftClient(String username, MultiplayerDisplay menu, String hostName, int connectTimeout) {
		super("MinicraftClient", openSocket(hostName, menu, connectTimeout));
		this.menu = menu;
		Game.ISONLINE = true;
		Game.ISHOST = false;
		
		if (super.isConnected()) {
			login(username);
			start();
		}
	}
	
	public int getPlayerCount() { return serverPlayerCount; }
	
	private void changeState(State newState) {
		if (Game.debug) System.out.println("CLIENT: Client state change from " + curState + " to " +newState);
		curState = newState;
		
		switch(newState) {
			case LOGIN: sendData(InputType.LOGIN, ((RemotePlayer)Game.player).getUsername()+ ";" +Game.VERSION); break;
			
			case LOADING:
				Game.setMenu(menu);
				menu.setLoadingMessage("Tiles");
				sendData(InputType.LOAD, String.valueOf(Game.currentLevel));
				break;
			
			case PLAY:
				if (Game.debug) System.out.println("CLIENT: Begin game!");
				World.levels[Game.currentLevel].add(Game.player);
				Renderer.readyToRenderGameplay = true;
				Game.setMenu(null);
				break;
			
			case RESPAWNING:
				Game.setMenu(menu);
				menu.setLoadingMessage("Spawnpoint");
				sendData(InputType.RESPAWN, "");
				break;
		}
	}
	
	private void login(String username) {
		if (Game.debug) System.out.println("CLIENT: Logging in to server...");
		
		try {
			Game.player = new RemotePlayer(Game.player, true, InetAddress.getLocalHost(), getConnectedPort());
			((RemotePlayer)Game.player).setUsername(username);
		} catch(UnknownHostException ex) {
			System.err.println("CLIENT: Could not get localhost address");
			ex.printStackTrace();
			menu.setError("unable to get localhost address");
		}
		changeState(State.LOGIN);
	}
	
	/** This method is responsible for parsing all data received by the socket. */
	public boolean parsePacket(InputType inType, String alldata) {
		String[] data = alldata.split(";");

		if (Game.packet_debug) System.out.println("Received:" + inType.toString() + ", " + alldata);
		
		switch(inType) {
			case INVALID:
				System.err.println("CLIENT: Received error: " + alldata);
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
				if (Game.debug) System.out.println("CLIENT: Received disconnect");
				menu.setError("Server Disconnected."); // this sets the menu back to the multiplayer menu, and tells the user what happened.
				endConnection();
				return true;
			
			case GAME:
				Settings.set("mode", data[0]);
				Updater.setTime(Integer.parseInt(data[1]));
				Updater.gamespeed = Float.parseFloat(data[2]);
				Updater.pastDay1 = Boolean.parseBoolean(data[3]);
				Updater.scoreTime = Integer.parseInt(data[4]);
				serverPlayerCount = Integer.parseInt(data[5]);
				Bed.setPlayersAwake(Integer.parseInt(data[6]));
				
				if (Game.isMode("creative"))
					Items.fillCreativeInv(Game.player.getInventory(), false);
				
				return true;
			
			case INIT:
				// server has validated ability to join
				Analytics.SessionJoin.ping();
				
				if (curState != State.LOGIN) {
					return false;
				}
				
				changeState(State.LOADING);
				//curState = State.LOADING; // I don't want to do the change state sequence quite yet.
				menu.setLoadingMessage("World");
				
				String[] infostrings = alldata.split(",");
				int[] info = new int[infostrings.length];
				for (int i = 0; i < info.length; i++)
					info[i] = Integer.parseInt(infostrings[i]);
				Game.player.eid = info[0];
				World.lvlw = info[1];
				World.lvlh = info[2];
				World.currentLevel = info[3];
				Game.player.x = info[4];
				Game.player.y = info[5];
				return true;
			
			case TILES:
				if (curState != State.LOADING) { // ignore
					if (Game.debug) System.out.println("Ignoring level tile data because client state is not LOADING: " + curState);
					return false;
				}
				if (Game.debug) System.out.println("CLIENT: Received tiles for level " + World.currentLevel);
				
				Level level = World.levels[World.currentLevel]; // receive tiles.
				if (level == null) {
					int lvldepth = World.idxToDepth[World.currentLevel];
					World.levels[World.currentLevel] = level = new Level(World.lvlw, World.lvlh, lvldepth, World.levels[World.lvlIdx(lvldepth+1)], false);
				}
				
				String[] tilestrs = alldata.split(",");
				byte[] tiledata = new byte[tilestrs.length];
				for (int i = 0; i < tiledata.length; i++)
					tiledata[i] = Byte.parseByte(tilestrs[i]);
				
				if (tiledata.length / 2 > level.tiles.length) {
					System.err.println("CLIENT ERROR: Received level tile data is too long for world size; level.tiles.length=" + level.tiles.length + ", tiles in data: " + (tiledata.length / 2) + ". Will truncate tile loading.");
				}
				
				for (int i = 0; i < tiledata.length/2 && i < level.tiles.length; i++) {
					level.tiles[i] = tiledata[i*2];
					level.data[i] = tiledata[i*2+1];
				}
				
				menu.setLoadingMessage("Entities");
				
				if (World.onChangeAction != null) {
					World.onChangeAction.act();
					World.onChangeAction = null;
				}
				
				return true;
			
			case ENTITIES:
				if (curState != State.LOADING) {// ignore
					System.out.println("Ignoring level entity data because client state is not LOADING: " + curState);
					return false;
				}
				
				if (Game.debug) System.out.println("CLIENT: Received entities");
				Level curLevel = World.levels[Game.currentLevel];
				Game.player.setLevel(curLevel, Game.player.x, Game.player.y); // so the shouldTrack() calls check correctly.
				
				String[] entities = alldata.split(",");
				for (String entityString: entities) {
					if (entityString.length() == 0) continue;
					
					if (Game.debug) System.out.println("CLIENT: Loading entity: " + entityString);
					Load.loadEntity(entityString, false);
				}
				
				// Ready to start game now.
				changeState(State.PLAY); // This will be set before the client receives any cached entities, so that should work out.
				return true;
			
			case TILE:
				Level theLevel = World.levels[Integer.parseInt(data[0])];
				if (theLevel == null) return false; // ignore, this is for an unvisited level.
				int pos = Integer.parseInt(data[1]);
				theLevel.tiles[pos] = Byte.parseByte(data[2]);
				theLevel.data[pos] = Byte.parseByte(data[3]);
				return true;
			
			case ADD:
				if (curState == State.LOADING)
					System.out.println("CLIENT: Received entity addition while loading level");
				
				if (alldata.length() == 0) {
					System.err.println("CLIENT WARNING: Received entity addition is blank...");
					return false;
				}
				
				Entity addedEntity = Load.loadEntity(alldata, false);
				if (addedEntity != null) {
					if (addedEntity.eid == Game.player.eid) {
						if (Game.debug) System.out.println("CLIENT: Added main game player back to level based on add packet");
						World.levels[Game.currentLevel].add(Game.player);
						Bed.removePlayer(Game.player);
					}

					entityRequests.remove(addedEntity.eid);
				}
				
				return true;
			
			case REMOVE:
				if (curState == State.LOADING)
					System.out.println("CLIENT: Received entity removal while loading level");
				
				int eid = Integer.parseInt(data[0]);
				Integer entityLevelDepth;
				if (data.length > 1)
					entityLevelDepth = Integer.parseInt(data[1]);
				else
					entityLevelDepth = null;
				
				Entity toRemove = Network.getEntity(eid);
				if (toRemove != null) {
					if (entityLevelDepth != null && toRemove.getLevel() != null && toRemove.getLevel().depth != entityLevelDepth) {
						if (Game.debug) System.out.println("CLIENT: Not removing entity " + toRemove + " because it is not on the specified level depth, " + entityLevelDepth + "; current depth = " + toRemove.getLevel().depth + ". Removing from specified level only...");
						Level l = World.levels[World.lvlIdx(entityLevelDepth)];
						if (l != null)
							l.remove(toRemove);
					}
					else
						toRemove.remove();
					return true;
				}
				return false;
			
			case ENTITY: // TODO: Make this method easier to look at
				// these shouldn't occur while loading, becuase the server caches them. But just in case, let's make sure.
				if (curState == State.LOADING)
					System.out.println("CLIENT: Received entity update while loading level");
				
				int entityid = Integer.parseInt(alldata.substring(0, alldata.indexOf(";")));
				String updates = alldata.substring(alldata.indexOf(";")+1);
				if (entityid == Game.player.eid) {
					Game.player.update(updates);
					return true;
				}
				Entity entity = Network.getEntity(entityid);
				if (entity == null) {
					if (entityRequests.containsKey(entityid) && (System.nanoTime() - entityRequests.get(entityid))/1E8 > 15L) { // this will make it so that there has to be at least 1.5 seconds between each time a certain entity is requested. Also, it won't request the entity the first time around; it has to wait a bit after the first attempt before it will actually request it.
						sendData(InputType.ENTITY, String.valueOf(entityid));
						entityRequests.put(entityid, System.nanoTime());
					}
					else if (!entityRequests.containsKey(entityid))
						entityRequests.put(entityid, (long)(System.nanoTime() - 7L*1E8)); // should "advance" the time so that it only takes 0.8 seconds after the first attempt to issue the actual request.
					return false;
				}
				else if (!((RemotePlayer)Game.player).shouldSync(entity.x >> 4, entity.y >> 4, entity.getLevel())) { // the entity is out of sync range; but not necessarily out of the tracking range, so it's *not* removed from the level here.
					return false;
				}
				else if (!((RemotePlayer)Game.player).shouldTrack(entity.x >> 4, entity.y >> 4, entity.getLevel())) { // the entity is out of tracking range, and so may as well be removed from the level.
					entity.remove();
					return false;
				}
				entity.update(updates);
				return true;
			
			case PLAYER:
				String[] playerparts = alldata.split("\\n");
				List<String> playerinfo = Arrays.asList(playerparts[1].split(","));
				List<String> playerinv = Arrays.asList(playerparts[2].split(","));
				Load load = new Load(new Version(playerparts[0]));
				if (Game.debug) System.out.println("CLIENT: Setting player vars from packet...");
				
				if (!(playerinv.size() == 1 && playerinv.get(0).equals("null")))
					load.loadInventory(Game.player.getInventory(), playerinv);
				load.loadPlayer(Game.player, playerinfo);
				
				if (curState == State.RESPAWNING)
					changeState(State.LOADING); // load the new data
				return true;
			
			case SAVE:
				if (Game.debug) System.out.println("CLIENT: Received save request");
				if (Game.debug) System.out.println("CLIENT: Sending save data");
				sendData(InputType.SAVE, Game.player.getPlayerData()); // send back the player data.
				return true;
			
			case NOTIFY:
				if (Game.debug) System.out.println("CLIENT: Received notification");
				if (curState != State.PLAY) return true; // ignoring for now
				int notetime = Integer.parseInt(alldata.substring(0, alldata.indexOf(";")));
				String note = alldata.substring(alldata.indexOf(";")+1);
				Game.notifications.add(note);
				Updater.notetick = notetime;
				return true;
			
			case CHESTOUT:
				if (curState != State.PLAY) return false; // shouldn't happen.
				Item item = Items.get(data[0]);
				int idx = Integer.parseInt(data[1]);
				Inventory playerInv = Game.player.getInventory();
				if (idx > playerInv.invSize())
					idx = playerInv.invSize();
				if (!Game.isMode("creative")) {
					Game.player.getInventory().add(idx, item);
					if (Game.getMenu() instanceof ContainerDisplay)
						((ContainerDisplay)Game.getMenu()).onInvUpdate(Game.player);
				}
				return true;
			
			case ADDITEMS:
				Inventory inv = Game.player.getInventory();
				for (String itemStr: data)
					inv.add(Items.get(itemStr));
				return true;
			
			case INTERACT:
				// the server went through with the interaction, and has sent back the new activeItem.
				Game.player.activeItem = Items.get(alldata, true);
				Game.player.resolveHeldItem();
				return true;
			
			case PICKUP:
				if (curState != State.PLAY) return false; // shouldn't happen.
				int ieid = Integer.parseInt(alldata);
				Entity ie = Network.getEntity(ieid);
				if (ie == null || !(ie instanceof ItemEntity)) {
					System.err.println("CLIENT: Error with PICKUP response: specified entity does not exist or is not an ItemEntity: " + ieid);
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
				int hurteid = Integer.parseInt(data[0]);
				int damage = Integer.parseInt(data[1]);
				Direction attackDir = Direction.values[Integer.parseInt(data[2])];
				Entity p = Network.getEntity(hurteid);
				if (p instanceof Player)
					((Player)p).hurt(damage, attackDir);
				return true;
			
			case STAMINA:
				Game.player.payStamina(Integer.parseInt(alldata));
				return true;

			case STOPFISHING:
				int stopeid = Integer.parseInt(data[0]);
				Entity player = Network.getEntity(stopeid);
				if (player instanceof Player) {
					((Player) player).isFishing = false;
					((Player) player).fishingTicks = ((Player) player).maxFishingTicks;
				}
				return true;
		}
		return false; // this isn't reached by anything, unless it's some packet type we aren't looking for. So in that case, return false.
	}
	
	// The below methods are all about sending data to the server, *not* setting any game values.
	
	public void move(Player player, int x, int y) {
		String movedata = x+ ";" +y+ ";" +player.dir.ordinal()+ ";" +World.lvlIdx(player.getLevel().depth);
		sendData(InputType.MOVE, movedata);
	}
	
	/** This is called when the player.attack() method is called. */
	public void requestInteraction(Player player) {
		// I don't think the player parameter is necessary, but it doesn't harm anything.
		String itemString = player.activeItem != null ? player.activeItem.getData() : "null";
		sendData(InputType.INTERACT, itemString+ ";" +player.stamina+ ";" +player.getInventory().count(Items.arrowItem));
	}
	
	public void requestTile(Level level, int xt, int yt) {
		if (level == null) return;
		sendData(InputType.TILE, level.depth+ ";" +xt+ ";" +yt);
	}
	
	public void dropItem(Item drop) { sendData(InputType.DROP, drop.getData()); }
	
	public void sendPlayerUpdate(Player player) {
		if (player.getUpdates().length() > 0) {
			sendData(InputType.PLAYER, player.getUpdates());
			player.flushUpdates();
		}
	}
	
	public void sendPlayerDeath(Player player, DeathChest dc) {
		if (player != Game.player && Game.player != null) return; // this is client is not responsible for that player.
		Level level = World.levels[Game.currentLevel];
		level.add(dc);
		dc.eid = -1;
		String chestData = Save.writeEntity(dc, false);
		level.remove(dc);
		sendData(InputType.DIE, chestData);
	}
	
	public void requestRespawn() { changeState(State.RESPAWNING); }
	
	public void addToChest(Chest chest, int index, Item item) {
		if (chest == null || item == null) return;
		sendData(InputType.CHESTIN, chest.eid+ ";" +index+ ";" +item.getData());
	}
	
	public void removeFromChest(Chest chest, int itemIndex, int inputIndex, boolean wholeStack) {
		if (chest == null) return;
		sendData(InputType.CHESTOUT, chest.eid+ ";" +itemIndex+ ";" +wholeStack+ ";" +inputIndex);
	}
	
	public void touchDeathChest(DeathChest chest) {
		sendData(InputType.CHESTOUT, chest.eid+ "");
	}
	
	public void pushFurniture(Furniture f) { sendData(InputType.PUSH, String.valueOf(f.eid)); }
	
	public void pickupItem(ItemEntity ie) {
		if (ie == null) return;
		sendData(InputType.PICKUP, String.valueOf(ie.eid));
	}
	
	public void sendShirtColor() { sendData(InputType.SHIRT, Game.player.shirtColor+ ""); }
	
	public void sendBedRequest(Bed bed) { sendData(InputType.BED, "true;" + bed.eid); }
	public void sendBedExitRequest() { sendData(InputType.BED, "false"); }
	
	public void requestLevel(int lvlidx) {
		if (Game.debug) System.out.println("CLIENT: Setting level before request to be sure, from " +Game.currentLevel+ " to " +lvlidx);
		Game.currentLevel = lvlidx; // just in case.
		changeState(State.LOADING);
	}
	
	public boolean checkConnection() {
		// if not connected, set menu to error screen
		if (!isConnected())
			menu.setError("Lost connection to server.");
		return isConnected();
	}
	
	public void endConnection() {
		if (isConnected() && curState == State.PLAY)
			sendData(InputType.SAVE, Game.player.getPlayerData()); // try to make sure that the player's info is saved before they leave.
		
		super.endConnection();
		
		curState = State.DISCONNECTED;
		
		// one may end the connection without an error; any errors should be set before calling this method, so there's no need to say anything here.
		if (Game.debug) System.out.println("Client has ended its connection.");
	}
	
	public boolean isConnected() { return super.isConnected() && curState != State.DISCONNECTED; }
	
	public String toString() { return "CLIENT"; }
}
