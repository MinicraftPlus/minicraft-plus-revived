package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

public class FishingRodItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		for (int i = 0; i < 4; i++) {
			items.add(new FishingRodItem(i));
		}

		return items;
	}

	private int uses = 0; // The more uses, the higher the chance of breaking
	public int level; // The higher the level the lower the chance of breaking

	private Random random = new Random();

	/* These numbers are a bit confusing, so here's an explanation
	 * If you want to know the percent chance of a category (let's say tool, which is third)
	 * You have to subtract 1 + the "tool" number from the number before it (for the first number subtract from 100)*/
	private static final int[][] LEVEL_CHANCES = {
		{ 44, 14, 9, 4 }, // They're in the order "fish", "junk", "tools", "rare"
		{ 24, 14, 9, 4 }, // Iron has very high chance of fish
		{ 59, 49, 9, 4 }, // Gold has very high chance of tools
		{ 79, 69, 59, 4 } // Gem has very high chance of rare items
	};

	private static final String[] LEVEL_NAMES = {
		"Wood",
		"Iron",
		"Gold",
		"Gem"
	};

	public FishingRodItem(int level) {
		super(LEVEL_NAMES[level] + " Fishing Rod", new LinkedSprite(SpriteType.Item,
			LEVEL_NAMES[level].toLowerCase().replace("wood", "wooden") + "_fishing_rod"));
		this.level = level;
	}

	public static int getChance(int idx, int level) {
		return LEVEL_CHANCES[level][idx];
	}

	@Override
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (tile == Tiles.get("water") && !player.isSwimming()) { // Make sure not to use it if swimming
			uses++;
			player.isFishing = true;
			player.fishingLevel = this.level;
			return true;
		}

		return false;
	}

	@Override
	public boolean canAttack() {
		return false;
	}

	@Override
	public boolean isDepleted() {
		if (random.nextInt(100) > 120 - uses + level * 6) { // Breaking is random, the lower the level, and the more times you use it, the higher the chance
			Game.notifications.add("Your Fishing rod broke.");
			return true;
		}
		return false;
	}

	@Override
	public @NotNull Item copy() {
		FishingRodItem item = new FishingRodItem(this.level);
		item.uses = this.uses;
		return item;
	}
}
