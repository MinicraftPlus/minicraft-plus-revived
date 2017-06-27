package minicraft.entity;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.gfx.Screen;
import minicraft.gfx.Font;
import minicraft.gfx.FontStyle;
import minicraft.gfx.Color;

/** This is used for players in multiplayer mode. */
public class RemotePlayer extends Player {
	
	/// these are used by the server to determine the distance limit for an entity/tile to be updated/added for a given player.
	private static final int xSyncRadius = 10;
	private static final int ySyncRadius = 8;
	
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
	
	public void tick() {
		if(!Game.isValidServer() && this == game.player)
			super.tick();
		else {
			// a minimal thing for render update purposes.
			if (attackTime > 0) attackTime--;
		}
	}
	
	/// this is simply to broaden the access permissions.
	public void attack() {
		super.attack();
	}
	
	public boolean move(int xa, int ya) {
		int oldxt = x >> 4, oldyt = y >> 4;
		
		boolean moved = super.move(xa, ya);
		
		if(!(oldxt == x>>4 && oldyt == y>>4)) {
			// if moved (and is client), then check any tiles no longer loaded, and remove any entities on them.
			if(Game.isValidClient() && this == game.player)
				updateSyncArea(oldxt, oldyt);
			
			if(Game.isValidServer()) {
				List<RemotePlayer> prevPlayers = Game.server.getPlayersInRange(level, oldxt, oldyt, true);
				List<RemotePlayer> activePlayers = Game.server.getPlayersInRange(this, true);
				for(int i = 0; i < Math.min(prevPlayers.size(), activePlayers.size()); i++) {
					if(activePlayers.contains(prevPlayers.get(i))) {
						activePlayers.remove(i);
						prevPlayers.remove(i);
						i--;
					}
				}
				// the lists should now only contain players that now out of range, and players that are just now in range.
				for(RemotePlayer rp: prevPlayers)
					Game.server.sendEntityRemoval(this.eid, rp);
				for(RemotePlayer rp: activePlayers)
					Game.server.sendEntityAddition(this, rp);
				
			}
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
		return shouldSync(xt, yt, 1); /// this means that there is one tile past the syncRadii in all directions, which marks the distance at which entities are added or removed.
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
		
		int xr = xSyncRadius + 1;
		int yr = ySyncRadius + 1;
		
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
				/// server loops through new tiles, and compares with old tiles below (server: x,y is new coord)
				/// client loops through old tiles, and compares with new tiles below (client: x,y is old coord)
				if(xt0 < 0 || yt0 < 0 || x > xt0+xr || x < xt0-xr || y > yt0+yr || y < yt0-yr) {
					if(isServer)
						Game.server.sendTileUpdate(x, y, this); // update the new tile
					
					/// SERVER NOTE: don't worry about removing entities that go to unloaded tiles; the client will do that. Now, as for mobs that wander out of or into a player's loaded tiles without the player moving, the MobAi class deals with that.
					
					for(Entity e: loadableEntites) {
						if(e != this && e.x >> 4 == x && e.y >> 4 == y) {
							if(isServer) /// send any entities on that new tile to be added.
								Game.server.sendEntityAddition(e, this);
							if(isClient) /// remove each entity on that tile.
								e.remove();
						}
					}
				} // end range checker conditional
			}
		} // end tile iteration loops
	}
}
