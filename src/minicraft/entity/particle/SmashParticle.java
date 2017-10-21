package minicraft.entity.particle;

import minicraft.core.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;

public class SmashParticle extends Particle {
	static int[][] mirrors = {{2, 3}, {0, 1}};
	
	public SmashParticle(int x, int y) {
		super(x, y, 10, new Sprite(5, 12, 2, 2, Color.WHITE, true, mirrors));
		Sound.monsterHurt.play(); // plays the sound of a monster getting hit.
	}
}
