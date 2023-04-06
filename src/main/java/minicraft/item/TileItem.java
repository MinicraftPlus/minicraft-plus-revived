package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.util.AdvancementElement;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TileItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		/// TileItem sprites all have 1x1 sprites.
		items.add(new TileItem("Flower", new LinkedSprite(SpriteType.Item, "white_flower"), "flower", "grass"));
		items.add(new TileItem("Acorn", new LinkedSprite(SpriteType.Item, "acorn"), "tree Sapling", "grass"));
		items.add(new TileItem("Dirt", new LinkedSprite(SpriteType.Item, "dirt"), "dirt", "hole", "water", "lava"));
		items.add(new TileItem("Natural Rock", new LinkedSprite(SpriteType.Item, "stone"), "rock", "hole", "dirt", "sand", "grass", "path", "water", "lava"));

		items.add(new TileItem("Plank", new LinkedSprite(SpriteType.Item, "plank"), "Wood Planks", "hole", "water", "cloud"));
		items.add(new TileItem("Plank Wall", new LinkedSprite(SpriteType.Item, "plank_wall"), "Wood Wall", "Wood Planks"));
		items.add(new TileItem("Wood Door", new LinkedSprite(SpriteType.Item, "wood_door"), "Wood Door", "Wood Planks"));
		items.add(new TileItem("Stone", new LinkedSprite(SpriteType.Item, "stone"), "Stone", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Stone Brick", new LinkedSprite(SpriteType.Item, "stone_brick"), "Stone Bricks", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Ornate Stone", new LinkedSprite(SpriteType.Item, "stone_brick"), "Ornate Stone", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Stone Wall", new LinkedSprite(SpriteType.Item, "stone_wall"), "Stone Wall", "Stone Bricks"));
		items.add(new TileItem("Stone Door", new LinkedSprite(SpriteType.Item, "stone_wall"), "Stone Door", "Stone Bricks"));
		items.add(new TileItem("Raw Obsidian", new LinkedSprite(SpriteType.Item, "obsidian"), "Raw Obsidian", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Obsidian Brick", new LinkedSprite(SpriteType.Item, "obsidian_brick"), "Obsidian", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Ornate Obsidian", new LinkedSprite(SpriteType.Item, "obsidian_brick"), "Ornate Obsidian","hole", "water", "cloud", "lava"));
		items.add(new TileItem("Obsidian Wall", new LinkedSprite(SpriteType.Item, "obsidian_wall"), "Obsidian Wall", "Obsidian"));
		items.add(new TileItem("Obsidian Door", new LinkedSprite(SpriteType.Item, "obsidian_door"), "Obsidian Door", "Obsidian"));

		items.add(new TileItem("Wool", new LinkedSprite(SpriteType.Item, "wool"), "Wool", "hole", "water"));
		items.add(new TileItem("Red Wool", new LinkedSprite(SpriteType.Item, "red_wool"), "Red Wool", "hole", "water"));
		items.add(new TileItem("Blue Wool", new LinkedSprite(SpriteType.Item, "blue_wool"), "Blue Wool", "hole", "water"));
		items.add(new TileItem("Green Wool", new LinkedSprite(SpriteType.Item, "green_wool"), "Green Wool", "hole", "water"));
		items.add(new TileItem("Yellow Wool", new LinkedSprite(SpriteType.Item, "yellow_wool"), "Yellow Wool", "hole", "water"));
		items.add(new TileItem("Black Wool", new LinkedSprite(SpriteType.Item, "black_wool"), "Black Wool", "hole", "water"));

		items.add(new TileItem("Sand", new LinkedSprite(SpriteType.Item, "sand"), "sand", "hole", "water", "lava"));
		items.add(new TileItem("Cactus", new LinkedSprite(SpriteType.Item, "cactus"), "cactus Sapling", "sand"));
		items.add(new TileItem("Bone", new LinkedSprite(SpriteType.Item, "bone"), "tree", "tree Sapling"));
		items.add(new TileItem("Cloud", new LinkedSprite(SpriteType.Item, "cloud"), "cloud", "Infinite Fall"));

		items.add(new TileItem("Wheat Seeds", new LinkedSprite(SpriteType.Item, "seed"), "wheat", "farmland"));
		items.add(new TileItem("Potato", new LinkedSprite(SpriteType.Item, "potato"), "potato", "farmland"));
		items.add(new TileItem("Grass Seeds", new LinkedSprite(SpriteType.Item, "seed"), "grass", "dirt"));

		// Creative mode available tiles:
		items.add(new TileItem("Farmland", SpriteLinker.missingTexture(SpriteType.Item), "farmland", "dirt", "grass", "hole"));
		items.add(new TileItem("Exploded", SpriteLinker.missingTexture(SpriteType.Item), "explode", "dirt", "grass"));
		items.add(new TileItem("hole", SpriteLinker.missingTexture(SpriteType.Item), "hole", "dirt", "grass"));
		items.add(new TileItem("lava", SpriteLinker.missingTexture(SpriteType.Item), "lava", "dirt", "grass", "hole"));
		items.add(new TileItem("path", SpriteLinker.missingTexture(SpriteType.Item), "path", "dirt", "grass", "hole"));
		items.add(new TileItem("water", SpriteLinker.missingTexture(SpriteType.Item), "water", "dirt", "grass", "hole"));

		return items;
	}

	public final String model;
	public final List<String> validTiles;

	protected TileItem(String name, LinkedSprite sprite, String model, String... validTiles) {
		this(name, sprite, 1, model, Arrays.asList(validTiles));
	}
	protected TileItem(String name, LinkedSprite sprite, int count, String model, String... validTiles) {
		this(name, sprite, count, model, Arrays.asList(validTiles));
	}
	protected TileItem(String name, LinkedSprite sprite, int count, String model, List<String> validTiles) {
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
				AdvancementElement.AdvancementTrigger.PlacedTileTrigger.INSTANCE.trigger(
					new AdvancementElement.AdvancementTrigger.PlacedTileTrigger.PlacedTileTriggerConditionHandler.PlacedTileTriggerConditions(
						this, level.getTile(xt, yt), level.getData(xt, yt), xt, yt, level.depth
					));

				Sound.play("craft");

				return super.interactOn(true);
			}
		}

		Logger.tag("TileItem").debug("{} cannot be placed on {}.", model, tile.name);

		String note = "";
		if (model.contains("WALL")) {
			note = Localization.getLocalized("minicraft.notification.invalid_placement", Tiles.getName(validTiles.get(0)));
		}
		else if (model.contains("DOOR")) {
			note = Localization.getLocalized("minicraft.notification.invalid_placement", Tiles.getName(validTiles.get(0)));
		}
		else if ((model.contains("BRICK") || model.contains("PLANK") || model.equals("STONE") || model.contains("ORNATE"))) {
			note = Localization.getLocalized("minicraft.notification.dig_hole");
		}

		if (note.length() > 0) {
			Game.notifications.add(note);
		}

		return super.interactOn(false);
	}

	@Override
	public boolean equals(Item other) {
		return super.equals(other) && model.equals(((TileItem)other).model);
	}

	@Override
	public int hashCode() { return super.hashCode() + model.hashCode(); }

	public @NotNull TileItem copy() {
		return new TileItem(getName(), sprite, count, model, validTiles);
	}
}
