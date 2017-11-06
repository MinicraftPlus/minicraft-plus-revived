package minicraft.item;

import java.util.ArrayList;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ClothingItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		items.add(new ClothingItem("Red Clothes", Color.get(-1, 100, 400, 500), 400));
		items.add(new ClothingItem("Blue Clothes", Color.get(-1, 001, 004, 005), 004));
		items.add(new ClothingItem("Green Clothes", Color.get(-1, 10, 40, 50), 40));
		items.add(new ClothingItem("Yellow Clothes", Color.get(-1, 110, 440, 550), 440));
		items.add(new ClothingItem("Black Clothes", Color.get(-1, 000, 111, 222), 111));
		items.add(new ClothingItem("Orange Clothes", Color.get(-1, 210, 430, 540), 320));
		items.add(new ClothingItem("Purple Clothes", Color.get(-1, 102, 203, 405), 203));
		items.add(new ClothingItem("Cyan Clothes", Color.get(-1, 12, 23, 45), 23));
		items.add(new ClothingItem("Reg Clothes", Color.get(-1, 111, 444, 555), 110));
		
		return items;
	}
	
	private int playerCol;
	
	private ClothingItem(String name, int color, int pcol) { this(name, 1, color, pcol); }
	private ClothingItem(String name, int count, int color, int pcol) {
		super(name, new Sprite(6, 12, color), count);
		playerCol = pcol;
	}
	
	// put on clothes
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if(player.shirtColor == playerCol) {
			return false;
		} else {
			player.shirtColor = playerCol;
			return super.interactOn(true);
		}
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public ClothingItem clone() {
		return new ClothingItem(getName(), count, sprite.color, playerCol);
	}
}
