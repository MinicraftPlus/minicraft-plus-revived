package minicraft.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import minicraft.Game;
import minicraft.entity.Bed;
import minicraft.entity.Chest;
import minicraft.entity.DeathChest;
import minicraft.entity.Entity;
import minicraft.entity.Furniture;
import minicraft.entity.ItemEntity;
import minicraft.entity.Player;
import minicraft.entity.RemotePlayer;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.level.Level;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.screen.DeadMenu;
import minicraft.screen.ModeMenu;
import minicraft.screen.MultiplayerMenu;

/// This class is only used by the client runtime; the server runtime doesn't touch it.
public class MinicraftClient extends MinicraftConnection {
	
	private Game game;
	private MultiplayerMenu menu;
	
	private static enum State {
		USERNAMES, LOGIN, LOADING, PLAY, DISCONNECTED
	}
	private State curState = State.DISCONNECTED;
	
	private boolean pingSuccessful = false;
	
	//private boolean sent = false;
	//private long sentTime;
	//private int tries = 0;
	//private static final int MAX_TRIES = 3;
	
	//private static final int MAX_WAIT_TIME = 8000;
	//private static final int RETRY_INTERVAL = 11000;
	//private static final int SLEEP_TIME = 10;
	
	//private int waitTime;
	
	private static final Socket openSocket(String hostName) {
		InetAddress hostAddress = null;
		Socket socket = null;
		
		try {
			hostAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException ex) {
			System.err.println("Don't know about host " + hostName);
			ex.printStackTrace();
			return null;
		}
		
		try {
			socket = new Socket(hostAddress, PORT);
		} catch (IOException ex) {
			System.err.println("Problem connecting socket to server:");
			ex.printStackTrace();
			return null;
		}
		
		return socket;
	}
	
	public MinicraftClient(Game game, MultiplayerMenu menu, String hostName) {
		super("MinicraftClient", openSocket(hostName));
		this.game = game;
		this.menu = menu;
		Game.ISONLINE = true;
		Game.ISHOST = false;
		
		if(super.isConnected()) {
			changeState(State.USERNAMES);
			start();
		}
	}
	
	/*public void run() {
		if (Game.debug) System.out.println("starting client.");
		
		changeState(State.PING);
		
		while(waitTime <= MAX_WAIT_TIME && !pingSuccessful) {
			//if (Game.debug) System.out.println("client: checking input...");
			if(checkInput()) continue;
			
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException ex) {}
			
			waitTime += SLEEP_TIME;
			
			if ((waitTime-SLEEP_TIME) % RETRY_INTERVAL > waitTime % RETRY_INTERVAL)	// resend the message
				sendData(InputType.PING, "");
		}
		
		if(waitTime > MAX_WAIT_TIME) {
			// the connection timed out.
			if (Game.debug) System.err.println("server didn't respond to client pings; client timed out.");
			menu.setError("Connection timed out.");
			return;
		}
		
		if (Game.debug) System.out.println("client recieved ping from server; starting client routine...");
		changeState(State.USERNAMES);
		
		super.run();
	}*/
	
	private void changeState(State newState) {
		curState = newState;
		
		switch(newState) {
			/*case PING:
				pingSuccessful = false;
				//waitTime = RETRY_INTERVAL / 2; // just so the first one doesn't take so long to send, but it's also not sent immediately.
				waitTime = 0;
				sendData(InputType.PING, "");
				break;
			*/
			case USERNAMES:
				if (Game.debug) System.out.println("CLIENT: requesting usernames");
				sendData(InputType.USERNAMES, "");
				break;
			
			case LOGIN: sendData(InputType.LOGIN, ((RemotePlayer)game.player).getUsername()+";"+Game.VERSION); break;
			
			case LOADING:
				game.setMenu(menu);
				menu.setLoadingMessage("Tiles");
				sendData(InputType.LOAD, String.valueOf(game.currentLevel));
				break;
			
			case PLAY:
				if (Game.debug) System.out.println("CLIENT: Begin game!");
				Game.levels[game.currentLevel].add(game.player);
				Game.readyToRenderGameplay = true;
				game.setMenu(null);
				break;
			
			case DISCONNECTED: endConnection(); break;
		}
	}
	
