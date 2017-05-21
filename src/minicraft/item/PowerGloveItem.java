package minicraft.item;

import java.util.ArrayList;
import minicraft.entity.Entity;
import minicraft.entity.Furniture;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;

public class PowerGloveItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		items.add(new PowerGloveItem());
		return items;
	}
	
	public PowerGloveItem() {
		super("Power Glove", new Sprite(7, 4, Color.get(-1, 100, 320, 430)));
	}
	/*
	public int getColor() {
		return Color.get(-1, 100, 320, 430); // sets the color of the powerglove
	}

	public int getSprite() {
		return 7 + 4 * 32; // returns the location of the sprite (image of the glove)
	}
	/*
	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0); // Renders the icon of the power glove to the screen
	}

	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0); // renders the icon of the power glove to the screen
		Font.draw(name, screen, x + 8, y, Color.get(-1, 555)); // renders the name of the powerglove to the screen
	}

	public String getName() {
		return "Power Glove";
	}*/

	public boolean interact(Player player, Entity entity, int attackDir) {
		if (entity instanceof Furniture) { // If the power glove is used on a piece of furniture...
			Furniture f = (Furniture) entity;
			f.take(player); // Takes (picks up) the furniture
			return true;
		}
		return false; // method returns false if we were not given a furniture entity.
	}
	
	public PowerGloveItem clone() {
		return new PowerGloveItem();
	}
}
