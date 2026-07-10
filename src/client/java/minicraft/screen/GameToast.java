/*
 * SPDX-FileCopyrightText: 2025 Minicraft+ contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.screen;

import minicraft.gfx.Dimension;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import org.jetbrains.annotations.NotNull;

/**
 * All implementations should all be based on sprites without custom drawing function invoke.
 * Dynamic dimensions are not supported, and sizes should maintain static.
 * Game Toasts are rendered only in gameplay and always located in the top-left corner without margin.
 * The content of game toasts is implementation-specific.
 */
public abstract class GameToast extends Toast {
	protected final Dimension size;

	public GameToast(int expireTime, Dimension size) {
		super(expireTime);
		this.size = size;
	}

	protected abstract @NotNull Sprite getSprite();

	@Override
	public final void render(Screen screen) {
		// From the left
		int x = -size.width * (ANIMATION_TIME - animationTick) / ANIMATION_TIME; // Shifting with animation (sliding)
		renderFrame(screen, x, 0, size.width / 8, size.height / 8, getSprite());
		render(screen, x);
	}

	protected abstract void render(Screen screen, int x);
}
