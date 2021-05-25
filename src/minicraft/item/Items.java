package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Items {
	
	// I've checked -- this is only used for making the creative inventory, and in Load.java.
	// ...well, that used to be true...
	
	/**
		Ok, so here's the actual big idea:
		
		This class is meant to define all the different kinds of items in minicraft. Item(Type).java might be what maps the different item sprites in the spritesheet to a name, but it doesn't really define anything final. This class has all the items you could possibly have, and every form of them, more or less.
		
		If you want to access one of those items, you do it through this class, by calling get("item name"); casing does not matter.
	*/
	private static ArrayList<Item> items = new ArrayList<>();
	
	private static void add(Item i) {
		items.add(i);
	}
	private static void addAll(ArrayList<Item> items) {
		for(Item i: items) add(i);
	}
	
	static {
		add(new PowerGloveItem());
		addAll(FurnitureItem.getAllInstances());
		addAll(TorchItem.getAllInstances());
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
	}
	
	/** fetches an item from the list given its name. */
	@NotNull
	public static Item get(String name) {
		Item i = get(name, false);
		if (i == null) return new UnknownItem("NULL"); // technically shouldn't ever happen
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
				data = Integer.parseInt(name.substring(name.indexOf("_")+1));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			name = name.substring(0, name.indexOf("_"));
		}
		else if (name.contains(";")) {
			hadUnderscore = true;
			try {
				data = Integer.parseInt(name.substring(name.indexOf(";")+1));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			name = name.substring(0, name.indexOf(";"));
		}
		
		if (name.equalsIgnoreCase("NULL")) {
			if (allowNull) return null;
			else {
				System.err.println("WARNING: Items.get passed argument \"null\" when null is not allowed; returning UnknownItem. StackTrace:");
				Thread.dumpStack();
				return new UnknownItem("NULL");
			}
		}
		
		if (name.equals("UNKNOWN"))
			return new UnknownItem("BLANK");
		
		Item i = null;
		for (Item cur: items) {
			if (cur.getName().compareToIgnoreCase(name) == 0) {
				i = cur;
				break;
			}
		}
		
		if (i != null) {
			i = i.clone();
			if (i instanceof StackableItem)
				((StackableItem)i).count = data;
			if (i instanceof ToolItem && hadUnderscore)
				((ToolItem)i).dur = data;
			return i;
		} else {
			System.out.println(Network.onlinePrefix() + "ITEMS GET: Invalid name requested: \"" + name + "\"");
			Thread.dumpStack();
			return new UnknownItem(name);
		}
	}
	
	public static Item arrowItem = get("arrow");
	
	public static void fillCreativeInv(Inventory inv) { fillCreativeInv(inv, true); }
	public static void fillCreativeInv(Inventory inv, boolean addAll) {
		for (Item item: items) {
			if (item instanceof PowerGloveItem) continue;
			if (addAll || inv.count(item) == 0)
				inv.add(item.clone());
		}
	}
}
	
