import minicraft.core.Game;
import minicraft.item.ArmorItem;
import minicraft.item.BookItem;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.screen.BookDisplay;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BookItemTest {

	private static ArrayList<BookItem> bookItems = new ArrayList<>();
	private static BookItem mockBookItem;

	@BeforeAll
	public static void setUp() {
		ArrayList<Item> allItems = Items.getAll();
		for (Item item : allItems) {
			if (item instanceof BookItem) {
				bookItems.add((BookItem) item);
			}
		}
		mockBookItem = bookItems.get(0);

	}

	@Test
	public void testGetAllInstances() {
		assertEquals(2,bookItems.size());
	}

	@Test
	public void testInteractOn() {
		boolean result = mockBookItem.interactOn(null, null, 0, 0, null, null);
		assertTrue(result);
	}

	@Test
	public void testInteractsWithWorld() {
		assertFalse(mockBookItem.interactsWithWorld());
	}

	@Test
	public void testCopy() {
		BookItem copiedBookItem = mockBookItem.copy();
		assertEquals(mockBookItem.getName(),copiedBookItem.getName());
		assertEquals(mockBookItem.sprite,copiedBookItem.sprite);
	}
}
