import minicraft.core.io.InputHandler;
import minicraft.entity.mob.Player;
import minicraft.item.PotionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PotionTypeBlackBoxTest {

	/**
	 * The class has two public methods: toogleEffect and TransmitEffect
	 * for toogleEffect, we partition the method with each type being a part.
	 * We achieve each choice coverage
	 */
	@Test
	void testToogleEffect_Light() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Light.toggleEffect(player, false));
	}

	@Test
	void testToogleEffect_speed() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Speed.toggleEffect(player, false));
	}

	@Test
	void testToogleEffect_Swim() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Swim.toggleEffect(player, false));
	}


	@Test
	void testToogleEffect_Energy() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Energy.toggleEffect(player, false));
	}

	@Test
	void testToogleEffect_Regen() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Regen.toggleEffect(player, false));
	}

	@Test
	void testToogleEffect_Health() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Health.toggleEffect(player, false));
	}

	@Test
	void testToogleEffect_Time() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Time.toggleEffect(player, false));
	}

	@Test
	void testToogleEffect_Lava() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Lava.toggleEffect(player, false));
	}

	@Test
	void testToogleEffect_Shield() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Shield.toggleEffect(player, false));
	}

	@Test
	void testToogleEffect_Haste() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Haste.toggleEffect(player, false));
	}

	@Test
	void testToogleEffect_Escape() {
		Player player = new Player(null, new InputHandler());
		assertTrue(PotionType.Escape.toggleEffect(player, false));
	}


	/**
	 * This function should alway return True
	 */
	@Test
	void testTransmitEffect() {
		assertTrue(PotionType.Energy.transmitEffect());
	}


}

