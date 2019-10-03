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
		
		items.add(new ClothingItem("Red Clothes", 1, Color.get(1, 204, 0, 0)));
		items.add(new ClothingItem("Blue Clothes", 0, Color.get(1, 0, 0, 204)));
		items.add(new ClothingItem("Green Clothes", 2, Color.get(1, 0, 204, 0)));
		items.add(new ClothingItem("Yellow Clothes", 3, Color.get(1, 204, 204, 0)));
		items.add(new ClothingItem("Black Clothes", 4, Color.get(1, 51)));
		items.add(new ClothingItem("Orange Clothes", 5, Color.get(1, 255, 102, 0)));
		items.add(new ClothingItem("Purple Clothes", 6, Color.get(1, 102, 0, 153)));
		items.add(new ClothingItem("Cyan Clothes", 8, Color.get(1, 0, 102, 153)));
		items.add(new ClothingItem("Reg Clothes", 9, Color.get(1, 51, 51, 0)));
		
		return items;
	}
	
	private int playerCol;
	
	private ClothingItem(String name, int offset, int pcol) { this(name, 1, offset, pcol); }
	private ClothingItem(String name, int count, int offset, int pcol) {
		super(name, new Sprite(offset, 10, 1), count);
		playerCol = pcol;
	}
	
	// put on clothes
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if(player.shirtColor == playerCol) {
			return false;
		} else {
			player.shirtColor = playerCol;
			if(Game.isValidClient())
				Game.client.sendShirtColor();
			return super.interactOn(true);
		}
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public ClothingItem clone() {
		return new ClothingItem(getName(), count, sprite.color, playerCol);
	}
}
