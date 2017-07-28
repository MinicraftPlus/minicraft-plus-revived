package minicraft.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.Player;
import minicraft.entity.RemotePlayer;
import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.saveload.Load;
import minicraft.saveload.Save;

public class MinicraftServerThread extends MinicraftConnection {
	
	class MyTask extends TimerTask {
		public MyTask() {}
		public void run() {}
	}
	
	private static final int PING_INTERVAL = 10; // measured in seconds
	
	private MinicraftServer serverInstance;
	private RemotePlayer client;
	
	protected boolean isPlaying = false;
	
	private Game game;
	
	private NetworkInterface computer = null;
	
	//private List<Integer> trackedEntities = new ArrayList<Integer>();
	private List<Timer> gameTimers;
	private boolean recievedPing = true;
	
	private List<InputType> packetTypesToKeep = new ArrayList<InputType>();
	private List<InputType> packetTypesToCache = new ArrayList<InputType>();
	private List<String> cachedPackets = new ArrayList<String>();
	
	public MinicraftServerThread(Game game, Socket socket, MinicraftServer serverInstance) {
		super("MinicraftServerThread", socket);
		
		this.serverInstance = serverInstance;
		this.game = game;
		
		if(serverInstance.isFull()) {
			sendError("server at max capacity.");
			super.endConnection();
			return;
		}
		
		client = new RemotePlayer(null, game, false, socket.getInetAddress(), socket.getPort());
		
		try {
			computer = NetworkInterface.getByInetAddress(socket.getInetAddress());
		} catch(SocketException ex) {
			System.err.println("SERVER THREAD ERROR: couldn't get network interface from socket address. ("+this+")");
			ex.printStackTrace();
		}
		
		if(computer == null)
			System.err.println("WARNING: network interface for " + this + "'s socket connection is null.");
		
		//if (Game.debug) System.out.println("network interface for " + this +": " + computer);
		
		packetTypesToKeep.addAll(InputType.tileUpdates);
		packetTypesToKeep.addAll(InputType.entityUpdates);
		
		gameTimers = new ArrayList<Timer>();
		
		Timer t = new Timer("ClientPing");
		t.schedule((new MyTask() {
			public void run() { MinicraftServerThread.this.ping(); }
		}), 1000, PING_INTERVAL*1000);
		gameTimers.add(t);
		
		start();
	}
	
	public RemotePlayer getClient() { return client; }
	
	protected synchronized boolean parsePacket(InputType inType, String data) {
		//if(inType == InputType.LOAD) isPlaying = true;
		
		if(inType == InputType.PING) {
			//if (Game.debug) System.out.println(this+" recieved ping");
			recievedPing = true;
			return true;
		}
		
		return serverInstance.parsePacket(this, inType, data);
	}
	
	private void ping() {
		//if (Game.debug) System.out.println(this+" is doing ping sequence. recieved ping: " + recievedPing);
		
		if(!recievedPing) {
			// disconnect from the client; they are taking too long to respond and probably don't exist anyway.
			sendError("connection timed out; ping too slow");
			endConnection();
		} else {
			recievedPing = false;
			sendData(InputType.PING, "");
		}
	}
	
	protected void sendError(String message) {
		if (Game.debug) System.out.println("SERVER: sending error to " + client + ": \"" + message + "\"");
		sendData(InputType.INVALID, message);
	}
	
	protected void cachePacketTypes(List<InputType> packetTypes) {
		packetTypesToCache.addAll(packetTypes);
		packetTypesToKeep.removeAll(packetTypes);
	}
	
	protected void sendCachedPackets() {
		packetTypesToCache.clear();
		
		for(String packet: cachedPackets) {
			InputType inType = InputType.values[Integer.parseInt(packet.substring(0, packet.indexOf(":")))];
			packet = packet.substring(packet.indexOf(":")+1);
			sendData(inType, packet);
		}
		
		cachedPackets.clear();
	}
	
	protected synchronized void sendData(InputType inType, String data) {
		if(packetTypesToCache.contains(inType))
			cachedPackets.add(inType.ordinal()+":"+data);
		else if(packetTypesToKeep.contains(inType))
			return;
		else
			super.sendData(inType, data);
	}
	
	public void sendTileUpdate(Level level, int x, int y) {
		sendTileUpdate(level.depth, x, y);
	}
	public void sendTileUpdate(int depth, int x, int y) {
		sendData(InputType.TILE, Tile.getData(depth, x, y));
	}
	
	public void sendEntityUpdate(Entity e, String updateString) {
		if(updateString.length() > 0) {
			//if (Game.debug && e instanceof Player) System.out.println("SERVER sending player update to " + client + ": " + e + "; data = " + updateString);
			sendData(InputType.ENTITY, e.eid+";"+updateString);
		}// else
		//	if(Game.debug) System.out.println("SERVER: skipping entity update b/c no new fields: " + e);
	}
	
	public void sendEntityAddition(Entity e) {
		String edata = Save.writeEntity(e, false);
		if(edata == null || edata.length() == 0)
			System.out.println("entity not worth adding to client level: " + e + "; not sending to " + client);
		else {
			/*if(trackedEntities.contains(e.eid)) { // this isn't going to work, b/c the client removes entities without telling the server, and i don't want the client to have to tell the server either, b/c that's sending more packets.
				//if(Game.debug) System.out.println(this+" blocking addition of entity ");
				return;
			}
			else trackedEntities.add(e.eid);*/
			//if(Game.debug) System.out.println(this+" sending entity addition to client: " + e);
			sendData(InputType.ADD, edata);
		}
	}
	
