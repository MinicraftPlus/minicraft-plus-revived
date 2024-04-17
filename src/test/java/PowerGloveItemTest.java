import minicraft.gfx.SpriteLinker;
import minicraft.item.PowerGloveItem;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class PowerGloveItemTest {

	@Test
	public void testConstructor() {
		SpriteLinker.LinkedSprite mockLinkedSprite = mock(SpriteLinker.LinkedSprite.class);
		try (MockedStatic<SpriteLinker> utilities = mockStatic(SpriteLinker.class)) {
			utilities.when(() -> SpriteLinker.missingTexture(any(SpriteLinker.SpriteType.class)))
				.thenReturn(mockLinkedSprite);

			PowerGloveItem orig = new PowerGloveItem();
			assertEquals("Power Glove",orig.getName());
			assertEquals(mockLinkedSprite,orig.sprite);

		}
	}
	@Test
	public void testCopy() {
		SpriteLinker.LinkedSprite mockLinkedSprite = mock(SpriteLinker.LinkedSprite.class);
		try (MockedStatic<SpriteLinker> utilities = mockStatic(SpriteLinker.class)) {
			utilities.when(() -> SpriteLinker.missingTexture(any(SpriteLinker.SpriteType.class)))
				.thenReturn(mockLinkedSprite);

			PowerGloveItem orig = new PowerGloveItem();
			PowerGloveItem copy = orig.copy();
			assertEquals(orig.getName(),copy.getName());
			assertEquals(mockLinkedSprite,orig.sprite);
			assertEquals(mockLinkedSprite,copy.sprite);
		}


	}
}
