package minicraft.screen;

import minicraft.gfx.Dimension;
import minicraft.gfx.Insets;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;

import java.util.List;

public abstract class Toast {
	protected static final int SPACING = 2;
	protected static final int ANIMATION_TIME = 12;

	protected final int expireTime;

	protected int tick = 0;
	protected int animationTick = 0; // 0 to ANIMATION_TIME, ANIMATION_TIME to 0

	protected static abstract class ToastFrame {
		protected final Insets paddings;

		protected ToastFrame(Insets paddings) {
			this.paddings = paddings;
		}

		public abstract void render(Screen screen, int x, int y, int w, int h); // w, h in units of cells

		/*
		 * Standard Frame Texture Format
		 *
		 * 3 * 3 cells for a sprite
		 * [Top-left corner] [Top frame edge (pattern)] [Top-right corner]
		 * [Left frame edge (pattern)] [content background (pattern)] [Right frame edge (pattern)]
		 * [Bottom-left corner] [Bottom frame edge] [Bottom-right corner]
		 */

		// Acts as a helper method in case some implementations may still use the standard texture formats.
		protected void render(Screen screen, int x, int y, int w, int h, Sprite sprite) {
			for (int i = 0; i < w; ++i) {
				for (int j = 0; j < h; ++j) {
					Sprite.Px[][] pxs = sprite.spritePixels;
					if (i == 0 && j == 0)
						screen.render(x, y, pxs[0][0]);
					else if (i == 0 && j == h - 1)
						screen.render(x, y + j * 8, pxs[2][0]);
					else if (i == w - 1 && j == 0)
						screen.render(x + i * 8, y, pxs[0][2]);
					else if (i == w - 1 && j == h - 1)
						screen.render(x + i * 8, y + j * 8, pxs[2][2]);
					else if (i == 0)
						screen.render(x, y + j * 8, pxs[1][0]);
					else if (j == 0)
						screen.render(x + i * 8, y, pxs[0][1]);
					else if (i == w - 1)
						screen.render(x + i * 8, y + j * 8, pxs[1][2]);
					else if (j == h - 1)
						screen.render(x + i * 8, y + j * 8, pxs[2][1]);
					else
						screen.render(x + i * 8, y + j * 8, pxs[1][1]);
				}
			}
		}
	}

	protected Toast(int expireTime) {
		this.expireTime = expireTime;
	}

	public void tick() {
		if (tick == 0 && animationTick < ANIMATION_TIME) animationTick++;
		else if (tick < expireTime) tick++;
		else if (tick == expireTime) animationTick--;
	}

	public abstract void render(Screen screen);

	public boolean isExpired() {
		return tick >= expireTime && animationTick <= 0;
	}
}
