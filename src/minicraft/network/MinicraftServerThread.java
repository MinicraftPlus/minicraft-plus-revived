package minicraft.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.RemotePlayer;
import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.saveload.Version;

public class MinicraftServerThread extends MinicraftConnection {
	
	class MyTask extends TimerTask {
		public MyTask() {}
		public void run() {}
	}
	
	//private static final int PING_INTERVAL = 5_000; // measured in milliseconds
	
	private MinicraftServer serverInstance;
	private RemotePlayer client;
	
	//protected boolean isPlaying = false;
	
	
	//private NetworkInterface computer = null;
	
	//private List<Integer> trackedEntities = new ArrayList<Integer>();
	private List<Timer> gameTimers = new ArrayList<>();
	private boolean receivedPing = true;
	
	private long manualPingTimestamp;
	
	private List<InputType> packetTypesToKeep = new ArrayList<>();
	private List<InputType> packetTypesToCache = new ArrayList<>();
	private List<String> cachedPackets = new ArrayList<>();
	
	MinicraftServerThread(Socket socket, MinicraftServer serverInstance) {
		super("MinicraftServerThread", socket);
		
		this.serverInstance = serverInstance;
		if(serverInstance.isFull()) {
			sendError("server at max capacity.");
			super.endConnection();
			return;
		}
		
		client = new RemotePlayer(null, false, socket.getInetAddress(), socket.getPort());
		
		// username is set later
		
		packetTypesToKeep.addAll(InputType.tileUpdates);
		packetTypesToKeep.addAll(InputType.entityUpdates);
		
		Timer t = new Timer("ClientPing");
		t.schedule((new MyTask() {
			public void run() { MinicraftServerThread.this.ping(); }
		}), 1000, PING_INTERVAL);
		gameTimers.add(t);
		
		start();
	}
	
	// this is to be a dummy thread.
	MinicraftServerThread(RemotePlayer player, MinicraftServer server) {
		super("MinicraftServerThread", null);
		this.client = player;
		this.serverInstance = server;
	}
	
	public RemotePlayer getClient() { return client; }
	
	protected boolean parsePacket(InputType inType, String data) {
		//if(inType == InputType.LOAD) isPlaying = true;
		
		if(inType == InputType.PING) {
			//if (Game.debug) System.out.println(this+" received ping");
			receivedPing = true;
			if(data.equals(manualPing)) {
				long nsPingDelay = System.nanoTime() - manualPingTimestamp;
				double pingDelay = Math.round(nsPingDelay*1.0 / 1E6)*1.0 / 1E3;
				System.out.println("received ping from " + client.getUsername() + "; delay = " + pingDelay + " seconds.");
			}
			
			return true;
		}
		
		return serverInstance.parsePacket(this, inType, data);
	}
	
	private void ping() {
		//if (Game.debug) System.out.println(this+" is doing ping sequence. received ping: " + receivedPing);
		
		if(!receivedPing) {
			// disconnect from the client; they are taking too long to respond and probably don't exist anyway.
			sendError("connection timed out; ping too slow");
			endConnection();
		} else {
			receivedPing = false;
			sendData(InputType.PING, autoPing);
		}
	}
	
	void doPing() {
		sendData(InputType.PING, manualPing);
		manualPingTimestamp = System.nanoTime();
	}
	
	void sendError(String message) {
		if (Game.debug) System.out.println("SERVER: sending error to " + client + ": \"" + message + "\"");
		sendData(InputType.INVALID, message);
	}
	
	void cachePacketTypes(List<InputType> packetTypes) {
		packetTypesToCache.addAll(packetTypes);
		packetTypesToKeep.removeAll(packetTypes);
	}
	
	void sendCachedPackets() {
		packetTypesToCache.clear();
		
		for(String packet: cachedPackets) {
			InputType inType = InputType.values[Integer.parseInt(packet.substring(0, packet.indexOf(":")))];
			packet = packet.substring(packet.indexOf(":")+1);
			sendData(inType, packet);
		}
		
		cachedPackets.clear();
	}
	
	protected void sendData(InputType inType, String data) {
		if(packetTypesToCache.contains(inType))
			cachedPackets.add(inType.ordinal()+":"+data);
		else if(!packetTypesToKeep.contains(inType))
			super.sendData(inType, data);
	}
	
