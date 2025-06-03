package minicraft.item;

import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.component.ComponentMap;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public abstract class Item {

	/* Note: Most of the stuff in the class is expanded upon in StackableItem/PowerGloveItem/FurnitureItem/etc */

	private final String name;
	private LinkedSprite sprite;

	private final ComponentMap components;

	public boolean used_pending = false; // This is for multiplayer, when an item has been used, and is pending server response as to the outcome, this is set to true so it cannot be used again unless the server responds that the item wasn't used. Which should basically replace the item anyway, soo... yeah. this never gets set back.

	public int maxCount = 1;

	protected Item(String name) {
		this(name, new ComponentMap());
	}

	protected Item(String name, ComponentMap components) {
		sprite = SpriteLinker.missingTexture(SpriteType.Item);
		this.name = name;
		this.components = components;
	}

	protected Item(String name, LinkedSprite sprite) {
		this(name, sprite, new ComponentMap());
	}

	protected Item(String name, LinkedSprite sprite, ComponentMap components) {
		this.name = name;
		this.sprite = sprite;
		this.components = components;
	}

	/**
	 * Renders an item on the HUD
	 */
	public void renderHUD(Screen screen, int x, int y, int fontColor, ItemStack stack) {
		String dispName = getDisplayName(stack);
		screen.render(x, y, sprite);
		Font.drawBackground(dispName, screen, x + 8, y, fontColor);
	}

	/**
	 * Determines what happens when the player interacts with a tile
	 */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir, ItemStack stack) {
		return false;
	}

	/**
	 * Returns the Item default components
	 */
	public ComponentMap getComponents() {
		return this.components;
	}

	public LinkedSprite getSprite(ItemStack stack) {
		return this.sprite;
	}

	/**
	 * Returning true causes this item to be removed from the player's active item slot
	 */
	public boolean isDepleted(ItemStack stack) {
		return false;
	}

	/**
	 * Returns if the item can attack mobs or not
	 */
	public boolean canAttack(ItemStack stack) {
		return false;
	}

	/**
	 * Sees if an item equals another item
	 */
	public boolean equals(Item item) {
		return item != null && item.getClass().equals(getClass()) && item.name.equals(name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * This returns a copy of this item, in all necessary detail.
	 */
// 	@NotNull
// 	public abstract Item copy();

	@Override
	public String toString() {
		return name + "-Item";
	}

	/**
	 * Gets the necessary data to send over a connection. This data should always be directly input-able into Items.get() to create a valid item with the given properties.
	 */
	public String getData(ItemStack stack) {
		return name;
	}

	/**
	 * Gets the description used for display item information.
	 */
	public String getDescription() {
		return getName();
	}

	public final String getName() {
		return name;
	}

	// Returns the String that should be used to display this item in a menu or list.
	public String getDisplayName(ItemStack stack) {
		return " " + Localization.getLocalized(getName());
	}

	public boolean interactsWithWorld(ItemStack stack) {
		return true;
	}
}
