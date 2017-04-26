package minicraft.item;

import minicraft.entity.Furniture;
import minicraft.entity.ItemEntity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class FurnitureItem extends Item {
	public Furniture furniture; // the furniture of this item
	public boolean placed = false; // value if the furniture has been placed or not.

	public FurnitureItem(Furniture furniture) {
		this.furniture = furniture; // Assigns the furniture to the item
	}
	
	public int getColor() {
		return furniture.col;
	
	/** Determines if you can attack enemies with furniture (you can't) */}
	
	public int getSprite() {
		return furniture.sprite + 10 * 32;
	}
	
	/** Renders the icon used for the furniture. */
	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
	}
	
	/** Renders the icon, and name of the furniture */
	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0); // renders the icon
		Font.draw(furniture.name, screen, x + 8, y, Color.get(-1, 555, 555, 555)); // draws the name of the furniture
	}
	
	/** What happens when you pick up the item off the ground (Not with the power glove) */
	public void onTake(ItemEntity itemEntity) {}
	
	/** Determines if you can attack enemies with furniture (you can't) */
	public boolean canAttack() {
		return false;
	}
	
	/** What happens when you press the "Attack" key with the furniture in your hands */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (tile.mayPass(level, xt, yt, furniture)) { // If the furniture can go on the tile
			 // Placed furniture's X and Y positions
			furniture.x = xt * 16 + 8;
			furniture.y = yt * 16 + 8;
			level.add(furniture); // adds the furniture to the world
			placed = true; // the value becomes true, which removes it from the player's active item
			return true;
		}
		return false;
	}
	
	/** Removes this item from the player's active item slot when depleted is true */
	public boolean isDepleted() {
		return placed;
	}

	public String getName() {
		return furniture.name;
	}
}
