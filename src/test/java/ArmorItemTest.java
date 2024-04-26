import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker;
import minicraft.item.ArmorItem;
import minicraft.item.Item;
import minicraft.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;

public class ArmorItemTest {

	private static ArrayList<ArmorItem> armorItems = new ArrayList<>();
	private static ArmorItem mockArmorItem;

	@BeforeAll
	public static void setUp() {
		ArrayList<Item> allItems = Items.getAll();
		for (Item item : allItems) {
			if (item instanceof ArmorItem) {
				armorItems.add((ArmorItem) item);
			}
		}
		mockArmorItem = armorItems.get(0);

	}

	@Test
	public void testGetAllInstances() {
		assertEquals(5,armorItems.size());
	}

	@Test
	public void testInteractOn_Success() {
		Player mockPlayer = mock(Player.class);
		when(mockPlayer.payStamina(anyInt())).thenReturn(true);
		boolean result = mockArmorItem.interactOn(null, null, 0, 0, mockPlayer, null);
		//Main effect verification
		assertTrue(result);
		// Side effect verification
		assertEquals(mockArmorItem, mockPlayer.curArmor);
		assertEquals(30,mockPlayer.armor); // expected armor = 550
		//Interaction verifications
		verify(mockPlayer, times(1)).payStamina(anyInt());
	}

	@Test
	public void testInteractOn_Failure() {
		Player mockPlayer = mock(Player.class);
		when(mockPlayer.payStamina(anyInt())).thenReturn(false);
		boolean result = mockArmorItem.interactOn(null, null, 0, 0, mockPlayer, null);
		//Main effect verification
		assertFalse(result);
		ArmorItem initialCurrentArmor = mockPlayer.curArmor;
		float initialArmor = mockPlayer.armor;
		// Side effect verification
		assertEquals(initialCurrentArmor, mockPlayer.curArmor);
		assertEquals(initialArmor,mockPlayer.armor); // expected armor = 550
		//Interaction verifications
		verify(mockPlayer, times(1)).payStamina(anyInt());
	}

	@Test
	public void testInteractOn_FailureNoneNullCurArmor() {
		Player mockPlayer = mock(Player.class);
		mockPlayer.curArmor = mock(ArmorItem.class);
		when(mockPlayer.payStamina(anyInt())).thenReturn(false);
		boolean result = mockArmorItem.interactOn(null, null, 0, 0, mockPlayer, null);
		//Main effect verification
		assertFalse(result);
		ArmorItem initialCurrentArmor = mockPlayer.curArmor;
		float initialArmor = mockPlayer.armor;
		// Side effect verification
		assertEquals(initialCurrentArmor, mockPlayer.curArmor);
		assertEquals(initialArmor,mockPlayer.armor); // expected armor = 550
		//Interaction verifications
		verify(mockPlayer, times(0)).payStamina(anyInt());
	}

	@Test
	public void testInteractsWithWorld() {
		assertFalse(mockArmorItem.interactsWithWorld());
	}

	@Test
	public void testCopy() {
		ArmorItem copiedArmorItem = mockArmorItem.copy();
		assertEquals(mockArmorItem.getName(),copiedArmorItem.getName());
		assertEquals(mockArmorItem.sprite,copiedArmorItem.sprite);
		assertEquals(mockArmorItem.count,copiedArmorItem.count);
		assertEquals(mockArmorItem.level,copiedArmorItem.level);
	}
}
