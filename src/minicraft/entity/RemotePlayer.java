package minicraft.entity;

import java.net.InetAddress;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.Color;

/** This is used only by the Server runtime, to represent a client player. */
public class RemotePlayer extends Player {
	
	public InetAddress ipAddress;
	public int port;
	
	public String username;
	
	public RemotePlayer(Game game, String username, InetAddress ip, int port) { this(game, false, username, ip, port); }
	public RemotePlayer(Game game, boolean useInput, String username, InetAddress ip, int port) {
		super(game, new InputHandler(game, useInput));
		this.ipAddress = ip;
		this.port = port;
		this.username = username;
	}
	public RemotePlayer(Game game, boolean useInput, RemotePlayer model) {
		this(game, useInput, model.username, model.ipAddress, model.port);
		eid = model.eid;
	}
	
	public void render(Screen screen) {
		super.render(screen);
		Font.drawCentered(username, screen, y - 10, Color.get(-1, 444)); // draw the username of the player above their head
	}
	
	public String getClientName() {
		return ipAddress.getHostAddress();
	}
	
	public String toString() {
		return "Player "+username+" on "+ipAddress.getHostAddress()+":"+port;
	}
	
	/// this is simply to broaden the access permissions.
	public void attack() {
		super.attack();
	}
}
