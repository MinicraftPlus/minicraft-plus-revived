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

public class MinicraftServer extends Thread implements MinicraftConnection {
	
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
	
	public void sendEntityUpdate(int eid, Entity e) {
		
	}
	
	public void sendTileUpdate(int depth, int x, int y) {
		int lvlidx = Game.lvlIdx(depth);
		Level curLevel = Game.levels[lvlidx];
		int pos = x + curLevel.w * y;
		byte[] posbytes = String.valueOf(pos).getBytes();
		byte[] tiledata = new byte[posbytes.length+3];
		tiledata[0] = lvlidx;
		tiledata[1] = curLevel.tiles[pos];
		tiledata[2] = curLevel.data[pos];
		for(int i = 0; i < posbytes.length; i++)
			tiledata[i+3] = posbytes[i];
		
		broadcastData(prependType(MinicraftProtocol.InputType.TILE, tiledata));
	}
	
	/*public void sendNotification(String note, int ntime) {
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
		
		// handle reports of type INVALID
		if(inType == MinicraftProtocol.InputType.INVALID) {
			String msg = new String(data);
			System.err.println(msg);
			return false;
		}
		
		/// identify the client who sent this packet:
		RemotePlayer sender = getClientPlayer(address, port);
		// the sender should always be null for LOGIN packets, but not for any other. maybe invalid, but that returns above.
		if(sender == null && inType != MinicraftProtocol.InputType.LOGIN) {
			System.err.println("SERVER error: cannot identify sender of packet; type="+inType.name());
			sendError("You must login first.", address, port);
			return false;
		}
		
		if(MinicraftProtocol.InputType.serverOnly.contains(inType)) {
			/// these are ALL illegal for a client to send.
			System.err.println("SERVER warning: client " + sender + " sent illegal packet type " + inType + " to server.");
			//sendError("You cannot set the world variables.", address, port);
			return false;
		}
		
		switch(inType) {
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
				RemotePlayer clientPlayer = new RemotePlayer(game, username, address, port);
				clientPlayer.findStartPos(Game.levels[Game.lvlIdx(0)]); // find start pos
				Game.levels[Game.lvlIdx(0)].add(clientPlayer); // add to level
				//making INIT_W packet
				String sendString = "";
				sendString += Game.levels[Game.lvlIdx(0)].w + ",";
				sendString += Game.levels[Game.lvlIdx(0)].h + ",";
				sendString += clientPlayer.x + ",";
				sendString += clientPlayer.y + ",";
				sendString += Game.tickCount + ",";
				sendString += ModeMenu.mode + ",";
				if(ModeMenu.score)
					sendString += game.scoreTime+",";
				/// send client world info
				sendData(prependType(MinicraftProtocol.InputType.INIT_W, sendString.getBytes()), sender);
				clientList.add(clientPlayer); /// file client player
				/// tell others of client joining
				broadcastData(prependType(MinicraftProtocol.InputType.ADD, clientPlayer.serialize().getBytes()), clientPlayer);
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
					sendData(tiledata, sender);
					return true;
				} else {
					System.err.println("SERVER warning: Client " + sender + " tried to request tiles from nonexistent level " + levelidx);
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
						edata += e.serialize();
						edata += "\n";
					}
					
					sendData(prependType(MinicraftProtocol.InputType.INIT_E, edata.getBytes()), sender);
					return true;
				} else {
					System.err.println("SERVER warning: Client " + sender + " tried to request entities from nonexistent level " + levelidx);
					sendError("requested level does not exist.", address, port);
					return false;
				}
				break;
			
			case DISCONNECT:
				/// tell the other clients to remove this client from their games
				broadcastData(prependType(MinicraftProtocol.InputType.REMOVE, (new byte[1] {sender.eid})), sender);
				clientList.remove(sender); // remove from the server list
				return true;
				
			
			case CHESTIN: case CHESTOUT:
				String[] info = new String(data).split(";");
				int eid = Integer.parseInt(info[0]);
				Entity e = Game.getEntity(eid);
				if(e == null || !e instanceof Chest) {
					System.err.println("SERVER error with CHESTOUT request: Specified chest entity did not exist or was not a chest.");
					return false;
				}
				Chest chest = (Chest) e;
				
				if(inType == MinicraftProtocol.InputType.CHESTIN) {
					Item item = Items.get(info[1]);
					if(item == null) {
						System.err.println("SERVER error with CHESTIN request: specified item could not be found from string: " + info[1]);
						return false;
					}
					chest.inventory.add(item);
				}
				else if(inType == MinicraftProtocol.InputType.CHESTOUT) {
					int index = Integer.parseInt(info[1]);
					if(index >= chest.inventory.length || index < 0) {
						System.err.println("SERVER error with CHESTOUT request: specified chest inv index is out of bounds: "+index+"; inv size:"+chest.inventory.length);
						return false;
					}
					// was a valid index
					Item removed = chest.inventory.remove(index);
					
					sendData(prependType(MinicraftProtocol.InputType.CHESTOUT, removed.getData().getBytes()), sender); // send back the *exact* same packet as was sent; the client will handle it accordingly, by changing their inventory.
				}
				
				broadcastData(prependType(MinicraftProtocol.InputType.ENTITY, chest.eid+";inventory["+chest.inventory.getItemData()+"]"), sender);
				return true;
			
			case PICKUP:
				int eid = Integer.parseInt(new String(data));
				Entity e = Game.getEntity(eid);
				if(e == null || !e instanceof ItemEntity) {
					System.err.println("SERVER error with PICKUP request: Requested entity does not exist or is not an ItemEntity.");
					return false;
				}
				if(!e.removed()) {
					e.remove();
					sendData(alldata, sender);
					broadcastData(prependType(MinicraftProtocol.InputType.REMOVE, String.valueOf(e.eid)), sender);
				} else
					return false;
				return true;
			
			case INTERACT:
				//x, y, dir, item
				String[] info = new String(data).split(";");
				// since this should be the most up-to-date data, just update the remote player coords with them.
				sender.x = Integer.parseInt(info[0]);
				sender.y = Integer.parseInt(info[1]);
				sender.dir = Integer.parseInt(info[2]);
				sender.activeItem = Items.get(info[3]); // this can be null; and that's fine, it means a fist. ;)
				int arrowCount = Integer.parseInt(info[4]);
				int curArrows = sender.inventory.count(Items.get("arrow"));
				if(curArrows < arrowCount)
					sender.inventory.add(Items.get("arrow"), arrowCount-curArrows);
				if(curArrows > arrowCount)
					sender.inventory.removeItems(Items.get("arrow"), curArrows-arrowCount);
				sender.attack();
				/// note: the player may have fired an arrow, but we won't tell because that player will update it theirself.
			
			/// I'm this should end up never being used...
			default:
				System.out.println("server used default behavior for input type " + inType);
				broadcastData(alldata, sender);
				return true;
		}
		
		return true;
	}
	
	public void broadcastData(byte[] data) {
		for(RemotePlayer client: clientList) {
			sendData(data, client);
		}
	}
	
	public void broadcastData(byte[] data, RemotePlayer sender) {
		for(RemotePlayer client: clientList) {
			if(client != sender) // send this packet to all EXCEPT the specified one.
				sendData(data, client);
		}
	}
	
	public void sendData(byte[] data, RemotePlayer rp) { sendData(data, rp.ipAddress, rp.port); }
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
