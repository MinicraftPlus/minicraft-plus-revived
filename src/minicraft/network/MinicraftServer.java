package minicraft.network;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import minicraft.core.Game;
import minicraft.core.Network;
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
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.StackableItem;
import minicraft.item.UnknownItem;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.saveload.Version;
import minicraft.screen.WorldSelectDisplay;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinicraftServer extends Thread implements MinicraftProtocol {
	
	class MyTask extends TimerTask {
		public MyTask() {}
		public void run() {}
	}
	
	private static final int UPDATE_INTERVAL = 10; // measured in seconds
	
	private List<MinicraftServerThread> threadList = Collections.synchronizedList(new ArrayList<>());
	private ServerSocket socket;
	
	private RemotePlayer hostPlayer = null;
	private String worldPath;
	
	private int playerCap = 5;
	
	public MinicraftServer() {
		super("MinicraftServer");
		Game.ISONLINE = true;
		Game.ISHOST = true; // just in case.
		Game.player.remove(); // the server has no player...
		
		worldPath = Game.gameDir + "/saves/" + WorldSelectDisplay.getWorldName();
		
		try {
			System.out.println("opening server socket...");
			socket = new ServerSocket(PORT);
			start();
		} catch (IOException ex) {
			System.err.println("failed to open server socket on port " + PORT);
			ex.printStackTrace();
		}
	}
	
	public void run() {
		if(Game.debug) System.out.println("server started.");
		
		Timer gameUpdateTimer = new Timer("GameUpdateTimer");
		gameUpdateTimer.schedule((new MyTask() {
			public void run() { updateGameVars(); }
		}), 5000, UPDATE_INTERVAL*1000);
		
		try {
			while (socket != null) {
				MinicraftServerThread mst = new MinicraftServerThread(socket.accept(), this);
				if(mst.isConnected())
					threadList.add(mst);
			}
		} catch (SocketException ex) { // this should occur when closing the thread.
			//ex.printStackTrace();
		} catch (IOException ex) {
			System.err.println("server socket encountered an error while attempting to listen on port " + PORT + ":");
			ex.printStackTrace();
		}
		
		gameUpdateTimer.cancel();
		System.out.println("closing server socket");
		
		endConnection();
	}
	
	public String getWorldPath() { return worldPath; }
	
	public int getPlayerCap() { return playerCap; }
	public void setPlayerCap(int val) {
		playerCap = Math.max(val, -1); // no need to set it to anything below -1.
	}
	
	public boolean isFull() {
		return playerCap >= 0 && threadList.size() >= playerCap;
	}
	
	public int getNumPlayers() { return threadList.size(); }
	
	private MinicraftServerThread[] getThreads() {
		return threadList.toArray(new MinicraftServerThread[threadList.size()]);
	}
	
	public String[] getClientInfo() {
		List<String> playerStrings = new ArrayList<>();
		for(MinicraftServerThread serverThread: getThreads()) {
			RemotePlayer clientPlayer = serverThread.getClient();
			/*if(clientPlayer.getUsername().length() == 0) {
				if(Game.debug) System.out.println("SERVER: client player " + clientPlayer + " has no username; not " +
						"adding to status listing.");
				continue; // they aren't done logging in yet.
			}*/
			
			playerStrings.add(clientPlayer.getUsername() + ": " + clientPlayer.getIpAddress().getHostAddress() + (Game.debug?" ("+(clientPlayer.x>>4)+","+(clientPlayer.y>>4)+")":""));
		}
		
		return playerStrings.toArray(new String[0]);
	}
	
	public List<RemotePlayer> getPlayersInRange(Entity e, boolean useTrackRange) {
		if(e == null || e.getLevel() == null) return new ArrayList<>();
		int xt = e.x >> 4, yt = e.y >> 4;
		return getPlayersInRange(e.getLevel(), xt, yt, useTrackRange); // NOTE if "e" is a RemotePlayer, the list returned *will* contain "e".
	}
	public List<RemotePlayer> getPlayersInRange(Level level, int xt, int yt, boolean useTrackRange) {
		List<RemotePlayer> players = new ArrayList<>();
		for(MinicraftServerThread thread: getThreads()) {
			RemotePlayer rp = thread.getClient();
			if(useTrackRange && rp.shouldTrack(xt, yt, level) || !useTrackRange && rp.shouldSync(xt, yt, level))
				players.add(rp);
		}
		
		return players;
	}
	
	@Nullable
	private RemotePlayer getIfPlayer(Entity e) {
		if(e instanceof RemotePlayer) {
			return (RemotePlayer) e;
			// this method is only used to remove a player from an array, so it is probably better that is doesn't check...
			/*RemotePlayer given = (RemotePlayer) e;
			MinicraftServerThread filed = getAssociatedThread(given);
			if(!filed.isValid()) {
				System.err.println("SERVER encountered a RemotePlayer not matched in the thread list: " + given);
				return null;
			}
			return filed.getClient();*/
		}
		else
			return null;
	}
	
	@Nullable
	public MinicraftServerThread getAssociatedThread(String username) {
		MinicraftServerThread match = null;
		
		for(MinicraftServerThread thread: getThreads()) {
			if(thread.getClient().getUsername().equalsIgnoreCase(username)) {
				match = thread;
				break;
			}
		}
		
		return match;
	}
	
	public List<MinicraftServerThread> getAssociatedThreads(String[] usernames) { return getAssociatedThreads(usernames, false); }
	public List<MinicraftServerThread> getAssociatedThreads(String[] usernames, boolean printError) {
		List<MinicraftServerThread> threads = new ArrayList<>();
		for(String username: usernames) {
			MinicraftServerThread match = getAssociatedThread(username);
			if(match != null)
				threads.add(match);
			else if(printError)
				System.err.println("couldn't match username \"" + username + "\"");
		}
		
		return threads;
	}
	
	@NotNull
	public MinicraftServerThread getAssociatedThread(RemotePlayer player) {
		MinicraftServerThread thread = null;
		
		for(MinicraftServerThread curThread: getThreads()) {
			if(curThread.getClient() == player) {
				thread = curThread;
				break;
			}
		}
		
		if(thread == null) {
			System.err.println("SERVER could not find thread for remote player " + player/* + "; stack trace:"*/);
			//Thread.dumpStack();
			thread = new MinicraftServerThread(player, this);
		}
		
		return thread;
	}
	
	private List<MinicraftServerThread> getAssociatedThreads(List<RemotePlayer> players) {
		List<MinicraftServerThread> threads = new ArrayList<>();
		
		/// NOTE I could do this the other way around, by looping though the thread list, and adding those whose player is found in the given list, which might be slightly more optimal... but I think it's better that this tells you when a player in the list doesn't have a matching thread.
		for(RemotePlayer client: players) {
			MinicraftServerThread thread = getAssociatedThread(client);
			if(thread.isValid())
				threads.add(thread);
			else
				System.err.println("SERVER WARNING: couldn't find server thread for client " + client);
		}
		
		return threads;
	}
	
	public void broadcastEntityUpdate(Entity e) { broadcastEntityUpdate(e, false); }
	public void broadcastEntityUpdate(Entity e, boolean updateSelf) {
		if(e.isRemoved()) {
			if(Game.debug) System.out.println("SERVER tried to broadcast update of removed entity: " + e);
			return;
		}
		List<RemotePlayer> players = getPlayersInRange(e, false);
		//if(Game.debug && e instanceof Player) System.out.println("SERVER found " + players.size() + " players in range of " + e + ", inc self.");
		if(!updateSelf) {
			players.remove(getIfPlayer(e));
			//if(Game.debug && e instanceof Player) System.out.println("Server removed player "+e+" of update: " + removed);
		}
		
		for(MinicraftServerThread thread: getAssociatedThreads(players)) {
			thread.sendEntityUpdate(e, e.getUpdates());
		}
		
		e.flushUpdates(); // it is important that this method is only called once: right here.
	}
	
	public void broadcastTileUpdate(Level level, int x, int y) {
		broadcastData(InputType.TILE, Tile.getData(level.depth, x, y));
	}
	
	public void broadcastEntityAddition(Entity e) { broadcastEntityAddition(e, false); }
	public void broadcastEntityAddition(Entity e, boolean addSelf) {
		if(e.isRemoved()) {
			if(Game.debug) System.out.println("SERVER tried to broadcast addition of removed entity: " + e);
			return;
		}
		List<RemotePlayer> players = getPlayersInRange(e, true);
		if(!addSelf)
			players.remove(getIfPlayer(e)); // if "e" is a player, this removes it from the list.
		int cnt = 0;
		if(Game.debug && e instanceof Player) System.out.println("SERVER: broadcasting player addition of "+e);
		for(MinicraftServerThread thread: getAssociatedThreads(players)) {
			thread.sendEntityAddition(e);
			cnt++;
		}
		if(Game.debug && e instanceof Player) System.out.println("SERVER: broadcasted player addition of "+e+" to "+cnt+" clients");
	}
	
	private List<RemotePlayer> getPlayersToRemove(Entity e, boolean removeSelf) {
		List<RemotePlayer> players = getPlayersInRange(e, true);
		if(players.size() == 0) return players;
		if (Game.debug && e instanceof Player) {
			System.out.println("SERVER: sending removal of player " + e + " to " + players.size() + " players (may remove equal player): ");
			for(RemotePlayer rp: players)
				System.out.println(rp);
		}
		
		if(!removeSelf)
			players.remove(getIfPlayer(e)); // if "e" is a player, this removes it from the list.
		
		if (Game.debug && e instanceof Player) System.out.println("...now sending player removal to " + players.size() + " players.");
		
		return players;
	}
	
	// remove only if on given level
	public void broadcastEntityRemoval(Entity e, Level level, boolean removeSelf) {
		List<RemotePlayer> players = getPlayersToRemove(e, removeSelf);
		
		if(level == null) {
			if(Game.debug) System.out.println("SERVER: cannot remove entity "+e+" from specified level, level given is null; ignoring request to broadcast entity removal.");
			return;
		}
		
		for(MinicraftServerThread thread: getAssociatedThreads(players))
			thread.sendEntityRemoval(e.eid, level.depth);
	}
	// remove regardless of level
	public void broadcastEntityRemoval(Entity e, boolean removeSelf) {
		List<RemotePlayer> players = getPlayersToRemove(e, removeSelf);
		
		for(MinicraftServerThread thread: getAssociatedThreads(players))
			thread.sendEntityRemoval(e.eid);
	}
	
	public void saveWorld() {
		broadcastData(InputType.SAVE, ""); // tell all the other clients to send their data over to be saved.
		new Save(WorldSelectDisplay.getWorldName());
	}
	
	public void broadcastNotification(String note, int notetime) {
		String data = notetime + ";" + note;
		broadcastData(InputType.NOTIFY, data);
	}
	
	public void broadcastPlayerHurt(int eid, int damage, Direction attackDir) {
		for(MinicraftServerThread thread: getThreads())
			thread.sendPlayerHurt(eid, damage, attackDir);
	}
	
	public void updateGameVars() { updateGameVars(getThreads()); }
	public void updateGameVars(MinicraftServerThread sendTo) {
		updateGameVars(new MinicraftServerThread[] {sendTo});
	}
	public void updateGameVars(MinicraftServerThread[] sendTo) {
		//if (Game.debug) System.out.println("SERVER: updating game vars...");
		if(sendTo.length == 0) return;
		
		String[] varArray = {
			Settings.get("mode").toString(),
			Updater.tickCount+"",
			Updater.gamespeed+"",
			Updater.pastDay1+"",
			Updater.scoreTime+"",
			getNumPlayers()+"",
			Bed.getPlayersAwake()+""
		};
		
		String vars = String.join(";", varArray);
		
		for(MinicraftServerThread thread: sendTo)
			thread.sendData(InputType.GAME, vars);
	}
	
	public void pingClients() {
		System.out.println("pinging clients ("+threadList.size()+" connected)...");
		for(MinicraftServerThread thread: getThreads())
			thread.doPing();
	}
	
	protected File[] getRemotePlayerFiles() {
		File saveFolder = new File(worldPath);
		
		File[] clientSaves = saveFolder.listFiles((file, name) -> name.startsWith("RemotePlayer"));
		
		if(clientSaves == null)
			clientSaves = new File[0];
		
		return clientSaves;
	}
	
	boolean parsePacket(MinicraftServerThread serverThread, InputType inType, String alldata) {
		String[] data = alldata.split(";");
		
		//if (Game.debug) System.out.println("received packet");
		
		RemotePlayer clientPlayer = serverThread.getClient();
		if(clientPlayer == null) {
			System.err.println("CRITICAL SERVER ERROR: server thread client is null: " + serverThread + "; cannot parse the received "+inType+" packet: " + alldata);
			return false;
		}
		
		// handle reports of type INVALID
		if(inType == InputType.INVALID) {
			if (Game.debug) System.out.println(serverThread + " received an error:");
			System.err.println(alldata);
			return false;
		}
		
		if(InputType.serverOnly.contains(inType)) {
			/// these are ALL illegal for a client to send.
			System.err.println("SERVER warning: client " + clientPlayer + " sent illegal packet type " + inType + " to server.");
			return false;
		}
		
		switch(inType) {
			case PING:
				System.out.println("Received ping from " + serverThread);
				return true;
			
			case LOGIN:
				if (Game.debug) System.out.println("SERVER: received login request");
				if (Game.debug) System.out.println("SERVER: login data: " + Arrays.toString(data));
				String username = data[0];
				Version clientVersion = new Version(data[1]);
				// check version; send back invalid if they don't match.
				if(clientVersion.compareTo(Game.VERSION) != 0) {
					serverThread.sendError("wrong game version; need " + Game.VERSION);
					return false;
				}
				
				// check if the same username already exists on the server (due to signing on a second time with the same account), and if so, prevent the new login
				if(getAssociatedThread(username) != null) {
					serverThread.sendError("Account is already logged in to server");
					return false;
				}
				/*while((oldThread = getAssociatedThread(username)) != null) {
					if(oldThread.isConnected())
						oldThread.sendError("User");
					oldThread.endConnection();
					oldThread.getClient().remove();
				}*/
				
				/// versions match, and username is unique; make client player
				clientPlayer.setUsername(username);
				
				/// now, we need to check if this player has played in this world before. If they have, then all previous settings and items and such will be restored.
				String playerdata = ""; // this stores the data fetched from the files.
				
				if(serverThread.getClient().getIpAddress().isLoopbackAddress() && hostPlayer == null) {
					/// this is the first person on localhost. I believe that a second person will be saved as a loopback address (later: jk the first one actually will), but a third will simply overwrite the second.
					if (Game.debug) System.out.println("SERVER: host player found");
					
					if(Game.player != null) {
						// save the player, and then remove it. It is leftover from when this was a single player world.
						playerdata = Game.player.getPlayerData();
						//if (Game.debug) System.out.println("SERVER: setting main player as remote from login.");
						Game.player.remove(); // all the important data has been saved.
						Game.player = null;
					} else {
						/// load the data from file instead.
						playerdata = Game.VERSION+"\n";
						try {
							playerdata += Load.loadFromFile(worldPath+"/Player"+Save.extension, true) + "\n";
							playerdata += Load.loadFromFile(worldPath+"/Inventory"+Save.extension, true);
						} catch(IOException ex) {
							System.err.println("SERVER had error while trying to load host player data from file:");
							ex.printStackTrace();
						}
					}
					
					hostPlayer = clientPlayer;
				}
				else {
					playerdata = serverThread.getRemotePlayerFileData();
				}
				
				if(playerdata.length() > 0) {
					/// if a save file was found, then send the data to the client so they can resume where they left off.
					// and now, initialize the RemotePlayer instance with the data.
					// first get the version that saved the file, which can be different if this is a remote player.
					//if(Game.debug) System.out.println("loaded player data: "+playerdata);
					String[] saveData = playerdata.split("\\n");
					new Load(new Version(saveData[0])).loadPlayer(clientPlayer, Arrays.asList(saveData[1].split(",")));
					// we really don't need to load the inventory.
				} else {
					clientPlayer.findStartPos(World.levels[World.lvlIdx(0)]); // find a new start pos
					// this is a new player.
					playerdata = clientPlayer.getPlayerData();
					// save the new player once, immediately.
					serverThread.writeClientSave(playerdata);
				}
				serverThread.sendData(InputType.PLAYER, playerdata);
				
				// now, we send the INIT_W packet and notify the others clients.
				
				int playerlvl = World.lvlIdx(clientPlayer.getLevel() != null ? clientPlayer.getLevel().depth : 0);
				if(!Arrays.asList(World.levels[playerlvl].getEntityArray()).contains(clientPlayer) && clientPlayer != hostPlayer) // this will be true if their file was already found, since they are added in Load.loadPlayer().
					World.levels[playerlvl].add(clientPlayer); // add to level (**id is generated here**) and also, maybe, broadcasted to other players?
				
				updateGameVars();
				//making INIT_W packet
				int[] toSend = {
					clientPlayer.eid,
					World.levels[playerlvl].w,
					World.levels[playerlvl].h,
					playerlvl, // these bottom three are actually unnecessary because of the previous PLAYER packet.
					clientPlayer.x,
					clientPlayer.y
				};
				StringBuilder sendString = new StringBuilder();
				for(int val: toSend)
					sendString.append(val).append(",");
				/// send client world info
				if (Game.debug) System.out.println("SERVER: sending INIT packet");
				serverThread.sendData(InputType.INIT, sendString.toString());
				return true;
			
			case LOAD:
				if (Game.debug) System.out.println("SERVER: received level data request");
				// send back the tiles in the level specified.
				int levelidx = Integer.parseInt(alldata);
				if(levelidx < 0 || levelidx >= World.levels.length) {
					System.err.println("SERVER warning: Client " + clientPlayer + " tried to request tiles from nonexistent level " + levelidx);
					serverThread.sendError("requested level ("+levelidx+") does not exist.");
					return false;
				}
				
				// move the associated player to the level they requested -- they shouldn't be requesting it if they aren't going to transfer to it.
				World.levels[levelidx].add(clientPlayer);
				// if it's the same level, it will cancel out.
				
				byte[] tiledata = new byte[World.levels[levelidx].tiles.length*2];
				for(int i = 0; i < tiledata.length/2 - 1; i++) {
					tiledata[i*2] = World.levels[levelidx].tiles[i];
					tiledata[i*2+1] = World.levels[levelidx].data[i];
				}
				serverThread.cachePacketTypes(InputType.tileUpdates);
				
				StringBuilder tiledataString = new StringBuilder();
				for(byte b: tiledata) {
					tiledataString.append(b).append(",");
				}
				serverThread.sendData(InputType.TILES, tiledataString.substring(0, tiledataString.length()-1));
				serverThread.sendCachedPackets();
				
				/// send back the entities in the level specified.
				
				Entity[] entities = World.levels[levelidx].getEntityArray();
				serverThread.cachePacketTypes(InputType.entityUpdates);
				
				StringBuilder edata = new StringBuilder();
				for(int i = 0; i < entities.length; i++) {
					Entity curEntity = entities[i];
					if(!clientPlayer.shouldTrack(curEntity.x>>4, curEntity.y>>4, curEntity.getLevel()))
						continue; // this is outside of the player's entity tracking range; doesn't need to know about it yet.
					String curEntityData = "";
					if(curEntity != clientPlayer) {
						curEntityData = Save.writeEntity(curEntity, false) + ",";
						if(Game.debug && curEntity instanceof Player) System.out.println("SERVER: sending player in ENTITIES packet: " + curEntity);
					}
					// there is enough space.
					if(curEntityData.length() > 1) // 1 b/c of the "," added; this prevents entities that aren't saved from causing ",," to appear.
						edata.append(curEntityData);
				}
				
				String edataToSend = edata.substring(0, Math.max(0, edata.length()-1)); // cut off trailing comma
				
				serverThread.sendData(InputType.ENTITIES, edataToSend);
				serverThread.sendCachedPackets();
				
				return true;
			
			case DIE:
				if (Game.debug) System.out.println("received player death");
				Entity dc = Load.loadEntity(alldata, false);
				broadcastEntityRemoval(clientPlayer, true);
				return true;
			
			case RESPAWN:
				serverThread.respawnPlayer();
				broadcastEntityAddition(clientPlayer);
				// added player; client will request level data when it's ready
				return true;
			
			case DISCONNECT:
				serverThread.endConnection();
				return true;
			
			case DROP:
				//if(Game.debug) System.out.println("SERVER: received item drop: " + alldata);
				Item dropped = Items.get(alldata);
				Level playerLevel = clientPlayer.getLevel();
				if(playerLevel != null)
					playerLevel.dropItem(clientPlayer.x, clientPlayer.y, dropped);
				return true;
			
			case TILE:
				int lvlDepth = Integer.parseInt(data[0]);
				int xt = Integer.parseInt(data[1]);
				int yt = Integer.parseInt(data[2]);
				for(int lvly = yt-1; lvly <= yt+1; lvly++)
					for(int lvlx = xt-1; lvlx <= xt+1; lvlx++)
						serverThread.sendTileUpdate(lvlDepth, lvlx, lvly);
				return true;
			
			case ENTITY:
				// client wants the specified entity sent in an ADD packet, becuase it couldn't find that entity upon receiving an ENTITY packet from the server.
				int enid = Integer.parseInt(alldata);
				Entity entityToSend = Network.getEntity(enid);
				if(entityToSend == null) {
					/// well THIS would be a problem, I think. Though... Actually, not really. It just means that an entity was removed between the time of sending an update for it, and the client then asking for it to be added. But since it would be useless to add it at this point, we'll just ignore the request.
					if (Game.debug) System.out.println("SERVER: ignoring request to add unknown entity (probably already removed): " + enid);
					return false;
				}
				
				if(!clientPlayer.shouldSync(entityToSend.x >> 4, entityToSend.y >> 4, entityToSend.getLevel())) {
					// the requested entity is not even in range
					return false;
				}
				if(Game.debug) System.out.println("SERVER: sending entity addition via " + serverThread + " b/c client requested it: " + entityToSend);
				serverThread.sendEntityAddition(entityToSend);
				return true;
			
			case SAVE:
				if (Game.debug) System.out.println("SERVER: received player save from " + serverThread.getClient());
				/// save this client's data to a file.
				/// first, determine if this is the main player. if not, determine if a file already exists for this client. if not, find an available file name. for simplicity, we will just count the number of remote player files saved.
				
				if(clientPlayer == hostPlayer) {
					if (Game.debug) System.out.println("SERVER: identified SAVE packet client as host");
					String[] parts = alldata.split("\\n");
					List<String> datastrs = new ArrayList<>();
					
					Save save = new Save(clientPlayer, false);
					datastrs.addAll(Arrays.asList(parts[1].split(",")));
					save.writeToFile(save.location+"Player"+Save.extension, datastrs);
					datastrs.clear();
					datastrs.addAll(Arrays.asList(parts[2].split(",")));
					save.writeToFile(save.location+"Inventory"+Save.extension, datastrs);
					
					return true;
				}
				
				serverThread.writeClientSave(alldata); // writes the data in a RemotePlayer save file.
				
				return true;
			
			case CHESTIN: case CHESTOUT:
				//if (Game.debug) System.out.println("SERVER: received chest request: " + inType);
				int eid = Integer.parseInt(data[0]);
				Entity e = Network.getEntity(eid);
				if(e == null || !(e instanceof Chest)) {
					System.err.println("SERVER error with CHESTOUT request: Specified chest entity did not exist or was not a chest.");
					return false;
				}
				
				Chest chest = (Chest) e;
				
				if(e instanceof DeathChest) {
					StringBuilder itemDataB = new StringBuilder();
					for(Item i: chest.getInventory().getItems())
						itemDataB.append(i.getData()).append(";");
					String itemData = itemDataB.toString();
					itemData = itemData.length() == 0 ? itemData : itemData.substring(0, itemData.length()-1);
					serverThread.sendItems(itemData);
					serverThread.sendNotification("Death chest retrieved!", 0);
					chest.remove();
					return true;
				}
				
				int itemIdx = Integer.parseInt(data[1]);
				
				if(inType == InputType.CHESTIN) {
					Item item = Items.get(data[2]);
					if(item instanceof UnknownItem) {
						System.err.println("SERVER error with CHESTIN request: specified item could not be found from string: " + data[2]);
						return false;
					}
					if(itemIdx > chest.getInventory().invSize())
						itemIdx = chest.getInventory().invSize();
					chest.getInventory().add(itemIdx, item);
				}
				else { /// inType == InputType.CHESTOUT
					//if (Game.debug) System.out.println("SERVER: received CHESTOUT request");
					
					if(itemIdx >= chest.getInventory().invSize() || itemIdx < 0) {
						System.err.println("SERVER error with CHESTOUT request: specified chest inv index is out of bounds: "+itemIdx+"; inv size:"+chest.getInventory().invSize());
						return false;
					}
					// if here, the index is valid
					boolean wholeStack = Boolean.parseBoolean(data[2]);
					Item toRemove = chest.getInventory().get(itemIdx);
					Item itemToSend = toRemove.clone();
					if(!wholeStack && toRemove instanceof StackableItem && ((StackableItem)toRemove).count > 1) {
						((StackableItem)itemToSend).count = 1;
						((StackableItem)toRemove).count--;
					} else
						chest.getInventory().remove(itemIdx);
					
					//if(Game.debug) System.out.println("SERVER sending chestout with item data: \"" + itemToSend.getData() + "\"");
					serverThread.sendData(InputType.CHESTOUT, itemToSend.getData()+";"+data[3]); // send back the item that the player should put in their inventory.
				}
				
				serverThread.sendEntityUpdate(chest, chest.getUpdates());
				
				// remove it if it is a death chest and there are no more items
				if(chest instanceof DeathChest && chest.getInventory().invSize() == 0) {
					//if (Game.debug) System.out.println("removed final item from death chest; removing chest");
					chest.remove();
				}
				return true;
			
			case PUSH:
				int furnitureID = Integer.parseInt(alldata);
				Entity furniture = Network.getEntity(furnitureID);
				if(furniture == null) {
					System.err.println("SERVER: couldn't find the specified piece of furniture to push: " + furnitureID);
					return false;
				} else if(!(furniture instanceof Furniture)) {
					System.err.println("SERVER: specified entity is not an instance of the furniture class: " + furniture + "; cannot push.");
					return false;
				}
				
				((Furniture)furniture).tryPush(clientPlayer);
				return true;
			
			case PICKUP:
				//if (Game.debug) System.out.println("SERVER: received itementity pickup request");
				int ieid = Integer.parseInt(alldata);
				Entity entity = Network.getEntity(ieid);
				if(entity == null || !(entity instanceof ItemEntity) || entity.isRemoved()) {
					System.err.println("SERVER could not find item entity in PICKUP request: " + ieid + ". Telling client to remove...");
					serverThread.sendEntityRemoval(ieid); // will happen when another guy gets to it first, so the this client shouldn't have it on the level anymore. It could also happen if the client didn't receive the packet telling them to pick it up... in which case it will be lost, but oh well.
					return false;
				}
				
				entity.remove();
				//if(Game.debug) System.out.println("SERVER: item entity pickup approved: " + entity);
				serverThread.sendData(inType, alldata);
				broadcastData(InputType.REMOVE, String.valueOf(entity.eid), serverThread);
				return true;
			
			case INTERACT:
				clientPlayer.activeItem = Items.get(data[0], true); // this can be null; and that's fine, it means a fist. ;)
				clientPlayer.stamina = Integer.parseInt(data[1]);
				int arrowCount = Integer.parseInt(data[2]);
				int curArrows = clientPlayer.getInventory().count(Items.arrowItem);
				if(curArrows < arrowCount)
					clientPlayer.getInventory().add(Items.arrowItem, arrowCount-curArrows);
				if(curArrows > arrowCount)
					clientPlayer.getInventory().removeItems(Items.arrowItem, curArrows-arrowCount);
				clientPlayer.attack(); /// NOTE the player may fire an arrow, but we won't sync the arrow count because that player will update it theirself.
				
				serverThread.sendData(InputType.INTERACT, ( clientPlayer.activeItem == null ? "null" : clientPlayer.activeItem.getData() ));
				return true;
			
			case BED:
				if (Game.debug) System.out.println("received bed request: " + alldata);
				boolean getIn = Boolean.parseBoolean(data[0]);
				if(getIn) {
					Entity bed = Network.getEntity(Integer.parseInt(data[1]));
					if(!(bed instanceof Bed) || !Bed.checkCanSleep(clientPlayer)) {
						updateGameVars();
						return false;
					}
					
					((Bed) bed).use(clientPlayer);
				}
				else {
					if(Bed.sleeping()) return false; // can't quit once everyone is in bed
					// else, get the player out of bed
					Bed.restorePlayer(clientPlayer);
				}
				return true;
			
			case POTION:
				boolean addEffect = Boolean.parseBoolean(data[0]);
				int typeIdx = Integer.parseInt(data[1]);
				PotionItem.applyPotion(clientPlayer, PotionType.values[typeIdx], addEffect);
				return true;
			
			case SHIRT:
				clientPlayer.shirtColor = Integer.parseInt(alldata);
				broadcastEntityUpdate(clientPlayer, false);
				return true;
			
			case PLAYER:
				clientPlayer.update(alldata);
				return true;
			
			case MOVE:
				/// the player moved.
				//if (Game.debug) System.out.println(serverThread+": received move packet");
				int plvlidx = Integer.parseInt(data[3]);
				if(plvlidx >= 0 && plvlidx < World.levels.length && World.levels[plvlidx] != clientPlayer.getLevel()) {
					clientPlayer.remove();
					World.levels[plvlidx].add(clientPlayer);
				}
				
				int oldx = clientPlayer.x>>4, oldy = clientPlayer.y>>4;
				int newx = Integer.parseInt(data[0]);
				int newy = Integer.parseInt(data[1]);
				
				boolean moved = clientPlayer.move(newx - clientPlayer.x, newy - clientPlayer.y); // this moves the player, and updates other clients.
				
				clientPlayer.dir = Direction.values[Integer.parseInt(data[2])]; // do this AFTERWARD, so that the move method doesn't mess something up.
				
				if(moved) clientPlayer.updateSyncArea(oldx, oldy); // this updates the current client.
				
				broadcastEntityUpdate(clientPlayer, !moved); // this will make it so that if the player is prevented from moving, the server will update the client, forcing it back to the last place the server recorded the player at. TODO this breaks down with a slow connection...
				clientPlayer.walkDist++; // hopefully will make walking animations work. Actually, they should be sent with Mob's update... no, it doesn't update, it just feeds back.
				
				//if (Game.debug) System.out.println("SERVER: "+(moved?"moved player to":"stopped player at")+" ("+clientPlayer.x+","+clientPlayer.y+"): " + clientPlayer);
				
				return true;
			
			/// I'm thinking this should end up never being used... oh, well maybe for notifications, actually.
			default:
				System.out.println("SERVER used default behavior for input type " + inType);
				broadcastData(inType, alldata, serverThread);
				return true;
		}
	}
	
	private void broadcastData(InputType inType, String data) {
		broadcastData(inType, data, (MinicraftServerThread)null);
	}
	private void broadcastData(InputType inType, String data, @Nullable MinicraftServerThread clientThreadToExclude) {
		for(MinicraftServerThread thread: getThreads()) {
			if(thread != clientThreadToExclude) // send this packet to all EXCEPT the specified one.
				thread.sendData(inType, data);
		}
	}
	private void broadcastData(InputType inType, String data, List<MinicraftServerThread> threads) {
		for(MinicraftServerThread thread: threads)
			thread.sendData(inType, data);
	}
	
	protected synchronized void onThreadDisconnect(MinicraftServerThread thread) {
		threadList.remove(thread);
		if(thread.getClient() == hostPlayer)
			hostPlayer = null;
	}
	
	public synchronized void endConnection() {
		if (Game.debug) System.out.println("SERVER: ending connection with threads: " + threadList);
		
		MinicraftServerThread[] threads = getThreads();
		for(MinicraftServerThread thread: threads)
			thread.endConnection();
		
		try {
			socket.close();
		} catch (IOException ignored) {}
		
		threadList.clear(); // should already be clear
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed();
	}
	
	public boolean hasClients() {
		return threadList.size() > 0;
	}
}
