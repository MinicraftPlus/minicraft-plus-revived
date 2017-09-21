package minicraft.entity;

import minicraft.Game;
import minicraft.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;

public class ItemEntity extends Entity {
	private int lifeTime; // the life time of this entity in the level
	public double xa, ya, za; // the x, y, and z accelerations.
	public double xx, yy, zz; // the x, y, and z coordinates; in double precision.
	public Item item; // the item that this entity is based off of.
	private int time = 0; // time it has lasted in the level
	
	// solely for multiplayer use.
	private boolean pickedUp = false;
	private long pickupTimestamp;
	
	public ItemEntity(Item item, int x, int y) {
		super(2, 2);
		
		this.item = item;
		this.x = x;
		this.y = y;
		xx = x;
		yy = y;
		
		zz = 2;
		// random direction for each acceleration
		xa = random.nextGaussian() * 0.3;
		ya = random.nextGaussian() * 0.2;
		za = random.nextFloat() * 0.7 + 1;
		
		lifeTime = 60 * 10 + random.nextInt(70); // sets the lifetime of the item. min = 600 ticks, max = 669 ticks.
		// the idea was to have it last 10-11 seconds, I think.
	}
	public ItemEntity(Item item, int x, int y, double zz, int lifetime, int time, double xa, double ya, double za) {
		this(item, x, y);
		this.lifeTime = lifetime;
		this.time = time;
		this.zz = zz;
		this.xa = xa;
		this.ya = ya;
		this.za = za;
	}
	
	public String getData() {
		return String.join(":", (new String[] {item.name, zz+"", lifeTime+"", time+"", xa+"", ya+"", za+""}));
	}
	
	public void tick() {
		time++;
		if (time >= lifeTime) { // if the time is larger or equal to lifeTime then...
			remove(); // remove from the world
			return; // skip the rest of the code
		}
		// moves each coordinate by the its acceleration
		xx += xa;
		yy += ya;
		zz += za;
		if (zz < 0) { // if z pos is smaller than 0 (which probably marks hitting the ground)
			zz = 0; // set it to zero
			// multiply the accelerations by an amount:
			za *= -0.5;
			xa *= 0.6;
			ya *= 0.6;
		}
		za -= 0.15; // decrease z acceleration by 0.15
		
		// storage of x and y positions before move
		int ox = x;
		int oy = y;
		// integer conversion of the double x and y postions (which have already been updated):
		int nx = (int) xx;
		int ny = (int) yy;
		// the difference between the double->int new positions, and the inherited x and y positions:
		int expectedx = nx - x; // expected movement distance
		int expectedy = ny - y;
		
		/// THIS is where x and y are changed.
		move(expectedx, expectedy); // move the ItemEntity.
		
		// finds the difference between the inherited before and after positions
		int gotx = x - ox;
		int goty = y - oy;
		// Basically, this accounts for any error in the whole double-to-int position conversion thing:
		xx += gotx - expectedx;
		yy += goty - expectedy;
	}

	public boolean isBlockableBy(Mob mob) {
		return false; // mobs cannot block this
	}

	public void render(Screen screen) {
		/* this first part is for the blinking effect */
		if (time >= lifeTime - 6 * 20) {
			if (time / 6 % 2 == 0) return;
		}
		item.sprite.render(screen, x-4, y-4, Color.get(-1, 0));
		item.sprite.render(screen, x-4, y-4 - (int)(zz) );
	}

	protected void touchedBy(Entity entity) {
		if (time > 30) { // conditional prevents this from being collected immediately.
			if(Game.isConnectedClient() && entity instanceof Player && entity == ((Player)entity).game.player) {// only register if the main player picks it up, on a client.
				if(!pickedUp) {
					Game.client.pickupItem(this);
					pickedUp = true;
					pickupTimestamp = System.nanoTime();
				} else if ((System.nanoTime() - pickupTimestamp) / 1E8 > 15L) { // should be converted to tenths of a second.
					/// the item has already been picked up;
					// but since more than 1.5 seconds has past, the item will be remarked as not picked up.
					pickedUp = false;
				}
			} else if(!(Game.ISONLINE && entity instanceof Player)) // don't register if a player touches it on a server; the player will register that.
				entity.touchItem(this);
		}
	}
	
	/** What happens when the player takes the item */
	public void take(Player player) {
		Sound.pickup.play();
		player.score++; // increase the player's score by 1
		remove(); // removes this from the world
	}
}
