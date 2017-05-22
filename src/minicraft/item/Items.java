package minicraft.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import minicraft.entity.Inventory;
// import java.io.File;
// import java.net.URL;
// import java.net.URI;
// import java.net.URISyntaxException;

public class Items {
	
	/// I've checked -- this is only used for making the creative inventory, and in Load.java.
	/// ...well, that used to be true...  && !name.contains(";)
	
	
	/**
		Ok, so here's the actual big idea:
		
		This class is meant to define all the different kinds of items in minicraft. Item(Type).java might be what maps the different item sprites in the spritesheet to a name, but it doesn't really define anything final. This class has all the items you could possibly have, and every form of them, more or less.
		
		If you want to access one of those items, you do it through this class, by calling get("item name"); casing does not matter.
	*/
	private static ArrayList<Item> items = new ArrayList<Item>();
	
	private static void add(Item i) {
		String name = i.name.toUpperCase();
		//System.out.println("adding " + name);
		items.add(i);
	}
	private static void addAll(ArrayList<Item> items) {
		for(Item i: items) add(i);
	}
	
	static {
		//String path = "/home/chris/Documents/minicraft/minicraft-plus-revived/src/minicraft/item";
		/*try {
			//Items.class.getClassLoader().getResource("minicraft/item/");
			System.out.println("url: " + url);
			path = url.toURI();
		} catch(URISyntaxException ex) {
			ex.printStackTrace();
		}*/
		/*System.out.println("path of classes: " + path);
		File dir = new File(path);
		System.out.println("path exists: " + dir.exists());
		List<String> classes = Arrays.asList((new File(path)).list(new java.io.FilenameFilter() {
			public boolean accept(File file, String name) {
				return true;//!file.isDirectory() && name.endsWith(".java") && !file.getName().equals("Items.java") && !file.getName().equals("Item.java");
			}
		}));
		System.out.println("classes: " + classes);
		classes = new ArrayList<String>(classes);
		for(int i = 0; i < classes.size(); i++) {
			String clazz = classes.get(i);
			System.out.println("found class: " + clazz);
			if(!clazz.contains(".java") || clazz.equals("Item.java") || clazz.equals("Items.java")) {
				classes.remove(i);
				i--;
			}
			else {
				classes.set(i, "minicraft.item."+clazz.replace(".java", ""));
				System.out.println("kept class " + clazz);
			}
		}
		
		//now i have the class package paths, to pass to Class.forName().
		try {
			for(String clazz: classes) {
				System.out.println(clazz);
				addAll((ArrayList<Item>)Class.forName(clazz).getMethod("getAllInstances").invoke(null));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}*/
		addAll(ArmorItem.getAllInstances());
		addAll(BookItem.getAllInstances());
		addAll(StackableItem.getAllInstances());
		addAll(TileItem.getAllInstances());
		addAll(ClothingItem.getAllInstances());
		addAll(FoodItem.getAllInstances());
		addAll(PotionItem.getAllInstances());
		addAll(PowerGloveItem.getAllInstances());
		addAll(BucketItem.getAllInstances());
		addAll(ToolItem.getAllInstances());
		addAll(TorchItem.getAllInstances());
		addAll(FurnitureItem.getAllInstances());
	}
	
	/** fetches an item from the list given it's name. I mean, I would have just used a HashMap... Hey, look! I'm using one! ^v^ */
	public static Item get(String name) {
		name = name.toUpperCase();
		//System.out.println("fetching name: " + name);
		Item i = null;
		for(Item cur: items) {
			if(cur.name.compareToIgnoreCase(name) == 0) {
				i = cur;
				break;
			}
		}
		if(i != null) {
			//System.out.println("got item " + i.name);
			return i.clone();
		} else {
			System.out.println("ITEMS GET: invalid name requested: " + name);
			return get("POWER GLOVE");
		}
		/*if(!name.equals("")) { // name is not nothing
			if(name.contains(";")) { // if has ";" in name for whatever reason...
				name = name.substring(0, name.indexOf(";")); // cut it off, plus anything after it.
			}
			
			for(int i = 0; i < items.size(); i++) { // loop through the items
				if(items.get(i).getName().equals(name)) { // if names match
					return items.get(i);//.clone(); // set the item
				}
			}
		}
		
		return null;
		*/
	}
	
	public static void fillCreativeInv(Inventory inv) {
		for(Item item: items) {
			inv.add(item);
		}
	}
	/*
	/// I am not horribly proud of this method... but it had to be done... for the sake of the Recipes...
	public static Item[] get(String name, int amount) {
		Item[] items = new Item[amount];
		Arrays.fill(items, get(name));
		return items;
	}*/
}
	
