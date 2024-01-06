package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteManager;
import minicraft.gfx.SpriteManager.SpriteLink;
import minicraft.gfx.SpriteManager.SpriteType;
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
		items.add(new TileItem("Flower", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "white_flower").createSpriteLink(), "flower", "grass"));
		items.add(new TileItem("Acorn", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "acorn").createSpriteLink(), "tree Sapling", "grass"));
		items.add(new TileItem("Dirt", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "dirt").createSpriteLink(), "dirt", "hole", "water", "lava"));
		items.add(new TileItem("Natural Rock", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "stone").createSpriteLink(), "rock", "hole", "dirt", "sand", "grass", "path", "water", "lava"));

		items.add(new TileItem("Plank", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "plank").createSpriteLink(), "Wood Planks", "hole", "water", "cloud"));
		items.add(new TileItem("Plank Wall", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "plank_wall").createSpriteLink(), "Wood Wall", "Wood Planks"));
		items.add(new TileItem("Wood Door", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "wood_door").createSpriteLink(), "Wood Door", "Wood Planks"));
		items.add(new TileItem("Stone", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "stone").createSpriteLink(), "Stone", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Stone Brick", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "stone_brick").createSpriteLink(), "Stone Bricks", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Ornate Stone", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "stone_brick").createSpriteLink(), "Ornate Stone", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Stone Wall", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "stone_wall").createSpriteLink(), "Stone Wall", "Stone Bricks"));
		items.add(new TileItem("Stone Door", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "stone_wall").createSpriteLink(), "Stone Door", "Stone Bricks"));
		items.add(new TileItem("Raw Obsidian", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "obsidian").createSpriteLink(), "Raw Obsidian", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Obsidian Brick", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "obsidian_brick").createSpriteLink(), "Obsidian", "hole", "water", "cloud", "lava"));
		items.add(new TileItem("Ornate Obsidian", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "obsidian_brick").createSpriteLink(), "Ornate Obsidian","hole", "water", "cloud", "lava"));
		items.add(new TileItem("Obsidian Wall", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "obsidian_wall").createSpriteLink(), "Obsidian Wall", "Obsidian"));
		items.add(new TileItem("Obsidian Door", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "obsidian_door").createSpriteLink(), "Obsidian Door", "Obsidian"));

		items.add(new TileItem("Wool", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "wool").createSpriteLink(), "Wool", "hole", "water"));
		items.add(new TileItem("Red Wool", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "red_wool").createSpriteLink(), "Red Wool", "hole", "water"));
		items.add(new TileItem("Blue Wool", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "blue_wool").createSpriteLink(), "Blue Wool", "hole", "water"));
		items.add(new TileItem("Green Wool", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "green_wool").createSpriteLink(), "Green Wool", "hole", "water"));
		items.add(new TileItem("Yellow Wool", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "yellow_wool").createSpriteLink(), "Yellow Wool", "hole", "water"));
		items.add(new TileItem("Black Wool", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "black_wool").createSpriteLink(), "Black Wool", "hole", "water"));

		items.add(new TileItem("Sand", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "sand").createSpriteLink(), "sand", "hole", "water", "lava"));
		items.add(new TileItem("Cactus", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "cactus").createSpriteLink(), "cactus Sapling", "sand"));
		items.add(new TileItem("Bone", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "bone").createSpriteLink(), "tree", "tree Sapling"));
		items.add(new TileItem("Cloud", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "cloud").createSpriteLink(), "cloud", "Infinite Fall"));

		items.add(new TileItem("Wheat Seeds", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "seed").createSpriteLink(), "wheat", "farmland"));
		items.add(new TileItem("Potato", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "potato").createSpriteLink(), "potato", "farmland"));
		items.add(new TileItem("Grass Seeds", new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "seed").createSpriteLink(), "grass", "dirt"));

		// Creative mode available tiles:
		items.add(new TileItem("Farmland", SpriteManager.missingTexture(SpriteType.Item), "farmland", "dirt", "grass", "hole"));
		items.add(new TileItem("Exploded", SpriteManager.missingTexture(SpriteType.Item), "explode", "dirt", "grass"));
		items.add(new TileItem("hole", SpriteManager.missingTexture(SpriteType.Item), "hole", "dirt", "grass"));
		items.add(new TileItem("lava", SpriteManager.missingTexture(SpriteType.Item), "lava", "dirt", "grass", "hole"));
		items.add(new TileItem("path", SpriteManager.missingTexture(SpriteType.Item), "path", "dirt", "grass", "hole"));
		items.add(new TileItem("water", SpriteManager.missingTexture(SpriteType.Item), "water", "dirt", "grass", "hole"));

		return items;
	}

	public final String model;
	public final List<String> validTiles;

	protected TileItem(String name, SpriteLink sprite, String model, String... validTiles) {
		this(name, sprite, 1, model, Arrays.asList(validTiles));
	}
	protected TileItem(String name, SpriteLink sprite, int count, String model, String... validTiles) {
		this(name, sprite, count, model, Arrays.asList(validTiles));
	}
	protected TileItem(String name, SpriteLink sprite, int count, String model, List<String> validTiles) {
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
