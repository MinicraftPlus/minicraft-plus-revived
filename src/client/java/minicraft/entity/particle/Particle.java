package minicraft.entity.particle;

import minicraft.entity.ClientTickable;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.item.Item;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.util.Logging;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.DestroyFailedException;

public class Particle extends Entity implements ClientTickable {
	private int time; // lifetime elapsed.
	private int lifetime;

	protected LinkedSprite sprite;

	/**
	 * Creates an particle entity at the given position. The particle has a x and y radius = 1.
	 * @param x X map coordinate
	 * @param y Y map coorindate
	 * @param xr x radius of the particle
	 * @param lifetime How many game ticks the particle lives before its removed
	 * @param sprite The particle's sprite
	 */
	public Particle(int x, int y, int xr, int lifetime, LinkedSprite sprite) {
		// Make a particle at the given coordinates
		super(xr, 1);
		this.x = x;
		this.y = y;
		this.lifetime = lifetime;
		this.sprite = sprite;
		time = 0;
	}

	public Particle(int x, int y, int lifetime, LinkedSprite sprite) {
		this(x, y, 1, lifetime, sprite);
	}

	@Override
	public void tick() {
		time++;
		if (time > lifetime) {
			remove();
			if (sprite != null) try {
				sprite.destroy();
			} catch (DestroyFailedException e) {
				Logging.SPRITE.trace(e);
			}
		}
	}

	@Override
	public void render(Screen screen) {
		screen.render(x, y, sprite);
	}

	@Override
	public boolean isSolid() {
		return false;
	}

	@Override
	public boolean isAttackable(Entity source, @Nullable Item item, Direction attackDir) {
		return false;
	}

	@Override
	public boolean isAttackable(Tile source, Level level, int x, int y, Direction attackDir) {
		return false;
	}

	@Override
	public boolean isUsable() {
		return false;
	}

	@Override
	protected void hurt(int damage, Direction attackDir) {}
}
