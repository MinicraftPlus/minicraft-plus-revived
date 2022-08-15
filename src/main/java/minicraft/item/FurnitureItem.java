package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.furniture.*;
import minicraft.entity.mob.*;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

import java.util.ArrayList;

public class FurnitureItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		/// There should be a spawner for each level of mob, or at least make the level able to be changed.
		items.add(new FurnitureItem(new Spawner(new Cow()), 1, 28));
		items.add(new FurnitureItem(new Spawner(new Pig()), 2, 28));
		items.add(new FurnitureItem(new Spawner(new Sheep()), 3, 28));
		items.add(new FurnitureItem(new Spawner(new Slime(1)), 4, 28));
		items.add(new FurnitureItem(new Spawner(new Zombie(1)), 5, 28));
		items.add(new FurnitureItem(new Spawner(new Creeper(1)), 6, 28));
		items.add(new FurnitureItem(new Spawner(new Skeleton(1)), 7, 28));
		items.add(new FurnitureItem(new Spawner(new Snake(1)), 8, 28));
		items.add(new FurnitureItem(new Spawner(new Knight(1)), 9, 28));

		items.add(new FurnitureItem(new Chest()));
		items.add(new FurnitureItem(new DungeonChest(false, true)));

		// Add the various types of crafting furniture
		for (Crafter.Type type: Crafter.Type.values()) {
			 items.add(new FurnitureItem(new Crafter(type)));
		}
		// Add the various lanterns
		for (Lantern.Type type: Lantern.Type.values()) {
			 items.add(new FurnitureItem(new Lantern(type)));
		}

		items.add(new FurnitureItem(new Tnt()));
		items.add(new FurnitureItem(new Bed()));

		return items;
	}

	public Furniture furniture; // The furniture of this item
	public boolean placed; // Value if the furniture has been placed or not.
	private int sx, sy; // Sprite position

	private static int getSpritePos(int fpos) {
		int x = fpos%32;
		int y = fpos/32;
		return ((x-8)/2) + y * 32;
	}

	private static Sprite getFurnitureSprite(Furniture furniture) {
		Sprite sprite;
		if (furniture instanceof Spawner) {
			MobAi mob = ((Spawner)furniture).mob;
			if (mob instanceof Cow) sprite = new Sprite(1, 28, 1, 1, 0);
			else if (mob instanceof Pig) sprite = new Sprite(2, 28, 1, 1, 0);
			else if (mob instanceof Sheep) sprite = new Sprite(3, 28, 1, 1, 0);
			else if (mob instanceof Slime) sprite = new Sprite(4, 28, 1, 1, 0);
			else if (mob instanceof Zombie) sprite = new Sprite(5, 28, 1, 1, 0);
			else if (mob instanceof Creeper) sprite = new Sprite(6, 28, 1, 1, 0);
			else if (mob instanceof Skeleton) sprite = new Sprite(7, 28, 1, 1, 0);
			else if (mob instanceof Snake) sprite = new Sprite(8, 28, 1, 1, 0);
			else if (mob instanceof Knight) sprite = new Sprite(9, 28, 1, 1, 0);
			else if (mob instanceof AirWizard) sprite = new Sprite(10, 28, 1, 1, 0);
			else sprite = new Sprite(getSpritePos(furniture.sprite.getPos()), 0);
		} else sprite = new Sprite(getSpritePos(furniture.sprite.getPos()), 0);
		return sprite;
	}

	public FurnitureItem(Furniture furniture) {
		super(furniture.name, getFurnitureSprite(furniture));
		this.furniture = furniture; // Assigns the furniture to the item
		placed = false;
	}

	public FurnitureItem(Furniture furniture, int sx , int sy) {
		super(furniture.name, new Sprite(sx, sy, 1, 1, 0)); // get the sprite directly
		this.sx = sx;
		this.sy = sy;
		this.furniture = furniture;
		placed = false;
	}

	/** Determines if you can attack enemies with furniture (you can't) */
	public boolean canAttack() {
		return false;
	}

	/** What happens when you press the "Attack" key with the furniture in your hands */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (tile.mayPass(level, xt, yt, furniture)) { // If the furniture can go on the tile
			Sound.place.play();

			// Placed furniture's X and Y positions
			furniture.x = xt * 16 + 8;
			furniture.y = yt * 16 + 8;

			level.add(furniture); // Adds the furniture to the world
			if (Game.isMode("minicraft.settings.mode.creative"))
				furniture = furniture.clone();
			else
				placed = true; // The value becomes true, which removes it from the player's active item

			return true;
		}
		return false;
	}

	public boolean isDepleted() {
		return placed;
	}

	public FurnitureItem clone() {
		// in case the item is a spawner, it will use the sprite position (sx, sy)
		// instead if it is not, the constructor will obtain said sprite
		if (furniture.name.contains("Spawner")) {
			return new FurnitureItem(furniture.clone(), sx, sy);
		} else {
			return new FurnitureItem(furniture.clone());
		}
	}
}