	public void login(String username) {
		if (Game.debug) System.out.println("CLIENT: logging in to server...");
		try {
			game.player = new RemotePlayer(game.player, game, true, InetAddress.getLocalHost(), PORT);
			((RemotePlayer)game.player).setUsername(username);
		} catch(UnknownHostException ex) {
			System.err.println("CLIENT could not get localhost address:");
			ex.printStackTrace();
			menu.setError("unable to get localhost address");
		}
		changeState(State.LOGIN);
	}
	
	/*
	public void run() {
		if (Game.debug) System.out.println("client started.");
		
		while(curState != State.DISCONNECTED) {
			/*if(curState != State.PLAY && curState != State.IDLING && !sent) {
				if(Game.debug) System.out.println("CLIENT: running action switch");
				
				boolean matched = false;
				switch(curState) {
					case USERNAMES:
						matched = true;
						break;
					
					case LOGIN:
						/// send login request.
						if (Game.debug) System.out.println("CLIENT: requesting login");
						String username = ((RemotePlayer)game.player).username;
						
						matched = true;
						break;
					
					/*case LOADING:
						// send request to load all tiles and entites around the player, then start.
						if(Game.debug) System.out.println("CLIENT: requesting initial load");
						sendData(InputType.LOAD, new byte[0]);
						matched = true;
						break;
					*//*
					
					case TILES:
						// send request for level tiles.
						if (Game.debug) System.out.println("CLIENT: requesting tiles");
						sendData(InputType.TILES, (new byte[] {(byte)game.currentLevel}));
						matched = true;
						break;
					
					case ENTITIES:
						// send request for level entities.
						if (Game.debug) System.out.println("CLIENT: requesting entities");
						sendData(InputType.ENTITIES, (new byte[] {(byte)game.currentLevel}));
						matched = true;
						break;
					
				}
				if(matched) {
					sent = true;
					sentTime = 0;
					tries++;
				}
			}*//*
			
			int targetTimeout = 5000;
			if(State.idleStates.contains(curState))
				targetTimeout = 500;
			else if(sentTime > 0) {
				targetTimeout -= sentTime / 1E6; // subtracts time already waited.
				//if(Game.debug) System.out.println("CLIENT targetTimeout: " + targetTimeout);
			}
			
			try {
				socket.setSoTimeout(targetTimeout);
			} catch(SocketException ex) {
				System.err.println("CLIENT: error setting socket timeout:");
				ex.printStackTrace();
			}
			
			byte[] data = new byte[packetSize];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			
			long startTime = System.nanoTime();
			State prevState = curState;
			
			try {
				socket.receive(packet);
				long waitTime = System.nanoTime() - startTime;
				sentTime += waitTime; // converts to milliseconds.
				parsePacket(packet.getData());
			} catch(SocketTimeoutException ex) {
				//System.out.println("time out in prev state" + prevState);
				long waitTime = System.nanoTime() - startTime;
				sentTime += waitTime; // converts to milliseconds.
			} catch (SocketException ex) {
				if(ex.getMessage().toLowerCase().contains("socket closed"))
					curState = State.DISCONNECTED;
				else
					ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			if(!State.idleStates.contains(prevState) && sentTime >= targetTimeout*1E6) {
				/*System.out.println("CLIENT timed out in state " + curState + "; targetTimeout="+targetTimeout);
				try {
					System.out.println("time out wait time: " + socket.getSoTimeout());
				} catch(SocketException ex) {
					System.err.println("CLIENT error getting socket timeout");
					ex.printStackTrace();
				}*//*
				if(tries < MAX_TRIES) {
					sent = false;
					if(Game.debug)
						System.out.println("CLIENT: did not recieve expected packet in time allotted. Retrying "+curState+" step, attempt "+(tries+1)+" of "+MAX_TRIES);
				}
				else {
					System.out.println("CLIENT: timed out waiting in state " + curState);
					menu.setError("connection timed out.");
					curState = State.DISCONNECTED;
				}
			}// else if(prevState == curState == State.LOADING) {
				// a packet was lost, and the state should "restart".
				//sent = false;
				// i don't need to clear entities, becuase the game won't add entities with duplicate ids.
				/*if(curState == State.TILES)
					Game.levels[game.currentLevel] = null;
				if(curState == State.ENTITIES)
					Game.levels[game.currentLevel].clearEntities();
				*//*
			//}
			
			packet = null;
		}
		
		if (Game.debug) System.out.println("client is not connected. ending run loop.");
	}*/
	
