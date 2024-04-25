import minicraft.entity.Entity;
import minicraft.entity.Spark;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.EnemyMob;
import minicraft.gfx.SpriteLinker;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToolItemBlackBoxTests {

	/**
	 * Inputs: int level, and toolType
	 * level partition: 0 - 4
	 * toolTypes: Shovel, Hoe, Sword, Pickaxe, Axe, Bow, Claymore, Shears.
	 * We will use each choice coverage
	 */

	@Test
	void testGetSpriteName_Axe_0() {

		ToolItem item = new ToolItem(ToolType.Axe, 0);
		assertTrue(item.getDisplayName().contains("Wood Axe"));
	}

	@Test
	void testGetSpriteName_Shovel_1() {
		ToolItem item = new ToolItem(ToolType.Shovel, 1);
		assertTrue(item.getDisplayName().contains("Rock Shovel"));
	}

	@Test
	void testGetSpriteName_Hoe_2() {
		ToolItem item = new ToolItem(ToolType.Hoe, 2);
		assertTrue(item.getDisplayName().contains("Iron Hoe"));
	}

	@Test
	void testGetSpriteName_Sword_3() {
		ToolItem item = new ToolItem(ToolType.Sword, 3);
		assertTrue(item.getDisplayName().contains("Gold Sword"));
	}

	@Test
	void testGetSpriteName_Pickaxe_4() {
		ToolItem item = new ToolItem(ToolType.Pickaxe, 4);
		assertTrue(item.getDisplayName().contains("Gem Pickaxe"));
	}

	@Test
	void testGetSpriteName_Bow_4() {
		ToolItem item = new ToolItem(ToolType.Bow, 4);
		assertTrue(item.getDisplayName().contains("Gem Bow"));
	}



	@Test
	void testGetSpriteName_Claymore_4() {
		ToolItem item = new ToolItem(ToolType.Claymore, 4);
		assertTrue(item.getDisplayName().contains("Gem Claymore"));
	}


	/**
	 * Testing getDisplay
	 * Can display toolType & level or just toolType only.
	 * The previous tests account for tooltype and level. the following test will test just tooltype.
	 */
	@Test
	void testgetDisplay_ToolType() {
		ToolItem item = new ToolItem(ToolType.Shears);
		assertTrue(item.getDisplayName().contains("Shears"));
	}


	/**
	 * the output can be true or false. I will do output partitioning
	 * Parts: true, false
	 * We cannot achieve each choice coverage since the function does not return False
	 * ie: the predicate evaluated does not return false as the elements evaluated are
	 * not changed by any operation in the class
	 */

	@Test
	void testIsDepleted_true() {
		ToolItem item = new ToolItem(ToolType.Claymore, 4);
		assertFalse(item.isDepleted());
	}

	/**
	 * We will use output partitioning
	 * parts: True, false
	 */

	@Test
	void testCanAttack_false() {
		ToolItem item = new ToolItem(ToolType.Claymore, 4);
		assertTrue(item.canAttack());
	}

	@Test
	void testCanAttack_true() {
		ToolItem item = new ToolItem(ToolType.Shears, 4);
		assertFalse(item.canAttack());
	}

	/**
	 * The function returns true or false
	 * I will use edge case tests for inputs.
	 */

	@Test
	void testPayDurability_level_0() {
		ToolItem item = new ToolItem(ToolType.Shears, 0);
		assertTrue(item.payDurability());
	}

	@Test
	void testPayDurability_level_4() {
		ToolItem item = new ToolItem(ToolType.Shears, 4);
		assertTrue(item.payDurability());
	}



	/**
	 * Testing getDamage.
	 * The function returns damages and has no input.
	 * Therefore, I will use error guessing no partitioning.
	 */

	@Test
	void testGetDamage() {
		ToolItem item = new ToolItem(ToolType.Shears, 0);
		assertInstanceOf(Integer.class, item.getDamage());
	}

	/**
	 * Testing getAttackDamageBonus
	 * Inputs: entity
	 * partitions: Entity is Mob, and entity is not mob
	 * We must achieve each choice coverage
	 */

	@Test
	void testGetAttackDamageBonus_mob() {
		ToolItem item = new ToolItem(ToolType.Shears, 0);
		SpriteLinker.LinkedSprite[][][] lvlSprites = new SpriteLinker.LinkedSprite[3][3][3];

		Entity e = new EnemyMob(3, lvlSprites, 4, 18);
		assertEquals(1, item.getAttackDamageBonus(e));
	}

	@Test
	void testGetAttackDamageBonus_NotMod() {
		ToolItem item = new ToolItem(ToolType.Shears, 0);


		AirWizard airwiz = new AirWizard();

		Entity e = new Spark(airwiz, 2.0, 5.2);
		assertEquals(0, item.getAttackDamageBonus(e));
	}

	/**
	 * Get data
	 * returns the current durability. It does not have any inputs
	 * Thus, I will use guessing to test this.
	 */

	@Test
	void testGetData() {
		ToolItem item = new ToolItem(ToolType.Shears, 0);
		assertFalse(item.getData().contains("30"));
	}

	/**
	 * Equals function
	 * The function can return true or false.
	 * I will do output partitioning. Parts being true, false
	 * Must have each choice coverage
	 */

	@Test
	void testEquals_false() {
		ToolItem item = new ToolItem(ToolType.Shears, 0);
		ToolItem item2 = new ToolItem(ToolType.Axe, 0);

		assertFalse(item.equals(item2));
	}

	@Test
	void testEquals_true() {
		ToolItem item = new ToolItem(ToolType.Shears, 0);
		ToolItem item2 = new ToolItem(ToolType.Shears, 0);

		assertTrue(item.equals(item2));
	}

	/**
	 * hashcode function
	 * The function returns the current hashcode and has no inputs.
	 * I will use guessing since it cannot be partitioned
	 */

	@Test
	void testHashCode() {
		ToolItem item = new ToolItem(ToolType.Shears, 0);
		assertEquals(-1819595822, item.hashCode());
	}


	/**
	 * copy
	 * The function copies the current Item. It has no input.
	 * I will use guessing to test this function
	 *
	 */

	@Test
	void testCopy() {
		ToolItem item = new ToolItem(ToolType.Shears, 0);

		ToolItem itemCopy = item.copy();
		assertTrue(item.equals(itemCopy));
	}

}
