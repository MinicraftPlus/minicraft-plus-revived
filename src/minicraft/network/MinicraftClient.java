package minicraft.network;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.IOException;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Player;
import minicraft.entity.RemotePlayer;
import minicraft.entity.Chest;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.screen.MultiplayerMenu;
import minicraft.screen.ModeMenu;
import minicraft.screen.TitleMenu;
import minicraft.level.Level;
import minicraft.saveload.Save;
import minicraft.saveload.Load;

/// This class is only used by the client runtime; the server runtime doesn't touch it.
public class MinicraftClient extends Thread implements MinicraftConnection {
	
	private Game game;
	private MultiplayerMenu menu;
	
	private DatagramSocket socket = null;
	private InetAddress ipAddress = null; // this is the ipAddress of the server that we are connecting to.
	
	private static enum State {
		USERNAMES, IDLING, LOGIN, LOADING, PLAY, DISCONNECTED;
		
		private static final List<State> idleStates = Arrays.asList(new State[] {IDLING, PLAY, DISCONNECTED});
	}
	private State curState = State.DISCONNECTED;
	
	private boolean sent = false;
	private long sentTime;
	private int tries = 0;
	private static final int MAX_TRIES = 5;
	
	private HashMap<Integer, Long> entityAdditionRequests = new HashMap<Integer, Long>();
	
	public MinicraftClient(Game game, MultiplayerMenu menu, String hostName) {
		super("MinicraftClient");
		this.game = game;
		this.menu = menu;
		Game.ISONLINE = true;
		Game.ISHOST = false;
		try {
			
			socket = new DatagramSocket();
			ipAddress = InetAddress.getByName(hostName);
			try {
				socket.connect(ipAddress, PORT);
			} catch (Exception ex) {
				System.err.println("CLIENT: error connecting to host:");
				menu.setError(ex.getMessage());
				ex.printStackTrace();
				return;
			}
			/*boolean canReach = ipAddress.isReachable(5000);
			if(!canReach) {
				// address was deemed unreachable
				System.out.println("couldn't reach");
				menu.setError("address is unreachable.");
				return;
			}
			System.out.println("address could be reached.");
			*/
			curState = State.USERNAMES;
			
			start();
			
		} catch (UnknownHostException ex) {
			System.err.println("Don't know about host " + hostName);
			ex.printStackTrace();
			menu.setError("host not found");
		} catch (SocketException ex) {
			ex.printStackTrace();
		}/* catch (IOException ex) {
			System.err.println("error while determining if address was reachable.");
			ex.printStackTrace();
		}*/
	}
	
	private void changeState(State newState) {
		curState = newState;
		tries = 0;
		sent = false;
	}
	
	public void login(String username) {
		if (Game.debug) System.out.println("CLIENT: logging in to server...");
		try {
			game.player = new RemotePlayer(game, true, username, InetAddress.getLocalHost(), PORT);
		} catch(UnknownHostException ex) {
			System.err.println("CLIENT could not get localhost address.");
			menu.setError("unable to get localhost address");
			ex.printStackTrace();
		}
		changeState(State.LOGIN);
	}
	
	public void run() {
		if (Game.debug) System.out.println("client started.");
		
		while(curState != State.DISCONNECTED) {
			if(curState != State.PLAY && curState != State.IDLING && !sent) {
				if(Game.debug) System.out.println("CLIENT: running action switch");
				
				boolean matched = false;
				switch(curState) {
					case USERNAMES:
						if (Game.debug) System.out.println("CLIENT: requesting usernames");
						sendData(InputType.USERNAMES, new byte[0]);
						matched = true;
						break;
					
					case LOGIN:
						/// send login request.
						if (Game.debug) System.out.println("CLIENT: requesting login");
						String username = ((RemotePlayer)game.player).username;
						sendData(InputType.LOGIN, (username+";"+Game.VERSION).getBytes());
						matched = true;
						break;
					
					case LOADING:
						// send request to load all tiles and entites around the player, then start.
						if(Game.debug) System.out.println("CLIENT: requesting initial load");
						sendData(InputType.LOAD, new byte[0]);
						matched = true;
						break;
					
					/*
					case TILES:
						// send request for level tiles.
						if (Game.debug) System.out.println("CLIENT: requesting tiles");
						sendData(InputType.INIT_T, (new byte[] {(byte)game.currentLevel}));
						matched = true;
						break;
					
					case ENTITIES:
						// send request for level entities.
						if (Game.debug) System.out.println("CLIENT: requesting entities");
						sendData(InputType.INIT_E, (new byte[] {(byte)game.currentLevel}));
						matched = true;
						break;
					*/
				}
				if(matched) {
					sent = true;
					sentTime = 0;
					tries++;
				}
			}
			
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
				parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
			} catch(SocketTimeoutException ex) {
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
				}*/
				if(tries < MAX_TRIES) {
					sent = false;
					if(Game.debug)
						System.out.println("CLIENT: did not recieve expected packet in time allotted. Retrying "+curState+" step, attempt "+tries+" of "+MAX_TRIES);
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
				*/
			//}
			
			packet = null;
		}
		
		if (Game.debug) System.out.println("client is not connected. ending run loop.");
	}
	