	/** This method is responsible for parsing all data recieved by the socket. */
	public synchronized boolean parsePacket(InputType inType, String alldata) {
		String[] data = alldata.split(";");
		
		switch(inType) {
			case INVALID:
				System.err.println("CLIENT recieved error: " + alldata);
				menu.setError(alldata);
				changeState(State.DISCONNECTED);
				return false;
			
			case PING:
				//pingSuccessful = true;
				System.out.println("client recieved ping");
				return true;
			
			case USERNAMES:
				if (Game.debug) System.out.println("CLIENT: recieved usernames");
				String[] namelist = alldata.split("\n");
				menu.setTakenNames(Arrays.asList(namelist));
				//curState = State.IDLING;
				return true;
			
			case LOGIN:
				System.err.println("Server tried to login...");
				return false;
			
			case DISCONNECT:
				if (Game.debug) System.out.println("CLIENT: recieved disconnect");
				menu.setError("Server Disconnected."); // this sets the menu back to the multiplayer menu, and tells the user what happened.
				changeState(State.DISCONNECTED);
				return true;
			
			case GAME:
				ModeMenu.updateModeBools(Integer.parseInt(data[0]));
				Game.setTime(Integer.parseInt(data[1]));
				Game.gamespeed = Float.parseFloat(data[2]);
				Game.pastDay1 = Boolean.parseBoolean(data[3]);
				Game.scoreTime = Integer.parseInt(data[4]);
				return true;
			
			case INIT:
				//if (Game.debug) System.out.println("CLIENT: recieved INIT packet");
				if(curState != State.LOGIN) {
					//System.out.println("WARNING: client recieved init packet in state " + curState + "; ignoring packet.");
					return false;
				}
				
				changeState(State.LOADING);
				//curState = State.LOADING; // I don't want to do the change state sequence quite yet.
				menu.setLoadingMessage("World");
				
				String[] infostrings = alldata.split(",");
				int[] info = new int[infostrings.length];
				for(int i = 0; i < info.length; i++)
					info[i] = Integer.parseInt(infostrings[i]);
				game.player.eid = info[0];
				Game.lvlw = info[1];
				Game.lvlh = info[2];
				game.currentLevel = info[3];
				game.player.x = info[4];
				game.player.y = info[5];
				return true;
			
			case TILES:
				if(curState != State.LOADING) { // ignore
					if (Game.debug) System.out.println("ignoring level tile data becuase client state is not LOADING: " + curState);
					return false;
				}
				if (Game.debug) System.out.println("CLIENT: recieved tiles");
				/// recieve tiles.
				Level level = Game.levels[game.currentLevel];
				if(level == null) {
					int lvldepth = Game.idxToDepth[game.currentLevel];
					Game.levels[game.currentLevel] = level = new Level(game, Game.lvlw, Game.lvlh, lvldepth, Game.levels[Game.lvlIdx(lvldepth+1)], false);
				}
				
				byte[] tiledata = new byte[alldata.length()];
				for(int i = 0; i < alldata.length(); i++)
					tiledata[i] = (byte)(((int)alldata.charAt(i)) - 1);
				
				if(tiledata.length / 2 > level.tiles.length) {
					System.err.println("CLIENT ERROR: recieved level tile data is too long for world size; level.tiles.length="+level.tiles.length+", tiles in data: " + (tiledata.length / 2) + ". Will truncate tile loading.");
				}
				
				for(int i = 0; i < tiledata.length/2 && i < level.tiles.length; i++) {
					level.tiles[i] = tiledata[i*2];
					level.data[i] = tiledata[i*2+1];
				}
				//Game.levels[game.currentLevel] = level;
				
				//if(idx+i == level.tiles.length) {
					//curState = State.ENTITIES;
					//sent = false;
					menu.setLoadingMessage("Entities");
					//if (Game.debug) System.out.println("CLIENT: progressed state to entities");
				/*} else if(Game.debug) {
					if(idx+i > level.tiles.length) System.out.println("CLIENT: recieved too many tiles");
					System.out.println("CLIENT: waiting for more tiles... ended: " + (i-1+idx) + " of " + (level.tiles.length-1));
				}*/
				return true;
			
			case ENTITIES:
				if(curState != State.LOADING) {// ignore
					System.out.println("ignoring level entity data becuase client state is not LOADING: " + curState);
					return false;
				}
				if (Game.debug) System.out.println("CLIENT: recieved entities");
				Level newLevel = Game.levels[game.currentLevel];
				String[] entities = alldata.split(",");
				for(String entityString: entities) {
					if(entityString.length() == 0) continue;
					/*if(entityString.equals("END")) {
						/// the end of the entity list has been reached.
						sent = false;
						break;
					}*/
					if (Game.debug) System.out.println("CLIENT: loading entity: " + entityString);
					Load.loadEntity(entityString, game, false);
				}
				
				//if(!sent) {
					// ready to start game now.
					changeState(State.PLAY); // this will be set before the client recieves any cached entities, so that should work out.
				//}
				//else if(Game.debug)
					//System.out.println("CLIENT: waiting for more entities...");
				return true;
			
			case TILE:
				Level theLevel = Game.levels[Integer.parseInt(data[0])];
				if(theLevel == null)
					return false; // ignore, this is for an unvisited level.
				int pos = Integer.parseInt(data[1]);
				theLevel.tiles[pos] = Byte.parseByte(data[2]);
				theLevel.data[pos] = Byte.parseByte(data[3]);
				return true;
			
			case ADD:
				if(curState == State.LOADING)
					System.out.println("CLIENT: recieved entity addition while loading level");
				
				//if (Game.debug) System.out.println("CLIENT: recieved entity addition: " + entityData);
				
				if(alldata.length() == 0) {
					System.err.println("CLIENT WARNING: recieved entity addition is blank...");
					return false;
				}
				
				Entity addedEntity = Load.loadEntity(alldata, game, false);
				if(addedEntity != null) {
					if(addedEntity.eid == game.player.eid/* && game.player.getLevel() == null*/) {
						if (Game.debug) System.out.println("CLIENT: added main game player back to level based on add packet");
						Game.levels[game.currentLevel].add(game.player);
						Bed.inBed = false;
					}
					//else if(Game.debug && addedEntity instanceof RemotePlayer)
						//System.out.println("CLIENT: added remote player from packet: " + addedEntity + "; game player has eid " + game.player.eid + "; this player has eid " + addedEntity.eid + "; are equal: " + (game.player.eid == addedEntity.eid));
				}
				
				return true;
			
			case REMOVE:
				if(curState == State.LOADING)
					System.out.println("CLIENT: recieved entity removal while loading level");
				
				int eid = Integer.parseInt(alldata);
				//if (Game.debug) System.out.println("CLIENT: recieved entity removal: " + eid);
				
				Entity toRemove = Game.getEntity(eid);
				if(toRemove != null) {
					toRemove.remove();
					return true;
				}
				return false;
			
			case ENTITY:
				// these shouldn't occur while loading, becuase the server caches them. But just in case, let's make sure.
				if(curState == State.LOADING)
					System.out.println("CLIENT recieved entity update while loading level");
				
				int entityid = Integer.parseInt(alldata.substring(0, alldata.indexOf(";")));
				//if (Game.debug) System.out.println("CLIENT: recieved entity update for: " + entityid);
				String updates = alldata.substring(alldata.indexOf(";")+1);
				Entity entity = Game.getEntity(entityid);
				if(entity == null) {
					//System.err.println("CLIENT: couldn't find entity specified to update: " + entityid + "; could not apply updates: " + updates);
					sendData(InputType.ENTITY, String.valueOf(entityid));
					return false;
				}
				else if(!((RemotePlayer)game.player).shouldSync(entity.x >> 4, entity.y >> 4)) {
					// the entity is out of sync range; but not necessarily out of the tracking range, so it's *not* removed from the level here.
					return false;
				}
				entity.update(updates);
				return true;
			
			case PLAYER:
				//if (Game.debug) System.out.println("CLIENT: recieved player packet");
				/*if(setPlayer) {
					if (Game.debug) System.out.println("CLIENT: ignoring set player, already set");
					return false;
				}*/
				// use the contained data to load up the player object vars.
				//if(Game.debug) System.out.println("CLIENT: player data recieved: " + alldata);
				String[] playerparts = alldata.split("\\n");
				List<String> playerinfo = Arrays.asList(playerparts[0].split(","));
				List<String> playerinv = Arrays.asList(playerparts[1].split(","));
				Load load = new Load();
				if (Game.debug) System.out.println("CLIENT: setting player vars from packet...");
				if(!(playerinv.size() == 1 && playerinv.get(0).equals("null")))
					load.loadInventory(game.player.inventory, playerinv);
				load.loadPlayer(game.player, playerinfo);
				//setPlayer = true;
				if(game.menu instanceof DeadMenu) {
					game.setMenu(null);
				}
				return true;
			
			case SAVE:
				if (Game.debug) System.out.println("CLIENT: recieved save request");
				// send back the player data.
				String playerdata = "";
				List<String> sdata = new ArrayList<String>();
				Save.writePlayer(game.player, sdata);
				for(String str: sdata)
					playerdata += str + ",";
				playerdata = playerdata.substring(0, playerdata.length()-1) + "\n";
				Save.writeInventory(game.player, sdata);
				for(String str: sdata)
					playerdata += str + ",";
				if(sdata.size() == 0)
					playerdata += "null";
				else
					playerdata = playerdata.substring(0, playerdata.length()-1);
				if (Game.debug) System.out.println("CLIENT: sending save data");
				sendData(InputType.SAVE, playerdata);
				return true;
			
			case NOTIFY:
				if (Game.debug) System.out.println("CLIENT: recieved notification");
				if(curState != State.PLAY) return true; // ignoring for now
				int notetime = Integer.parseInt(alldata.substring(0, alldata.indexOf(";")));
				String note = alldata.substring(alldata.indexOf(";")+1);
				Game.notifications.add(note);
				Game.notetick = notetime;
				return true;
			
			case CHESTOUT:
				if(curState != State.PLAY) return false; // shouldn't happen.
				Item item = Items.get(alldata);
				if (Game.debug) System.out.println("CLIENT: recieved chestout with item: " + item);
				game.player.inventory.add(0, item);
				//if (Game.debug) System.out.println("CLIENT successfully took " + item + " from chest and added to inv.");
				return true;
			
			case INTERACT:
				// the server went through with the interaction, and has sent back the new activeItem.
				Item holdItem = Items.get(alldata);
				if(Game.debug) System.out.println("CLIENT: recieved interaction success; setting player item to " + holdItem);
				game.player.activeItem = holdItem;
				return true;
			
			case PICKUP:
				if(curState != State.PLAY) return false; // shouldn't happen.
				int ieid = Integer.parseInt(alldata);
				if (Game.debug) System.out.println("CLIENT: recieved pickup approval for: " + ieid);
				Entity ie = Game.getEntity(ieid);
				if(ie == null || !(ie instanceof ItemEntity)) {
					System.err.println("CLIENT error with PICKUP response: specified entity does not exist or is not an ItemEntity: " + ieid);
					return false;
				}
				game.player.touchItem((ItemEntity)ie);
				return true;
			
			case POTION:
				boolean addEffect = Boolean.parseBoolean(data[0]);
				int typeIdx = Integer.parseInt(data[1]);
				PotionItem.applyPotion(game.player, PotionType.values[typeIdx], addEffect);
				return true;
			
			case HURT:
				// the player got attacked.
				if(Game.debug) System.out.println("CLIENT: recieved hurt packet");
				int damage = Integer.parseInt(data[0]);
				int attackDir = Integer.parseInt(data[1]);
				game.player.hurt(damage, attackDir);
				return true;
		}
		
		//System.out.println("CLIENT: recieved unexpected packet type " + inType + "; ignoring packet.");
		return false; // this isn't reached by anything, unless it's some packet type we aren't looking for. So in that case, return false.
	}
	
