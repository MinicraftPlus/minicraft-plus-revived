package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.AchievementsDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PotionItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		for (PotionType type : PotionType.values())
			items.add(new PotionItem(type));

		return items;
	}

	public PotionType type;

	private PotionItem(PotionType type) {
		this(type, 1);
	}

	private PotionItem(PotionType type, int count) {
		super(type.name, new LinkedSprite(SpriteType.Item, "potion").setColor(type.dispColor), count);
		this.type = type;
	}

	// The return value is used to determine if the potion was used, which means being discarded.
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (type.equals(PotionType.Lava)) {
			AchievementsDisplay.setAchievement("minicraft.achievement.lava", true);
		}
		return interactOn(applyPotion(player, type, true), player);
	}

	protected boolean interactOn(boolean subClassSuccess, Player player) {
		if (subClassSuccess && !Game.isMode("minicraft.settings.mode.creative"))
			player.tryAddToInvOrDrop(Items.get("glass bottle"));
		return super.interactOn(subClassSuccess);
	}

	/// Only ever called to load from file
	public static boolean applyPotion(Player player, PotionType type, int time) {
		boolean result = applyPotion(player, type, time > 0);
		if (result && time > 0) player.addPotionEffect(type, time); // Overrides time
		return result;
	}

	/// Main apply potion method
	public static boolean applyPotion(Player player, PotionType type, boolean addEffect) {
		if (player.getPotionEffects().containsKey(type) != addEffect) { // If hasEffect, and is disabling, or doesn't have effect, and is enabling...
			if (!type.toggleEffect(player, addEffect))
				return false; // Usage failed
		}

		if (addEffect && type.duration > 0) player.potioneffects.put(type, type.duration); // Add it
		else player.potioneffects.remove(type);

		return true;
	}

	@Override
	public boolean equals(Item other) {
		return super.equals(other) && ((PotionItem) other).type == type;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + type.name.hashCode();
	}

	@Override
	public boolean interactsWithWorld() {
		return false;
	}

	public @NotNull PotionItem copy() {
		return new PotionItem(type, count);
	}
}
