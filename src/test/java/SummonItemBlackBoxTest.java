import minicraft.core.io.InputHandler;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.item.Items;
import minicraft.item.SummonItem;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.WaterTile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SummonItemBlackBoxTest {

	/**
	 * All but three functions of the class are public. Therefore, I will blackbox test those three functions.
	 * First, interactOn
	 * Partitionable inputs: xt, yt, attackDir
	 * partitiosn: xt: < 0, 0, >0
	 * yt: < 0, 0, > 0
	 * attackDir: up, down, left, right, None
	 */

	@Test
	void testInteractNeg_Neg_Up() {
		Tiles.initTileList();
		WaterTile watertile = (WaterTile)Tiles.get("water");

		Level level = new Level(5, 8, 3, null);
		Player player = new Player(null, new InputHandler());

		SummonItem summonItem = (SummonItem) Items.get("Totem of Air");

		assertFalse(summonItem.interactOn(watertile, level, -1, -3, player, Direction.UP));
		assertTrue(summonItem.getData().contains("Totem of Air_1"));

	}

	@Test
	void testInteract_Pos_Neg_Down() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null);
		Player player = new Player(null, new InputHandler());

		SummonItem summonItem = (SummonItem) Items.get("Totem of Air");
		assertFalse(summonItem.interactOn(grassTile, level, 3, -3, player, Direction.DOWN));

	}

	@Test
	void testInteract_Pos_Pos_Left() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());


		SummonItem summonItem = (SummonItem) Items.get("Totem of Air");

		assertFalse(summonItem.interactOn(grassTile, level, 3, 3, player, Direction.LEFT));

	}

	@Test
	void testInteract_Zero_Zero_Right() {
		Tiles.initTileList();
		Tile grassTile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());


		SummonItem summonItem = (SummonItem) Items.get("Totem of Air");

		assertFalse(summonItem.interactOn(grassTile, level, 0, 0, player, Direction.RIGHT));

	}


	// Testing edge case calling interactOn twice in a row

	@Test
	void testInteract_Zero_Zero_None() {
		Tiles.initTileList();
		Tile watertile = Tiles.get("Grass");

		Level level = new Level(5, 8, 3, null, false);
		Player player = new Player(null, new InputHandler());

		SummonItem summonItem = (SummonItem) Items.get("Totem of Air");

		assertFalse(summonItem.interactOn(null, level, 0, 0, null, Direction.NONE));
		assertFalse(summonItem.interactOn(null, level, 0, 0, null, Direction.NONE));
	}

	/**
	 * IteractWithWorld
	 * This function returns false all the time
	 *
	 */

	@Test
	void testInteractWithTheWorld_False() {
		SummonItem summonItem = (SummonItem) Items.get("Totem of Air");
		assertFalse(summonItem.interactsWithWorld());
	}

	/**
	 * copy function
	 * This function returns the copy of the current Item
	 * There is no inputs. I will use guessing
	 * We cannot concretely check for equality since there is getters to check equality of states.
	 */

	@Test
	void testCopy() {
		SummonItem summonItem = (SummonItem) Items.get("Totem of Air");

		SummonItem summonCopy = summonItem.copy();

		assertInstanceOf(SummonItem.class, summonCopy);
	}


}
