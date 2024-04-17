import minicraft.core.Game;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Bed;
import minicraft.entity.furniture.Furniture;
import minicraft.item.FishingData;
import minicraft.item.FoodItem;
import minicraft.item.FurnitureItem;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.saveload.Load;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FurnitureItemTest {

	private Bed bedMock;
	private FurnitureItem testItem;

	@BeforeEach
	public void setUp() {
		bedMock = mock(Bed.class);
		bedMock.name = "mock bed";
		testItem = new FurnitureItem(bedMock);
	}

	@Test
	public void testGetAllInstances() {
		ArrayList<FurnitureItem> furnitureItems = new ArrayList<>();
		ArrayList<Item> allItems = Items.getAll();
		for (Item item : allItems) {
			if (item instanceof FurnitureItem) {
				furnitureItems.add((FurnitureItem) item);
			}
		}
		assertEquals(23,furnitureItems.size());
	}

	@Test
	public void testCanAttack() {
		assertFalse(testItem.canAttack());
	}

	@Test
	public void testInteractOnFurnitureThatCanNotGoOnTile() {
		Tile mockTile = mock(Tile.class);
		when(mockTile.mayPass(any(Level.class),anyInt(),anyInt(),any(Entity.class))).thenReturn(false);
		assertFalse(testItem.interactOn(mockTile,null,0,0,null,null));
	}

	@Test
	public void testInteractOnFurnitureThatCanGoOnTileNoneCreative() {
		Tile mockTile = mock(Tile.class);
		Level mockLevel = mock(Level.class);
		when(mockTile.mayPass(any(Level.class),anyInt(),anyInt(),any(Entity.class))).thenReturn(true);
		try (MockedStatic<Game> utilities = mockStatic(Game.class)) {
			utilities.when(() -> Game.isMode("minicraft.settings.mode.creative"))
				.thenReturn(false);

			assertTrue(testItem.interactOn(mockTile,mockLevel,0,0,null,null));


		}
		verify(mockLevel,times(1)).add(bedMock);
		verify(mockTile,times(1)).mayPass(any(Level.class),anyInt(),anyInt(),any(Entity.class));
		assertEquals(8,bedMock.x);
		assertEquals(8,bedMock.y);
		assertTrue(testItem.placed);
		verify(bedMock,times(0)).copy();
	}

	@Test
	public void testInteractOnFurnitureThatCanGoOnTileCreative() {
		Tile mockTile = mock(Tile.class);
		Level mockLevel = mock(Level.class);
		when(mockTile.mayPass(any(Level.class),anyInt(),anyInt(),any(Entity.class))).thenReturn(true);
		try (MockedStatic<Game> utilities = mockStatic(Game.class)) {
			utilities.when(() -> Game.isMode("minicraft.settings.mode.creative"))
				.thenReturn(true);

			assertTrue(testItem.interactOn(mockTile,mockLevel,0,0,null,null));


		}
		verify(mockLevel,times(1)).add(bedMock);
		verify(mockTile,times(1)).mayPass(any(Level.class),anyInt(),anyInt(),any(Entity.class));
		assertEquals(8,bedMock.x);
		assertEquals(8,bedMock.y);
		assertFalse(testItem.placed);
		verify(bedMock,times(1)).copy();
	}

	@Test
	public void testIsDepletedOnNewFurnitureItem() {
		assertFalse(testItem.isDepleted());
	}

	@Test
	public void testIsDepletedOnInteractedFurniture() {
		Tile mockTile = mock(Tile.class);
		Level mockLevel = mock(Level.class);
		when(mockTile.mayPass(any(Level.class),anyInt(),anyInt(),any(Entity.class))).thenReturn(true);
		try (MockedStatic<Game> utilities = mockStatic(Game.class)) {
			utilities.when(() -> Game.isMode("minicraft.settings.mode.creative"))
				.thenReturn(false);
			assertTrue(testItem.interactOn(mockTile,mockLevel,0,0,null,null));
		}
		assertTrue(testItem.isDepleted());
	}

	@Test
	public void testCopy() {
		FurnitureItem testItem = new FurnitureItem(new Furniture("Furniture",null,null));
		FurnitureItem copiedItem = testItem.copy();
		assertEquals(testItem.getName(),copiedItem.getName());
		assertEquals(testItem.sprite,copiedItem.sprite);
	}



}
