/*
 * SPDX-FileCopyrightText: 2011-2024 Minicraft+ Developers and Contributors
 * SPDX-License-Identifier: GPL-3.0-only
 */

package minicraft.entity.particle;

import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;

import java.util.Random;

public class SandParticle extends Particle {
	public static final LinkedSprite sprite = new LinkedSprite(SpriteType.Entity, "sand_footsteps");

	/**
	 * Creating a sand particle.
	 * @param x X map position
	 * @param y Y map position
	 */
	public SandParticle(int x, int y) {
		super(x, y, 180 + new Random().nextInt(81) - 40, sprite);
	}
}
