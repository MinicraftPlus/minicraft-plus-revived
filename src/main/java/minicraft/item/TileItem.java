package minicraft.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class TileItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		/// TileItem sprites all have 1x1 sprites.
		items.add(new TileItem("Flower", (new Sprite(4, 0, 0)), "flower", "grass"));
		items.add(new TileItem("Acorn", (new Sprite(7, 3, 0)), "tree Sapling", "grass"));
		items.add(new TileItem("Dirt", (new Sprite(0, 0, 0)), "dirt", "hole", "water", "lava"));
		items.add(new TileItem("Natural Rock", (new Sprite(2, 0, 0)), "rock", "hole", "dirt", "sand", "grass", "path", "water", "lava"));
		
		items.add(new TileItem("Plank", (new Sprite(0, 5, 0)), "Wood Planks", "hole", "water", "cloud"));
		items.add(new TileItem("Plank Wall", (new Sprite(1, 5, 0)), "Wood Wall", "Wood Planks"));
		items.add(new TileItem("Wood Door", (new Sprite(2, 5, 0)), "Wood Door", "Wood Planks"));
		items.add(new TileItem("Stone Brick", (new Sprite(3, 5, 0)), "Stone Bricks", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Stone Wall", (new Sprite(4, 5, 0)), "Stone Wall", "Stone Bricks"));
		items.add(new TileItem("Stone Door", (new Sprite(5, 5, 0)), "Stone Door", "Stone Bricks"));
		items.add(new TileItem("Obsidian Brick", (new Sprite(6, 5, 0)), "Obsidian", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Obsidian Wall", (new Sprite(7, 5, 0)), "Obsidian Wall", "Obsidian"));
		items.add(new TileItem("Obsidian Door", (new Sprite(8, 5, 0)), "Obsidian Door", "Obsidian"));
	
		items.add(new TileItem("Wool", (new Sprite(5, 3, 0)), "Wool", "hole", "water"));
		items.add(new TileItem("Red Wool", (new Sprite(4, 3, 0)), "Red Wool", "hole", "water"));
		items.add(new TileItem("Blue Wool", (new Sprite(3, 3, 0)), "Blue Wool", "hole", "water"));
		items.add(new TileItem("Green Wool", (new Sprite(2, 3, 0)), "Green Wool", "hole", "water"));
		items.add(new TileItem("Yellow Wool", (new Sprite(1, 3, 0)), "Yellow Wool", "hole", "water"));
		items.add(new TileItem("Black Wool", (new Sprite(0, 3, 0)), "Black Wool", "hole", "water"));
		
		items.add(new TileItem("Sand", (new Sprite(6, 3, 0)), "sand", "hole", "water", "lava"));
		items.add(new TileItem("Cactus", (new Sprite(8, 3, 0)), "cactus Sapling", "sand"));
		items.add(new TileItem("Bone", (new Sprite(9, 3, 0)), "tree", "tree Sapling"));
		items.add(new TileItem("Cloud", (new Sprite(10, 3, 0)), "cloud", "Infinite Fall"));

		items.add(new TileItem("Wheat Seeds", (new Sprite(3, 0, 0)), "wheat", "farmland"));
		items.add(new TileItem("Potato", (new Sprite(18, 0, 0)), "potato", "farmland"));
		items.add(new TileItem("Grass Seeds", (new Sprite(3, 0, 0)), "grass", "dirt"));

		return items;
	}
	
	public final String model;
	public final List<String> validTiles;
	
	protected TileItem(String name, Sprite sprite, String model, String... validTiles) {
		this(name, sprite, 1, model, Arrays.asList(validTiles));
	}
	protected TileItem(String name, Sprite sprite, int count, String model, String... validTiles) {
		this(name, sprite, count, model, Arrays.asList(validTiles));
	}
	protected TileItem(String name, Sprite sprite, int count, String model, List<String> validTiles) {
		super(name, sprite, count);
		this.model = model.toUpperCase();
		this.validTiles = new ArrayList<>();
		for (String tile: validTiles)
			 this.validTiles.add(tile.toUpperCase());
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		for (String tilename : validTiles) {
			if (tile.matches(level.getData(xt, yt), tilename)) {
				level.setTile(xt, yt, model); // TODO maybe data should be part of the saved tile..?

				Sound.place.play();

				return super.interactOn(true);
			}
		}
		
		if (Game.debug) System.out.println(model + " cannot be placed on " + tile.name);
		
		String note = "";
		if (model.contains("WALL")) {
			note = "Can only be placed on " + Tiles.getName(validTiles.get(0)) + "!";
		}
		else if (model.contains("DOOR")) {
			note = "Can only be placed on " + Tiles.getName(validTiles.get(0)) + "!";
		}
		else if ((model.contains("BRICK") || model.contains("PLANK"))) {
			note = "Dig a hole first!";
		}
		
		if (note.length() > 0) {
			if (!Game.isValidServer())
				Game.notifications.add(note);
			else
				Game.server.getAssociatedThread((RemotePlayer)player).sendNotification(note, 0);
		}
		
		return super.interactOn(false);
	}
	
	@Override
	public boolean equals(Item other) {
		return super.equals(other) && model.equals(((TileItem)other).model);
	}
	
	@Override
	public int hashCode() { return super.hashCode() + model.hashCode(); }
	
	public TileItem clone() {
		return new TileItem(getName(), sprite, count, model, validTiles);
	}
}
