package minicraft.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import minicraft.Game;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

/// this is meant to replace PlantableItem/Item, I think.

public class TileItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		
		/// TileItem sprites are all on line 4, and have 1x1 sprites.
		items.add(new TileItem("Flower", (new Sprite(0, 4, Color.get(-1, 10, 444, 330))), "flower", "grass"));
		items.add(new TileItem("Acorn", (new Sprite(3, 4, Color.get(-1, 100, 531, 320))), "tree Sapling", "grass"));
		items.add(new TileItem("Dirt", (new Sprite(2, 4, Color.get(-1, 100, 322, 432))), "dirt", "hole", "water", "lava"));
	
		items.add(new TileItem("Plank", (new Sprite(1, 4, Color.get(-1, 200, 531, 530))), "Wood Planks", "hole", "water"));
		items.add(new TileItem("Plank Wall", (new Sprite(16, 4, Color.get(-1, 200, 531, 530))), "Plank Wall", "Wood Planks"));
		items.add(new TileItem("Wood Door", (new Sprite(17, 4, Color.get(-1, 200, 531, 530))), "Wood Door", "Wood Planks"));
		items.add(new TileItem("Stone Brick", (new Sprite(1, 4, Color.get(-1, 333, 444, 444))), "Stone Bricks", "hole", "water", "lava"));
		items.add(new TileItem("Stone BrickWall", (new Sprite(16, 4, Color.get(-1, 100, 333, 444))), "Stone Wall", "Stone Bricks"));
		items.add(new TileItem("Stone Door", (new Sprite(17, 4, Color.get(-1, 111, 333, 444))), "Stone Door", "Stone Bricks"));
		items.add(new TileItem("Obsidian BrickWall", (new Sprite(16, 4, Color.get(-1, 159, 59, 59))), "Obsidian Wall", "Obsidian"));
		items.add(new TileItem("Obsidian Brick", (new Sprite(1, 4, Color.get(-1, 159, 59, 59))), "Obsidian", "hole", "water", "lava"));
		items.add(new TileItem("Obsidian Door", (new Sprite(17, 4, Color.get(-1, 159, 59, 59))), "Obsidian Door", "Obsidian"));
	
		// TODO make a method in Item.java; calls clone(), but then changes color, and returns itself. Call it cloneAsColor, or changeColor, or maybe *asColor()*.
		items.add(new TileItem("Wool", (new Sprite(2, 4, Color.get(-1, 555))), "wool", "hole", "water"));
		items.add(new TileItem("Red Wool", (new Sprite(2, 4, Color.get(-1, 100, 300, 500))), "red Wool", "hole", "water"));
		items.add(new TileItem("Blue Wool", (new Sprite(2, 4, Color.get(-1, 005, 115, 115))), "blue Wool", "hole", "water"));
		items.add(new TileItem("Green Wool", (new Sprite(2, 4, Color.get(-1, 10, 40, 50))), "green Wool", "hole", "water"));
		items.add(new TileItem("Yellow Wool", (new Sprite(2, 4, Color.get(-1, 110, 440, 552))), "yellow Wool", "hole", "water"));
		items.add(new TileItem("Black Wool", (new Sprite(2, 4, Color.get(-1, 000, 111, 111))), "black Wool", "hole", "water"));
	
		items.add(new TileItem("Sand", (new Sprite(2, 4, Color.get(-1, 110, 440, 550))), "sand", "dirt"));
		items.add(new TileItem("Cactus", (new Sprite(4, 4, Color.get(-1, 10, 40, 50))), "cactus Sapling", "sand"));
		items.add(new TileItem("Seeds", (new Sprite(5, 4, Color.get(-1, 10, 40, 50))), "wheat", "farmland"));
		items.add(new TileItem("Grass Seeds", (new Sprite(5, 4, Color.get(-1, 10, 30, 50))), "grass", "dirt"));
		items.add(new TileItem("Bone", (new Sprite(15, 4, Color.get(-1, 222, 555, 555))), "tree", "tree Sapling"));
		items.add(new TileItem("Cloud", (new Sprite(2, 4, Color.get(-1, 222, 555, 444))), "cloud", "Infinite Fall"));
		
		return items;
	}
	
	public final String model;
	public final List<String> reqTiles;
	
	protected TileItem(String name, Sprite sprite, String model, String... reqTiles) {
		this(name, sprite, 1, model, Arrays.asList(reqTiles));
	}
	protected TileItem(String name, Sprite sprite, int count, String model, String... reqTiles) {
		this(name, sprite, count, model, Arrays.asList(reqTiles));
	}
	protected TileItem(String name, Sprite sprite, int count, String model, List<String> reqTiles) {
		super(name, sprite, count);
		this.model = model;
		this.reqTiles = reqTiles;
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if(reqTiles.contains(tile.name)) {
			level.setTile(xt, yt, Tiles.get(model), 0); // TODO maybe data should be part of the saved tile..?
			return true;
		}
		
		if(model.contains("Wall") && reqTiles.size() == 1) {
			Game.notifications.add("Can only be placed on " + reqTiles.get(0) + "!");
		}
		return false;
	}
	/*
	public String getName() {
		return model.name + " Item";
	}*/
	
	public boolean matches(Item other) {
		return super.matches(other) && model.equals(((TileItem)other).model);
	}
	
	public TileItem clone() {
		return new TileItem(name, sprite, count, model, reqTiles);
	}
}
