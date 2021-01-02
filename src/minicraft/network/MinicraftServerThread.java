package minicraft.network;

import javax.swing.Timer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import minicraft.core.Game;
import minicraft.core.World;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.saveload.Load;
import minicraft.saveload.Save;
import minicraft.saveload.Version;

public class MinicraftServerThread extends MinicraftConnection {
	
	private static final String autoPing = "ping";
	private static final String manualPing = "manual";
	
	private static final int MISSED_PING_THRESHOLD = 5;
	private static final int PING_INTERVAL = 1_000; // measured in milliseconds
	
	
	private MinicraftServer serverInstance;
	private RemotePlayer client;
	
	/// PING
	
	private Timer pingTimer;
	private boolean receivedPing = true; // after first pause, it will act as if the ping was successful, since it didn't even send one in the first place and was just buying time for everything to get settled before pinging.
	private int missedPings = 0;
	
	private long manualPingTimestamp;
	
	
	private List<InputType> packetTypesToKeep = new ArrayList<>();
	private List<InputType> packetTypesToCache = new ArrayList<>();
	private List<String> cachedPackets = new ArrayList<>();
	
	private final boolean valid;
	
	MinicraftServerThread(Socket socket, MinicraftServer serverInstance) {
		super("MinicraftServerThread", socket);
		valid = true;
		
		this.serverInstance = serverInstance;
		if (serverInstance.isFull()) {
			sendError("Server at max capacity.");
			super.endConnection();
			return;
		}
		
		client = new RemotePlayer(null, false, socket.getInetAddress(), socket.getPort());
		
		// username is set later
		
		packetTypesToKeep.addAll(InputType.tileUpdates);
		packetTypesToKeep.addAll(InputType.entityUpdates);
		
		pingTimer = new Timer(PING_INTERVAL, e -> {
			if (!isConnected()) {
				pingTimer.stop();
				return;
			}

			if (!receivedPing) {
				missedPings++;
				if (missedPings >= MISSED_PING_THRESHOLD) {
					// disconnect from the client; they are taking too long to respond and probably don't exist at this point.
					pingTimer.stop();
					sendError("Client ping too slow, server timed out");
					endConnection();
				}
			} else {
				missedPings = 0;
				receivedPing = false;
			}
			
			sendData(InputType.PING, autoPing);
		});
		pingTimer.setRepeats(true);
		pingTimer.setCoalesce(true); // don't try to make up for lost pings.
		pingTimer.start();
		
		start();
	}
	
	// this is to be a dummy thread.
	MinicraftServerThread(RemotePlayer player, MinicraftServer server) {
		super("MinicraftServerThread", null);
		valid = false;
		this.client = player;
		this.serverInstance = server;
	}
	
	public boolean isValid() { return valid; }
	
	public RemotePlayer getClient() { return client; }
	
	protected boolean parsePacket(InputType inType, String data) {
		if (inType == InputType.PING) {
			receivedPing = true;
			if (data.equals(manualPing)) {
				long nsPingDelay = System.nanoTime() - manualPingTimestamp;
				double pingDelay = Math.round(nsPingDelay*1.0 / 1E6)*1.0 / 1E3;
				System.out.println("Received ping from " + client.getUsername() + "; delay = " + pingDelay + " seconds.");
			}
			
			return true;
		}
		
		return serverInstance.parsePacket(this, inType, data);
	}
	
	void doPing() {
		sendData(InputType.PING, manualPing);
		manualPingTimestamp = System.nanoTime();
	}
	
	void sendError(String message) {
		if (Game.debug) System.out.println("SERVER: Sending error to " + client + ": \"" + message + "\"");
		sendData(InputType.INVALID, message);
	}
	
	void cachePacketTypes(List<InputType> packetTypes) {
		packetTypesToCache.addAll(packetTypes);
		packetTypesToKeep.removeAll(packetTypes);
	}
	
	void sendCachedPackets() {
		packetTypesToCache.clear();
		
		for (String packet: cachedPackets) {
			InputType inType = InputType.values[Integer.parseInt(packet.substring(0, packet.indexOf(":")))];
			packet = packet.substring(packet.indexOf(":")+1);
			sendData(inType, packet);
		}
		
		cachedPackets.clear();
	}
	
	protected void sendData(InputType inType, String data) {
		if (packetTypesToCache.contains(inType))
			cachedPackets.add(inType.ordinal()+ ":" +data);
		else if (!packetTypesToKeep.contains(inType))
			super.sendData(inType, data);
	}
	
	public void sendTileUpdate(Level level, int x, int y) {
		sendTileUpdate(level.depth, x, y);
	}
	public void sendTileUpdate(int depth, int x, int y) {
		String data = Tile.getData(depth, x, y);
		if (data.length() > 0)
			sendData(InputType.TILE, data);
	}
	
