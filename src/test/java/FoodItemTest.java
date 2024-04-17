import minicraft.entity.mob.Player;
import minicraft.item.FishingRodItem;
import minicraft.item.FoodItem;
import minicraft.item.Item;
import minicraft.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FoodItemTest {

	private static ArrayList<FoodItem> foodItems = new ArrayList<>();

	@BeforeAll
	public static void setUp() {
		ArrayList<Item> allItems = Items.getAll();
		for (Item item : allItems) {
			if (item instanceof FoodItem) {
				foodItems.add((FoodItem) item);
			}
		}
	}

	@Test
	public void testGetAllInstances() {
		assertEquals(10,foodItems.size());
	}

	@Test
	public void testInteractOnHungryPlayerWithStamina() {
		Player mockPLayer = mock(Player.class);
		when(mockPLayer.payStamina(anyInt())).thenReturn(true);
		FoodItem testItem = foodItems.get(0).copy();
		mockPLayer.hunger = 2;
		assertTrue(testItem.interactOn(null,null,0,0,mockPLayer,null));
		verify(mockPLayer,times(1)).payStamina(anyInt());
		assertTrue(mockPLayer.hunger > 2);
	}

	@Test
	public void testInteractOnHungryPlayerWithNoStamina() {
		Player mockPLayer = mock(Player.class);
		when(mockPLayer.payStamina(anyInt())).thenReturn(false);
		FoodItem testItem = foodItems.get(0).copy();
		mockPLayer.hunger = 2;
		assertFalse(testItem.interactOn(null,null,0,0,mockPLayer,null));
		verify(mockPLayer,times(1)).payStamina(anyInt());
		assertEquals(2,mockPLayer.hunger  );
	}

	@Test
	public void testInteractOnFullPlayer() {
		Player mockPLayer = mock(Player.class);
		when(mockPLayer.payStamina(anyInt())).thenReturn(true);
		FoodItem testItem = foodItems.get(0).copy();
		mockPLayer.hunger = Player.maxHunger;
		assertFalse(testItem.interactOn(null,null,0,0,mockPLayer,null));
		verify(mockPLayer,times(0)).payStamina(anyInt());
		assertEquals(Player.maxHunger,mockPLayer.hunger);
	}

	@Test
	public void testInteractsWithWorld() {
		assertFalse(foodItems.get(0).interactsWithWorld());
	}

	@Test
	public void testCopy() {
		FoodItem testItem = foodItems.get(0);
		FoodItem copiedItem = foodItems.get(0).copy();
		assertEquals(testItem.getName(),copiedItem.getName());
		assertEquals(testItem.sprite,copiedItem.sprite);
		assertEquals(testItem.count,copiedItem.count);
	}


}
