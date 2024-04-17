import minicraft.core.Game;
import minicraft.entity.furniture.Furniture;
import minicraft.item.ClothingItem;
import minicraft.item.FurnitureItem;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PowerGloveItem;
import minicraft.item.StackableItem;
import minicraft.item.UnknownItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.calls;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InventoryTest {

	private class TestRandom extends Random {
		private int[] values;
		private int index = 0;

		public TestRandom(int... values) {
			this.values = values;
		}

		@Override
		public int nextInt(int bound) {
			return values[index++ % values.length];
		}
	}

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

	@Test
	public void testRemoveItemNoneStackingItems() {
		Item nonStacking = mock(Item.class);
		testInventory.add(nonStacking);
		Item nonStackingMock = mock(Item.class);
		when(nonStacking.equals(nonStackingMock)).thenReturn(true);
		when(nonStackingMock.copy()).thenReturn(nonStackingMock);
		testInventory.removeItem(nonStackingMock);
		assertEquals(0,testInventory.invSize());
		verify(nonStacking,times(1)).equals(nonStackingMock);
		verify(nonStackingMock,times(1)).copy();
	}

	@Test
	public void testRemoveItemNoneStackingItemManyRemove() {
		Item nonStacking = mock(Item.class);
		Item nonStacking2 = mock(Item.class);
		testInventory.add(nonStacking);
		testInventory.add(nonStacking2);
		Item nonStackingMock = mock(Item.class);
		when(nonStacking.equals(nonStackingMock)).thenReturn(true);
		when(nonStacking2.equals(nonStackingMock)).thenReturn(true);
		testInventory.removeItems(nonStackingMock,2);
		assertEquals(0,testInventory.invSize()); // When there's only one item left doesn't remove
		verify(nonStacking,times(1)).equals(nonStackingMock);
		verify(nonStacking2,times(1)).equals(nonStackingMock);
	}

	@Test
	public void testRemoveItemNoneStackingNoneExisting() {
		Item nonStacking = mock(Item.class);
		testInventory.add(nonStacking);
		Item nonStackingMock = mock(Item.class);
		when(nonStacking.equals(nonStackingMock)).thenReturn(false);
		testInventory.removeItems(nonStackingMock,1);
		assertEquals(1,testInventory.invSize());
		verify(nonStacking,times(1)).equals(nonStackingMock);
	}

	@Test
	public void testRemoveItemWithStackingItem() {
		Item nonStacking = mock(Item.class);
		StackableItem inventoryStackingMockStacks = mock(StackableItem.class);
		StackableItem inventoryStackingMockStacksCopy = mock(StackableItem.class);
		StackableItem inventoryStackingMockStacks2 = mock(StackableItem.class);
		StackableItem inventoryStackingMockStacks2Copy = mock(StackableItem.class);
		StackableItem inventoryStackingMockNoStacks = mock(StackableItem.class);
		when(inventoryStackingMockNoStacks.copy()).thenReturn(inventoryStackingMockNoStacks);
		when(inventoryStackingMockStacks.copy()).thenReturn(inventoryStackingMockStacksCopy);
		when(inventoryStackingMockStacks2.copy()).thenReturn(inventoryStackingMockStacks2Copy);
		inventoryStackingMockStacks.count = 1;
		inventoryStackingMockStacksCopy.count = 1;
		inventoryStackingMockStacks2.count = 2;
		inventoryStackingMockStacks2Copy.count = 2;
		inventoryStackingMockNoStacks.count = 100;
		StackableItem stackingMock = mock(StackableItem.class);
		when(stackingMock.copy()).thenReturn(stackingMock);
		when(inventoryStackingMockStacksCopy.stacksWith(stackingMock)).thenReturn(true);
		when(inventoryStackingMockStacks2Copy.stacksWith(stackingMock)).thenReturn(true);
		when(inventoryStackingMockNoStacks.stacksWith(stackingMock)).thenReturn(false);
		testInventory.add(nonStacking);
		testInventory.add(inventoryStackingMockNoStacks);
		testInventory.add(inventoryStackingMockStacks);
		testInventory.add(inventoryStackingMockStacks2);
		stackingMock.count = 3;
		testInventory.removeItem(stackingMock);
		assertEquals(2,testInventory.invSize());
	}

	@Test
	public void testCountOnNullItem() {
		assertEquals(0,testInventory.count(null));
	}

	@Test
	public void testCountOnNonStackableNonEqualItem() {
		Item mockItem = mock(Item.class);
		Item mockCountItem = mock(Item.class);
		when(mockItem.equals(mockCountItem)).thenReturn(false);
		testInventory.add(mockItem);
		assertEquals(0,testInventory.count(mockCountItem));
	}

	@Test
	public void testCountOnNonStackableEqualItem() {
		Item mockItem = mock(Item.class);
		Item mockCountItem = mock(Item.class);
		when(mockItem.equals(mockCountItem)).thenReturn(true);
		testInventory.add(mockItem);
		assertEquals(1,testInventory.count(mockCountItem));
	}

	@Test
	public void testCountOnStackableEqualItem() {
		StackableItem mockItem = mock(StackableItem.class);
		StackableItem mockItemCopy = mock(StackableItem.class);
		StackableItem mockCountItem = mock(StackableItem.class);
		when(mockItem.copy()).thenReturn(mockItemCopy);
		when(mockItemCopy.stacksWith(mockCountItem)).thenReturn(true);
		mockItem.count = 10;
		mockItemCopy.count = 10;
		testInventory.add(mockItem);
		assertEquals(10,testInventory.count(mockCountItem));
	}

	@Test
	public void testGetItemDataOnEmptyList() {
		assertEquals("",testInventory.getItemData());
	}

	@Test
	public void testGetItemDataOnOneItemList() {
		Item mockItem = mock(Item.class);
		String mockItemData = "I'm a mock";
		when(mockItem.getData()).thenReturn(mockItemData);
		testInventory.add(mockItem);
		assertEquals(mockItemData,testInventory.getItemData());
	}

	@Test
	public void testGetItemDataOnTwoItemList() {
		Item mockItem = mock(Item.class);
		Item mockItem2 = mock(Item.class);
		String mockItemData = "I'm a mock";
		when(mockItem.getData()).thenReturn(mockItemData);
		when(mockItem2.getData()).thenReturn(mockItemData);
		testInventory.add(mockItem);
		testInventory.add(mockItem2);
		assertEquals(mockItemData + ":" + mockItemData,testInventory.getItemData());
	}

	@Test
	public void testUpdateInvOnEmptyList() {
		Item mockItem = mock(Item.class);
		testInventory.add(mockItem);
		assertEquals(1,testInventory.invSize());
		testInventory.updateInv("");
		assertEquals(0,testInventory.invSize());
	}

	@Test
	public void testUpdateInvOnInvalidList() {
		Item mockItem = mock(Item.class);
		testInventory.add(mockItem);
		UnknownItem mockUnknown = mock(UnknownItem.class);
		UnknownItem mockUnknownCopy = mock(UnknownItem.class);
		when(mockUnknown.copy()).thenReturn(mockUnknownCopy);
		mockUnknown.count = 1;
		mockUnknownCopy.count = 1;
		try (MockedStatic<Items> utilities = mockStatic(Items.class)) {
			utilities.when(() -> Items.get(anyString()))
				.thenReturn(mockUnknown);
			testInventory.updateInv("I'm invalid");
		}
		assertEquals(1,testInventory.invSize());
		assertEquals(mockUnknownCopy,testInventory.get(0));
	}

	@Test
	public void testUpdateInvOnValidList() {
		Item mockItem = mock(Item.class);
		testInventory.add(mockItem);
		StackableItem genItem = mock(StackableItem.class);
		StackableItem genItemCopy = mock(StackableItem.class);
		when(genItem.copy()).thenReturn(genItemCopy);
		genItem.count = 1;
		genItemCopy.count = 1;
		ClothingItem clothingItemMock = mock(ClothingItem.class);
		ClothingItem clothingItemMock2 = mock(ClothingItem.class);
		when(clothingItemMock.copy()).thenReturn(clothingItemMock2);
		clothingItemMock.count = 1;
		clothingItemMock2.count = 1;
		try (MockedStatic<Items> utilities = mockStatic(Items.class)) {
			utilities.when(() -> Items.get("Stacking Item"))
				.thenReturn(genItem);
			utilities.when(() -> Items.get("Clothing Item"))
				.thenReturn(clothingItemMock);
			testInventory.updateInv("Stacking Item:Clothing Item");
		}
		assertEquals(2,testInventory.invSize());
		assertEquals(genItemCopy,testInventory.get(0));
		assertEquals(clothingItemMock2,testInventory.get(1));
	}

	@Test
	public void testTryAddAllOrNothingAddAll() {
		TestRandom random = new TestRandom(0,1,2,3);
		Item testItem = mock(Item.class);
		when(testItem.copy()).thenReturn(testItem);
		testInventory.tryAdd(random,5,testItem,5,true);
		assertEquals(5,testInventory.invSize());
	}

	@Test
	public void testTryAddAllOrNothingAddNone() {
		TestRandom random = new TestRandom(1,2,3);
		Item testItem = mock(Item.class);
		when(testItem.copy()).thenReturn(testItem);
		testInventory.tryAdd(random,5,testItem,5,true);
		assertEquals(0,testInventory.invSize());
	}

	@Test
	public void testTryAddNotAllOrNothingAddSome() {
		TestRandom random = new TestRandom(0,1,2,3);
		Item testItem = mock(Item.class);
		when(testItem.copy()).thenReturn(testItem);
		testInventory.tryAdd(random,5,testItem,4,false);
		assertEquals(1,testInventory.invSize());
	}

	@Test
	public void testTryAddNullItem() {
		TestRandom random = new TestRandom(0,1,2,3);
		testInventory.tryAdd(random,5,null,4);
		assertEquals(0,testInventory.invSize());
	}

	@Test
	public void testTryAddNoneStackableItem() {
		TestRandom random = new TestRandom(0,1,2,3);
		Item testItem = mock(Item.class);
		when(testItem.copy()).thenReturn(testItem);
		testInventory.tryAdd(random,5,testItem,4);
		assertEquals(1,testInventory.invSize());
	}


	@Test
	public void testTryAddStackableItem() {
		TestRandom random = new TestRandom(0,1,2,3);
		StackableItem testItem = mock(StackableItem.class);
		StackableItem testItemCopy = mock(StackableItem.class);

		when(testItem.copy()).thenAnswer((Answer<StackableItem>) invocation -> {
            testItemCopy.count = testItem.count;
            return testItemCopy;
        });

		when(testItemCopy.copy()).thenAnswer((Answer<StackableItem>) invocation -> {
            StackableItem copy = mock(StackableItem.class);
            copy.count = testItem.count;
            return copy;
        });
		testItem.count = 1;
		testItemCopy.count = 1;
		testInventory.tryAdd(random,5,testItem,4);
		assertEquals(1,testInventory.invSize());
		StackableItem storedItem = (StackableItem) testInventory.get(0);
		assertEquals(4,storedItem.count);
	}







}

