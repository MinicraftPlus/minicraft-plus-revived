import minicraft.core.Game;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.item.PotionType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class PotionTypeTest {

	@Test
	public void testAwkwardVariables() {
		int awkwardDispColor = 10;
		PotionType awkward;
		try (MockedStatic<Color> utilities = mockStatic(Color.class)) {
			utilities.when(() -> Color.get(1, 41, 51, 255))
				.thenReturn(awkwardDispColor);
			awkward = PotionType.Awkward;
		}
		assertEquals(awkwardDispColor,awkward.dispColor);
		assertEquals(0,awkward.duration);
	}

	@Test
	public void testSpeedVariables() {
		int speedDispColor = 10;
		PotionType speed;
		try (MockedStatic<Color> utilities = mockStatic(Color.class)) {
			utilities.when(() -> Color.get(1, 105, 209, 105))
				.thenReturn(speedDispColor);
			speed = PotionType.Speed;
		}
		assertEquals(speedDispColor,speed.dispColor);
		assertEquals(4200,speed.duration);
	}

	@Test
	public void testSpeedToggleEffectNoAddEffectFast() {
		int speedDispColor = 10;
		PotionType speed;
		try (MockedStatic<Color> utilities = mockStatic(Color.class)) {
			utilities.when(() -> Color.get(1, 105, 209, 105))
				.thenReturn(speedDispColor);
			speed = PotionType.Speed;
		}
		Player mockPlayer = mock(Player.class);
		mockPlayer.moveSpeed = 10;
		assertTrue(speed.toggleEffect(mockPlayer,false));
		assertEquals(9,mockPlayer.moveSpeed);
	}

	@Test
	public void testSpeedToggleEffectNoAddEffectSlow() {
		int speedDispColor = 10;
		PotionType speed;
		try (MockedStatic<Color> utilities = mockStatic(Color.class)) {
			utilities.when(() -> Color.get(1, 105, 209, 105))
				.thenReturn(speedDispColor);
			speed = PotionType.Speed;
		}
		Player mockPlayer = mock(Player.class);
		mockPlayer.moveSpeed = 0.5;
		assertTrue(speed.toggleEffect(mockPlayer,false));
		assertEquals(0.5,mockPlayer.moveSpeed);
	}

	@Test
	public void testSpeedToggleEffectAddEffect() {
		int speedDispColor = 10;
		PotionType speed;
		try (MockedStatic<Color> utilities = mockStatic(Color.class)) {
			utilities.when(() -> Color.get(1, 105, 209, 105))
				.thenReturn(speedDispColor);
			speed = PotionType.Speed;
		}
		Player mockPlayer = mock(Player.class);
		mockPlayer.moveSpeed = 10;
		assertTrue(speed.toggleEffect(mockPlayer,true));
		assertEquals(11,mockPlayer.moveSpeed);
	}

	@Test
	public void testLightVariables() {
		int lightDispColor = 10;
		PotionType light;
		try (MockedStatic<Color> utilities = mockStatic(Color.class)) {
			utilities.when(() -> Color.get(1, 183, 183, 91))
				.thenReturn(lightDispColor);
			light = PotionType.Light;
		}
		assertEquals(lightDispColor,light.dispColor);
		assertEquals(6000,light.duration);
	}




}