	public void sendEntityUpdate(Entity e, String updateString) {
		if (updateString.length() > 0) {
			sendData(InputType.ENTITY, e.eid+ ";" +updateString);
		}
	}
	
	public void sendEntityAddition(Entity e) {
		if (Game.debug && e instanceof Player) System.out.println("SERVER: Sending addition of player " + e + " to client through " + this);
		if (Game.debug && e.eid == client.eid) System.out.println("SERVER: Sending addition of player to itself");
		String edata = Save.writeEntity(e, false);
		if (edata.length() == 0)
			System.out.println("Entity not worth adding to client level: " + e + "; not sending to " + client);
		else
			sendData(InputType.ADD, edata);
	}
	
	public void sendEntityRemoval(int eid, int levelDepth) {
		sendData(InputType.REMOVE, eid + ";" + levelDepth);
	}
	public void sendEntityRemoval(int eid) { // remove regardless of current level
		sendData(InputType.REMOVE, String.valueOf(eid));
	}
	
	public void sendNotification(String note, int notetime) {
		sendData(InputType.NOTIFY, notetime+ ";" +note);
	}
	
	public void sendPlayerHurt(int eid, int damage, Direction attackDir) {
		sendData(InputType.HURT, eid+ ";" +damage+ ";" +attackDir.ordinal());
	}

	public void sendStopFishing(int eid) {
		sendData(InputType.STOPFISHING, "" + eid);
	}
	
	public void sendStaminaChange(int amt) {
		sendData(InputType.STAMINA, amt+ "");
	}
	
	public void updatePlayerActiveItem(Item heldItem) {
		if (client.activeItem != null && !(client.activeItem instanceof PowerGloveItem))
			sendData(InputType.CHESTOUT, client.activeItem.getData());
		client.activeItem = heldItem;
		
		sendData(InputType.INTERACT, ( client.activeItem == null ? "null" : client.activeItem.getData() ));
	}
	
	public void sendItems(String itemData) {
		sendData(InputType.ADDITEMS, itemData);
	}
	
	protected void respawnPlayer() {
		client.remove(); // hopefully removes it from any level it might still be on
		client = new RemotePlayer(false, client);
		client.respawn(World.levels[World.lvlIdx(0)]); // get the spawn loc. of the client
		sendData(InputType.PLAYER, client.getPlayerData()); // send spawn loc.
	}
	
	private File getRemotePlayerFile() {
		File[] clientFiles = serverInstance.getRemotePlayerFiles();
		
		for (File file: clientFiles) {
			String username = "";
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				try {
					username = br.readLine().trim();
				} catch(IOException ex) {
					System.err.println("Failed to read line from file.");
					ex.printStackTrace();
				}
			} catch(FileNotFoundException ex) {
				System.err.println("Couldn't find remote player file: " + file);
				ex.printStackTrace();
			}
			
			if (username.equals(client.getUsername())) {
				/// this player has been here before.
				if (Game.debug) System.out.println("Remote player file found; returning file " + file.getName());
				return file;
			}
		}
		
		return null;
	}
	
	protected String getRemotePlayerFileData() {
		File rpFile = getRemotePlayerFile();
		
		String playerdata = "";
		if (rpFile != null && rpFile.exists()) {
			try {
				String content = Load.loadFromFile(rpFile.getPath(), false);
				playerdata = content.substring(content.indexOf("\n")+1); // cut off username
				// assume the data version is dev6 if it isn't written (it isn't before dev7).
				if (!Version.isValid(playerdata.substring(0, playerdata.indexOf("\n"))))
					playerdata = "2.0.4-dev6\n" +playerdata;
			} catch(IOException ex) {
				System.err.println("Failed to read remote player file: " + rpFile);
				ex.printStackTrace();
				return "";
			}
		}
		
		return playerdata;
	}
	
	protected void writeClientSave(String playerdata) {
		String filename; // this will hold the path to the file that will be saved to.
		
		File rpFile = getRemotePlayerFile();
		if (rpFile != null && rpFile.exists()) // check if this remote player already has a file.
			filename = rpFile.getName();
		else {
			File[] clientSaves = serverInstance.getRemotePlayerFiles();
			int numFiles = clientSaves.length;
			filename = "RemotePlayer" + numFiles+Save.extension;
		}
		
		String filedata = String.join("\n", client.getUsername(), playerdata);
		
		String filepath = serverInstance.getWorldPath() + "/" +filename;
		try {
			Save.writeToFile(filepath, filedata.split("\\n"), false);
		} catch(IOException ex) {
			System.err.println("Problem writing remote player to file: " + filepath);
			ex.printStackTrace();
		}
		// the above will hopefully write the data to file.
	}
	
	public void endConnection() {
		pingTimer.stop();
		super.endConnection();
		
		client.remove();
		
		serverInstance.onThreadDisconnect(this);
	}
	
	public String toString() {
		return "ServerThread for " + (client==null?"null":client.getUsername());
	}
}
