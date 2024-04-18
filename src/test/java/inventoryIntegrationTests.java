import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PowerGloveItem;
import minicraft.item.StackableItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



public class inventoryIntegrationTests {

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


	@Test
	public void testAddSlot_PowerGloveItem() {
		PowerGloveItem testItem = new PowerGloveItem();
		assertEquals(0,inventory.add(0,testItem));
	}

	@Test
	public void testAddSlot_Stackable() {
		StackableItem stackable = null;
		for (Item item : allItems) {
			if (item instanceof StackableItem) {
				stackable = (StackableItem) item;
				break;
			}
		}
		if (stackable != null) {
			assertEquals(1, inventory.add(1, stackable));
		}
	}

	@Test
	void testAddSlot_Duplicates() {                 // There is a call on StackableItem's stacksWith
		StackableItem stackable = null;
		for (Item item : allItems) {
			if (item instanceof StackableItem) {
				stackable = (StackableItem) item;
				break;
			}
		}

		if (stackable != null) {
			assertEquals(1, inventory.add(1, stackable));
			assertEquals(0, inventory.add(1, stackable));
		}

		assertEquals(1, inventory.getItems().size());
	}

	@Test
	void testAddStackable_JustBelowLimit() {
		StackableItem stackable = null;
		for (Item item : allItems) {
			if (item instanceof StackableItem) {
				stackable = (StackableItem) item;
				break;
			}
		}

		if (stackable != null) {
			assertEquals(99, inventory.add(stackable, 99));
		}

		assertEquals(1, inventory.getItems().size());
	}

	@Test
	void testAddStackable_AboveLimit() {
		StackableItem stackable = null;
		for (Item item : allItems) {
			if (item instanceof StackableItem) {
				stackable = (StackableItem) item;
				break;
			}
		}

		if (stackable != null) {
			assertEquals(101, inventory.add(stackable, 101));
		}

		assertEquals(2, inventory.getItems().size());
	}

	@Test
	void testAddUnstackable() {
		int addedItems = 0;
		for (Item item : allItems) {
			if (!(item instanceof StackableItem) && !(item instanceof PowerGloveItem)) {
				assertEquals(1, inventory.add(item));
				addedItems += 1;

				if (addedItems == 5) break;
			}
		}

		assertEquals(5, inventory.getItems().size());
	}

	@Test
	void testRemoveFromStack() {
		StackableItem stackable = null;
		for (Item item : allItems) {
			if (item instanceof StackableItem) {
				stackable = (StackableItem) item;
				break;
			}
		}

		if (stackable != null) {
			assertEquals(99, inventory.add(stackable, 99));
		}

		inventory.removeItems(stackable, 12);
		StackableItem currentItem = (StackableItem) inventory.getItems().get(0);
		assertEquals(99 - 12, currentItem.count);
	}

	@Test
	void testRemoveUnstackableItems() {
		ArrayList<Item> listAddedItems = new ArrayList<>();
		int addedItems = 0;
		for (Item item : allItems) {
			if (!(item instanceof StackableItem) && !(item instanceof PowerGloveItem)) {
				assertEquals(1, inventory.add(item));
				listAddedItems.add(item);
				addedItems += 1;

				if (addedItems == 5) break;
			}
		}

		assertEquals(5, inventory.getItems().size());

		inventory.removeItem(listAddedItems.get(0));
		assertEquals(4, inventory.getItems().size());
	}

}
