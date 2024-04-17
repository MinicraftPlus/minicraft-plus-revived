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

	@Test
	public void testSwimVariables() {
		int swimDispColor = 10;
		PotionType swim;
		try (MockedStatic<Color> utilities = mockStatic(Color.class)) {
			utilities.when(() -> Color.get(1, 51, 51, 255))
				.thenReturn(swimDispColor);
			swim = PotionType.Swim;
		}
		assertEquals(swimDispColor,swim.dispColor);
		assertEquals(4800,swim.duration);
	}

	@Test
	public void testEnergyVariables() {
		int energyDispColor = 10;
		PotionType energy;
		try (MockedStatic<Color> utilities = mockStatic(Color.class)) {
			utilities.when(() -> Color.get(1, 237, 110, 78))
				.thenReturn(energyDispColor);
			energy = PotionType.Energy;
		}
		assertEquals(energyDispColor,energy.dispColor);
		assertEquals(8400,energy.duration);
	}

	@Test
	public void testRegenVariables() {
		int regenDispColor = 10;
		PotionType regen;
		try (MockedStatic<Color> utilities = mockStatic(Color.class)) {
			utilities.when(() -> Color.get(1, 219, 70, 189))
				.thenReturn(regenDispColor);
			regen = PotionType.Regen;
		}
		assertEquals(regenDispColor,regen.dispColor);
		assertEquals(1800,regen.duration);
	}








}