	/// the below methods are all about sending data to the server, *not* setting any game values.
	
	/*public void sendData(InputType inType, byte[] startdata) {
		if (Game.debug && inType != InputType.MOVE && inType != InputType.ADD) System.out.println("CLIENT: sending "+inType+" packet...");
		sendData(prependType(inType, startdata));
	}
	public void sendData(byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, PORT);
		try {
			socket.send(packet);
		} catch(IOException ex) {
			//System.err.println("CLIENT: error sending "+inType+" packet:");
			if(ex.getMessage().toLowerCase().contains("invalid argument")) {
				socket.close();
				menu.setError("Address is invalid.");
			} else
				ex.printStackTrace();
		}
	}*/
	
	public void move(Player player) {
		//if(Game.debug) System.out.println("CLIENT: sending player movement to ("+player.x+","+player.y+"): " + player);
		String movedata = player.x+";"+player.y+";"+player.dir+";"+Game.lvlIdx(player.getLevel().depth);
		sendData(InputType.MOVE, movedata);
	}
	
	/** This is called when the player.attack() method is called. */
	public void requestInteraction(Player player) {
		/// I don't think the player parameter is necessary, but it doesn't harm anything.
		String itemString = player.activeItem != null ? player.activeItem.getData() : "null";
		sendData(InputType.INTERACT, itemString+";"+player.inventory.count(Items.get("arrow")));
	}
	