	public void sendEntityRemoval(int eid) {
		//trackedEntities.remove(eid);
		sendData(InputType.REMOVE, String.valueOf(eid));
	}
	
	public void sendNotification(String note, int notetime) {
		sendData(InputType.NOTIFY, notetime+";"+note);
	}
	
	public void sendPlayerHurt(int damage, int attackDir) {
		sendData(InputType.HURT, damage+";"+attackDir);
	}
	
	public void updatePlayerActiveItem(Item heldItem) {
		if(client.activeItem == null && heldItem == null || client.activeItem.matches(heldItem)) {
			System.out.println("SERVER THREAD: player active item is already the one specified: " + heldItem + "; not updating.");
			return;
		}
		
		if(client.activeItem != null && !(client.activeItem instanceof PowerGloveItem))
			sendData(InputType.CHESTOUT, client.activeItem.getData());
		client.activeItem = heldItem;
		
		sendData(InputType.INTERACT, ( client.activeItem == null ? "null" : client.activeItem.getData() ));
	}
	
	public void setClientPos(int lvlDepth, int x, int y) {
		sendData(InputType.MOVE, lvlDepth+";"+x+";"+y);
	}
	
	protected void respawnPlayer() {
		client = new RemotePlayer(game, true, client);
		client.respawn(Game.levels[Game.lvlIdx(0)]); // get the spawn loc. of the client
		sendData(InputType.PLAYER, client.getData()); // send spawn loc.
	}
	
	protected File getRemotePlayerFile() {
		File[] clientFiles = serverInstance.getRemotePlayerFiles();
		
		for(File file: clientFiles) {
			String macString = "";
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				try {
					macString = br.readLine().trim();
				} catch(IOException ex) {
					System.err.println("failed to read line from file.");
					ex.printStackTrace();
				}
			} catch(FileNotFoundException ex) {
				System.err.println("couldn't find remote player file: " + file);
				ex.printStackTrace();
			}
			
			try {
				if(computer != null && macString.equals(getMacString(computer.getHardwareAddress()))) {
					/// this player has been here before.
					if (Game.debug) System.out.println("remote player file found; returning file " + file.getName());
					return file;
				}
			} catch(SocketException ex) {
				System.err.println("problem fetching mac address.");
				ex.printStackTrace();
			}
		}
		
		return null;
	}
	
	protected String getRemotePlayerFileData() {
		File rpFile = getRemotePlayerFile();
		
		String playerdata = "";
		if(rpFile != null && rpFile.exists()) {
			try {
				String content = Load.loadFromFile(rpFile.getPath(), false); //Files.readAllLines(rpFile.toPath(), StandardCharsets.UTF_8);
				playerdata = content.substring(content.indexOf("\n")+1);
			} catch(IOException ex) {
				System.err.println("failed to read remote player file: " + rpFile);
				ex.printStackTrace();
				return "";
			}
		}
		
		return playerdata;
	}
	
	private static String getMacString(byte[] macAddress) {
		StringBuilder macString = new StringBuilder();
		for(byte b: macAddress) {
			//String hexInt = Integer.toHexString((int)b);
			//if (Game.debug) System.out.println("mac byte as hex int: " + hexInt);
			//macString.append(hexInt.substring(hexInt.length()-2));
			macString.append(String.format("%02x", b));
		}
		if(Game.debug) System.out.println("mac as hex: " + macString);
		return macString.toString();
	}
	
	protected void writeClientSave(String playerdata) {
		String filename = ""; // this will hold the path to the file that will be saved to.
		
		File rpFile = getRemotePlayerFile();
		if(rpFile != null && rpFile.exists()) // check if this remote player already has a file.
			filename = rpFile.getName();
		else {
			File[] clientSaves = serverInstance.getRemotePlayerFiles();
			int numFiles = clientSaves.length;
			filename = "RemotePlayer"+numFiles+Save.extension;
		}
		
		byte[] macAddress = null;
		try {
			macAddress = computer.getHardwareAddress();
		} catch(NullPointerException ex) {
			System.err.println("network interface for "+this+" is null: "+computer+"; couldn't get mac address.");
		} catch(SocketException ex) {
			System.err.println("couldn't get mac address.");
			ex.printStackTrace();
		}
		if(macAddress == null) {
			System.err.println("SERVER: error saving player file; couldn't get client MAC address.");
			return;
		}
		
		String filedata = getMacString(macAddress) + "\n" + playerdata;
		
		String filepath = serverInstance.getWorldPath()+"/"+filename;
		//java.nio.file.Path theFile = (new File(filepath)).toPath();
		try {
			Save.writeToFile(filepath, filedata.split("\\n"), false);
			//Files.write(theFile, Arrays.asList(filedata.split("\\n")), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			//Files.setAttribute(theFile, "isRegularFile", (new Boolean(true)), (java.nio.file.LinkOption[])null);
		} catch(IOException ex) {
			System.err.println("problem writing remote player to file: " + filepath);
			ex.printStackTrace();
		}
		// the above will hopefully write the data to file.
	}
	
	public void endConnection() {
		for(Timer t: gameTimers)
			t.cancel();
		super.endConnection();
		
		client.remove();
		
		serverInstance.onThreadDisconnect(this);
	}
	
	public String toString() {
		return "ServerThread for " + client.getIpAddress().getCanonicalHostName();
	}
}
