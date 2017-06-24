package minicraft.entity;

import java.net.InetAddress;
import java.util.HashMap;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.Color;

/** This is used for players in multiplayer mode. */
public class RemotePlayer extends Player {
	
	public InetAddress ipAddress;
	public int port;
	
	public String username;
	
	/// this one here is solely for the server, as an easy way to keep track of the whether entity additions or removals have been confirmed by this client. The key is the eid, and the value is the timestamp.
	public HashMap<Integer, Long> unconfirmedAdditions = new HashMap<Integer, Long>();
	public HashMap<Integer, Long> unconfirmedRemovals = new HashMap<Integer, Long>();
	//public HashMap<String, Long> unconfirmedTiles = new HashMap<String, Long>();
	
	public RemotePlayer(Game game, String username, InetAddress ip, int port) { this(game, false, username, ip, port); }
	public RemotePlayer(Game game, boolean isMainPlayer, String username, InetAddress ip, int port) {
		super(game, (isMainPlayer?game.input:new InputHandler(game, false)));
		this.ipAddress = ip;
		this.port = port;
		this.username = username;
	}
	public RemotePlayer(Game game, boolean isMainPlayer, RemotePlayer model) {
		this(game, isMainPlayer, model.username, model.ipAddress, model.port);
		eid = model.eid;
	}
	
	public void render(Screen screen) {
		super.render(screen);
		Font.draw(username, screen, x - Font.textWidth(username)/2, y - 20, Color.get(-1, 444)); // draw the username of the player above their head
	}
	
	public String toString() {
		return "RemotePlayer "+username+" on "+ipAddress.getHostAddress()+":"+port;
	}
	
	/// this is simply to broaden the access permissions.
	public void attack() {
		super.attack();
	}
}
