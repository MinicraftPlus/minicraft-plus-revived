import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class PowerGloveItemBlackBoxTest {

	/**
	 * this class has only one function we can use. Namely, copy
	 */
	@Test
	void testCopy() {
		PowerGloveItem powerGlove = new PowerGloveItem();

		Item item = powerGlove.copy();

		assertInstanceOf(PowerGloveItem.class, item);
	}
}
