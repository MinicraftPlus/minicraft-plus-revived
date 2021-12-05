package minicraft.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import minicraft.entity.Entity;
import minicraft.level.Level;
import minicraft.network.Analytics;
import minicraft.network.MinicraftServer;
import minicraft.saveload.Load;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.WorldSelectDisplay;
import org.tinylog.Logger;

public class Network extends Game {
	private Network() {}
	
	private static final Random random = new Random();
	
	static boolean autoclient = false; // Used in the initScreen method; jumps to multiplayer menu as client
	
	private static VersionInfo latestVersion = null;
	
	// Obviously, this can be null.
	public static VersionInfo getLatestVersion() { return latestVersion; }
	
	
	public static void findLatestVersion(Action callback) {
		new Thread(() -> {
			Logger.debug("Fetching release list from GitHub..."); // Fetch the latest version from GitHub
			try {
				HttpResponse<JsonNode> response = Unirest.get("https://api.github.com/repos/chrisj42/minicraft-plus-revived/releases").asJson();
				if (response.getStatus() != 200) {
					Logger.error("Version request returned status code " + response.getStatus() + ": " + response.getStatusText());
					Logger.error("Response body: " + response.getBody());
					latestVersion = new VersionInfo(VERSION, "", "");
				} else {
					latestVersion = new VersionInfo(response.getBody().getArray().getJSONObject(0));
				}
			} catch (UnirestException e) {
				e.printStackTrace();
				latestVersion = new VersionInfo(VERSION, "", "");
			}
			
			callback.act(); // finished.
		}).start();
	}
	
	@Nullable
	public static Entity getEntity(int eid) {
		for (Level level: levels) {
			if (level == null) continue;
			for (Entity e: level.getEntityArray())
				if (e.eid == eid)
					return e;
		}
		
		return null;
	}
	
	public static int generateUniqueEntityId() {
		int eid;
		int tries = 0; // Just in case it gets out of hand.
		do {
			tries++;
			if (tries == 1000)
				System.out.println("Note: Trying 1000th time to find valid entity id...(Will continue)");
			
			eid = random.nextInt();
		} while (!idIsAvailable(eid));
		
		return eid;
	}
	
	public static boolean idIsAvailable(int eid) {
		if (eid == 0) return false; // This is reserved for the main player... kind of...
		if (eid < 0) return false; // ID's must be positive numbers.
		
		for (Level level: levels) {
			if (level == null) continue;
			for (Entity e: level.getEntityArray()) {
				if (e.eid == eid)
					return false;
			}
		}
		
		return true;
	}
	
	public static String onlinePrefix() {
		if (!ISONLINE) return "";
		String prefix = "From ";
		if (isValidServer())
			prefix += "Server";
		else if (isValidClient())
			prefix += "Client";
		else
			prefix += "nobody";
		
		prefix += ": ";
		return prefix;
	}
	

	@SuppressWarnings("unused")
	public static void startMultiplayerServer() {
		System.out.println("Error: Multiplayer mode is not enabled in this version. It has been temporarily discontinued due to lack of stability.");
		if(true) return;
		
		if (debug) System.out.println("Starting multiplayer server...");
		
		if (HAS_GUI) {
			// Here is where we need to start the new client.
			String jarFilePath = "";
			try {
				java.net.URI uri = Game.class.getProtectionDomain().getCodeSource().getLocation().toURI();
				jarFilePath = uri.getPath();
				if (FileHandler.OS.contains("windows") && jarFilePath.startsWith("/"))
					jarFilePath = jarFilePath.substring(1);
			} catch (URISyntaxException ex) {
				System.err.println("Problem with jar file URI syntax.");
				ex.printStackTrace();
			}
			List<String> arguments = new ArrayList<>();
			arguments.add("java");
			arguments.add("-jar");
			arguments.add(jarFilePath);
			
			if (debug)
				arguments.add("--debug");
			
			// This will just always be added.
			arguments.add("--savedir");
			arguments.add(FileHandler.systemGameDir);
			
			arguments.add("--localclient");
			
			// This *should* start a new JVM from the running jar file...
			try {
				new ProcessBuilder(arguments).inheritIO().start();
			} catch (IOException ex) {
				System.err.println("Problem starting new jar file process:");
				ex.printStackTrace();
			}
			
			// Ping that we're technically "starting" a multiplayer session
			Analytics.MultiplayerGame.ping();
		}
		else
			setMenu(new LoadingDisplay()); // Gets things going to load up a (server) world
		
		// Now that that's done, let's turn *this* running JVM into a server:
		server = new MinicraftServer(Game.CUSTOM_PORT);
		
		new Load(WorldSelectDisplay.getWorldName(), server); // Load server config
		
		if (latestVersion == null) {
			System.out.println("VERSIONCHECK: Checking for updates...");
			findLatestVersion(() -> {
				if (latestVersion.version.compareTo(Game.VERSION) > 0) // Link new version
					System.out.println("VERSIONCHECK: Found newer version: Version " + latestVersion.releaseName + " Available! Download direct from \""+latestVersion.releaseUrl +"\". Can also be found with change log at \"https://www.github.com/chrisj42/minicraft-plus-revived/releases\".");
				else if (latestVersion.releaseName.length() > 0)
					System.out.println("VERSIONCHECK: No updates found, you have the latest version.");
				else
					System.out.println("VERSIONCHECK: Connection failed, could not check for updates.");
			});
		}
	}
	
	
}
