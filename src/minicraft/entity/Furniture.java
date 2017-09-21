package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.FurnitureItem;
import minicraft.item.PowerGloveItem;
import minicraft.screen.ModeMenu;

/** Many furniture classes are very similar; they might not even need to be there at all... */

public class Furniture extends Entity {
	
	protected int pushTime = 0, multiPushTime = 0; // time for each push; multi is for multiplayer, to make it so not so many updates are sent.
	protected int pushDir = -1; // the direction to push the furniture
	public Sprite sprite;
	public String name;
	
	public Furniture(String name, Sprite sprite) { this(name, sprite, 3, 3); }
	public Furniture(String name, Sprite sprite, int xr, int yr) {
		// all of these are 2x2 on the spritesheet; radius is for collisions only.
		super(xr, yr);
		this.name = name;
		this.sprite = sprite;
		col = sprite.color;
	}
	
	public Furniture clone() {
		try {
			return getClass().newInstance();//new Furniture(name, color, sprite, xr, yr);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void tick() {
		// moves the furniture in the correct direction.
		if (pushDir == 0) move(0, +1);
		if (pushDir == 1) move(0, -1);
		if (pushDir == 2) move(-1, 0);
		if (pushDir == 3) move(+1, 0);
		pushDir = -1; // makes pushDir -1 so it won't repeat itself.
		if (pushTime > 0) pushTime--; // update pushTime by subtracting 1.
		else multiPushTime = 0;
	}
	
	/** Draws the furniture on the screen. */
	public void render(Screen screen) {
		sprite.render(screen, x-8, y-8);
	}
	
	/** Determines if this entity can block others */
	public boolean blocks(Entity e) {
		return true; // yes this can block your way (Needed for pushing)
	}
	
	/** What happens when this is touched by another entity */
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player)
			tryPush((Player) entity);
	}
	
	public void tryPush(Player player) {
		if (pushTime == 0) {
			pushDir = player.dir; // set pushDir to the player's dir.
			pushTime = multiPushTime = 10; // set pushTime to 10.
			
			if(Game.isConnectedClient())
				Game.client.pushFurniture(this, pushDir);
		}
	}
	
	/** Used in PowerGloveItem.java */
	public void take(Player player) {
		remove(); // remove this from the world
		if(!Game.ISONLINE) {
			if (!ModeMenu.creative && player.activeItem != null && !(player.activeItem instanceof PowerGloveItem))
				player.inventory.add(0, player.activeItem); // put whatever item the player is holding into their inventory (should never be a power glove, since it is put in a taken out again all in the same frame).
			player.activeItem = new FurnitureItem(this); // make this the player's current item.
		}
		else if(Game.isValidServer() && player instanceof RemotePlayer)
			Game.server.getAssociatedThread((RemotePlayer)player).updatePlayerActiveItem(new FurnitureItem(this));
		else
			System.out.println("WARNING: undefined behavior; online game was not server and ticked furniture: "+this+"; and/or player in online game found that isn't a RemotePlayer: " + player);
		
		//if (Game.debug) System.out.println("set active item of player " + player + " to " + player.activeItem + "; picked up furniture: " + this);
	}
	
	public boolean canWool() {
		return true;
	}
	
	protected String getUpdateString() {
		return super.getUpdateString()+
		//";pushDir,"+pushDir+
		";pushTime,"+multiPushTime;
	}
	
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		
		switch(field) {
			//case "pushDir": pushDir = Integer.parseInt(val); return true;
			case "pushTime": pushTime = Integer.parseInt(val); return true;
		}
		
		return false;
	}
}
