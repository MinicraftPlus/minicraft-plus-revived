package minicraft.entity.furniture;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.FurnitureItem;
import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Many furniture classes are very similar; they might not even need to be there at all...
 */

public class Furniture extends Entity {

	public LinkedSprite sprite;
	public LinkedSprite itemSprite;
	public String name;

	/**
	 * Constructor for the furniture entity.
	 * Size will be set to 3.
	 * @param name Name of the furniture.
	 * @param sprite Furniture sprite.
	 */
	public Furniture(String name, LinkedSprite sprite, LinkedSprite itemSprite) {
		this(name, sprite, itemSprite, 3, 3);
	}

	/**
	 * Constructor for the furniture entity.
	 * Radius is only used for collision detection.
	 * @param name Name of the furniture.
	 * @param sprite Furniture sprite.
	 * @param xr Horizontal radius.
	 * @param yr Vertical radius.
	 */
	public Furniture(String name, LinkedSprite sprite, LinkedSprite itemSprite, int xr, int yr) {
		// All of these are 2x2 on the spritesheet; radius is for collisions only.
		super(xr, yr);
		this.name = name;
		this.sprite = sprite;
		this.itemSprite = itemSprite;
	}

	public @NotNull Furniture copy() {
		try {
			return getClass().getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new Furniture(name, sprite, itemSprite);
	}

	@Override
	public void tick() {
		// Moves the furniture in the correct direction.
		move(pushDir.getX(), pushDir.getY());
		pushDir = Direction.NONE;

		if (pushTime > 0) pushTime--; // Update pushTime by subtracting 1.
		else multiPushTime = 0;
	}

	/**
	 * Draws the furniture on the screen.
	 */
	public void render(Screen screen) {
		screen.render(x - 8, y - 8, sprite);
	}

	/**
	 * Called when the player presses the MENU key in front of this.
	 */
	public boolean use(Player player) {
		return false;
	}

	@Override
	public boolean blocks(Entity e) {
		return true; // Furniture blocks all entities, even non-solid ones like arrows.
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player)
			tryPush((Player) entity);
	}

	/**
	 * Used in PowerGloveItem.java to let the user pick up furniture.
	 * @param player The player picking up the furniture.
	 */
	@Override
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (item instanceof PowerGloveItem) {
			Sound.play("monsterhurt");
			remove();
			if (player.activeItem != null && !(player.activeItem instanceof PowerGloveItem))
				player.getLevel().dropItem(player.x, player.y, player.activeItem); // Put whatever item the player is holding into their inventory
			player.activeItem = new FurnitureItem(this); // Make this the player's current item.
			return true;
		}
		return false;
	}

	@Override
	public boolean canWool() {
		return true;
	}
}
