package minicraft.entity.furniture;

import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.UnlimitedInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class RewardChest extends Chest {
	/**
	 * Creates a custom chest with the name Rewards
	 */
	public RewardChest(@Nullable Collection<@NotNull Item> items) {
		super(new RewardChestInventory(), "Rewards", defaultSprite);
		if (items != null) items.forEach(((RewardChestInventory) inventory)::add0);
	}

	private final static class RewardChestInventory extends UnlimitedInventory {
		@Override
		public @Nullable Item add(@Nullable Item item) {
			return item; // Items cannot be added by player
		}

		private void add0(@NotNull Item item) { // But internally
			super.add(item);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (inventory.invSize() == 0) {
			remove();
		}
	}
}
