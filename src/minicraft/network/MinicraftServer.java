package minicraft.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.RemotePlayer;

public class MinicraftServer extends Thread {
	
	public ArrayList<RemotePlayer> clientList = new ArrayList<RemotePlayer>();
	
	private Game game;
	private DatagramSocket socket = null;
	
	public MinicraftServer(Game game) {
		super("MinicraftServer");
		this.game = game;
		try {
			System.out.println("opening server socket...");
			socket = new DatagramSocket(MinicraftProtocol.PORT);
		} catch (SocketException ex) {
			System.err.println("failed to open server socket.");
			ex.printStackTrace();
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
			parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
		}
		
		System.out.println("closing server socket");
		
		endConnection();
	}
	
	/*public void sendEntityUpdate(int eid, Entity e) {
		for(RemotePlayer client: clientList) {
			thread.sendEntityUpdate(eid, e);
		}
	}
	
	public void sendTileUpdate(int x, int y) {
		for(RemotePlayer client: clientList) {
			thread.sendTileUpdate(x, y);
		}
	}
	
	public void sendNotification(String note, int ntime) {
		for(RemotePlayer client: clientList) {
			thread.sendNotification(note, ntime);
		}
	}*/
	
	public ArrayList<String> getClientNames() {
		ArrayList<String> names = new ArrayList<String>();
		for(RemotePlayer client: clientList) {
			names.add(client.getClientName());
		}
		
		return names;
	}
	
	public boolean parsePacket(byte[] alldata, InetAddress address, int port) {
		
		MinicraftProtocol.InputType inType = MinicraftProtocol.getInputType(alldata[0]);
		
		byte[] data = Arrays.copyOfRange(alldata, 1, alldata.length);
		/*
			wanted behavior for each input type:
			
			-INVALID: print message to console.
			
			-LOGIN
				expected data: username and game version
				behavior: check version to match game version
					-if match:
						+create remote player
						+find good start pos from game instance's surface level
						+send back INIT_W packet to given address and port
						+send ADD packet to all clients *besides* the one that sent the packet, with remote player data
					-if no match:
						+send back INVALID packet with message that versions didn't match; need ____(current) version
			
			-INIT_W: ignore or send back INVALID: not allowed to set data
			
			-INIT_T
				expected data: level number
				behavior: send back list of tiles for specified level
			
			-INIT_E
				expected data: level number
				behavior: send back list of entities on specified level, enough data to construct them anew
			
			-DISCONNECT
				expected data: none
				behavior: send REMOVE packet to the other clients, data being the id of the remote player matching the address from which the DISCONNECT packet was recieved.
			
			-TILE, ENTITY, ADD, REMOVE, NOTIFY: broadcast the packet to all the other clients
		*/
		
		switch(inType) {
			case INVALID:
				String msg = new String(data);
				System.err.println(msg);
				return false;
			
			case INIT_W:
				System.err.println("SERVER warning: client " + address+":"+port + " tried to request INIT_W");
				sendError("You cannot set the world variables.", address, port);
				return false;
			
			case LOGIN:
				String info = new String(data);
				String username = data.split(";")[0];
				Load.Version clientVersion = new Load.Version(data.split(";")[1]);
				// check version; send back invalid if they don't match.
				if(clientVersion.compareTo(new Load.Version(Game.VERSION)) != 0) {
					sendError("wrong game version; need " + Game.VERSION, address, port);
					return false;
				}
				
				/// versions match; make client player
				int eid = generateUniqueEntityId();
				RemotePlayer clientPlayer = new RemotePlayer(game, eid, username, address, port);
				clientPlayer.findStartPos(Game.levels[3]); // find start pos
				Game.levels[3].add(clientPlayer); // add to level
				//making INIT_W packet
				String sendString = "";
				sendString += Game.levels[3].w + ",";
				sendString += Game.levels[3].h + ",";
				sendString += clientPlayer.x + ",";
				sendString += clientPlayer.y + ",";
				sendString += Game.tickCount + ",";
				sendString += ModeMenu.mode + ",";
				if(ModeMenu.score)
					sendString += game.scoreTime+",";
				/// send client world info
				sendData(prependType(MinicraftProtocol.InputType.INIT_W, sendString.getBytes()), address, port);
				clientList.add(clientPlayer); /// file client player
				/// tell others of client joining
				broadcastData(prependType(MinicraftProtocol.InputType.ADD, clientPlayer.toString().getBytes()), clientPlayer);
				return true;
			
			case INIT_T:
				// send back the tiles in the level specified.
				int levelidx = (int) data[0];
				if(levelidx >= 0 && levelidx < Game.levels.length) {
					byte[] tiledata = new byte[Game.levels[levelidx].tiles.length*2 + 1];
					tiledata[0] = (byte) MinicraftProtocol.InputType.INIT_T.ordinal();
					for(int i = 0; i < tiledata.length/2 - 2; i++) {
						tiledata[i*2 + 1] = Game.levels[levelidx].tiles[i];
						tiledata[i*2+1 + 1] = Game.levels[levelidx].data[i];
					}
					sendData(tiledata, address, port);
					return true;
				} else {
					System.err.println("SERVER warning: Client " + address+":"+port + " tried to request tiles from nonexistent level " + levelidx);
					sendError("requested level does not exist.", address, port);
					return false;
				}
				break;
			
			case INIT_E:
				// send back the entities in the level specified.
				int levelidx = (int) data[0];
				if(levelidx >= 0 && levelidx < Game.levels.length) {
					String edata = "";
					for(Entity e: Game.levels[levelidx].getEntities()) {
						edata += e.toString();
						edata += "\n";
					}
					
					sendData(prependType(MinicraftProtocol.InputType.INIT_E, edata.getBytes()), address, port);
					return true;
				} else {
					System.err.println("SERVER warning: Client " + address+":"+port + " tried to request entities from nonexistent level " + levelidx);
					sendError("requested level does not exist.", address, port);
					return false;
				}
				break;
			
			case DISCONNECT:
				RemotePlayer sender = getClientPlayer(address, port);
				if(sender != null) {
					/// tell the other clients to remove this client from their games
					broadcastData(prependType(MinicraftProtocol.InputType.REMOVE, (new byte[1] {sender.eid})), sender);
					clientList.remove(sender); // remove from the server list
					return true;
				}
				return false;
			
			case ADD:
				/// TODO check entity id to see if it is unique. if so, proceed to default. else, change the id, and send an entity update packet to the sender client to update their entity's id.
			
			default:
				broadcastData(alldata, getClientPlayer(address, port));
				return true;
		}
		
		return true;
	}
	
