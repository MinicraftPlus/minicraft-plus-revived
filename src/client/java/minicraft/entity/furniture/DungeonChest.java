package minicraft.entity.furniture;

import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class DungeonChest extends Chest {
	private static final LinkedSprite openSprite = new LinkedSprite(SpriteType.Entity, "dungeon_chest");
	private static final LinkedSprite lockSprite = new LinkedSprite(SpriteType.Entity, "white_chest");

	public Random random = new Random();
	private boolean isLocked;

	/**
	 * Creates a custom chest with the name Dungeon Chest.
	 *
	 * @param populateInv Populate the inventory of the DungeonChest using the loot table system.
	 */
	public DungeonChest(boolean populateInv) {
		this(populateInv, false);
	}

	public DungeonChest(boolean populateInv, boolean unlocked) {
		super("Dungeon Chest");
		if (populateInv) {
			populateInv();
		}

		setLocked(!unlocked);
	}

	@Override
	public @NotNull Furniture copy() {
		return new DungeonChest(false, !this.isLocked);
	}

	public boolean use(Player player) {
		if (isLocked) {
			boolean activeKey = player.activeItem != null && player.activeItem.equals(Items.get("Key"));
			boolean invKey = player.getInventory().count(Items.get("key")) > 0;

			if (activeKey || invKey) { // If the player has a key...
				if (activeKey) { // Remove activeItem
					StackableItem key = (StackableItem) player.activeItem;
					key.count--;
				} else { // Remove from inv
					player.getInventory().removeItem(Items.get("key"));
				}

				isLocked = false;
				this.sprite = openSprite; // Set to the unlocked color

				level.add(new SmashParticle(x * 16, y * 16));
				level.add(new TextParticle(Localization.getLocalized("minicraft.text_particales.key_consumed"), x, y, Color.RED));
				level.chestCount--;

				// If this is the last chest.
				if (level.chestCount == 0) {
					level.dropItem(x, y, 5, Items.get("Gold Apple"));
				}

				return super.use(player); // the player unlocked the chest.
			}

			return false; // the chest is locked, and the player has no key.
		} else return super.use(player); // the chest was already unlocked.
	}

	/**
	 * Populate the inventory of the DungeonChest using the loot table system.
	 */
	private void populateInv() {
		// Clear inventory.
		Inventory inv = getInventory();
		inv.clearInv();

		// Populate inventory.
		populateInvRandom("dungeonchest", 0);
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean locked) {
		this.isLocked = locked;

		// auto update sprite
		sprite = locked ? DungeonChest.lockSprite : DungeonChest.openSprite;
	}

	/**
	 * what happens if the player tries to push a Dungeon Chest.
	 */
	@Override
	protected void touchedBy(Entity entity) {
		if (!isLocked) // can only be pushed if unlocked.
			super.touchedBy(entity);
	}

	@Override
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (!isLocked)
			return super.interact(player, item, attackDir);
		return false;
	}
}
