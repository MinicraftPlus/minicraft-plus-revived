package minicraft.network;

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

/// This class is only used by the client runtime; the server runtime doesn't touch it.
public class MinicraftClient extends Thread {
	
	private Game game;
	private DatagramSocket socket = null;
	private InetAddress ipAddress = null;
	
	private static final int requestRepeatInterval = 100;
	private int requestTimer = 0;
	
	public enum State {
		LOGIN, TILES, ENTITIES, START, PLAY, DISCONNECTED
	}
	
	public State curState = State.DISCONNECTED;
	
	public MinicraftClient(Game game, String hostName, String playerName) {
		try {
			socket = new DatagramSocket();
			ipAddress = InetAddress.getByName(hostName);
			
			game.player = new RemotePlayer(game, playerName, socket.getHostAddress(), socket.getPort());
			
			curState = State.LOGIN;
			
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
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
			
			parsePacket(game, packet.getData(), packet.getAddress(), packet.getPort());
			
			if(curState != State.PLAY) {
				boolean act = requestTimer <= 0;
				if(act) requestTimer = requestRepeatInterval;
				switch(curState) {
					case LOGIN:
						if(!act) break; // don't send request constantly.
						/// send login request.
						String username = ((RemotePlayer)game.player).username;
						sendData(MinicraftProtocol.InputType.LOGIN, username+";"+Game.VERSION);
						break;
					
					case TILES:
						if(!act) break;
						// send request for level tiles.
						sendData(MinicraftProtocol.InputType.INIT_T, (new byte[1] {(byte)game.currentLevel}));
						break;
					
					case ENTITIES:
						if(!act) break;
						// send request for level entities.
						sendData(MinicraftProtocol.InputType.INIT_E, (new byte[1] {(byte)game.currentLevel}));
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
						thread.sleep(10);
					} catch(InterruptedException ex) {}
					requestTimer -= 10;
				}
			}
		}
	}
	
	/** This method is responsible for parsing all data recieved by the socket. */
	public boolean parsePacket(byte[] alldata, InetAddress address, int port) {
		//if (Game.debug) System.out.println("checking input...");
		if(alldata == null || alldata.length() == 0) return false;
		
		InputType inType = MinicraftProtocol.getInputType(alldata[0]);
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
				System.err.println(msg);
				game.notifications.add("Server Error: " + msg);
				return false;
			
			case LOGIN:
				System.err.println("Server tried to login...");
				return false;
			
			case DISCONNECT:
				game.setMenu(new MultiplayerMenu(false, new TitleMenu()));
				return true;
			
			case INIT_W:
				String[] infostrings = new String(data).split(",");
				int[] info = new int[infostrings.length];
				for(int i = 0; i < info.length; i++)
					info[i] = Integer.parseInt(infostrings[i]);
				Game.lvlw = info[0];
				Game.lvlh = info[1];
				game.player.x = info[2];
				game.player.x = info[3];
				game.tickCount = info[4];
				ModeMenu.updateModeBools(info[5]);
				if(ModeMenu.score)
					game.scoreTime = info[6];
				
				/// vars are set; now start requesting entities.
				if(curState == State.LOGIN) // only go through the sequence if this is called as part of start up.
					curState = State.TILES;
				return true;
			
			case INIT_T:
				/// recieve tiles.
				Level level = new Level(game, Game.lvlw, Game.lvlh, (game.currentLevel==5?-4:game.currentLevel-3), Game.levels[game.currentLevel==4?null:game.currentLevel==5?0:game.currentLevel+1], false);
				for(int i = 0; i < level.tiles.length && i < data.length/2-1; i++) {
					level.tiles[i] = data[i*2];
					level.data[i] = data[i*2+1];
				}
				Game.levels[game.currentLevel] = level;
				curState = State.ENTITIES;
				return true;
			
			case INIT_E:
				Level level = Game.levels[game.currentLevel];
				String[] entities = new String(data).split("\\n");
				for(String entityString: entities) {
					// decode the string back into an entity
					String[] constructorTypes = entityString.split(";");
					Class c = Class.forName("minicraft.entity."+constructorTypes[0]);
					Class[] constructorParams = new Class[constructorTypes.length-1];
					for(int i = 0; i < constructorParams.length; i++) {
						constructorParams[i] = constructorTypes[i+1].split(",")[0];
					}
					c.getConstructor();
					// TODO finish this.
				}
				
				// ready to start game now.
				curState = State.START;
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
				// TODO finish this. Should have level seperate.
				byte lvl = data[0];
				String info = new String(Arrays.copyOfRange(data, 1, data.length));
				break;
			
			case ENTITY:
				String info = new String(data);
				int eid = Integer.parseInt(info.substring(0, info.indexOf(";")));
				// TODO here we update the entity.
				break;
			
			case REMOVE:
				//int eid = 0;
				//for(int i = 0; i < data.length; i++)
					//eid += data[i] << i*8;
				int eid = (data[0]<<(8*3)) + (data[1]<<(8*2)) + (data[2]<<8) + data[3];
				Entity e = Game.getEntity(eid);
				if(e != null) {
					e.remove();
					return true;
				}
				return false;
			
			case NOTIFY:
				String[] info = new String(data).split(";");
				String msg = info[0];
				int notetime = Integer.parseInt(info[1]);
				Game.notifications.add(msg);
				Game.notetick = notetime;
				return true;
		}
		
		return true; // this is reached by InputType.ENTITY and InputType.ADD, currently.
	}
	
	public void sendData(MinicraftProtocol.InputType inType, byte[] startdata) {
		byte[] data = prependType(inType, startdata);
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, MinicraftProtocol.PORT);
		try {
			socket.send(packet);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/** This method is responsible for sending an updated tile. */
	public void sendTileUpdate(int x, int y) {
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
	}
	
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
			sendData(MinicraftProtocol.InputType.DISCONNECT, (new byte[0] {})); // send exit signal
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {}
		
		curState = State.DISCONNECTED;
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed() && socket.isConnected() && curState != State.DISCONNECTED;
	}
}
