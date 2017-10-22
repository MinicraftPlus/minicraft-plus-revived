package minicraft.core;

import javax.swing.Timer;
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

import org.jetbrains.annotations.Nullable;

public class Network extends Game {
	private Network() {}
	
	private static final Random random = new Random();
	
	static boolean autoclient = false; // used in the init method; jumps to multiplayer menu as client
	
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
			setMenu(new LoadingDisplay()); // gets things going to load up a world
		
		Timer t = new Timer(1000, e -> {
			// now that that's done, let's turn *this* running JVM into a server:
			server = new MinicraftServer();
			
			/// load up any saved config options for the server.
			new Load(WorldSelectDisplay.getWorldName(), server);
		});
		t.setRepeats(false);
		t.start();
	}
}
