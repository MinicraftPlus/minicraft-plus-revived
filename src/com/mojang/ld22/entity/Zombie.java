package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Zombie extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(0, 14);
	
	public Zombie(int lvl) {
		super(lvl, sprites, 5, 100);
		
		col0 = Color.get(-1, 10, 152, 40);
		col1 = Color.get(-1, 20, 252, 50);
		col2 = Color.get(-1, 10, 152, 40);
		col3 = Color.get(-1, 0, 30, 20);
		col4 = Color.get(-1, 10, 42, 30);
	}
	
	public void tick() {
		super.tick();
	}

	public void render(Screen screen) {
		// x,y coord of sprite in spritesheet:
		/*int xt = 0;
		int yt = 14;
		
		// change the 3 in (walkDist >> 3) to change the time it will take to switch sprites. (bigger number = longer time).
		 // These will either be a 1 or a 0 depending on the walk distance (Used for walking effect by mirroring the sprite)
		int flip1 = (walkDist >> 3) & 1;
		int flip2 = (walkDist >> 3) & 1;
		
		if (dir == 1) { // if facing up
			xt += 2; // change sprite to up
		}
		if (dir > 1) { // if facing left or down
			flip1 = 0; // controls flipping left and right
			flip2 = ((walkDist >> 4) & 1); // mirror sprite based on walk dist; animates slightly slower than the above
			if (dir == 2) { // if facing left
				flip1 = 1; // flip the sprite so it looks like we are facing left
			}
			xt += 4 + ((walkDist >> 3) & 1) * 2; // animation based on walk distance
		}
		*/
		/* where to draw the sprite relative to our position */
		//int xo = x - 8;
		//int yo = y - 11;
		
		col0 = Color.get(-1, 10, 152, 40);
		col1 = Color.get(-1, 20, 252, 50);
		col2 = Color.get(-1, 10, 152, 40);
		col3 = Color.get(-1, 0, 30, 20);
		col4 = Color.get(-1, 10, 152, 40);
		
		if (isLight()) {
			col0 = col1 = col2 = col3 = col4 = Color.get(-1, 20, 252, 50);
		}
		
		if (lvl == 2) col = Color.get(-1, 100, 522, 050);
		else if (lvl == 3) col = Color.get(-1, 111, 444, 050);
		else if (lvl == 4) col = Color.get(-1, 000, 111, 020);
		
		else if (level.dirtColor == 322) {
			if (Game.time == 0) col = col0;
			if (Game.time == 1) col = col1;
			if (Game.time == 2) col = col2;
			if (Game.time == 3) col = col3;
		} else col = col4;
		
		super.render(screen);
	}

	/*protected void touchedBy(Entity entity) { // if the entity touches the player
		super.touchedBy(entity);
		// hurts the player, damage is based on lvl.
		if(entity instanceof Player) {
			if (OptionsMenu.diff != OptionsMenu.hard)
				entity.hurt(this, lvl, dir);
			else entity.hurt(this, lvl * 2, dir);
		}
		/*if (OptionsMenu.diff == OptionsMenu.norm) {
			if (entity instanceof Player) {
				entity.hurt(this, lvl, dir);
			}
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			if (entity instanceof Player) {
				entity.hurt(this, lvl * 2, dir);
			}
		}*/
	//}

	public boolean canWool() {
		return true;
	}

	protected void die() {
		if (OptionsMenu.diff == OptionsMenu.easy) dropResource(2, 4, Resource.cloth);
		if (OptionsMenu.diff == OptionsMenu.norm) dropResource(2, 3, Resource.cloth);
		if (OptionsMenu.diff == OptionsMenu.hard) dropResource(1, 2, Resource.cloth);
		
		if(random.nextInt(60) == 2) {
			dropResource(Resource.ironIngot);
		}
		
		if(random.nextInt(40) == 19) {
			int rand = random.nextInt(3);
			if(rand == 0) {
				dropResource(Resource.greenclothes);
			} else if(rand == 1) {
				dropResource(Resource.redclothes);
			} else if(rand == 2) {
				dropResource(Resource.blueclothes);
			}
		}
		
		super.die();
	}
}
