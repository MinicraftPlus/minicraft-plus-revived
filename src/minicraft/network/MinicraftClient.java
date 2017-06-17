package minicraft.network;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
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
	private InetAddress ipAddress = null;
	
	private static final int requestRepeatInterval = 100;
	private int requestTimer = 0;
	
	public static enum State {
		USERNAMES, IDLING, LOGIN, TILES, ENTITIES, START, PLAY, DISCONNECTED
	}
	
	public State curState = State.DISCONNECTED;
	
	public MinicraftClient(Game game, String hostName) {
		try {
			socket = new DatagramSocket();
			ipAddress = InetAddress.getByName(hostName);
			//game.player = new RemotePlayer(game, playerName, socket.getInetAddress(), socket.getPort());
			
			curState = State.USERNAMES;
			
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void login(String username) {
		game.player = new RemotePlayer(game, username, socket.getInetAddress(), socket.getPort());
		curState = State.LOGIN;
	}
	
	public void run() {
		while(isConnected()) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
			
			if(curState != State.PLAY && curState != State.IDLING) {
				boolean act = requestTimer <= 0;
				if(act) requestTimer = requestRepeatInterval;
				switch(curState) {
					case USERNAMES:
						if(!act) break;
						sendData(MinicraftProtocol.InputType.USERNAMES, new byte[0]);
						break;
					
					case LOGIN:
						if(!act) break; // don't send request constantly.
						/// send login request.
						String username = ((RemotePlayer)game.player).username;
						sendData(MinicraftProtocol.InputType.LOGIN, (username+";"+Game.VERSION).getBytes());
						break;
					
					case TILES:
						if(!act) break;
						// send request for level tiles.
						sendData(MinicraftProtocol.InputType.INIT_T, (new byte[] {(byte)game.currentLevel}));
						break;
					
					case ENTITIES:
						if(!act) break;
						// send request for level entities.
						sendData(MinicraftProtocol.InputType.INIT_E, (new byte[] {(byte)game.currentLevel}));
						break;
					
					case START:
						/// this means that the data has been recieved, but the game hasn't started yet.
						// i don't seem to really need this much right now... but it seems like a good idea.
						Game.levels[game.currentLevel].add(game.player);
						Game.readyToRenderGameplay = true;
						game.setMenu(null);
						curState = State.PLAY;
						break;
				}
				
				if(curState == State.LOGIN || curState == State.TILES || curState == State.ENTITIES) {
					try {
						Thread.sleep(10);
					} catch(InterruptedException ex) {}
					requestTimer -= 10;
				}
			}
		}
	}
	
	/** This method is responsible for parsing all data recieved by the socket. */
	public boolean parsePacket(byte[] alldata, InetAddress address, int port) {
		//if (Game.debug) System.out.println("checking input...");
		if(alldata == null || alldata.length == 0) return false;
		
		MinicraftProtocol.InputType inType = MinicraftProtocol.getInputType(alldata[0]);
		if(inType == null)
			return false;
		
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
				String msg = new String(data);
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
				
				return false;
			
			case USERNAMES:
				String names = new String(data);
				String[] namelist = names.split("\n");
				if(game.menu instanceof MultiplayerMenu) {
					MultiplayerMenu mm = (MultiplayerMenu) game.menu;
					mm.takenNames = Arrays.asList(namelist);
					mm.curState = MultiplayerMenu.State.ENTERNAME;
				}
				return true;
			
			case LOGIN:
				System.err.println("Server tried to login...");
				return false;
			
			case INTERACT:
				System.err.println("Server tried to interact...");
				return false;
			
			case DISCONNECT:
				game.setMenu(new MultiplayerMenu(false));
				return true;
			
			case SAVE:
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
				playerdata = playerdata.substring(0, playerdata.length()-1);
				sendData(MinicraftProtocol.InputType.SAVE, playerdata.getBytes());
				return true;
			
			case PLAYER:
				// use the contained data to load up the player object vars.
				String pdata = new String(data);
				String[] playerparts = pdata.split("\\n");
				List<String> playerinfo = Arrays.asList(playerparts[0].split(","));
				List<String> playerinv = Arrays.asList(playerparts[1].split(","));
				Load load = new Load();
				load.loadInventory(game.player.inventory, playerinv);
				load.loadPlayer(game.player, playerinfo);
				return true;
			
			case INIT_W:
				MultiplayerMenu mm = null;
				if(game.menu instanceof MultiplayerMenu) {
					mm = (MultiplayerMenu) game.menu;
					mm.loadingMessage = "World";
					mm.curState = MultiplayerMenu.State.LOADING;
				}
				
				String[] infostrings = new String(data).split(",");
				int[] info = new int[infostrings.length];
				for(int i = 0; i < info.length; i++)
					info[i] = Integer.parseInt(infostrings[i]);
				game.player.eid = info[0];
				Game.lvlw = info[1];
				Game.lvlh = info[2];
				game.player.x = info[3];
				game.player.x = info[4];
				game.tickCount = info[5];
				ModeMenu.updateModeBools(info[6]);
				if(ModeMenu.score && info.length > 7)
					game.scoreTime = info[7];
				
				/// vars are set; now start requesting entities.
				if(curState == State.LOGIN) {// only go through the sequence if this is called as part of start up.
					curState = State.TILES;
					mm.loadingMessage = "Tiles";
				}
				return true;
			
			case INIT_T:
				/// recieve tiles.
				int lvldepth = Game.idxToDepth[game.currentLevel];
				Level level = new Level(game, Game.lvlw, Game.lvlh, lvldepth, Game.levels[Game.lvlIdx(lvldepth+1)], false);
				for(int i = 0; i < level.tiles.length && i < data.length/2-1; i++) {
					level.tiles[i] = data[i*2];
					level.data[i] = data[i*2+1];
				}
				Game.levels[game.currentLevel] = level;
				curState = State.ENTITIES;
				if(game.menu instanceof MultiplayerMenu)
					((MultiplayerMenu)game.menu).loadingMessage = "Entities";
				return true;
			
			case INIT_E:
				Level newLevel = Game.levels[game.currentLevel];
				String[] entities = new String(data).split(",");
				for(String entityString: entities) {
					newLevel.add((new Load()).loadEntity(entityString, game, false));
					/*// decode the string back into an entity
					String[] constructorTypes = entityString.split(";");
					Class c = Class.forName("minicraft.entity."+constructorTypes[0]);
					Class[] constructorParams = new Class[constructorTypes.length-1];
					for(int i = 0; i < constructorParams.length; i++) {
						constructorParams[i] = constructorTypes[i+1].split(",")[0];
					}
					c.getConstructor();
					*/
				}
				
				// ready to start game now.
				curState = State.START;
				if(game.menu instanceof MultiplayerMenu)
					((MultiplayerMenu)game.menu).curState = MultiplayerMenu.State.CONNECTED;
				break;
			
			case TILE:
				byte lvl = data[0], id = data[1], tdata = data[2];
				if(Game.levels[lvl] == null)
					return true; // this client doesn't need to worry about that level's updates.
				int pos = Integer.parseInt(new String(Arrays.copyOfRange(data, 3, data.length)));
				Game.levels[lvl].tiles[pos] = id;
				Game.levels[lvl].data[pos] = tdata;
				return true;
			
			case ADD:
				byte curLevel = data[0];
				String entityData = new String(Arrays.copyOfRange(data, 1, data.length));
				Entity e = (new Load()).loadEntity(entityData, game, false);
				if(Game.levels[curLevel] != null)
					Game.levels[curLevel].add(e);
				else return false;
				return true;
			
			case ENTITY:
				String updates = new String(data);
				int entityid = Integer.parseInt(updates.substring(0, updates.indexOf(";")));
				updates = updates.substring(updates.indexOf(";")+1);
				Entity entity = Game.getEntity(entityid);
				if(entity == null) {
					System.err.println("CLIENT error with ENTITY request: specified entity could not be found.");
					return false;
				}
				entity.update(updates);
			
			case REMOVE:
				//int eid = 0;
				//for(int i = 0; i < data.length; i++)
					//eid += data[i] << i*8;
				int eid = (data[0]<<(8*3)) + (data[1]<<(8*2)) + (data[2]<<8) + data[3];
				Entity toRemove = Game.getEntity(eid);
				if(toRemove != null) {
					toRemove.remove();
					return true;
				}
				return false;
			
			case NOTIFY:
				String[] parts = new String(data).split(";");
				String note = parts[0];
				int notetime = Integer.parseInt(parts[1]);
				Game.notifications.add(note);
				Game.notetick = notetime;
				return true;
			
			case CHESTOUT:
				Item item = Items.get(new String(data));
				game.player.inventory.add(item);
				return true;
			
			case PICKUP:
				int ieid = Integer.parseInt(new String(data));
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
			socket.send(packet);
		} catch(IOException ex) {
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
			sendData(MinicraftProtocol.InputType.DISCONNECT, (new byte[] {})); // send exit signal
			socket.close();
		} catch (NullPointerException ex) {}
		
		curState = State.DISCONNECTED;
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed() && socket.isConnected() && curState != State.DISCONNECTED;
	}
}
