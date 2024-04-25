import minicraft.core.io.InputHandler;
import minicraft.entity.mob.Player;
import minicraft.item.Item;
import minicraft.item.Recipe;
import org.junit.jupiter.api.Test;

import static minicraft.entity.furniture.Crafter.Type.Workbench;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecipeBlackBoxTest {

	/**
	 * getProduct
	 */
	@Test
	void testRecipe() {
		Recipe recipe = new Recipe("Workbench_1", "Wood_10");


		assertInstanceOf(Item.class, recipe.getProduct());
		assertEquals(recipe.getProduct().getData(), "Workbench");
	}

	@Test
	void testGetAmount() {
		Recipe recipe = new Recipe("Workbench_1", "Wood_10");

		assertEquals(1, recipe.getAmount());
	}

	@Test
	void testGetCanCraft() {
		Recipe recipe = new Recipe("Workbench_1", "Wood_10");

		assertFalse(recipe.getCanCraft());
	}

	@Test
	void testCheckCanCraft_false() {
		Recipe recipe = new Recipe("Workbench_1", "Wood_10");

		Player player = new Player(null, new InputHandler());

		assertFalse(recipe.checkCanCraft(player));
	}

	/**
	 * The following tests are only generating either only True or only False
	 * To achieve each choice coverage for output would require controlling player, which is outside our scope.
	 * Another option could be mocking the class, but this should not be done in blackbox testing.
	 * Therefore, we will not achieve each choice coverage
	 */

	@Test
	void testCheckCanCraft_true() {
		Recipe recipe = new Recipe("Workbench_1", "Wood_10");

		Player player = new Player(null, new InputHandler());

		assertFalse(recipe.checkCanCraft(player));
	}

	@Test
	void testCraft_false() {
		Recipe recipe = new Recipe("Workbench_1", "Wood_10");

		Player player = new Player(null, new InputHandler());

		assertFalse(recipe.craft(player));
	}


	/**
	 * equals
	 * We will use output coverage: parts are true and false
	 * achieve each choice coverage
	 */

	@Test
	void testEqual_true() {
		Recipe recipe_1 = new Recipe("Workbench_1", "Wood_10");
		Recipe recipe_2 = new Recipe("Workbench_1", "Wood_10");

		assertTrue(recipe_2.equals(recipe_1));
	}

	@Test
	void testEqeua_false() {
		Recipe recipe_1 = new Recipe("Workbench_1", "Wood_10");
		Recipe recipe_2 = new Recipe("plank_2", "Wood_1");

		assertFalse(recipe_2.equals(recipe_1));
	}

	@Test
	void testEquals_EdgeCase() {
		Recipe recipe_1 = new Recipe("Workbench_1", "Wood_10");
		Recipe recipe_2 = new Recipe("Workbench_1", "Wood_1");

		assertFalse(recipe_2.equals(recipe_1));
	}


	/**
	 * hashCode
	 * I will use guessing
	 */

	@Test
	void testHashCode() {
		Recipe recipe = new Recipe("Workbench_1", "Wood_10");

		assertEquals(-1725184215, recipe.hashCode());
	}


}
