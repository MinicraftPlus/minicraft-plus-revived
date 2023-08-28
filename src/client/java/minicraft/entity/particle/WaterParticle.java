package minicraft.entity.particle;

import minicraft.gfx.SpriteLinker;

public class WaterParticle extends Particle {
	private final int destX;
	private final int destY;
	private int count;
	private boolean stopped;

	public WaterParticle(int x, int y, int lifetime, SpriteLinker.LinkedSprite sprite, int destX, int destY) {
		super(x, y, lifetime, sprite);
		this.destX = destX;
		this.destY = destY;
		count = 0;
		stopped = false;
	}

	@Override
	public void tick() {
		move:
		if (!stopped) {
			count++;
			if (x == destX && y == destY) {
				stopped = true;
				break move;
			}
			if (count == 2) {
				int diffX = destX - x;
				int diffY = destY - y;
				if (Math.abs(diffX) < 3 && Math.abs(diffY) < 3) {
					move(destX, destY);
					stopped = true;
					break move;
				}

				double phi = Math.atan2(diffY, diffX);
				double moveX = Math.cos(phi);
				double moveY = Math.sin(phi);
				int moveXI = 0;
				int moveYI = 0;
				if (Math.abs(moveX / moveY) > 1.4) moveXI = (int) Math.signum(moveX); // Difference in X is greater.
				else if (Math.abs(moveY / moveX) > 1.4)
					moveYI = (int) Math.signum(moveY); // Difference in Y is greater.
				else { // The difference is small.
					moveXI = (int) Math.signum(moveX);
					moveYI = (int) Math.signum(moveY);
				}

				if (!move(moveXI, moveYI))
					stopped = true;
				count = 0;
			}
		}

		super.tick();
	}
}
