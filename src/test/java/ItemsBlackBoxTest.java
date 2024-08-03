import minicraft.entity.Arrow;
import minicraft.item.BucketItem;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.UnknownItem;
import minicraft.item.WateringCanItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemsBlackBoxTest {

	/**
	 * check if getAll returns instances of all items
	 * To check this, we can check the size of the returned arrayList
	 * There is a total of 177 possible distinct instances of items
	 */

	@Test
	void testGetAll() {
		ArrayList<Item> allItems = Items.getAll();

		assertEquals(177, allItems.size());
	}

	/**
	 * Get has string item name as input.
	 * Return the item specifies
	 * Partition items: valid item, invalid item
	 */

	@Test
	void testGet() {
		Item item = Items.get("Watering Can");

		assertInstanceOf(WateringCanItem.class, item);
	}

	@Test
	void testGet_InvalidInput() {
		Item item = Items.get("some name");
		assertInstanceOf(UnknownItem.class, item);
	}

	/**
	 * get with inputs: String name, and boolean allowNull
	 * partitions:
	 * name: valid and invalid
	 * allowNull: true, and false
	 * We have to achieve each choice coverage
	 */

	@Test
	void testGet_validName_AllowNull() {
		Item item = Items.get("Watering Can", true);

		assertInstanceOf(WateringCanItem.class, item);
	}

	@Test
	void testGet_InvalidName_AllowNull() {
		Item item = Items.get("klsdsd", true);

		assertInstanceOf(UnknownItem.class, item);
	}

	@Test
	void testGet_InvalidName_NotAllowNull() {
		Item item = Items.get("klsdsd", false);

		assertInstanceOf(UnknownItem.class, item);
	}

	/**
	 * getCount
	 * We will partition the items call by stackable and non_stackable
	 *
	 */

	@Test
	void testGetCount_stackable() {
		Item item = Items.get("Watering Can");
		assertEquals(1, Items.getCount(item));
	}

	@Test
	void testGetCount_non_stackable() {
		Item item = Items.get("Apple");

		assertEquals(1, Items.getCount(item));
	}

	/**
	 * getCreativeMode
	 * must return an instance of creativeModeInventory
	 */

	@Test
	void testGetCreativeModeInventory() {
		Object obj = Items.getCreativeModeInventory();

		assertInstanceOf(Items.CreativeModeInventory.class, obj);
	}




}
