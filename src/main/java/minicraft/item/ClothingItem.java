package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ClothingItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		items.add(new ClothingItem("Red Clothes", new Sprite(0, 10, 0), Color.get(1, 204, 0, 0)));
		items.add(new ClothingItem("Blue Clothes", new Sprite(1, 10, 0), Color.get(1, 0, 0, 204)));
		items.add(new ClothingItem("Green Clothes",  new Sprite(2, 10, 0), Color.get(1, 0, 204, 0)));
		items.add(new ClothingItem("Yellow Clothes",  new Sprite(3, 10, 0), Color.get(1, 204, 204, 0)));
		items.add(new ClothingItem("Black Clothes",  new Sprite(4, 10, 0), Color.get(1, 51)));
		items.add(new ClothingItem("Orange Clothes",  new Sprite(5, 10, 0), Color.get(1, 255, 102, 0)));
		items.add(new ClothingItem("Purple Clothes",  new Sprite(6, 10, 0), Color.get(1, 102, 0, 153)));
		items.add(new ClothingItem("Cyan Clothes",  new Sprite(7, 10, 0), Color.get(1, 0, 102, 153)));
		items.add(new ClothingItem("Reg Clothes",  new Sprite(8, 10, 0), Color.get(1, 51, 51, 0)));
		
		return items;
	}
	
	private int playerCol;
	private Sprite sprite;
	
	private ClothingItem(String name, Sprite sprite, int pcol) { this(name, 1, sprite, pcol); }
	private ClothingItem(String name, int count, Sprite sprite, int pcol) {
		super(name, sprite, count);
		playerCol = pcol;
		this.sprite = sprite;
	}
	
	// Put on clothes
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (player.shirtColor == playerCol) {
			return false;
		} else {
			player.shirtColor = playerCol;
			return super.interactOn(true);
		}
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public ClothingItem clone() {
		return new ClothingItem(getName(), count, sprite, playerCol);
	}
}
