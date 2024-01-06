package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteManager.SpriteLink;
import minicraft.gfx.SpriteManager.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ArmorItem extends StackableItem {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new ArmorItem("Leather Armor",
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "leather_armor").createSpriteLink(), .3f, 1));
		items.add(new ArmorItem("Snake Armor",
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "snake_armor").createSpriteLink(), .4f, 2));
		items.add(new ArmorItem("Iron Armor",
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "iron_armor").createSpriteLink(), .5f, 3));
		items.add(new ArmorItem("Gold Armor",
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "gold_armor").createSpriteLink(), .7f, 4));
		items.add(new ArmorItem("Gem Armor",
			new SpriteLink.SpriteLinkBuilder(SpriteType.Item, "gem_armor").createSpriteLink(), 1f, 5));

		return items;
	}

	private final float armor;
	private final int staminaCost;
	public final int level;

	private ArmorItem(String name, SpriteLink sprite, float health, int level) { this(name, sprite, 1, health, level); }
	private ArmorItem(String name, SpriteLink sprite, int count, float health, int level) {
		super(name, sprite, count);
		this.armor = health;
		this.level = level;
		staminaCost = 9;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;
		if (player.curArmor == null && player.payStamina(staminaCost)) {
			player.curArmor = this; // Set the current armor being worn to this.
			player.armor = (int) (armor * Player.maxArmor); // Armor is how many hits are left
			success = true;
		}

		return super.interactOn(success);
	}

	@Override
	public boolean interactsWithWorld() { return false; }

	public @NotNull ArmorItem copy() {
		return new ArmorItem(getName(), sprite, count, armor, level);
	}
}
