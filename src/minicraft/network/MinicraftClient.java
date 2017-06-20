package minicraft.network;

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
	private DatagramSocket socket = null;
	private InetAddress ipAddress = null; // this is the ipAddress of the server that we are connecting to.
	
	//private static final int requestRepeatInterval = 100;
	//private int requestTimer = 0;
	
	public static enum State {
		USERNAMES, IDLING, LOGIN, TILES, ENTITIES, PLAY, DISCONNECTED
	}
	
	private boolean sent = false;
	//private boolean setPlayer = false;
	
	public State curState = State.DISCONNECTED;
	
	public MinicraftClient(Game game, String hostName) {
		super("MinicraftClient");
		this.game = game;
		Game.ISONLINE = true;
		Game.ISHOST = false;
		try {
			
			socket = new DatagramSocket();
			ipAddress = InetAddress.getByName(hostName);
			
			curState = State.USERNAMES;
			
			start();
			
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void login(String username) {
		if (Game.debug) System.out.println("CLIENT: logging in to server...");
		try {
			game.player = new RemotePlayer(game, true, username, InetAddress.getLocalHost(), MinicraftProtocol.PORT);
		} catch(UnknownHostException ex) {
			System.err.println("CLIENT could not get localhost address.");
			ex.printStackTrace();
		}
		sent = false;
		curState = State.LOGIN;
	}
	
	public void run() {
		if (Game.debug) System.out.println("client started.");
		/*try {
			socket.setSoTimeout(2500);
		} catch(SocketException ex) {
			System.err.println("error setting client socket timeout");
			ex.printStackTrace();
		}*/
		while(curState != State.DISCONNECTED) {
			if(curState != State.PLAY && curState != State.IDLING && !sent) {
				//boolean act = requestTimer <= 0;
				//if(act) requestTimer = requestRepeatInterval;
				//else if (Game.debug && requestTimer % 20 == 0) System.out.println("CLIENT: waiting for response...");
				/*try {
					socket.setSoTimeout(1000);//5000);
				} catch(SocketException ex) {
					System.err.println("error setting socket timeout");
					ex.printStackTrace();
				}*/
				
				if(Game.debug) System.out.println("CLIENT: running action switch");
				
				switch(curState) {
					case USERNAMES:
						//if(!act) break;
						if (Game.debug) System.out.println("CLIENT: requesting usernames");
						sendData(MinicraftProtocol.InputType.USERNAMES, new byte[0]);
						sent = true;
						break;
					
					case LOGIN:
						//if(!act) break; // don't send request constantly.
						/// send login request.
						if (Game.debug) System.out.println("CLIENT: requesting login");
						String username = ((RemotePlayer)game.player).username;
						sendData(MinicraftProtocol.InputType.LOGIN, (username+";"+Game.VERSION).getBytes());
						sent = true;
						break;
					
					case TILES:
						//if(!act) break;
						// send request for level tiles.
						if (Game.debug) System.out.println("CLIENT: requesting tiles");
						sendData(MinicraftProtocol.InputType.INIT_T, (new byte[] {(byte)game.currentLevel}));
						sent = true;
						break;
					
					case ENTITIES:
						//if(!act) break;
						// send request for level entities.
						if (Game.debug) System.out.println("CLIENT: requesting entities");
						sendData(MinicraftProtocol.InputType.INIT_E, (new byte[] {(byte)game.currentLevel}));
						sent = true;
						break;
					
					/*case START:
						/// this means that the data has been recieved, but the game hasn't started yet.
						// i don't seem to really need this much right now... but it seems like a good idea.
						//game.setMenu(null);
						curState = State.PLAY;
						if (Game.debug) System.out.println("CLIENT: starting game");
						break;
					*/
				}
				
				/*if(curState == State.USERNAMES || curState == State.LOGIN || curState == State.TILES || curState == State.ENTITIES) {
					try {
						Thread.sleep(10);
					} catch(InterruptedException ex) {}
					requestTimer -= 10;
				}*/
				
				//curState = State.IDLING;
				
			}/* else if(curState == State.) {
				try {
					socket.setSoTimeout(0);
				} catch(SocketException ex) {
					System.err.println("error setting socket timeout");
					ex.printStackTrace();
				}
			}*/
			
			int targetTimeout = 0;
			if(curState == State.IDLING) targetTimeout = 500;
			try {
				socket.setSoTimeout(targetTimeout);
			} catch(SocketException ex) {
				System.err.println("CLIENT: error setting socket timeout:");
				ex.printStackTrace();
			}
			
			byte[] data = new byte[MinicraftProtocol.packetSize];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			
			try {
				socket.receive(packet);
				parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
			} catch(SocketTimeoutException ex) {
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
		
		if (Game.debug) System.out.println("client is not connected. ending run loop.");
	}
	
	/** This method is responsible for parsing all data recieved by the socket. */
	public boolean parsePacket(byte[] alldata, InetAddress address, int port) {
		if(alldata == null || alldata.length == 0) return false;
		
		MinicraftProtocol.InputType inType = MinicraftProtocol.getInputType(alldata[0]);
		if(inType == null)
			return false;
		
		//if (Game.debug) System.out.println("CLIENT: recieved packet");
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
				if(game.menu instanceof MultiplayerMenu) {
					MultiplayerMenu mm = (MultiplayerMenu) game.menu;
					mm.errorMessage = msg;
					mm.curState = MultiplayerMenu.State.ERROR;
				}
				else {
					game.notifications.add("Server Error:");
					game.notifications.add(msg);
				}
				curState = State.IDLING;
				
				return false;
			
			case USERNAMES:
				if (Game.debug) System.out.println("CLIENT: recieved usernames");
				String names = new String(data).trim();
				String[] namelist = names.split("\n");
				if(game.menu instanceof MultiplayerMenu) {
					MultiplayerMenu mm = (MultiplayerMenu) game.menu;
					mm.takenNames = Arrays.asList(namelist);
					mm.typing = System.getProperty("user.name"); // a little trick for a nice default username. ;)
					mm.curState = MultiplayerMenu.State.ENTERNAME;
				}
				curState = State.IDLING;
				return true;
			
			case LOGIN:
				System.err.println("Server tried to login...");
				return false;
			
			case INTERACT:
				System.err.println("Server tried to interact...");
				return false;
			
			case DISCONNECT:
				if (Game.debug) System.out.println("CLIENT: recieved disconnect");
				game.setMenu(new MultiplayerMenu());
				return true;
			
			case INIT_W:
				if (Game.debug) System.out.println("CLIENT: recieved INIT_W packet");
				MultiplayerMenu mm = null;
				if(game.menu instanceof MultiplayerMenu) {
					mm = (MultiplayerMenu) game.menu;
					mm.loadingMessage = "World";
					mm.curState = MultiplayerMenu.State.LOADING;
					if (Game.debug) System.out.println("CLIENT: progressed menu state to loading");
				}
				
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
				
				/// vars are set; now start requesting entities.
				
				sent = false;
				if(curState == State.LOGIN) {// only go through the sequence if this is called as part of start up.
					curState = State.TILES;
					if(mm != null)
						mm.loadingMessage = "Tiles";
				}
				return true;
			
			case INIT_T:
				if (Game.debug) System.out.println("CLIENT: recieved tiles");
				/// recieve tiles.
				Level level = Game.levels[game.currentLevel];
				if(level == null) {
					int lvldepth = Game.idxToDepth[game.currentLevel];
					Game.levels[game.currentLevel] = level = new Level(game, Game.lvlw, Game.lvlh, lvldepth, Game.levels[Game.lvlIdx(lvldepth+1)], false);
				}
				//level.tiles = new byte[data.length/2];
				//level.data = new byte[data.length/2];
				int idx = ((int)data[0]) * ((MinicraftProtocol.packetSize-2)/2);
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
					if(game.menu instanceof MultiplayerMenu) {
						((MultiplayerMenu)game.menu).loadingMessage = "Entities";
					}
					if (Game.debug) System.out.println("CLIENT: progressed state to entities");
				} else if(Game.debug) {
					if(idx+i > level.tiles.length) System.out.println("CLIENT: recieved too many tiles");
					System.out.println("CLIENT: waiting for more tiles... ended: " + (i-1+idx) + " of " + (level.tiles.length-1));
				}
				return true;
			
			case INIT_E:
				if (Game.debug) System.out.println("CLIENT: recieved entities");
				Level newLevel = Game.levels[game.currentLevel];
				String[] entities = new String(data).trim().split(",");
				for(String entityString: entities) {
					if(entityString.equals("END")) {
						/// the end of the entity list has been reached.
						sent = false;
						break;
					}
					if (Game.debug) System.out.println("CLIENT: loaded entity: " + entityString);
					newLevel.add((new Load()).loadEntity(entityString, game, false));
				}
				
				if(!sent) {
					// ready to start game now.
					curState = State.PLAY;
					if(game.menu instanceof MultiplayerMenu)
						((MultiplayerMenu)game.menu).curState = MultiplayerMenu.State.CONNECTED;
					if (Game.debug) System.out.println("CLIENT: progressed state to start");
				}
				else if(Game.debug)
					System.out.println("CLIENT: waiting for more entities...");
				return true;
			
			case TILE:
				if(curState != State.PLAY) return true; // ignoring for now
				if (Game.debug) System.out.println("CLIENT: recieved tile update");
				byte lvl = data[0], id = data[1], tdata = data[2];
				if(Game.levels[lvl] == null)
					return true; // this client doesn't need to worry about that level's updates.
				int pos = Integer.parseInt(new String(Arrays.copyOfRange(data, 3, data.length)).trim());
				Game.levels[lvl].tiles[pos] = id;
				Game.levels[lvl].data[pos] = tdata;
				return true;
			
			case ADD:
				if(curState != State.PLAY) return true; // ignoring for now
				byte curLevel = data[0];
				String entityData = new String(Arrays.copyOfRange(data, 1, data.length)).trim();
				if (Game.debug) System.out.println("CLIENT: recieved entity addition: " + entityData);
				Entity e = (new Load()).loadEntity(entityData, game, false);
				if(Game.levels[curLevel] != null)
					Game.levels[curLevel].add(e);
				else return false;
				return true;
			
			case REMOVE:
				//int eid = 0;
				//for(int i = 0; i < data.length; i++)
					//eid += data[i] << i*8;
				if(curState != State.PLAY) return true; // ignoring for now
				int eid = (data[0]<<(8*3)) + (data[1]<<(8*2)) + (data[2]<<8) + data[3];
				if (Game.debug) System.out.println("CLIENT: recieved entity removal: " + eid);
				Entity toRemove = Game.getEntity(eid);
				if(toRemove != null) {
					toRemove.remove();
					return true;
				}
				return false;
			
			case ENTITY:
				if(curState != State.PLAY) return true; // ignoring for now
				String updates = new String(data).trim();
				//if (Game.debug) System.out.println("CLIENT: recieved entity update: " + updates);
				int entityid = Integer.parseInt(updates.substring(0, updates.indexOf(";")));
				updates = updates.substring(updates.indexOf(";")+1);
				Entity entity = Game.getEntity(entityid);
				if(entity == null) {
					//System.err.println("CLIENT error with ENTITY request: specified entity could not be found.");
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
				if(Game.debug) System.out.println("CLIENT: player data recieved: " + pdata);
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
				sendData(MinicraftProtocol.InputType.SAVE, playerdata.getBytes());
				return true;
			
			case NOTIFY:
				if (Game.debug) System.out.println("CLIENT: recieved notification");
				if(curState != State.PLAY) return true; // ignoring for now
				String[] parts = new String(data).trim().split(";");
				String note = parts[0];
				int notetime = Integer.parseInt(parts[1]);
				Game.notifications.add(note);
				Game.notetick = notetime;
				return true;
			
			case CHESTOUT:
				if(curState != State.PLAY) return true; // ignoring for now
				//if (Game.debug) System.out.println("CLIENT: recieved chestout");
				Item item = Items.get(new String(data).trim());
				game.player.inventory.add(item);
				return true;
			
			case PICKUP:
				if(curState != State.PLAY) return true; // ignoring for now
				//if (Game.debug) System.out.println("CLIENT: recieved pickup approval");
				int ieid = Integer.parseInt(new String(data).trim());
				Entity ie = Game.getEntity(ieid);
				if(ie == null || !(ie instanceof ItemEntity)) {
					System.err.println("CLIENT error with PICKUP response: specified entity does not exist or is not an ItemEntity: " + ie);
					return false;
				}
				ie.remove();
				game.player.inventory.add(((ItemEntity)ie).item);
				return true;
		}
		
		return true; // this is reached by InputType.ENTITY and InputType.ADD, currently.
	}
	
	/// the below methods are all about sending data to the server, *not* setting any game values.
	
	public void sendData(MinicraftProtocol.InputType inType, byte[] startdata) {
		byte[] data = prependType(inType, startdata);
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, MinicraftProtocol.PORT);
		try {
			if (Game.debug) System.out.println("CLIENT: sending "+inType+" packet...");
			socket.send(packet);
		} catch(IOException ex) {
			System.err.println("CLIENT: error sending "+inType+" packet:");
			ex.printStackTrace();
		}
	}
	
	/** This is called when the player.attack() method is called. */
	public void requestInteraction(Player player) {
		/// I don't think the player parameter is necessary, but it doesn't harm anything.
		String itemString = player.activeItem != null ? player.activeItem.name : "null";
		sendData(MinicraftProtocol.InputType.INTERACT, (player.x+";"+player.y+";"+player.dir+";"+itemString+";"+player.inventory.count(Items.get("arrow"))).getBytes());
	}
	
	public void addToChest(Chest chest, Item item) {
		if(chest == null || item == null) return;
		sendData(MinicraftProtocol.InputType.CHESTIN, (chest.eid+";"+item).getBytes());
	}
	
	public void removeFromChest(Chest chest, int index) {
		if(chest == null) return;
		sendData(MinicraftProtocol.InputType.CHESTOUT, (chest.eid+";"+index).getBytes());
	}
	
	public void pickupItem(ItemEntity ie) {
		if(ie == null) return;
		sendData(MinicraftProtocol.InputType.PICKUP, String.valueOf(ie.eid).getBytes());
	}
	
	/** This method is responsible for sending an updated tile. */
	/*public void sendTileUpdate(int x, int y) {
		Level level = Game.levels[game.currentLevel];
		byte[] data = new byte[7];
		data[0] = (byte) game.currentLevel;
		data[1] = level.getTile(x, y).id;
		data[2] = level.getData(x, y);
		int pos = x + level.h * y;
		data[3] = pos >> (8*3);
		data[4] = pos >> (8*2) & 0xff;
		data[5] = pos >> 8 & 0xff;
		data[6] = pos & 0xff;
		
		sendData(MinicraftProtocol.InputType.TILE, data);
	}*/
	
	/*public void sendNotification(String note, int notetick) {
		if(note == null || note.length() == 0) {
			System.out.println("tried to send blank notification");
			return;
		}
		sendData("NOTIFY:"+note+","+notetick);
	}*/
	
	/*public static byte[] prependType(MinicraftProtocol.InputType type, byte[] data) {
		byte[] fulldata = new byte[data.length+1];
		fulldata[0] = (byte) type.ordinal();
		for(int i = 1; i < fulldata.length; i++)
			fulldata[i] = data[i-1];
		return fulldata;
	}*/
	
	public void endConnection() {
		if (Game.debug) System.out.println("closing client socket and ending connection");
		try {
			sendData(MinicraftProtocol.InputType.DISCONNECT, (new byte[0])); // send exit signal
			socket.close();
		} catch (NullPointerException ex) {}
		
		curState = State.DISCONNECTED;
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed() && socket.isConnected() && curState != State.DISCONNECTED;
	}
}
