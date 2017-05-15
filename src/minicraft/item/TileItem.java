package minicraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import minicraft.Game;
import minicraft.level.tile.Tile;
import minicraft.gfx.Sprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Color;
import minicraft.entity.Player;
import minicraft.level.Level;

/// this is meant to replace PlantableItem/Item, I think.

public class TileItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		
		/// TileItem sprites are all on line 4, and have 1x1 sprites.
		items.add(new TileItem("Flower", new Sprite(0, 4, Color.get(-1, 10, 444, 330)), Tile.flower, Tile.grass));
		items.add(new TileItem("Acorn", new Sprite(3, 4, Color.get(-1, 100, 531, 320)), Tile.treeSapling, Tile.grass));
		items.add(new TileItem("Dirt", new Sprite(2, 4, Color.get(-1, 100, 322, 432)), Tile.dirt, Tile.hole, Tile.water, Tile.lava));
	
		items.add(new TileItem("Plank", new Sprite(1, 4, Color.get(-1, 200, 531, 530)), Tile.plank, Tile.hole, Tile.water));
		items.add(new TileItem("Plank Wall", new Sprite(16, 4, Color.get(-1, 200, 531, 530)), Tile.plankwall, Tile.plank));
		items.add(new TileItem("Wood Door", new Sprite(17, 4, Color.get(-1, 200, 531, 530)), Tile.wdc, Tile.plank));
		items.add(new TileItem("Stone Brick", new Sprite(1, 4, Color.get(-1, 333, 444, 444)), Tile.sbrick, Tile.hole, Tile.water, Tile.lava));
		items.add(new TileItem("Stone BrickWall", new Sprite(16, 4, Color.get(-1, 100, 333, 444)), Tile.stonewall, Tile.sbrick));
		items.add(new TileItem("Stone Door", new Sprite(17, 4, Color.get(-1, 111, 333, 444)), Tile.sdc, Tile.sbrick));
		items.add(new TileItem("Obsidian BrickWall", new Sprite(16, 4, Color.get(-1, 159, 59, 59)), Tile.ow, Tile.o));
		items.add(new TileItem("Obsidian Brick", new Sprite(1, 4, Color.get(-1, 159, 59, 59)), Tile.o, Tile.hole, Tile.water, Tile.lava));
		items.add(new TileItem("Obsidian Door", new Sprite(17, 4, Color.get(-1, 159, 59, 59)), Tile.odc, Tile.o));
	
		// TODO make a method in Item.java; calls clone(), but then changes color, and returns itself. Call it cloneAsColor, or changeColor, or maybe *asColor()*.
		items.add(new TileItem("Wool", new Sprite(2, 4, Color.get(-1, 555)), Tile.wool, Tile.hole, Tile.water));
		items.add(new TileItem("Red Wool", new Sprite(2, 4, Color.get(-1, 100, 300, 500)), Tile.redwool, Tile.hole, Tile.water));
		items.add(new TileItem("Blue Wool", new Sprite(2, 4, Color.get(-1, 005, 115, 115)), Tile.bluewool, Tile.hole, Tile.water));
		items.add(new TileItem("Green Wool", new Sprite(2, 4, Color.get(-1, 10, 40, 50)), Tile.greenwool, Tile.hole, Tile.water));
		items.add(new TileItem("Yellow Wool", new Sprite(2, 4, Color.get(-1, 110, 440, 552)), Tile.yellowwool, Tile.hole, Tile.water));
		items.add(new TileItem("Black Wool", new Sprite(2, 4, Color.get(-1, 000, 111, 111)), Tile.blackwool, Tile.hole, Tile.water));
	
		items.add(new TileItem("Sand", new Sprite(2, 4, Color.get(-1, 110, 440, 550)), Tile.sand, Tile.dirt));
		items.add(new TileItem("Cactus", new Sprite(4, 4, Color.get(-1, 10, 40, 50)), Tile.cactusSapling, Tile.sand));
		items.add(new TileItem("Seeds", new Sprite(5, 4, Color.get(-1, 10, 40, 50)), Tile.wheat, Tile.farmland));
		items.add(new TileItem("Grass Seeds", new Sprite(5, 4, Color.get(-1, 10, 30, 50)), Tile.grass, Tile.dirt));
		items.add(new TileItem("Bone", new Sprite(15, 4, Color.get(-1, 222, 555, 555)), Tile.tree, Tile.treeSapling));
		items.add(new TileItem("Cloud", new Sprite(2, 4, Color.get(-1, 222, 555, 444)), Tile.cloud, Tile.infiniteFall));
		
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
		
		if(model.getName().contains("Wall") && reqTiles.size() == 1) {
			Game.notifications.add("Can only be placed on " + reqTiles.get(0).getName() + "!");
		}
		return false;
	}
	/*
	public String getName() {
		return model.getName() + " Item";
	}*/
	
	public boolean matches(Item other) {
		return super.matches(other) && model.matches(((TileItem)other).model);
	}
	
	public TileItem clone() {
		return new TileItem(name, sprite, count, model, reqTiles);
	}
}
