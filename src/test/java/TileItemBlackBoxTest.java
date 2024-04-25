import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.TileItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class TileItemBlackBoxTest {


	@Test
	void testTile() {
		Item tileItem = Items.get("Tile");
		assertInstanceOf(TileItem.class, tileItem);
	}
}
