package minicraft.entity.particle;

import minicraft.gfx.SpriteLinker;

public class WaterParticle extends Particle {
	private final int destX;
	private final int destY;
	private boolean stopped;
	private int stepCounter;

	public WaterParticle(int x, int y, int lifetime, SpriteLinker.LinkedSprite sprite, int destX, int destY) {
		super(x, y, lifetime, sprite);
		this.destX = destX;
		this.destY = destY;
		this.stopped = false;
		this.stepCounter = 0;
	}

	@Override
	public void tick() {
		if (!stopped) {
			stepCounter++;

			if (x == destX && y == destY) {
				stopped = true;
			} else if (stepCounter % 2 == 0) {
				int dx = Integer.compare(destX, x);
				int dy = Integer.compare(destY, y);
				move(dx, dy);

				if (x == destX && y == destY) {
					stopped = true;
				}
			}
		}

		super.tick();
	}
}
