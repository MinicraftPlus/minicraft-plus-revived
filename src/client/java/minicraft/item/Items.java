package minicraft.item;

import minicraft.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class Items {

	// I've checked -- this is only used for making the creative inventory, and in Load.java.
	// ...well, that used to be true...

	/**
	 * Ok, so here's the actual big idea:
	 * <p>
	 * This class is meant to define all the different kinds of items in minicraft. Item(Type).java might be what maps the different item sprites in the spritesheet to a name, but it doesn't really define anything final. This class has all the items you could possibly have, and every form of them, more or less.
	 * <p>
	 * If you want to access one of those items, you do it through this class, by calling get("item name"); casing does not matter.
	 */
	private static final ArrayList<Item> items = new ArrayList<>();

	private static void add(Item i) {
		items.add(i);
	}

	private static void addAll(ArrayList<Item> items) {
		for (Item i : items) add(i);
	}

	static {
		add(new PowerGloveItem());
		addAll(FurnitureItem.getAllInstances());
		addAll(BucketItem.getAllInstances());
		addAll(BookItem.getAllInstances());
		addAll(TileItem.getAllInstances());
		addAll(ToolItem.getAllInstances());
		addAll(FoodItem.getAllInstances());
		addAll(StackableItem.getAllInstances());
		addAll(ClothingItem.getAllInstances());
		addAll(ArmorItem.getAllInstances());
		addAll(PotionItem.getAllInstances());
		addAll(FishingRodItem.getAllInstances());
		addAll(SummonItem.getAllInstances());
		addAll(HeartItem.getAllInstances());
		addAll(WateringCanItem.getAllInstances());
		addAll(DyeItem.getAllInstances());
		addAll(WoolItem.getAllInstances());
	}

	public static ArrayList<Item> getAll() {
		return new ArrayList<>(items);
	}

	/**
	 * fetches an item from the list given its name.
	 */
	@NotNull
	public static Item get(String name) {
		Item i = get(name, false);
		if (i == null) return new UnknownItem("NULL"); // Technically shouldn't ever happen
		return i;
	}

	@Nullable
	public static Item get(String name, boolean allowNull) {
		name = name.toUpperCase();
		//System.out.println("fetching name: \"" + name + "\"");
		int data = 1;
		boolean hadUnderscore = false;
		if (name.contains("_")) {
			hadUnderscore = true;
			try {
				data = Integer.parseInt(name.substring(name.indexOf("_") + 1));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			name = name.substring(0, name.indexOf("_"));
		} else if (name.contains(";")) {
			hadUnderscore = true;
			try {
				data = Integer.parseInt(name.substring(name.indexOf(";") + 1));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			name = name.substring(0, name.indexOf(";"));
		}

		if (name.equalsIgnoreCase("NULL")) {
			if (allowNull) return null;
			else {
				Logging.ITEMS.warn("Items.get passed argument \"null\" when null is not allowed; returning UnknownItem.");
				return new UnknownItem("NULL");
			}
		}

		if (name.equals("UNKNOWN"))
			return new UnknownItem("BLANK");

		Item i = null;
		for (Item cur : items) {
			if (cur.getName().equalsIgnoreCase(name)) {
				i = cur;
				break;
			}
		}

		if (i != null) {
			i = i.copy();
			if (i instanceof StackableItem)
				((StackableItem) i).count = data;
			if (i instanceof ToolItem && hadUnderscore)
				((ToolItem) i).dur = data;
			if (i instanceof WateringCanItem)
				((WateringCanItem) i).content = data;
			return i;
		} else {
			Logging.ITEMS.error("Requested invalid item with name: '{}'", name);
			return new UnknownItem(name);
		}
	}

	public static Item arrowItem = get("arrow");

	public static int getCount(Item item) {
		if (item instanceof StackableItem) {
			return ((StackableItem) item).count;
		} else if (item != null) {
			return 1;
		} else {
			return 0;
		}
	}

	public static CreativeModeInventory getCreativeModeInventory() {
		return new CreativeModeInventory();
	}

	public static class CreativeModeInventory extends Inventory {
		CreativeModeInventory() {
			unlimited = true;
			items.forEach(i -> {
				if (!(i instanceof PowerGloveItem)) add(i.copy());
			});
		}
	}
}

