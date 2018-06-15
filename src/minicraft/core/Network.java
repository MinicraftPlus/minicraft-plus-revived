package minicraft.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import minicraft.entity.Entity;
import minicraft.level.Level;
import minicraft.network.MinicraftServer;
import minicraft.saveload.Load;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.WorldSelectDisplay;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.jetbrains.annotations.Nullable;

public class Network extends Game {
	private Network() {}
	
	private static final Random random = new Random();
	
	static boolean autoclient = false; // used in the initScreen method; jumps to multiplayer menu as client
	
	private static VersionInfo latestVersion = null;
	
	// obviously, this can be null.
	public static VersionInfo getLatestVersion() { return latestVersion; }
	
	
	public static void findLatestVersion(Action callback) {
		new Thread(() -> {
			// fetch the latest version from github
			if(debug) System.out.println("fetching release list from github...");
			try {
				HttpResponse<JsonNode> response = Unirest.get("https://api.github.com/repos/chrisj42/minicraft-plus-revived/releases").asJson();
				if(response.getStatus() != 200) {
					System.err.println("version request returned status code "+response.getStatus()+": "+response.getStatusText());
					System.err.println("response body: "+response.getBody());
					latestVersion = new VersionInfo(VERSION, "", "");
				}
				else {
					latestVersion = new VersionInfo(response.getBody().getArray().getJSONObject(0));
				}
			} catch(UnirestException e) {
				e.printStackTrace();
				latestVersion = new VersionInfo(VERSION, "", "");
			}
			
			callback.act(); // finished.
		}).start();
	}
	
	@Nullable
	public static Entity getEntity(int eid) {
		for(Level level: levels) {
			if(level == null) continue;
			for(Entity e: level.getEntityArray())
				if(e.eid == eid)
					return e;
		}
		
		return null;
	}
	
	public static int generateUniqueEntityId() {
		int eid;
		int tries = 0; // just in case it gets out of hand.
		do {
			tries++;
			if(tries == 1000)
				System.out.println("note: trying 1000th time to find valid entity id...(will continue)");
			
			eid = random.nextInt();
		} while(!idIsAvaliable(eid));
		
		return eid;
	}
	
	public static boolean idIsAvaliable(int eid) {
		if(eid == 0) return false; // this is reserved for the main player... kind of...
		if(eid < 0) return false; // id's must be positive numbers.
		
		for(Level level: levels) {
			if(level == null) continue;
			for(Entity e: level.getEntityArray()) {
				if(e.eid == eid)
					return false;
			}
		}
		
		return true;
	}
	
	public static String onlinePrefix() {
		if(!ISONLINE) return "";
		String prefix = "From ";
		if(isValidServer())
			prefix += "Server";
		else if(isValidClient())
			prefix += "Client";
		else
			prefix += "nobody";
		
		prefix += ": ";
		return prefix;
	}
	
	public static void startMultiplayerServer() {
		if(debug) System.out.println("starting multiplayer server...");
		
		if(HAS_GUI) {
			// here is where we need to start the new client.
			String jarFilePath = "";
			try {
				java.net.URI uri = Game.class.getProtectionDomain().getCodeSource().getLocation().toURI();
				//if (debug) System.out.println("jar path: " + uri.getPath());
				//if (debug) System.out.println("jar string: " + uri.toString());
				jarFilePath = uri.getPath();
				if(FileHandler.OS.contains("windows") && jarFilePath.startsWith("/"))
					jarFilePath = jarFilePath.substring(1);
			} catch(URISyntaxException ex) {
				System.err.println("problem with jar file URI syntax.");
				ex.printStackTrace();
			}
			List<String> arguments = new ArrayList<>();
			arguments.add("java");
			arguments.add("-jar");
			arguments.add(jarFilePath);
			
			if(debug)
				arguments.add("--debug");
			
			// this will just always be added.
			arguments.add("--savedir");
			arguments.add(FileHandler.systemGameDir);
			
			arguments.add("--localclient");
			
			/// this *should* start a new JVM from the running jar file...
			try {
				new ProcessBuilder(arguments).inheritIO().start();
			} catch(IOException ex) {
				System.err.println("problem starting new jar file process:");
				ex.printStackTrace();
			}
		}
		else
			setMenu(new LoadingDisplay()); // gets things going to load up a (server) world
		
		// now that that's done, let's turn *this* running JVM into a server:
		server = new MinicraftServer();
		
		new Load(WorldSelectDisplay.getWorldName(), server); // load server config
		
		if(latestVersion == null) {
			System.out.println("VERSIONCHECK: Checking for updates...");
			findLatestVersion(() -> {
				if(latestVersion.version.compareTo(Game.VERSION) > 0) // link new version
					System.out.println("VERSIONCHECK: Found newer version: Version " + latestVersion.releaseName + " Available! Download direct from \""+latestVersion.releaseUrl +"\". Can also be found with change log at \"https://www.github.com/chrisj42/minicraft-plus-revived/releases\".");
				else if(latestVersion.releaseName.length() > 0)
					System.out.println("VERSIONCHECK: No updates found, you have the latest version.");
				else
					System.out.println("VERSIONCHECK: Connection failed, could not check for updates.");
			});
		}
		/*Timer t = new Timer(1000, e -> {
			// meanwhile... the loading screen is about to initialize the world, if this was started from the command line.
			
			/// load up any saved config options for the server.
			new Load(WorldSelectDisplay.getWorldName(), server);
		});
		t.setRepeats(false);
		t.start();*/
	}
	
	
}