	public void sendTileUpdate(Level level, int x, int y) {
		sendTileUpdate(level.depth, x, y);
	}
	public void sendTileUpdate(int depth, int x, int y) {
		String data = Tile.getData(depth, x, y);
		if(data.length() > 0)
			sendData(InputType.TILE, data);
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
		if(edata.length() == 0)
			System.out.println("entity not worth adding to client level: " + e + "; not sending to " + client);
		else
			sendData(InputType.ADD, edata);
	}
	
	public void sendEntityRemoval(int eid, int levelDepth) {
		sendData(InputType.REMOVE, String.valueOf(eid)+";"+String.valueOf(levelDepth));
	}
	public void sendEntityRemoval(int eid) { // remove regardless of current level
		sendData(InputType.REMOVE, String.valueOf(eid));
	}
	
	public void sendNotification(String note, int notetime) {
		sendData(InputType.NOTIFY, notetime+";"+note);
	}
	
	public void sendPlayerHurt(int eid, int damage, Direction attackDir) {
		sendData(InputType.HURT, eid+";"+damage+";"+attackDir.ordinal());
	}
	
	public void sendStaminaChange(int amt) {
		sendData(InputType.STAMINA, amt+"");
	}
	
	public void updatePlayerActiveItem(Item heldItem) {
		/*if(client.activeItem == null && heldItem == null || client.activeItem != null && client.activeItem.equals
				(heldItem)) {
			System.out.println("SERVER THREAD: player active item is already the one specified: " + heldItem + "; not updating.");
			return;
		}*/
		
		if(client.activeItem != null && !(client.activeItem instanceof PowerGloveItem))
			sendData(InputType.CHESTOUT, client.activeItem.getData());
		client.activeItem = heldItem;
		
		sendData(InputType.INTERACT, ( client.activeItem == null ? "null" : client.activeItem.getData() ));
	}
	
	public void sendItems(String itemData) {
		sendData(InputType.ADDITEMS, itemData);
	}
	
	public void setClientPos(int lvlDepth, int x, int y) {
		sendData(InputType.MOVE, lvlDepth+";"+x+";"+y);
	}
	
	protected void respawnPlayer() {
		client.remove(); // hopefully removes it from any level it might still be on
		client = new RemotePlayer(false, client);
		client.respawn(World.levels[World.lvlIdx(0)]); // get the spawn loc. of the client
		sendData(InputType.PLAYER, client.getPlayerData()); // send spawn loc.
	}
	
	private File getRemotePlayerFile() {
		File[] clientFiles = serverInstance.getRemotePlayerFiles();
		
		for(File file: clientFiles) {
			String username = "";
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				try {
					username = br.readLine().trim();
				} catch(IOException ex) {
					System.err.println("failed to read line from file.");
					ex.printStackTrace();
				}
			} catch(FileNotFoundException ex) {
				System.err.println("couldn't find remote player file: " + file);
				ex.printStackTrace();
			}
			
			if(username.equals(client.getUsername())) {
				/// this player has been here before.
				if (Game.debug) System.out.println("remote player file found; returning file " + file.getName());
				return file;
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
				playerdata = content.substring(content.indexOf("\n")+1); // cut off username
				// assume the data version is dev6 if it isn't written (it isn't before dev7).
				if(!Version.isValid(playerdata.substring(0, playerdata.indexOf("\n"))))
					playerdata = "2.0.4-dev6\n"+playerdata;
			} catch(IOException ex) {
				System.err.println("failed to read remote player file: " + rpFile);
				ex.printStackTrace();
				return "";
			}
		}
		
		return playerdata;
	}
	
	protected void writeClientSave(String playerdata) {
		String filename; // this will hold the path to the file that will be saved to.
		
		File rpFile = getRemotePlayerFile();
		if(rpFile != null && rpFile.exists()) // check if this remote player already has a file.
			filename = rpFile.getName();
		else {
			File[] clientSaves = serverInstance.getRemotePlayerFiles();
			int numFiles = clientSaves.length;
			filename = "RemotePlayer"+numFiles+Save.extension;
		}
		
		String filedata = String.join("\n", client.getUsername(), Game.VERSION, playerdata);
		
		String filepath = serverInstance.getWorldPath()+"/"+filename;
		try {
			Save.writeToFile(filepath, filedata.split("\\n"), false);
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
		return "ServerThread for " + (client==null?"null":client.getUsername());
	}
}
