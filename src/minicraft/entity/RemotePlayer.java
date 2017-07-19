package minicraft.entity;

import java.net.InetAddress;
import java.util.List;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Screen;

/** This is used for players in multiplayer mode. */
public class RemotePlayer extends Player {
	
	/// these are used by the server to determine the distance limit for an entity/tile to be updated/added for a given player.
	private static final int xSyncRadius = 12;
	private static final int ySyncRadius = 10;
	private static final int entityTrackingBuffer = 0;
	
	private String username = "";
	private InetAddress ipAddress;
	private int port;
	
	public RemotePlayer(Player previous, Game game, InetAddress ip, int port) { this(previous, game, false, ip, port); }
	public RemotePlayer(Player previous, Game game, boolean isMainPlayer, InetAddress ip, int port) {
		super(previous, game, (isMainPlayer?game.input:new InputHandler(game, false)));
		this.ipAddress = ip;
		this.port = port;
	}
	public RemotePlayer(Game game, boolean isMainPlayer, RemotePlayer model) {
		this(model, game, isMainPlayer, model.ipAddress, model.port);
		eid = model.eid;
		setUsername(username);
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUsername() { return username; }
	public InetAddress getIpAddress() { return ipAddress; }
	
	public String getData() {
		return username + ":" + ipAddress.getCanonicalHostName() + ":" + port;
	}
	
	public void tick() {
		if(!Game.isValidServer() && this == game.player)
			super.tick();
		else {
			// a minimal thing for render update purposes.
			if (attackTime > 0) {
				attackTime--;
				if(attackTime == 0) attackItem = null; // null the attackItem once we are done attacking.
			}
		}
	}
	
	/// this is simply to broaden the access permissions.
	public void attack() {
		super.attack();
	}
	
	public boolean move(int xa, int ya) {
		int oldxt = x >> 4, oldyt = y >> 4;
		
		boolean moved = super.move(xa, ya);
		
		if(!(oldxt == x>>4 && oldyt == y>>4) && Game.isValidClient() && this == game.player) {
			// if moved (and is client), then check any tiles no longer loaded, and remove any entities on them.
			updateSyncArea(oldxt, oldyt);
		}
		
		return moved;
	}
	
	public void render(Screen screen) {
		super.render(screen);
		new FontStyle(Color.get(-1, 444)).setShadowType(Color.get(-1, 0), true).setXPos(x - Font.textWidth(username)/2).setYPos(y - 20).draw(username, screen); // draw the username of the player above their head
	}
	
	/// this determines if something at a given coordinate should be synced to this client, or if it is too far away to matter.
	public boolean shouldSync(int xt, int yt) {
		return shouldSync(xt, yt, 0);
	}
	public boolean shouldTrack(int xt, int yt) {
		return shouldSync(xt, yt, entityTrackingBuffer); /// this means that there is one tile past the syncRadii in all directions, which marks the distance at which entities are added or removed.
	}
	private boolean shouldSync(int xt, int yt, int offset) { // IDEA make this isWithin(). Decided not to b/c different x and y radii.
		int px = x >> 4, py = y >> 4;
		int xdist = Math.abs(xt - px);
		int ydist = Math.abs(yt - py);
		return xdist <= xSyncRadius+offset && ydist <= ySyncRadius+offset;
	}
	
	public String toString() {
		return super.toString()+"{"+username+" on "+ipAddress.getHostAddress()+":"+port+"}";
	}
	
	public void updateSyncArea(int oldxt, int oldyt) {
		if(level == null) {
			System.err.println("CLIENT couldn't check world around player b/c player has no level: " + this);
			return;
		}
		
		int xt = x >> 4;
		int yt = y >> 4;
		if(xt == oldxt && yt == oldyt) // no change is needed.
			return;
		
		boolean isServer = Game.isValidServer();
		boolean isClient = Game.isValidClient();
		
		int xr = xSyncRadius + entityTrackingBuffer;
		int yr = ySyncRadius + entityTrackingBuffer;
		
		int xt0, yt0, xt1, yt1;
		if(isServer) {
			xt0 = oldxt;
			yt0 = oldyt;
			xt1 = xt;
			yt1 = yt;
		}
		else if(isClient) {
			xt0 = xt;
			yt0 = yt;
			xt1 = oldxt;
			yt1 = oldyt;
		} else {
			System.err.println("ERROR: RemotePlayer sync method called when game is not client or server.");
			return;
		}
		
		/// the Math.mins and maxes make it so it doesn't try to update tiles outside of the level bounds.
		int xmin = Math.max(xt1 - xr, 0);
		int xmax = Math.min(xt1 + xr, level.w-1);
		int ymin = Math.max(yt1 - yr, 0);
		int ymax = Math.min(yt1 + yr, level.h-1);
		
		List<Entity> loadableEntites = level.getEntitiesInTiles(xmin, ymin, xmax, ymax);
		//loadableEntites.remove(this);
		
		for(int y = ymin; y <= ymax; y++) {
			for(int x = xmin; x <= xmax; x++) {
				/// server loops through current tiles, and filters out old tiles, so only new tiles are left.
				/// client loops through old tiles, and filters out current tiles, so only old tiles are left.
				if(xt0 < 0 || yt0 < 0 || x > xt0+xr || x < xt0-xr || y > yt0+yr || y < yt0-yr) {
					
					/// SERVER NOTE: don't worry about removing entities that go to unloaded tiles; the client will do that. Now, as for mobs (or players) that wander out of or into a player's loaded tiles without the player moving, the Mob class deals with that.
					
					for(Entity e: loadableEntites) {
						if(e != this && e.x >> 4 == x && e.y >> 4 == y) {
							if(isServer) /// send any entities on that new tile to be added.
								Game.server.getAssociatedThread(this).sendEntityAddition(e);
							if(isClient) /// remove each entity on that tile.
								e.remove();
						}
					}
				} // end range checker conditional
			}
		} // end tile iteration loops
	}
}
