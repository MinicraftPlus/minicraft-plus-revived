package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class PassiveMob extends MobAi {
	
	public PassiveMob(MobSprite[][] sprites) {this(sprites, 3);}
	public PassiveMob(MobSprite[][] sprites, int healthFactor) {
		super(sprites, 5 + healthFactor * OptionsMenu.diff, 45, 40);
	}
	
	public void randomizeWalkDir(boolean byChance) {
		if(xa == 0 && ya == 0 && random.nextInt(5) == 0 || byChance || !byChance && random.nextInt(randomWalkChance) == 0) {
			randomWalkTime = randomWalkDuration;
			// multiple at end ups the chance of not moving by 50%.
			xa = (random.nextInt(3) - 1) * random.nextInt(2);
			ya = (random.nextInt(3) - 1) * random.nextInt(2);
		}
	}
	
	protected void die() {
		if (level.player != null) {
			level.player.score += 15;
		}
		
		super.die();
	}
	
	/** Tries once to find an appropriate spawn location for friendly mobs. */
	public static boolean checkStartPos(Level level, int x, int y) {
		
		int r = (ModeMenu.score ? 22 : 15) + (Game.time == 3 ? 0 : 5); // get no-mob radius by
		
		if(!MobAi.checkStartPos(level, x, y, 80, r))
			return false;
		
		Tile tile = level.getTile(x >> 4, y >> 4);
		if (tile == Tile.grass || tile == Tile.lightgrass || tile == Tile.flower || tile == Tile.lightflower) {
			return true;
		}

		return false;
	}
}
