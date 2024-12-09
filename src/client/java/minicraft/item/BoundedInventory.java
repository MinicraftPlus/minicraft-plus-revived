package minicraft.item;

import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

public abstract class BoundedInventory extends Inventory {
	/**
	 * Gets the current maximum capacity of inventory.
	 * This value is capable to inventory expanding (e.g. upgrades), but not changing by other
	 * conditions such as the contents.
	 * @return current value of maximum capacity of general slots
	 */
	public abstract int getMaxSlots();

	@Override
	public @Nullable Item add(@Nullable Item item) {
		if (item == null) return null;
		// Do not add to inventory if it is a PowerGlove
		if (item instanceof PowerGloveItem) {
			Logging.INVENTORY.warn("Tried to add power glove to inventory. stack trace:", new Exception());
			return null;
		}

		int maxItem = getMaxSlots();
		if (item instanceof StackableItem) { // If the item is a item...
			StackableItem toTake = (StackableItem) item; // ...convert it into a StackableItem object.
			for (Item value : items) {
				if (toTake.stacksWith(value)) {
					StackableItem stack = (StackableItem) value;
					if (stack.count < stack.maxCount) {
						int r = stack.maxCount - stack.count;
						if (r >= toTake.count) {
							// Matching implies that the other item is stackable, too.
							stack.count += toTake.count;
							return null;
						} else {
							toTake.count -= r;
							stack.count += r;
						}
					}
				}
			}

			if (items.size() < maxItem) {
				while (toTake.count > 0) {
					if (items.size() == maxItem) return toTake;
					StackableItem adding = toTake.copy();
					adding.count = Math.min(toTake.count, toTake.maxCount);
					items.add(adding); // Add the item to the items list
					toTake.count -= adding.count;
				}
				return null;
			} else {
				return toTake;
			}
		}

		if (items.size() < maxItem) {
			items.add(item); // Add the item to the items list
			return null;
		} else {
			return item;
		}
	}
}
