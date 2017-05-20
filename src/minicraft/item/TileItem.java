package minicraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import minicraft.Game;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.gfx.Sprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Color;
import minicraft.entity.Player;
import minicraft.level.Level;

/// this is meant to replace PlantableItem/Item, I think.

public class TileItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		
		/// TileItem sprites are all on line 4, and have 1x1 sprites.
		items.add(new TileItem("Flower", new Sprite(0, 4, Color.get(-1, 10, 444, 330)), Tiles.get("flower"), Tiles.get("grass")));
		items.add(new TileItem("Acorn", new Sprite(3, 4, Color.get(-1, 100, 531, 320)), Tiles.get("tree Sapling"), Tiles.get("grass")));
		items.add(new TileItem("Dirt", new Sprite(2, 4, Color.get(-1, 100, 322, 432)), Tiles.get("dirt"), Tiles.get("hole"), Tiles.get("water"), Tiles.get("lava")));
	
		items.add(new TileItem("Plank", new Sprite(1, 4, Color.get(-1, 200, 531, 530)), Tiles.get("Wood Plank"), Tiles.get("hole"), Tiles.get("water")));
		items.add(new TileItem("Plank Wall", new Sprite(16, 4, Color.get(-1, 200, 531, 530)), Tiles.get("plankwall"), Tiles.get("Wood Plank")));
		items.add(new TileItem("Wood Door", new Sprite(17, 4, Color.get(-1, 200, 531, 530)), Tiles.get("Wood Door"), Tiles.get("Wood Plank")));
		items.add(new TileItem("Stone Brick", new Sprite(1, 4, Color.get(-1, 333, 444, 444)), Tiles.get("Stone Brick"), Tiles.get("hole"), Tiles.get("water"), Tiles.get("lava")));
		items.add(new TileItem("Stone BrickWall", new Sprite(16, 4, Color.get(-1, 100, 333, 444)), Tiles.get("Stone Wall"), Tiles.get("Stone Brick")));
		items.add(new TileItem("Stone Door", new Sprite(17, 4, Color.get(-1, 111, 333, 444)), Tiles.get("Stone Door"), Tiles.get("Stone Brick")));
		items.add(new TileItem("Obsidian BrickWall", new Sprite(16, 4, Color.get(-1, 159, 59, 59)), Tiles.get("ow"), Tiles.get("Obsidian")));
		items.add(new TileItem("Obsidian Brick", new Sprite(1, 4, Color.get(-1, 159, 59, 59)), Tiles.get("Obsidian"), Tiles.get("hole"), Tiles.get("water"), Tiles.get("lava")));
		items.add(new TileItem("Obsidian Door", new Sprite(17, 4, Color.get(-1, 159, 59, 59)), Tiles.get("Obsidian Door"), Tiles.get("Obsidian")));
	
		// TODO make a method in Item.java; calls clone(), but then changes color, and returns itself. Call it cloneAsColor, or changeColor, or maybe *asColor()*.
		items.add(new TileItem("Wool", new Sprite(2, 4, Color.get(-1, 555)), Tiles.get("wool"), Tiles.get("hole"), Tiles.get("water")));
		items.add(new TileItem("Red Wool", new Sprite(2, 4, Color.get(-1, 100, 300, 500)), Tiles.get("red Wool"), Tiles.get("hole"), Tiles.get("water")));
		items.add(new TileItem("Blue Wool", new Sprite(2, 4, Color.get(-1, 005, 115, 115)), Tiles.get("blue Wool"), Tiles.get("hole"), Tiles.get("water")));
		items.add(new TileItem("Green Wool", new Sprite(2, 4, Color.get(-1, 10, 40, 50)), Tiles.get("green Wool"), Tiles.get("hole"), Tiles.get("water")));
		items.add(new TileItem("Yellow Wool", new Sprite(2, 4, Color.get(-1, 110, 440, 552)), Tiles.get("yellow Wool"), Tiles.get("hole"), Tiles.get("water")));
		items.add(new TileItem("Black Wool", new Sprite(2, 4, Color.get(-1, 000, 111, 111)), Tiles.get("black Wool"), Tiles.get("hole"), Tiles.get("water")));
	
		items.add(new TileItem("Sand", new Sprite(2, 4, Color.get(-1, 110, 440, 550)), Tiles.get("sand"), Tiles.get("dirt")));
		items.add(new TileItem("Cactus", new Sprite(4, 4, Color.get(-1, 10, 40, 50)), Tiles.get("cactus Sapling"), Tiles.get("sand")));
		items.add(new TileItem("Seeds", new Sprite(5, 4, Color.get(-1, 10, 40, 50)), Tiles.get("wheat"), Tiles.get("farmland")));
		items.add(new TileItem("Grass Seeds", new Sprite(5, 4, Color.get(-1, 10, 30, 50)), Tiles.get("grass"), Tiles.get("dirt")));
		items.add(new TileItem("Bone", new Sprite(15, 4, Color.get(-1, 222, 555, 555)), Tiles.get("tree"), Tiles.get("tree Sapling")));
		items.add(new TileItem("Cloud", new Sprite(2, 4, Color.get(-1, 222, 555, 444)), Tiles.get("cloud"), Tiles.get("Infinite Fall")));
		
		return items;
	}
	
	public final Tile model;
	public final List<Tile> reqTiles;
	
	public TileItem(String name, Sprite sprite, Tile model, Tile... reqTiles) {
		this(name, sprite, 1, model, Arrays.asList(reqTiles));
	}
	public TileItem(String name, Sprite sprite, int count, Tile model, Tile... reqTiles) {
		this(name, sprite, count, model, Arrays.asList(reqTiles));
	}
	public TileItem(String name, Sprite sprite, int count, Tile model, List<Tile> reqTiles) {
		super(name, sprite, count);
		this.model = model;
		this.reqTiles = reqTiles;
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if(reqTiles.contains(tile)) {
			level.setTile(xt, yt, model, 0); // TODO maybe data should be part of the saved tile..?
			return true;
		}
		
		if(model.name.contains("Wall") && reqTiles.size() == 1) {
			Game.notifications.add("Can only be placed on " + reqTiles.get(0).name + "!");
		}
		return false;
	}
	/*
	public String getName() {
		return model.name + " Item";
	}*/
	
	public boolean matches(Item other) {
		return super.matches(other) && model.matches(((TileItem)other).model);
	}
	
	public TileItem clone() {
		return new TileItem(name, sprite, count, model, reqTiles);
	}
}