	/** This method is responsible for parsing all data recieved by the socket. */
	public boolean parsePacket(byte[] alldata, InetAddress address, int port) {
		if(alldata == null || alldata.length == 0) return false;
		
		InputType inType = MinicraftConnection.getInputType(alldata[0]);
		if(inType == null)
			return false;
		
		//if (Game.debug) System.out.println("CLIENT: recieved "+inType+" packet.");
		byte[] data = Arrays.copyOfRange(alldata, 1, alldata.length);
		
		// at this point, it has been determined that the data is of a valid type, and that type is stored in inType
		
		/*
			wanted behavior for each input type:
			
			-INVALID: print message to console. possibly don't connect to server.
			
			-LOGIN
				+ sent when: the client wants to join the game. Passes username and game version.
				+ when recieved: ignore. perhaps print to console that the server tried logging in.
			
			-INIT_W
				expected data: world size, start pos, time of day, game mode, and any info relevant to that game mode (time left for score mode).
				behavior: set the appropriate vars, and then start requesting INIT_T, passing 3 for the level number.
			
			-INIT_T: same old... set tiles for level, and then request INIT_E.
			
			-INIT_E: mostly same... decode entities for level, and then begin the game.
			
			-DISCONNECT
				+ sent when: this client is leaving the game. no data necessary.
				+ when recieved: this client is being told that it's been kicked from the server, probably because the host left the game. Behavior is... not much. Go to title screen? Display message.
			
			-TILE, ENTITY, ADD, REMOVE, NOTIFY
				+ sent when: updates have occurred; the player has done something.
					for the host computer, it will send these much more often, because it is doing all the mob ai ticks and updates.
				+ when recieved: updates the specified entities accordingly.
		*/
		
		switch(inType) {
			case INVALID:
				String msg = new String(data).trim();
				System.err.println("CLIENT recieved error: " + msg);
				menu.setError(msg);
				curState = State.IDLING;
				
				return false;
			
			case USERNAMES:
				if (Game.debug) System.out.println("CLIENT: recieved usernames");
				String names = new String(data).trim();
				String[] namelist = names.split("\n");
				menu.setTakenNames(Arrays.asList(namelist));
				curState = State.IDLING;
				return true;
			
			case LOGIN:
				System.err.println("Server tried to login...");
				return false;
			
			case DISCONNECT:
				if (Game.debug) System.out.println("CLIENT: recieved disconnect");
				curState = State.DISCONNECTED;
				menu.setError("Server Disconnected."); // this sets the menu back to the multiplayer menu, and tells the user what happened.
				//endConnection(); // this is called in the run loop.
				return true;
			
			case MODE:
				ModeMenu.updateModeBools((int)data[0]);
				return true;
			
			case INIT:
				if (Game.debug) System.out.println("CLIENT: recieved INIT packet");
				menu.setLoadingMessage("World");
				
				String[] infostrings = new String(data).trim().split(",");
				int[] info = new int[infostrings.length];
				for(int i = 0; i < info.length; i++)
					info[i] = Integer.parseInt(infostrings[i]);
				game.player.eid = info[0];
				Game.lvlw = info[1];
				Game.lvlh = info[2];
				game.currentLevel = info[3];
				game.player.x = info[4];
				game.player.y = info[5];
				Game.setTime(info[6]);
				Game.pastDay1 = info[7]==0?false:true;
				ModeMenu.updateModeBools(info[8]);
				if(ModeMenu.score && info.length > 9)
					game.scoreTime = info[9];
				
				// initialize the levels
				for(int i = 0; i < Game.levels.length; i++) {
					int lvldepth = Game.idxToDepth[i];
					Game.levels[i] = new Level(game, Game.lvlw, Game.lvlh, lvldepth, Game.levels[Game.lvlIdx(lvldepth+1)], false);
				}
				
				/// vars are set; now start requesting entities.
				
				sent = false;
				if(curState == State.LOGIN) {// only go through the sequence if this is called as part of start up.
					curState = State.LOADING;
					//menu.setLoadingMessage("Tiles");
				}
				return true;
			
			case START:
				if (Game.debug) System.out.println("CLIENT: Recieved START packet. Begin game!");
				//sendData(alldata); // send confirmation to server.
				changeState(State.PLAY);
				Game.levels[game.currentLevel].add(game.player);
				Game.readyToRenderGameplay = true;
				game.setMenu(null);
				return true;
			
			/*
			case INIT_T:
				if (Game.debug) System.out.println("CLIENT: recieved tiles");
				if(curState != State.TILES) // ignore
					return false;
				/// recieve tiles.
				Level level = Game.levels[game.currentLevel];
				if(level == null) {
					int lvldepth = Game.idxToDepth[game.currentLevel];
					Game.levels[game.currentLevel] = level = new Level(game, Game.lvlw, Game.lvlh, lvldepth, Game.levels[Game.lvlIdx(lvldepth+1)], false);
				}
				//level.tiles = new byte[data.length/2];
				//level.data = new byte[data.length/2];
				int idx = ((int)data[0]) * ((packetSize-2)/2);
				if(Game.debug) System.out.println("CLIENT: loading tiles starting from " + idx);
				int i;
				for(i = 0; i < data.length/2-2 && idx+i < level.tiles.length; i++) {
					level.tiles[idx + i] = data[i*2 +1];
					level.data[idx + i] = data[i*2+1 +1];
				}
				Game.levels[game.currentLevel] = level;
				
				if(idx+i == level.tiles.length) {
					curState = State.ENTITIES;
					sent = false;
					menu.setLoadingMessage("Entities");
					if (Game.debug) System.out.println("CLIENT: progressed state to entities");
				} else if(Game.debug) {
					if(idx+i > level.tiles.length) System.out.println("CLIENT: recieved too many tiles");
					System.out.println("CLIENT: waiting for more tiles... ended: " + (i-1+idx) + " of " + (level.tiles.length-1));
				}
				return true;
			
			case INIT_E:
				if(curState != State.ENTITIES) // ignore
					return false;
				if (Game.debug) System.out.println("CLIENT: recieved entities");
				Level newLevel = Game.levels[game.currentLevel];
				String[] entities = new String(data).trim().split(",");
				Load loader = new Load();
				for(String entityString: entities) {
					if(entityString.length() == 0) continue;
					if(entityString.equals("END")) {
						/// the end of the entity list has been reached.
						sent = false;
						break;
					}
					if (Game.debug) System.out.println("CLIENT: loading entity: " + entityString);
					loader.loadEntity(entityString, game, false);
				}
				
				if(!sent) {
					// ready to start game now.
					curState = State.PLAY;
					if (Game.debug) System.out.println("CLIENT: Begin game!");
					Game.levels[game.currentLevel].add(game.player);
					Game.readyToRenderGameplay = true;
					game.setMenu(null);
				}
				else if(Game.debug)
					System.out.println("CLIENT: waiting for more entities...");
				return true;
			*/
			case TILE:
				//if(curState != State.PLAY) return true; // ignoring for now
				//if (Game.debug) System.out.println("CLIENT: recieved tile update: " + (new String(data)));
				//byte id = data[0], tdata = data[1];
				//int pos = Integer.parseInt(new String(Arrays.copyOfRange(data, 2, data.length)).trim());
				String[] tiledata = (new String(data)).trim().split(";");
				Level level = Game.levels[game.currentLevel];
				int pos = Integer.parseInt(tiledata[0]);
				level.tiles[pos] = Byte.parseByte(tiledata[1]);
				level.data[pos] = Byte.parseByte(tiledata[2]);
				return true;
			
			case ADD:
				//if(curState != State.PLAY) return true; // ignoring for now
				String entityData = new String(data).trim();
				if (Game.debug) System.out.println("CLIENT: recieved entity addition: " + entityData);
				if(entityData.length() == 0) {
					System.err.println("CLIENT: recieved entity is blank...");
					return false;
				}
				
				Entity addedEntity = (new Load()).loadEntity(entityData, game, false);
				if(addedEntity != null) {
					sendData(InputType.ADD, String.valueOf(addedEntity.eid).getBytes());
					entityAdditionRequests.remove(addedEntity.eid);
				}
				
				return true;
			
			case REMOVE:
				//int eid = 0;
				//for(int i = 0; i < data.length; i++)
					//eid += data[i] << i*8;
				//if(curState != State.PLAY) return true; // ignoring for now
				int eid = Integer.parseInt((new String(data)).trim());//(data[0]<<(8*3)) + (data[1]<<(8*2)) + (data[2]<<8) + data[3];
				if (Game.debug) System.out.println("CLIENT: recieved entity removal: " + eid);
				Entity toRemove = Game.getEntity(eid);
				sendData(alldata); // lets the server know that the packet was recieved.
				if(toRemove != null) {
					toRemove.remove();
					return true;
				}
				return false;
			
			case ENTITY:
				if(curState != State.PLAY) return true; // ignoring for now... super important things that won't just be updated next frame shouldn't *be* updated, anyway.
				String updates = new String(data).trim();
				int entityid = Integer.parseInt(updates.substring(0, updates.indexOf(";")));
				//if (Game.debug) System.out.println("CLIENT: recieved entity update for: " + entityid);
				updates = updates.substring(updates.indexOf(";")+1);
				Entity entity = Game.getEntity(entityid);
				if(entity == null) {
					if(entityAdditionRequests.containsKey(entityid) && (System.nanoTime() - entityAdditionRequests.get(entityid)) / 1E8 < 20L) {
						// it has been less than 2 seconds since this entity was last requested, so don't request it again at this time.
						return false;
					}
					// at this point: the entity has not been requested, or it has been more than 2 seconds since the last request.
					/*System.out.println("CLIENT could not find entity specified to be updated ("+entityid+"); requesting entity from server...");
					sendData(InputType.ENTITY, String.valueOf(entityid).getBytes());
					entityAdditionRequests.put(entityid, System.nanoTime());
					*/
					return false;
				}
				else if(!((RemotePlayer)game.player).shouldSync(entity.x >> 4, entity.y >> 4)) {
					// the entity is out of sync range.
					return false;
				}
				entity.update(updates);
				return true;
			
			case PLAYER:
				if (Game.debug) System.out.println("CLIENT: recieved player packet");
				/*if(setPlayer) {
					if (Game.debug) System.out.println("CLIENT: ignoring set player, already set");
					return false;
				}*/
				// use the contained data to load up the player object vars.
				String pdata = new String(data).trim();
				//if(Game.debug) System.out.println("CLIENT: player data recieved: " + pdata);
				String[] playerparts = pdata.split("\\n");
				List<String> playerinfo = Arrays.asList(playerparts[0].split(","));
				List<String> playerinv = Arrays.asList(playerparts[1].split(","));
				Load load = new Load();
				if (Game.debug) System.out.println("CLIENT: setting player vars from packet...");
				if(!(playerinv.size() == 1 && playerinv.get(0).equals("null")))
					load.loadInventory(game.player.inventory, playerinv);
				load.loadPlayer(game.player, playerinfo);
				//setPlayer = true;
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
				sendData(InputType.SAVE, playerdata.getBytes());
				return true;
			
			case NOTIFY:
				if (Game.debug) System.out.println("CLIENT: recieved notification");
				if(curState != State.PLAY) return true; // ignoring for now
				String notedata = new String(data).trim();
				int notetime = Integer.parseInt(notedata.substring(0, notedata.indexOf(";")));
				String note = notedata.substring(notedata.indexOf(";")+1);
				Game.notifications.add(note);
				Game.notetick = notetime;
				return true;
			
			case CHESTOUT:
				if(curState != State.PLAY) return false; // shouldn't happen.
				//if (Game.debug) System.out.println("CLIENT: recieved chestout");
				Item item = Items.get(new String(data).trim());
				game.player.inventory.add(item);
				return true;
			
			case INTERACT:
				// the server went through with the interaction, and has sent back the new activeItem.
				Item holdItem = Items.get((new String(data)).trim());
				if(Game.debug) System.out.println("CLIENT: recieved interaction success; setting player item to " + holdItem);
				game.player.activeItem = holdItem;
				return true;
			
			case PICKUP:
				if(curState != State.PLAY) return false; // shouldn't happen.
				int ieid = Integer.parseInt(new String(data).trim());
				if (Game.debug) System.out.println("CLIENT: recieved pickup approval for: " + ieid);
				Entity ie = Game.getEntity(ieid);
				if(ie == null || !(ie instanceof ItemEntity)) {
					System.err.println("CLIENT error with PICKUP response: specified entity does not exist or is not an ItemEntity: " + ieid);
					return false;
				}
				game.player.touchItem((ItemEntity)ie);
				return true;
			
			case HURT:
				// the player got attacked.
				int damage = data[0];
				int attackDir = data[1];
				game.player.hurt(damage, attackDir);
				return true;
		}
		
		return false; // this isn't reached by anything, unless it's some packet type we aren't looking for. So in that case, return false.
	}
	
