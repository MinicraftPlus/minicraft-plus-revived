import minicraft.core.io.Settings;
import minicraft.entity.mob.Player;
import minicraft.item.ArmorItem;
import minicraft.item.BucketItem;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BucketItemTest {
	private static ArrayList<BucketItem> bucketItems = new ArrayList<>();

	@BeforeAll
	public static void setUp() {
		Tiles.initTileList();
		ArrayList<Item> allItems = Items.getAll();
		for (Item item : allItems) {
			if (item instanceof BucketItem) {
				bucketItems.add((BucketItem) item);
			}
		}

	}

	@Test
	public void testGetAllInstances() {
		assertEquals(3,bucketItems.size());
	}

	@Test
	public void testFillEnumEmpty() {
		BucketItem.Fill fill = BucketItem.Fill.Empty;
		assertEquals(Tiles.get("hole"), fill.contained);
		assertEquals(2, fill.offset);
	}

	@Test
	public void testFillEnumWater() {
		BucketItem.Fill fill = BucketItem.Fill.Water;
		assertEquals(Tiles.get("water"), fill.contained);
		assertEquals(0, fill.offset);
	}

	@Test
	public void testFillEnumLava() {
		BucketItem.Fill fill = BucketItem.Fill.Lava;
		assertEquals(Tiles.get("lava"), fill.contained);
		assertEquals(1, fill.offset);
	}

	@Test
	public void testInteractOnForeignTile() {
		Tile mockTile = mock(Tile.class);
		assertFalse(bucketItems.get(0).interactOn(mockTile,null,0,0,null,null));
	}

	//empty - hole - 3
	//water - water - 6
	//lava - lava - 17
	@Test
	public void testInteractOnBaseEmptyFilling() {
		Tile mockEmptyTile = mock(Tile.class);
		mockEmptyTile.id = (short) 3;
		Level mockLevel = mock(Level.class);
		Player mockPlayer = mock(Player.class);
		BucketItem emptyBucket = bucketItems.get(0);
		assertTrue(emptyBucket.interactOn(mockEmptyTile,mockLevel,0,0,mockPlayer,null));
		verify(mockLevel,times(1)).setTile(0,0,Tiles.get("hole"));
		assertEquals(1,emptyBucket.count);
		assertTrue(emptyBucket.equals(mockPlayer.activeItem));
	}

	@Test
	public void testInteractOnEmptyFillingCreativeMode() {
		String oldMode = (String)Settings.get("mode");
		Settings.set("mode","minicraft.settings.mode.creative");
		Tile mockEmptyTile = mock(Tile.class);
		mockEmptyTile.id = (short) 3;
		Level mockLevel = mock(Level.class);
		Player mockPlayer = mock(Player.class);
		Item mockItem = mock(Item.class);
		mockPlayer.activeItem = mockItem;
		assertTrue(bucketItems.get(0).interactOn(mockEmptyTile,mockLevel,0,0,mockPlayer,null));
		verify(mockLevel,times(1)).setTile(0,0,Tiles.get("hole"));
		assertEquals(mockItem,mockPlayer.activeItem);
		Settings.set("mode",oldMode); //cleanup
	}

	@Test
	public void testInteractOn0CountEmptyFill() {
		Tile mockEmptyTile = mock(Tile.class);
		mockEmptyTile.id = (short) 3;
		Level mockLevel = mock(Level.class);
		Player mockPlayer = mock(Player.class);
		BucketItem waterBucket = bucketItems.get(1);
		waterBucket.count = 0;
		assertTrue(waterBucket.interactOn(mockEmptyTile,mockLevel,0,0,mockPlayer,null));
		verify(mockLevel, times(1)).setTile(anyInt(), anyInt(), any(Tile.class));
		assertNull(mockPlayer.activeItem);
		waterBucket.count = 1; //Clean up change
	}

	@Test
	public void testInteractOnEmptyFillCreativeMode() {
		String oldMode = (String)Settings.get("mode");
		Settings.set("mode","minicraft.settings.mode.creative");
		Tile mockEmptyTile = mock(Tile.class);
		mockEmptyTile.id = (short) 3;
		Level mockLevel = mock(Level.class);
		Player mockPlayer = mock(Player.class);
		Item mockItem = mock(Item.class);
		mockPlayer.activeItem = mockItem;
		BucketItem waterBucket = bucketItems.get(1);
		assertTrue(waterBucket.interactOn(mockEmptyTile,mockLevel,0,0,mockPlayer,null));
		verify(mockLevel, times(1)).setTile(anyInt(), anyInt(), any(Tile.class));
		assertEquals(mockItem,mockPlayer.activeItem);
		Settings.set("mode",oldMode); //cleanup
	}

	@Test
	public void testInteractOn2CountLavaFillWaterFilling() {
		Tile mockLavaTile = mock(Tile.class);
		mockLavaTile.id = (short) 17; // Lava id
		Level mockLevel = mock(Level.class);
		Player mockPlayer = mock(Player.class);
		Inventory mockInventory = mock(Inventory.class);
		when(mockInventory.add(any())).thenReturn(0);
		when(mockPlayer.getInventory()).thenReturn(mockInventory);
		when(mockPlayer.getLevel()).thenReturn(mock(Level.class));
		BucketItem waterBucket = bucketItems.get(1);
		waterBucket.count = 2;
		assertTrue(waterBucket.interactOn(mockLavaTile,mockLevel,0,0,mockPlayer,null));
		verify(mockLevel, times(1)).setTile(0, 0, Tiles.get("Obsidian"));
		verify(mockPlayer,times(1)).getInventory();
		verify(mockPlayer,times(1)).getLevel();
		assertEquals(1,waterBucket.count);
		assertEquals(mockPlayer.activeItem,waterBucket);
	}

	@Test
	public void testInteractOn2CountLavaFillWaterFillingOnEmptyItemList() {
		Tile mockLavaTile = mock(Tile.class);
		mockLavaTile.id = (short) 17; // Lava id
		Level mockLevel = mock(Level.class);
		Player mockPlayer = mock(Player.class);
		Inventory mockInventory = mock(Inventory.class);
		when(mockInventory.add(any())).thenReturn(1);
		when(mockPlayer.getInventory()).thenReturn(mockInventory);
		when(mockPlayer.getLevel()).thenReturn(mock(Level.class));
		BucketItem waterBucket = bucketItems.get(1);
		waterBucket.count = 2;
		assertTrue(waterBucket.interactOn(mockLavaTile,mockLevel,0,0,mockPlayer,null));
		verify(mockLevel, times(1)).setTile(0, 0, Tiles.get("Obsidian"));
		verify(mockPlayer,times(1)).getInventory();
		verify(mockPlayer,times(0)).getLevel();
		assertEquals(1,waterBucket.count);
		assertEquals(mockPlayer.activeItem,waterBucket);
	}

	@Test
	public void testInteractOnLavaFillWaterFillingCreativeMode() {
		String oldMode = (String)Settings.get("mode");
		Settings.set("mode","minicraft.settings.mode.creative");
		Tile mockLavaTile = mock(Tile.class);
		mockLavaTile.id = (short) 17; // Lava id
		Level mockLevel = mock(Level.class);
		Player mockPlayer = mock(Player.class);
		BucketItem waterBucket = bucketItems.get(1);
		Item mockItem = mock(Item.class);
		mockPlayer.activeItem = mockItem;
		assertTrue(waterBucket.interactOn(mockLavaTile,mockLevel,0,0,mockPlayer,null));
		verify(mockLevel, times(1)).setTile(0, 0, Tiles.get("Obsidian"));
		verify(mockPlayer,times(0)).getInventory();
		verify(mockPlayer,times(0)).getLevel();
		assertEquals(mockPlayer.activeItem,mockItem);
		Settings.set("mode",oldMode); //cleanup
	}

	@Test
	public void testInteractOnNonEmptyFillingElseCase() {
		Tile mockWaterTile = mock(Tile.class);
		mockWaterTile.id = (short) 6; // Water id
		Level mockLevel = mock(Level.class);
		Player mockPlayer = mock(Player.class);
		BucketItem waterBucket = bucketItems.get(1);
		assertFalse(waterBucket.interactOn(mockWaterTile,mockLevel,0,0,mockPlayer,null));
	}

	@Test
	public void testCopy() {
		BucketItem mockBucketItem = bucketItems.get(1);
		BucketItem copiedBucketItem = mockBucketItem.copy();
		assertEquals(mockBucketItem.getName(),copiedBucketItem.getName());
		assertEquals(mockBucketItem.count,copiedBucketItem.count);
	}

	@Test
	public void testEqualsWithEqualItems() {
		assertTrue(bucketItems.get(1).equals(bucketItems.get(1).copy()));
	}

	@Test
	public void testEqualsWithNonEqualItems() {
		assertFalse(bucketItems.get(1).equals(bucketItems.get(2).copy()));
	}

	@Test
	public void testHashCode(){
		assertEquals(-1260761957,bucketItems.get(0).hashCode());
	}










}
