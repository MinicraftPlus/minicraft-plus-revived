package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.FurnitureItem;
import minicraft.item.PowerGloveItem;

/** Many furniture classes are very similar; they might not even need to be there at all... */

public class Furniture extends Entity {
	
	protected int pushTime = 0, multiPushTime = 0; // time for each push; multi is for multiplayer, to make it so not so many updates are sent.
	private Direction pushDir = Direction.NONE; // the direction to push the furniture
	public Sprite sprite;
	public String name;
	
	/**
	 * Constructor for the furniture entity.
	 * Size will be set to 3.
	 * @param name Name of the furniture.
	 * @param sprite Furniture sprite.
	 */
	public Furniture(String name, Sprite sprite) { 
		this(name, sprite, 3, 3);
	}
	
	/**
	 * Constructor for the furniture entity.
	 * Radius is only used for collision detection.
	 * @param name Name of the furniture.
	 * @param sprite Furniture sprite.
	 * @param xr Horizontal radius.
	 * @param yr Vertical radius.
	 */
	public Furniture(String name, Sprite sprite, int xr, int yr) {
		// all of these are 2x2 on the spritesheet; radius is for collisions only.
		super(xr, yr);
		this.name = name;
		this.sprite = sprite;
		col = sprite.color;
	}
	
	@Override
	public Furniture clone() {
		try {
			return getClass().newInstance();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return new Furniture(name, sprite);
	}

	@Override
	public void tick() {
		// moves the furniture in the correct direction.
		move(pushDir.getX(), pushDir.getY());
		pushDir = Direction.NONE;
		
		if (pushTime > 0) pushTime--; // update pushTime by subtracting 1.
		else multiPushTime = 0;
	}
	
	/** Draws the furniture on the screen. */
	public void render(Screen screen) { sprite.render(screen, x-8, y-8); }
	
	/** Called when the player presses the MENU key in front of this. */
	public boolean use(Player player) { return false; }
	
	@Override
	public boolean blocks(Entity e) {
		return true; // furniture blocks all entities, even non-solid ones like arrows.
	}
	
	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player)
			tryPush((Player) entity);
	}
	
	/**
	 * Tries to let the player push this furniture.
	 * @param player The player doing the pushing.
	 */
	public void tryPush(Player player) {
		if (pushTime == 0) {
			pushDir = player.dir; // set pushDir to the player's dir.
			pushTime = multiPushTime = 10; // set pushTime to 10.
			
			if(Game.isConnectedClient())
				Game.client.pushFurniture(this, pushDir);
		}
	}
	
	/**
	 * Used in PowerGloveItem.java to let the user pick up furniture.
	 * @param player The player picking up the furniture.
	 */
	public void take(Player player) {
		remove(); // remove this from the world
		if(!Game.ISONLINE) {
			if (!Game.isMode("creative") && player.activeItem != null && !(player.activeItem instanceof PowerGloveItem))
				player.getInventory().add(0, player.activeItem); // put whatever item the player is holding into their inventory (should never be a power glove, since it is put in a taken out again all in the same frame).
			player.activeItem = new FurnitureItem(this); // make this the player's current item.
		}
		else if(Game.isValidServer() && player instanceof RemotePlayer)
			Game.server.getAssociatedThread((RemotePlayer)player).updatePlayerActiveItem(new FurnitureItem(this));
		else
			System.out.println("WARNING: undefined behavior; online game was not server and ticked furniture: "+this+"; and/or player in online game found that isn't a RemotePlayer: " + player);
		
		//if (Game.debug) System.out.println("set active item of player " + player + " to " + player.activeItem + "; picked up furniture: " + this);
	}
	
	@Override
	public boolean canWool() { return true; }
	
	@Override
	protected String getUpdateString() {
		return super.getUpdateString()+
		";pushTime,"+multiPushTime;
	}
	
	@Override
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		
		switch(field) {
			case "pushTime": pushTime = Integer.parseInt(val); return true;
		}
		
		return false;
	}
}
