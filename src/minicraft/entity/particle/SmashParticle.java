package minicraft.entity.particle;

import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.sound.Sound;

public class SmashParticle extends Particle {
	static int[][] mirrors = {{2, 3}, {0, 1}};
	
	public SmashParticle(int x, int y) {
		super(x, y, 10, new Sprite(5, 12, 2, 2, Color.get(-1, 555), true, mirrors));
		Sound.monsterHurt.play(); // plays the sound of a monster getting hit.
	}
}
