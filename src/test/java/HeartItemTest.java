import minicraft.core.Updater;
import minicraft.entity.mob.Player;
import minicraft.item.HeartItem;
import minicraft.item.Item;
import minicraft.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class HeartItemTest {
	private static final ArrayList<HeartItem> heartItems = new ArrayList<>();

	@BeforeAll
	public static void setUp() {
		ArrayList<Item> allItems = Items.getAll();
		for (Item item : allItems) {
			if (item instanceof HeartItem) {
				heartItems.add((HeartItem) item);
			}
		}
	}

	@Test
	public void testGetAllInstances() {
		assertEquals(1,heartItems.size());
	}

	@Test
	public void testInteractOnNonMaxHealthPlayer() {
		Player mockPlayer = mock(Player.class);
		int health = 5;
		int oldPlayerHealth = 0;
		int oldPlayerExtraHealth = Player.extraHealth;
		Player.extraHealth = 0;
		mockPlayer.health = oldPlayerHealth;
		assertTrue(heartItems.get(0).interactOn(null,null,0,0,mockPlayer,null));
		assertEquals(health,Player.extraHealth);
		assertEquals(health,mockPlayer.health);
		Player.extraHealth = oldPlayerExtraHealth;
	}

	@Test
	public void testInteractOnMaxHealthPlayer() {
		int oldPlayerBaseHealth = Player.baseHealth;
		Player.baseHealth = Player.maxHealth;
		try (MockedStatic<Updater> utilities = mockStatic(Updater.class)) {
			utilities.when(() -> Updater.notifyAll(anyString())).thenAnswer(invocation -> {
					assertEquals("Health increase is at max!", invocation.getArgument(0));
					return null; // Return type is Void, so null is returned
				});
			assertFalse(heartItems.get(0).interactOn(null,null,0,0,mock(Player.class),null));
		}
		Player.baseHealth = oldPlayerBaseHealth;
	}

	@Test
	public void testInteractsWithWorld() {
		assertFalse(heartItems.get(0).interactsWithWorld());
	}

	@Test
	public void testCopy() {
		HeartItem original = heartItems.get(0);
		HeartItem copy = original.copy();
		assertEquals(original.getName(),copy.getName());
		assertEquals(original.sprite,copy.sprite);
		assertEquals(original.count,copy.count);
	}
}
