import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PowerGloveItem;
import minicraft.item.StackableItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Stack;

import static com.sun.org.apache.bcel.internal.Repository.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;


public class inventoryTest {

	private Inventory inventory;
	private ArrayList<Item> allItems;

	@BeforeEach
	 void setUp() {
		inventory = new Inventory();
		allItems = new ArrayList<>();
		for (Item item: Items.getAll()) {
			if (item != null) {
				allItems.add(item);
			}
		}
	}

	@Test
	void testGetMaxSlots() {

		assertEquals(inventory.getMaxSlots(), 27);
	}

	@Test
	void testAdd() {
		int num_returned = inventory.add(allItems.get(1), 3);

		assertEquals(3, inventory.getItems().size());
		assertEquals(3, num_returned);
	}

	@Test
	void testAddItemNull() {
		inventory.add(null);
		assertEquals(0, inventory.getItems().size());
	}

	@Test
	void testAddItemNotNull() {
		inventory.add(allItems.get(1));
		assertEquals(1, inventory.getItems().size());
	}



	@Test
	void testAddItem_PowerGloveFails() {
		for (Item item : allItems) {
			if (item instanceof PowerGloveItem) {
				inventory.add(item, 1);
			}
		}

		assertEquals(0, inventory.getItems().size());
	}

	@Test
	void testAddItem_AllOtherItems_ExceedLimit() {
		int nonPowerItems = 0;
		for (Item item : allItems) {
			if (item instanceof StackableItem) {       // Note: The documentation should specify that it is stackableItems only.
				nonPowerItems += 1;                    //       and it is not mentioning it currently.
				inventory.add(item, 1);
			}
		}

		assertEquals(27, inventory.getItems().size());
		assertTrue(nonPowerItems > 27);
	}

	@Test
	void testAddItem_allOtherItems_WithinLimit() {
		int non_powerItems = 0;
		for (Item item : allItems) {
			if (item instanceof StackableItem) {
				non_powerItems += 1;
				inventory.add(item, 1);
			}

			if (non_powerItems == 12) break;
		}

		assertEquals(12, inventory.getItems().size());
	}

	@Test
	void testClearInventory() {

		int non_powerItems = 0;
		for (Item item : allItems) {
			if (item instanceof StackableItem) {
				non_powerItems += 1;
				inventory.add(item, 1);
			}

			if (non_powerItems == 12) break;
		}

		assertEquals(12, inventory.getItems().size());

		inventory.clearInv();
		assertEquals(0, inventory.getItems().size());

	}

	@Test
	void testRemove() {
		int num_returned = inventory.add(allItems.get(1), 3);

		assertEquals(3, inventory.getItems().size());
		Item removeItem = inventory.remove(0);

		assertEquals(2, inventory.getItems().size());
	}

// 	@Test
// 	void testRemoveItem() {
// 		int non_powerItems = 0;
// 		for (Item item : allItems) {
// 			if (item instanceof StackableItem) {
// 				non_powerItems += 1;
// 				inventory.add(item, 2);
// 			}
//
// 			if (non_powerItems == 12) break;
// 		}
//
// 		assertEquals(24, inventory.getItems().size());
//
// 		for (Item item : allItems) {
// 			if (item instanceof StackableItem) {
// 				inventory.remove(item);
// 			}
// 		}
//
// 	}



}