	public void broadcastData(byte[] data) {
		for(RemotePlayer client: clientList) {
			sendData(data, client.ipAddress, client.port);
		}
	}
	
	public void broadcastData(byte[] data, RemotePlayer sender) {
		for(RemotePlayer client: clientList) {
			if(client != sender)
				sendData(data, client.ipAddress, client.port);
		}
	}
	
	public void sendData(byte[] data, InetAddress ip, int port) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
		try {
			socket.send(packet);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void sendError(String message, InetAddress ip, int port) {
		sendData(prependType(MinicraftProtocol.InputType.INVALID, message.getBytes()), ip, port);
	}
	
	public static int generateUniqueEntityId() {
		java.util.Random random = new java.util.Random();
		int eid = 0;
		while(!idIsUnused(eid))
			eid = random.nextInt(Integer.MAX_VALUE);
		
		return eid;
	}
	
	public static boolean idIsUnused(int eid) {
		for(Level level: Game.levels) {
			for(Entity e: level.getEntities()) {
				if(e.eid == eid)
					return false;
			}
		}
		
		return true;
	}
	
	/*public static byte[] prependType(MinicraftProtocol.InputType type, byte[] data) {
		byte[] fulldata = new byte[data.length+1];
		fulldata[0] = (byte) type.ordinal();
		for(int i = 1; i < fulldata.length; i++)
			fulldata[i] = data[i-1];
		return fulldata;
	}*/
	
	public RemotePlayer getClientPlayer(InetAddress ip, int port) {
		for(RemotePlayer client: clientList)
			if(client.ipAddress.equals(ip) && client.port == port)
				return client;
		
		return null;
	}
	
	public void endConnection() {
		synchronized ("lock") {
			for(RemotePlayer client: clientList) {
				broadcastData(prependType(MinicraftProtocol.InputType.DISCONNECT, (new byte[0] {})));
			}
			
			clientList.clear();
		}
		
		try {
			socket.close();
		} catch (SocketException ex) {
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
		}
	}
	
	public boolean isConnected() {
		return socket != null && !socket.isClosed() && clientList.size() > 0;
	}
}