	public void sendPlayerDeath(Player player, DeathChest dc) {
		if(player != game.player && game.player != null) return; // this is client is not responsible for that player.
		Level level = Game.levels[game.currentLevel];
		level.add(dc);
		dc.eid = -1;
		String chestData = Save.writeEntity(dc, false);
		level.remove(dc);
		sendData(InputType.DIE, chestData);
	}
	
	public void requestRespawn() {
		sendData(InputType.RESPAWN, "");
		//menu.setLoadingMessage("spawnpoint")
		//game.setMenu(menu);
	}
	
	public void addToChest(Chest chest, Item item) {
		if(chest == null || item == null) return;
		sendData(InputType.CHESTIN, chest.eid+";"+item.getData());
	}
	
	public void removeFromChest(Chest chest, int index) {
		if(chest == null) return;
		sendData(InputType.CHESTOUT, chest.eid+";"+index);
	}
	
	public void pushFurniture(Furniture f, int pushDir) {
		sendData(InputType.PUSH, String.valueOf(f.eid));
	}
	
	public void pickupItem(ItemEntity ie) {
		if(ie == null) return;
		if(Game.debug) System.out.println("CLIENT: requesting pickup of item: " + ie);
		sendData(InputType.PICKUP, String.valueOf(ie.eid));
	}
	
	public void sendBedRequest(Player player, Bed bed) {
		sendData(InputType.BED, String.valueOf(bed.eid));
	}
	
	public void requestLevel(int lvlidx) {
		game.currentLevel = lvlidx; // just in case.
		changeState(State.LOADING);
	}
	
	public void endConnection() {
		super.endConnection();
		
		curState = State.DISCONNECTED;
		
		if(game.menu == null)
			menu.setError("Connection to server has ended.");
		//System.out.println("CLIENT: connection ended.");
	}
	
	public boolean isConnected() {
		return super.isConnected() && curState != State.DISCONNECTED;
	}
	
	public String toString() {
		return "CLIENT";
	}
}
