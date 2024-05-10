import minicraft.core.io.InputHandler;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.item.Items;
import minicraft.item.WateringCanItem;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.WaterTile;
import minicraft.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class WateringCanItemBlackboxTest {

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
	void testInteractOn_water_Neg_Neg_Up() {
		Tiles.initTileList();
		WaterTile watertile = (WaterTile)Tiles.get("water");

		Level level = new Level(5, 8, 3, null);
		Player player = new Player(null, new InputHandler());

		WateringCanItem canItem = (WateringCanItem) Items.get("Watering Can");

		assertTrue(canItem.interactOn(watertile, level, -1, -3, player, Direction.UP));
		assertTrue(canItem.getData().contains("1800"));

	}

	@Test
	void testInteractOn_NonWater_Pos_Neg_Down() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null);
		Player player = new Player(null, new InputHandler());

		WateringCanItem canItem = (WateringCanItem) Items.get("Watering Can");
		assertTrue(canItem.interactOn(grassTile, level, 3, -3, player, Direction.DOWN));

	}

	@Test
	void testInteractOn_NonWater_Pos_Pos_Left() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());


		WateringCanItem canItem = (WateringCanItem) Items.get("Watering Can");

		assertTrue(canItem.interactOn(grassTile, level, 3, 3, player, Direction.LEFT));

	}

	@Test
	void testInteractOn_NonWater_Zero_Zero_Right() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());


		WateringCanItem canItem = (WateringCanItem) Items.get("Watering Can");

		assertTrue(canItem.interactOn(grassTile, level, 0, 0, player, Direction.RIGHT));

	}


	// Testing edge case calling interactOn twice in a row

	@Test
	void testInteractOn_NonWater_Zero_Zero_None() {
		Tiles.initTileList();
		Tile watertile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());

		WateringCanItem canItem = (WateringCanItem) Items.get("Watering Can");

		assertTrue(canItem.interactOn(null, level, 0, 0, null, Direction.NONE));
		assertFalse(canItem.interactOn(null, level, 0, 0, null, Direction.NONE));
	}

	/**
	 * The following is a test for getData method. It does not have any inputs, thus, has no input partitioning either.
	 */

	@Test
	void testGetData() {
		WateringCanItem canItem = (WateringCanItem) Items.get("Watering Can");
		assertTrue(canItem.getData().contains("Watering Can_1"));
	}

}
