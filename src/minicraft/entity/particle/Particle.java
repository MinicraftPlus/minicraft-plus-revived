package minicraft.entity.particle;

import minicraft.entity.Entity;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;

public class Particle extends Entity {
	private int time; // lifetime elapsed.
	protected int lifetime;
	
	protected Sprite sprite;
	
	public Particle(int x, int y, int lifetime, Sprite sprite) {
		// make a particle at the given coordinates
		super(1, 1);
		this.x = x;
		this.y = y;
		this.lifetime = lifetime;
		this.sprite = sprite;
		time = 0;
	}

	public void tick() {
		time++;
		if(time > lifetime) {
			remove();
		}
	}

	public void render(Screen screen) {
		sprite.render(screen, x, y);
	}
}
