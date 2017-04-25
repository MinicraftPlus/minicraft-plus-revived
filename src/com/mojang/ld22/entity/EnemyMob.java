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

public class EnemyMob extends MobAi {
	
	public int lvl;
	public int detectDist;
	
	public EnemyMob(int lvl, MobSprite[][] sprites, int health, boolean isFactor, int detectDist, int rwTime, int rwChance) {
		super(sprites, isFactor ? lvl * lvl * health*((Double)(Math.pow(2, OptionsMenu.diff-1))).intValue() : health, rwTime, rwChance);
		this.lvl = lvl;
		this.detectDist = detectDist;
	}
	public EnemyMob(int lvl, MobSprite[][] sprites, int health, int detectDist) {
		this(lvl, sprites, health, true, detectDist, 60, 200);
	}
	
	public void tick() {
		super.tick();
		
		if (level.player != null && !Bed.inBed && randomWalkTime <= 0) { // checks if player is on zombies level and if there is no time left on randonimity timer
			int xd = level.player.x - x;
			int yd = level.player.y - y;
			if (xd * xd + yd * yd < detectDist * detectDist) {
				/// if player is less than 6.25 tiles away, then set move dir towards player
				int sig0 = 0; // this prevents too precise estimates, preventing mobs from bobbing up and down.
				xa = ya = 0;
				if (xd < sig0) xa = -1;
				if (xd > sig0) xa = +1;
				if (yd < sig0) ya = -1;
				if (yd > sig0) ya = +1;
			} else {
				// if the enemy was following the player, but has now lost it, it stops moving.
					//*that would be nice, but I'll just make it move randomly instead.
				randomizeWalkDir(false);
			}
		}
	}
	
	protected void touchedBy(Entity entity) { // if the entity touches the player
		super.touchedBy(entity);
		// hurts the player, damage is based on lvl.
		if(entity instanceof Player) {
			if (OptionsMenu.diff != OptionsMenu.hard)
				entity.hurt(this, lvl, dir);
			else entity.hurt(this, lvl * 2, dir);
		}
	}
	
	protected void die() {
		if (level.player != null) { // if player is on zombie level
			level.player.score += (50 * lvl) * Game.multiplier; // add score for zombie death
		}
		
		super.die();
		
		Game.addMultiplier(1);
	}
	
	public static boolean checkStartPos(Level level, int x, int y) { // Find a place to spawn the mob
		int r = (level.depth == -4 ? (ModeMenu.score ? 22 : 15) : 13);
		
		if(!MobAi.checkStartPos(level, x, y, 60, r))
			return false;
		
		x = x >> 4;
		y = y >> 4;
		
		if(level.depth == -4) {
			if (level.getTile(x, y) != Tile.o) return false;
		} else if (level.getTile(x, y) != Tile.sdo && level.getTile(x, y) != Tile.wdo && level.getTile(x, y) != Tile.wheat && level.getTile(x, y) != Tile.farmland && level.getTile(x, y) != Tile.lightsbrick && level.getTile(x, y) != Tile.lightplank && level.getTile(x, y) != Tile.lightwool && level.getTile(x, y) != Tile.lightrwool && level.getTile(x, y) != Tile.lightbwool && level.getTile(x, y) != Tile.lightgwool && level.getTile(x, y) != Tile.lightywool && level.getTile(x, y) != Tile.lightblwool && level.getTile(x, y) != Tile.lightgrass && level.getTile(x, y) != Tile.lightsand && level.getTile(x, y) != Tile.lightdirt && level.getTile(x, y) != Tile.lightflower && level.getTile(x, y) != Tile.torchgrass && level.getTile(x, y) != Tile.torchsand && level.getTile(x, y) != Tile.torchdirt && level.getTile(x, y) != Tile.torchplank && level.getTile(x, y) != Tile.torchsbrick && level.getTile(x, y) != Tile.torchwool && level.getTile(x, y) != Tile.torchwoolred && level.getTile(x, y) != Tile.torchwoolblue && level.getTile(x, y) != Tile.torchwoolgreen && level.getTile(x, y) != Tile.torchwoolyellow && level.getTile(x, y) != Tile.torchwoolblack
		) {  // prevents mobs from spawning on lit tiles (unless in the dungeons)
			return true;
		} else return false;

		return true;
	}
}
