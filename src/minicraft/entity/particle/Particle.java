package minicraft.entity.particle;

import minicraft.entity.Entity;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;

public class Particle extends Entity {
	private int time; // lifetime elapsed.
	protected int lifetime;
	
	protected Sprite sprite;
	
	/**
	 * Creates an particle entity at the given position. The particle has a x and y radius = 1.
	 * @param x X map coordinate
	 * @param y Y map coorindate
	 * @param lifetime How many game ticks the particle lives before its removed
	 * @param sprite The particle's sprite
	 */
	public Particle(int x, int y, int lifetime, Sprite sprite) {
		// make a particle at the given coordinates
		super(1, 1);
		this.x = x;
		this.y = y;
		this.lifetime = lifetime;
		this.sprite = sprite;
		time = 0;
	}

	@Override
	public void tick() {
		time++;
		if(time > lifetime) {
			remove();
		}
	}

	@Override
	public void render(Screen screen) {
		sprite.render(screen, x, y);
	}
}
