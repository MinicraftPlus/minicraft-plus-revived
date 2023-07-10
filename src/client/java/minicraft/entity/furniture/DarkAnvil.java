package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import minicraft.screen.DarkAnvilDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class DarkAnvil extends Furniture {
	private static final SpriteLinker.LinkedSprite sprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "dark_anvil");
	private static final SpriteLinker.LinkedSprite itemSprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "dark_anvil");

	public static final int MAX_ENERGY = 8;

	private int energy = 0;

	private StackableItem fuelStore = null;

	public DarkAnvil() {
		super("Dark Anvil", sprite, itemSprite);
	}
	public DarkAnvil(int store, int energy) {
		this();
		this.energy = energy;
		this.fuelStore = (StackableItem) Items.get("Cloud Ore");
		fuelStore.count = store;
	}

	@Range(from = 0, to = MAX_ENERGY)
	public int getEnergy() {
		return energy;
	}

	/** Try to refill energy using cloud ore in storage.
	 * @return {@code true} if there was no energy left and successfully refilled */
	public boolean tryRefillEnergy() {
		if (energy == 0 && fuelStore != null) {
			boolean success = false;
			if (fuelStore.count > 0) {
				fuelStore.count--;
				energy = MAX_ENERGY;
				success = true;
			}

			if (fuelStore.count == 0) fuelStore = null;
			return success;
		}

		return false;
	}

	/** @see minicraft.screen.entry.SlotEntry.SynchronizedSlotEntry#withdrawSlot(boolean, int) */
	public @Nullable StackableItem withdrawStore(boolean whole, @Range(from = 0, to = Integer.MAX_VALUE) int maxStackSize) {
		return withdrawStore(whole ? maxStackSize : 1);
	}
	/** @see minicraft.screen.entry.SlotEntry.SynchronizedSlotEntry#withdrawSlot(int) */
	public @Nullable StackableItem withdrawStore(int count) {
		if (fuelStore == null) return null;
		StackableItem item;
		if (count >= fuelStore.count) {
			item = fuelStore;
			fuelStore = null;
		} else {
			item = fuelStore.copy();
			item.count = Math.min(count, fuelStore.count);
			fuelStore.count -= item.count;
			if (fuelStore.isDepleted()) fuelStore = null;
		}
		return item;
	}

	@Range(from = 0, to = Integer.MAX_VALUE)
	public int getStore() {
		if (fuelStore == null) return 0;
		return fuelStore.count;
	}

	/** @return {@code true} if 1 energy point is successfully deducted */
	public boolean deduceEnergy() {
		if (energy > 0) {
			energy--;
			return true;
		}

		return false;
	}

	/** @return {@code true} if the provided {@code item} is not depleted
	 * @see minicraft.screen.entry.SlotEntry.SynchronizedSlotEntry#depositSlot(Item) */
	public boolean depositStore(StackableItem item) {
		if (item.getName().equalsIgnoreCase("Cloud Ore")) {
			if (!item.isDepleted()) {
				if (fuelStore == null) {
					fuelStore = item.copy();
					fuelStore.count = Math.min(item.count, fuelStore.maxCount);
					item.count -= fuelStore.count;
				} else {
					int toAdd = Math.min(item.count, fuelStore.maxCount - fuelStore.count);
					fuelStore.count += toAdd;
					item.count -= toAdd;
				}
			}

			return !item.isDepleted();
		}

		return true;
	}

	@Override
	public boolean use(Player player) {
		Game.setDisplay(new DarkAnvilDisplay(this, player));
		return true;
	}

	@Override
	public @NotNull Furniture copy() {
		return new DarkAnvil();
	}
}
