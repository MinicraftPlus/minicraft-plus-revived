import minicraft.core.io.Settings;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.item.ClothingItem;
import minicraft.item.Item;
import minicraft.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClothingItemTest {

	private static ArrayList<ClothingItem> clothingItems = new ArrayList<>();
	private static ClothingItem mockClothingItem;

	private final static int mockClothingItemPlayerCol = Color.get(1, 204, 0, 0);
	private final static int blueClothingItemPlayerCol = Color.get(1, 0, 0, 204);

	@BeforeAll
	public static void setUp() {
		ArrayList<Item> allItems = Items.getAll();
		for (Item item : allItems) {
			if (item instanceof ClothingItem) {
				clothingItems.add((ClothingItem) item);
			}
		}
		mockClothingItem = clothingItems.get(0);

	}

	@Test
	public void testGetAllInstances() {
		assertEquals(9,clothingItems.size());
	}

	@Test
	public void testInteractOnPlayerWithSameShirtColor() {
		Player mockPlayer = mock(Player.class);
		mockPlayer.shirtColor = mockClothingItemPlayerCol;
		assertFalse(mockClothingItem.interactOn(null,null,0,0,mockPlayer,null));
	}

	@Test
	public void testInteractOnPlayerWithDifferentValidShirtColor() {
		Player mockPlayer = mock(Player.class);
		ArgumentCaptor<ClothingItem> valueCapture = ArgumentCaptor.forClass(ClothingItem.class);
		mockPlayer.shirtColor = blueClothingItemPlayerCol;
		doNothing().when(mockPlayer).tryAddToInvOrDrop(valueCapture.capture());
		assertTrue(mockClothingItem.interactOn(null,null,0,0,mockPlayer,null));
		assertEquals("Blue Clothes",valueCapture.getValue().getName());
		verify(mockPlayer,times(1)).tryAddToInvOrDrop(any(ClothingItem.class));
	}

	@Test
	public void testInteractOnPlayerWithDifferentValidShirtColorInCreative() {
		String oldMode = (String) Settings.get("mode");
		Settings.set("mode","minicraft.settings.mode.creative");
		Player mockPlayer = mock(Player.class);
		mockPlayer.shirtColor = blueClothingItemPlayerCol;
		verify(mockPlayer,times(0)).tryAddToInvOrDrop(any(ClothingItem.class));
		assertEquals(blueClothingItemPlayerCol,mockPlayer.shirtColor);
		Settings.set("mode",oldMode); //cleanup
	}

	@Test
	public void testInteractOnPlayerWithDifferentInValidShirtColor() {
		Player mockPlayer = mock(Player.class);
		ArgumentCaptor<ClothingItem> valueCapture = ArgumentCaptor.forClass(ClothingItem.class);
		int unmappedShirtColor = 1000000;
		mockPlayer.shirtColor = unmappedShirtColor; //Unmapped shirt color
		doNothing().when(mockPlayer).tryAddToInvOrDrop(valueCapture.capture());
		assertTrue(mockClothingItem.interactOn(null,null,0,0,mockPlayer,null));
		assertEquals("Reg Clothes",valueCapture.getValue().getName());
		verify(mockPlayer,times(1)).tryAddToInvOrDrop(any(ClothingItem.class));
		assertNotEquals(mockPlayer.shirtColor, unmappedShirtColor);
	}

	@Test
	public void testInteractWithWorld() {
		assertFalse(mockClothingItem.interactsWithWorld());
	}

	@Test
	public void testCopy() {
		ClothingItem copiedItem = mockClothingItem.copy();
		assertEquals(mockClothingItem.getName(),copiedItem.getName());
		assertEquals(mockClothingItem.count,copiedItem.count);
		assertEquals(mockClothingItem.sprite,copiedItem.sprite);
	}


}