	/// the below methods are all about sending data to the server, *not* setting any game values.
	
	public void sendData(InputType inType, byte[] startdata) {
		if (Game.debug && inType != InputType.MOVE) System.out.println("CLIENT: sending "+inType+" packet...");
		sendData(prependType(inType, startdata));
	}
	public void sendData(byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, PORT);
		try {
			socket.send(packet);
		} catch(IOException ex) {
			//System.err.println("CLIENT: error sending "+inType+" packet:");
			ex.printStackTrace();
		}
	}
	
	public void move(Player player) {
		String movedata = player.x+";"+player.y+";"+player.dir;
		sendData(InputType.MOVE, movedata.getBytes());
	}
	
	/** This is called when the player.attack() method is called. */
	public void requestInteraction(Player player) {
		/// I don't think the player parameter is necessary, but it doesn't harm anything.
		String itemString = player.activeItem != null ? player.activeItem.getData() : "null";
		sendData(InputType.INTERACT, (itemString+";"+player.inventory.count(Items.get("arrow"))).getBytes());
	}
	
	public void addToChest(Chest chest, Item item) {
		if(chest == null || item == null) return;
		sendData(InputType.CHESTIN, (chest.eid+";"+item).getBytes());
	}
	
	public void removeFromChest(Chest chest, int index) {
		if(chest == null) return;
		sendData(InputType.CHESTOUT, (chest.eid+";"+index).getBytes());
	}
	
	public void pickupItem(ItemEntity ie) {
		if(ie == null) return;
		if(Game.debug) System.out.println("CLIENT: requesting pickup of item: " + ie);
		sendData(InputType.PICKUP, String.valueOf(ie.eid).getBytes());
	}
	
	public void sendNotification(String note, int notetime) {
		String data = notetime + ";" + note;
		sendData(InputType.NOTIFY, data.getBytes());
	}
	
	public void requestLevel(int lvlidx) {
		curState = State.LOADING;
		game.currentLevel = lvlidx; // just in case.
	}
	
	public void endConnection() {
		if(!socket.isClosed()) {
			if (Game.debug) System.out.println("closing client socket and ending connection");
			if(curState != State.DISCONNECTED)
				sendData(InputType.DISCONNECT, (new byte[0])); // send exit signal
			try {
				socket.disconnect();
				socket.close();
			} catch (NullPointerException ex) {
			}
		}
		curState = State.DISCONNECTED;
		System.out.println("CLIENT: connection ended.");
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed() && socket.isConnected() && curState != State.DISCONNECTED;
	}
}
