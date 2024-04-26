import minicraft.core.io.InputHandler;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.TileItem;
import minicraft.item.TileItem;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.WaterTile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TileItemBlackBoxTest {


	/**
	 * To test InteractOn function, I will use partitioning.
	 * The following are partitions to be explored in the tests. We must achieve each choice coverage
	 * Tile parts: waterTile and non- waterTile
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
	void testInteract_Neg_Neg_Up() {
		Tiles.initTileList();
		WaterTile watertile = (WaterTile)Tiles.get("water");

		Level level = new Level(5, 8, 3, null);
		Player player = new Player(null, new InputHandler());

		TileItem tileItem = (TileItem) Items.get("Flower");

		assertFalse(tileItem.interactOn(watertile, level, -1, -3, player, Direction.UP));

	}

	@Test
	void testInteractOn_Pos_Neg_Down() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null);
		Player player = new Player(null, new InputHandler());

		TileItem tileItem = (TileItem) Items.get("Flower");
		assertTrue(tileItem.interactOn(grassTile, level, 3, -3, player, Direction.DOWN));

	}

	@Test
	void testInteractOn_Pos_Pos_Left() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());


		TileItem tileItem = (TileItem) Items.get("Flower");

		assertTrue(tileItem.interactOn(grassTile, level, 3, 3, player, Direction.LEFT));

	}

	@Test
	void testInteractOn_Zero_Zero_Right() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());


		TileItem tileItem = (TileItem) Items.get("Flower");

		assertTrue(tileItem.interactOn(grassTile, level, 0, 0, player, Direction.RIGHT));

	}


	// Testing edge case calling interactOn twice in a row

	@Test
	void testInteractOn_Zero_Zero_None() {
		Tiles.initTileList();
		Tile watertile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());

		TileItem tileItem = (TileItem) Items.get("Flower");

		assertTrue(tileItem.interactOn(watertile, level, 0, 0, player, Direction.RIGHT));
		assertTrue(tileItem.interactOn(watertile, level, 0, 0, player, Direction.NONE));
	}

	/**
	 * equals function == same test for copy function
	 * There are two parts: Equal tiles, and non equal tiles
	 * Get each choice coverage
	 *
	 */

	@Test
	void testEquals_true() {
		TileItem tileItem = (TileItem) Items.get("Flower");
		TileItem tile2 = tileItem.copy();

		assertTrue(tileItem.equals(tile2));
	}

	@Test
	void TestEquals_false() {
		TileItem tileItem = (TileItem) Items.get("Flower");
		TileItem tile2 = (TileItem) Items.get("Acorn");

		assertFalse(tileItem.equals(tile2));
	}

	/**
	 * hashcode
	 * The functions has no input
	 * I will use error guessing
	 */

	@Test
	void testHashcode() {
		TileItem tileItem = (TileItem) Items.get("Flower");
		int valueExecution1 = tileItem.hashCode();
		int valueExecution2 = tileItem.hashCode();

		assertEquals(valueExecution1, valueExecution2);
	}

	/**
	 * copy
	 */

	@Test
	void testCopy() {
		TileItem tileItem = (TileItem) Items.get("Flower");
		TileItem tile2 = tileItem.copy();

		assertTrue(tileItem.equals(tile2));
	}

}
