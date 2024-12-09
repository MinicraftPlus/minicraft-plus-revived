package minicraft.item;

import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

/**
 * A general inventory implementation basically without size limit (maximum number of slots).
 */
public class UnlimitedInventory extends Inventory {
	@Override
	public @Nullable Item add(@Nullable Item item) {
		if (item == null) return null;
		// Do not add to inventory if it is a PowerGlove
		if (item instanceof PowerGloveItem) {
			Logging.INVENTORY.warn("Tried to add power glove to inventory. stack trace:", new Exception());
			return null;
		}

		if (item instanceof StackableItem) { // If the item is a item...
			StackableItem toTake = (StackableItem) item; // ...convert it into a StackableItem object.
			for (Item value : items) {
				if (toTake.stacksWith(value)) {
					((StackableItem) value).count += toTake.count;
					return null;
				}
			}

			items.add(toTake);
			return null;
		}

		items.add(item);
		return null;
	}
}
