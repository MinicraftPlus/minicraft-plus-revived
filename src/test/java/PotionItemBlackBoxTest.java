import minicraft.core.io.InputHandler;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.WateringCanItem;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.WaterTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PotionItemBlackBoxTest {

	private static PotionItem potionItem;

	@BeforeEach
	public void setup() {
		ArrayList<Item> allItems = Items.getAll();
		for (Item item : allItems) {
			if (item instanceof PotionItem) {
				potionItem = (PotionItem) item;
				break;
			}
		}
	}


	/**
	 * To test InteractOn function, I will use partitioning.
	 * The following are partitions to be explored in the tests. We must achieve each choice coverage
	 * Level has one class: Level
	 * xt has 3 parts: 0, < 0, and > 0
	 * yt has 3 parts: 0, < 0, and > 0
	 * Player has one part: Player
	 * DirAttack has 5 classes: Up, Down, Left, Right, None.
	 *
	 * In each instance, the function should return True.
	 *
	 */

	@Test
	void testInteractOn_Neg_Neg_Up() {
		Tiles.initTileList();
		WaterTile watertile = (WaterTile)Tiles.get("water");

		Level level = new Level(5, 8, 3, null);
		Player player = new Player(null, new InputHandler());

		assertFalse(potionItem.interactOn(watertile, level, -1, -3, player, Direction.UP));
		assertTrue(potionItem.getData().contains("Awkward Potion_1"));

	}

	@Test
	void testInteractOn_Pos_Neg_Down() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null);
		Player player = new Player(null, new InputHandler());

		assertFalse(potionItem.interactOn(grassTile, level, 3, -3, player, Direction.DOWN));

	}

	@Test
	void testInteractOn_Pos_Pos_Left() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());



		assertFalse(potionItem.interactOn(grassTile, level, 3, 3, player, Direction.LEFT));

	}

	@Test
	void testInteractOn_Zero_Zero_Right() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());


		assertFalse(potionItem.interactOn(grassTile, level, 0, 0, player, Direction.RIGHT));

	}


	// Testing edge case calling interactOn twice in a row

	@Test
	void testInteractOn_Zero_Zero_None() {
		Tiles.initTileList();
		Tile watertile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());


		assertFalse(potionItem.interactOn(null, level, 0, 0, player, Direction.NONE));
		assertFalse(potionItem.interactOn(null, level, 0, 0, player, Direction.NONE));
	}

	/**
	 * applyPotion function
	 * Inputs are player, type, and time
	 * Partitions: Type: partitioned to time type or non-time type
	 *
	 */

	@Test
	void testApplyPotion_timeType() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionItem.applyPotion(player, PotionType.Time, 12));
	}

	@Test
	void testApplyPotion_Non_TimeType() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionItem.applyPotion(player, PotionType.Lava, 0));
	}

	/**
	 * applyPotion function
	 * Inputs are player, type, and boolean addEffect
	 * Partitions: Type: partitioned to time type or non-time type
	 *             addEffect: true, false
	 * must achieve each choice coverage
	 */

	@Test
	void testApplyPotion_Non_TimeType_true() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionItem.applyPotion(player, PotionType.Lava, true));
	}

	@Test
	void testApplyPotion_TimeType_false() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionItem.applyPotion(player, PotionType.Time, false));
	}

	/**
	 * equals
	 * has no input. Partition to same objects or different objects
	 * The first test for equals will be reused as the test for copy
	 */

	@Test
	void testEquals_true() {
		Item item = potionItem.copy();

		assertTrue(item.equals(potionItem));
	}

	@Test
	void testEqualsFalse() {
		Item item = Items.get("Grass");
		assertFalse(potionItem.equals(item));
	}

	/**
	 * hashcode function
	 * This function has no input
	 * I will use guessing to test
	 */
	@Test
	void testHashcode() {
		assertEquals(-145769532, potionItem.hashCode());
	}

	/**
	 * InteractWithWorld always returns false
	 */
	@Test
	void testInteractWithWorld() {
		assertFalse(potionItem.interactsWithWorld());
	}

	@Test
	void testCopy() {
		Item item = potionItem.copy();

		assertTrue(item.equals(potionItem));
	}



}
