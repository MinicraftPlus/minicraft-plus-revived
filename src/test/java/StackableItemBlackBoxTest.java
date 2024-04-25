import minicraft.item.ArmorItem;
import minicraft.item.BucketItem;
import minicraft.item.ClothingItem;
import minicraft.item.Item;
import minicraft.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StackableItemBlackBoxTest {

	/**
	 * StackWith function
	 * has Item input. There are two cases: Item can be the same type as the current item, or not.
	 */

	@Test
	void testStackWith_DifferentType() {
		ArmorItem armor = (ArmorItem) Items.get("Leather Armor");
		ClothingItem bucket = (ClothingItem) Items.get("Red Clothes");

		assertFalse(armor.stacksWith(bucket));
	}

	@Test
	void testStackWith_SameType() {
		ArmorItem armor_1 = (ArmorItem) Items.get("Snake Armor");
		ArmorItem armor_2 = (ArmorItem) Items.get("Snake Armor");

		assertTrue(armor_1.stacksWith(armor_2));

	}

	@Test
	void testStackWith_EdgeCase() {
		ArmorItem armor_1 = (ArmorItem) Items.get("Snake Armor");
		ArmorItem armor_2 = (ArmorItem) Items.get("Leather Armor");

		assertFalse(armor_1.stacksWith(armor_2));

	}

	/**
	 * IsDepleted function
	 * the function has no input. It returns true is depleted, false otherwise
	 * I will use error guessing
	 *
	 */

	@Test
	void testIsDepleted() {
		ArmorItem armor = (ArmorItem) Items.get("Leather Armor");

		assertFalse(armor.isDepleted());

	}

	@Test
	void testIsDepleted_true() {
		ArmorItem armor = (ArmorItem) Items.get("Leather Armor");
		armor.count = -1;
		assertTrue(armor.isDepleted());

	}

	/**
	 * Copy
	 * returns the copy
	 * We will use guessing
	 */

	@Test
	void testCopy() {
		ArmorItem armor = (ArmorItem) Items.get("Leather Armor");
		Item item = armor.copy();

		assertInstanceOf(ArmorItem.class, item);
		assertEquals(item.toString(), armor.toString());
	}

	/**
	 * ToString
	 * function has no input.
	 * I will use error guessing
	 */

	@Test
	void testToString() {
		ArmorItem armor = (ArmorItem) Items.get("Leather Armor");

		assertEquals("Leather Armor-Item-Stack_Size:1", armor.toString());
	}

	/**
	 * GetData
	 * function has no input.
	 * I will use error guessing
	 */
	@Test
	void testGetData() {
		ArmorItem armor = (ArmorItem) Items.get("Leather Armor");

		assertEquals("Leather Armor_1", armor.getData());
	}

	/**
	 * GetDisplayName
	 * function has no input.
	 * I will use error guessing
	 */

	@Test
	void testGetDisplayName() {
		ArmorItem armor = (ArmorItem) Items.get("Leather Armor");

		assertEquals(" 1 Leather Armor", armor.getDisplayName());
	}

}
