import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.UnknownItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnknownItemBlackBoxTest {

	/**
	 * This class has only one public function, and this function has no input and one possible output.
	 * Therefore, I will partition the output to the only feasible part (part made of unknown)
	 */

	@Test
	void testUnknownItem() {
		Item item = Items.get("unknown");
		assertNotNull(item);
		assertInstanceOf(UnknownItem.class, item);
	}
}
