import minicraft.entity.furniture.Furniture;
import minicraft.item.FurnitureItem;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import minicraft.item.StackableItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.calls;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InventoryTest {

	private Inventory testInventory;

	@BeforeEach
	public void setUp() {
		testInventory = new Inventory();
	}
	@Test
	public void testGetMaxSlots() {
		assertEquals(27,testInventory.getMaxSlots());
	}

	@Test
	public void testAddNotNull() {
		Furniture testFurniture = new Furniture("testFurniture",null,null);
		FurnitureItem item = new FurnitureItem(testFurniture);
		assertEquals(1,testInventory.add(item));
	}

	@Test
	public void testAddNull() {
		assertEquals(0,testInventory.add(null));
	}

	@Test
	public void testGetItems() {
		assertEquals(0,testInventory.getItems().size());
		Furniture testFurniture = new Furniture("testFurniture",null,null);
		FurnitureItem item = new FurnitureItem(testFurniture);
		testInventory.add(item);
		List<Item> receivedItems = testInventory.getItems();
		assertEquals(1,receivedItems.size());
		assertEquals(item.getName(),receivedItems.get(0).getName());
	}

	@Test
	public void testClearInv() {
		Furniture testFurniture = new Furniture("testFurniture",null,null);
		FurnitureItem item = new FurnitureItem(testFurniture);
		testInventory.add(item);
		assertEquals(1,testInventory.getItems().size());
		testInventory.clearInv();
		assertEquals(0,testInventory.getItems().size());
	}

	@Test
	public void testInventorySize() {
		assertEquals(0,testInventory.invSize());
		Furniture testFurniture = new Furniture("testFurniture",null,null);
		FurnitureItem item = new FurnitureItem(testFurniture);
		testInventory.add(item);
		assertEquals(1,testInventory.invSize());
	}

	@Test
	public void testGet() {
		Furniture testFurniture = new Furniture("testFurniture",null,null);
		FurnitureItem item = new FurnitureItem(testFurniture);
		testInventory.add(item);
		assertEquals(item.getName(),testInventory.get(0).getName());
	}

	@Test
	public void testMultiAdd() {
		Furniture testFurniture = new Furniture("testFurniture",null,null);
		FurnitureItem item = new FurnitureItem(testFurniture);
		int numAdded = testInventory.add(item,3);
		assertEquals(3,numAdded);
		assertEquals(3,testInventory.invSize());
		List<Item> receivedItems = testInventory.getItems();
		for(Item receivedItem: receivedItems) {
			assertEquals(item.getName(),receivedItem.getName());
		}
	}

	@Test
	public void testSlotAddPowerGloveItem() {
		PowerGloveItem testItem = mock(PowerGloveItem.class);
		assertEquals(0,testInventory.add(0,testItem));
	}

	@Test
	public void testStackableItemNotStacking() {
		Furniture testFurniture = new Furniture("testFurniture",null,null);
		FurnitureItem item = new FurnitureItem(testFurniture);
		testInventory.add(item);
		StackableItem testItem = mock(StackableItem.class);
		testItem.count = 99;
		when(testItem.stacksWith(any(Item.class))).thenReturn(false);
		when(testItem.copy()).thenReturn(testItem);
		assertEquals(99,testInventory.add(1,testItem));
		assertEquals(2,testInventory.invSize());
		verify(testItem,times(1)).stacksWith(any(Item.class));
		verify(testItem,times(1)).copy();
	}

	@Test
	public void testStackableItemStackingCountIsMaxCount() {
		StackableItem testItem = mock(StackableItem.class);
		testItem.maxCount = 0;
		testItem.count = 2;
		when(testItem.stacksWith(any(Item.class))).thenReturn(true);
		when(testItem.copy()).thenReturn(testItem);
		testInventory.add(testItem);
		assertEquals(0,testInventory.add(1,testItem));
		assertEquals(1,testInventory.invSize());
	}

	@Test
	public void testStackableItemStackingCountIsNotMaxCountMatching() {
		StackableItem testItem = mock(StackableItem.class);
		testItem.maxCount = 10;
		testItem.count = 2;
		when(testItem.stacksWith(any(Item.class))).thenReturn(true);
		when(testItem.copy()).thenReturn(testItem);
		testInventory.add(testItem);
		assertEquals(0,testInventory.add(1,testItem));
		assertEquals(1,testInventory.invSize());
	}

	@Test
	public void testStackableItemStackCountIsNotMaxNotMatching() {
		StackableItem testItem = mock(StackableItem.class);
		testItem.maxCount = 10;
		testItem.count = 2;
		when(testItem.stacksWith(any(Item.class))).thenReturn(true);
		when(testItem.copy()).thenReturn(testItem);
		testInventory.add(testItem);
		StackableItem testItem2 = mock(StackableItem.class);
		when(testItem2.stacksWith(any(Item.class))).thenReturn(true);
		when(testItem2.copy()).thenReturn(testItem);
		testItem2.count = 11;
		assertEquals(11,testInventory.add(1,testItem2));
		assertEquals(2,testInventory.invSize());
	}



	@Test
	public void testStackableItemNotStackingMaxItems() {
		Furniture testFurniture = new Furniture("testFurniture",null,null);
		FurnitureItem item = new FurnitureItem(testFurniture);
		testInventory.add(item,testInventory.getMaxSlots());
		StackableItem testItem = mock(StackableItem.class);
		testItem.count = 100;
		when(testItem.stacksWith(any(Item.class))).thenReturn(false);
		when(testItem.copy()).thenReturn(testItem);
		assertEquals(0,testInventory.add(1,testItem));
		assertEquals(testInventory.getMaxSlots(),testInventory.invSize());
	}

	@Test
	public void testStackableItemNotStackingOneLessThanMaxItems() {
		Furniture testFurniture = new Furniture("testFurniture",null,null);
		FurnitureItem item = new FurnitureItem(testFurniture);
		int numSlots = testInventory.getMaxSlots() - 1;
		testInventory.add(item,numSlots);
		StackableItem testItem = mock(StackableItem.class);
		testItem.count = 200;
		when(testItem.stacksWith(any(Item.class))).thenReturn(false);
		when(testItem.copy()).thenReturn(testItem);
		assertEquals(100,testInventory.add(1,testItem));
		assertEquals(testInventory.getMaxSlots(),testInventory.invSize());
	}

	@Test
	public void testItemAddSlot() {
		Item mockItem = mock(Item.class);
		assertEquals(1,testInventory.add(0,mockItem));
		assertEquals(mockItem,testInventory.get(0));
	}

	@Test
	public void testItemAddSlotMaxSize() {
		Item mockItem = mock(Item.class);
		when(mockItem.copy()).thenReturn(mockItem);
		testInventory.add(mockItem,testInventory.getMaxSlots());
		assertEquals(0,testInventory.add(0,mockItem));
	}
}

