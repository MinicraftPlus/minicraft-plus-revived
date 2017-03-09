package com.mojang.ld22.entity;

import java.util.List;

import com.mojang.ld22.InputHandler.Mouse;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.sound.Sound;

public class Mob extends Entity {
	Player player;
	protected int walkDist = 0;
	protected int dir = 0;
	public int hurtTime = 0;
	protected int xKnockback, yKnockback;
	public int maxHealth = 10;
	public int health = maxHealth;
	public int maxHunger = 10;
	public int hunger = maxHunger;
	public int swimTimer = 0;
	public int woolTimer = 0;
	public int lightTimer = 0;
	public int tickTime = 0;
	public int r;
	public int xx;
	public int yy;
	public boolean isenemy = false;
	public int lvl;
	
	public Mob() {
		x = y = 8;	
		xr = 4;
		yr = 3;
		xx = x;
		yy = y;
	}
	
	public void tick() {
		tickTime++;
		if (level.getTile(x >> 4, y >> 4) == Tile.lava) {
			hurt(this, 4, dir ^ 1);
		}
		
		if (health <= 0) {
			die();
		}
		if (hurtTime > 0) hurtTime--;
	}
	
	protected void die() {
		remove();
	}
	
	public boolean move(int xa, int ya) {
		if (isSwimming()) {
			if (swimTimer++ % 2 == 0) return true;
		}
		if (isWooling()) {
			if (woolTimer++ % 2 == 0) return true;
		}
		if (isLight()) {
			if (lightTimer++ % 8000 == 0) return true;
		}
		if (xKnockback < 0) {
			move2(-1, 0);
			xKnockback++;
		}
		if (xKnockback > 0) {
			move2(1, 0);
			xKnockback--;
		}
		if (yKnockback < 0) {
			move2(0, -1);
			yKnockback++;
		}
		if (yKnockback > 0) {
			move2(0, 1);
			yKnockback--;
		}
		if (hurtTime > 0) return true;
		if (xa != 0 || ya != 0) {
			walkDist++;
			if (xa < 0) dir = 2;
			if (xa > 0) dir = 3;
			if (ya < 0) dir = 1;
			if (ya > 0) dir = 0;
		}
		return super.move(xa, ya);
	}
	
	protected boolean isWooling() {
		Tile tile = level.getTile(x >> 0, y >> 0);
		return tile == Tile.wool;
	}
	
	public boolean isLight() {
		Tile tile = level.getTile(x >> 4, y >> 4);
		return 
	    tile == Tile.lightgrass || 
		tile == Tile.lightsand || 
		tile == Tile.lightwater|| 
		tile == Tile.lightdirt|| 	
		tile == Tile.lightflower|| 	
		tile == Tile.lightstairsDown || 
		tile == Tile.lightstairsUp || 
		tile == Tile.lightplank|| 
		tile == Tile.lightsbrick|| 	
		tile == Tile.lwdo|| 	
		tile == Tile.lsdo|| 
		tile == Tile.lighthole|| 	
		tile == Tile.lightwool|| 	
		tile == Tile.lightrwool|| 
		tile == Tile.lightbwool|| 
		tile == Tile.lightgwool|| 
		tile == Tile.lightywool|| 
		tile == Tile.lightblwool|| 
		tile == Tile.lightts|| 
		tile == Tile.lightcs|| 
		tile == Tile.torchgrass||
		tile == Tile.torchsand||
		tile == Tile.torchdirt||
		tile == Tile.torchplank||
		tile == Tile.torchsbrick||
		tile == Tile.torchwool||
		tile == Tile.torchwoolred||
		tile == Tile.torchwoolblue||
		tile == Tile.torchwoolgreen||
		tile == Tile.torchwoolyellow||
		tile == Tile.torchwoolblack;
	}
	
	protected boolean isSwimming() {
		Tile tile = level.getTile(x >> 4, y >> 4);
		return tile == Tile.water || tile == Tile.lava || tile == Tile.lightwater;
	}
	
	public boolean blocks(Entity e) {
		return e.isBlockableBy(this);
	}
	
	public void hurt(Tile tile, int x, int y, int damage) {
		int attackDir = dir ^ 1;
		doHurt(damage, attackDir);
	}
	
	public void hurt(Mob mob, int damage, int attackDir) {
		doHurt(damage, attackDir);
	}
	public void heal(int heal) {
		if (hurtTime > 0) return;
		
		level.add(new TextParticle("" + heal, x, y, Color.get(-1, 50, 50, 50)));
		health += heal;
		if (health > maxHealth) health = maxHealth;
	}
	
	public void hungerHeal(int hungerHeal) {
	
		hunger += hungerHeal;
		if (hunger > maxHunger) hunger = maxHunger;
	}
	
	protected void doHurt(int damage, int attackDir) {
		if (hurtTime > 0) return;
		
		if (level.player != null) {
			int xd = level.player.x - x;
			int yd = level.player.y - y;
			if (xd * xd + yd * yd < 80 * 80) {
				Sound.monsterHurt.play();
			}
		}
		level.add(new TextParticle("" + damage, x, y, Color.get(-1, 500, 500, 500)));
		health -= damage;
		if (attackDir == 0) yKnockback = +6;
		if (attackDir == 1) yKnockback = -6;
		if (attackDir == 2) xKnockback = -6;
		if (attackDir == 3) xKnockback = +6;
		hurtTime = 10;
	}
	
	public boolean findStartPos(Level level) {
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		int xx = x * 16 + 8;
		int yy = y * 16 + 8;
		
		if (level.player != null) {
		
			int xd = level.player.x - xx;
			int yd = level.player.y - yy;
						
			if (xd * xd + yd * yd < 60 * 60) return false;
				
		}
		
		int r = level.monsterDensity * 13;
		if (level.getEntities(xx - r, yy - r, xx + r, yy + r).size() > 0) return false;
		
		if (level.getTile(x, y).mayPass(level, x, y, this)) {
				if (level.getTile(x, y) != Tile.sdo){
				if (level.getTile(x, y) != Tile.wdo){
				if (level.getTile(x, y) != Tile.wheat){
				if (level.getTile(x, y) != Tile.farmland){
				if (level.getTile(x, y) != Tile.lightsbrick){
				if (level.getTile(x, y) != Tile.lightplank){
				if (level.getTile(x, y) != Tile.lightwool){
				if (level.getTile(x, y) != Tile.lightrwool){
				if (level.getTile(x, y) != Tile.lightbwool){
				if (level.getTile(x, y) != Tile.lightgwool){
				if (level.getTile(x, y) != Tile.lightywool){
				if (level.getTile(x, y) != Tile.lightblwool){
				if (level.getTile(x, y) != Tile.lightgrass){
				if (level.getTile(x, y) != Tile.lightsand){
				if (level.getTile(x, y) != Tile.lightdirt){
				if (level.getTile(x, y) != Tile.lightflower){
				if (level.getTile(x, y) != Tile.torchgrass){
				if (level.getTile(x, y) != Tile.torchsand){
				if (level.getTile(x, y) != Tile.torchdirt){
				if (level.getTile(x, y) != Tile.torchplank){
				if (level.getTile(x, y) != Tile.torchsbrick){
				if (level.getTile(x, y) != Tile.torchwool){
				if (level.getTile(x, y) != Tile.torchwoolred){
				if (level.getTile(x, y) != Tile.torchwoolblue){
				if (level.getTile(x, y) != Tile.torchwoolgreen){
				if (level.getTile(x, y) != Tile.torchwoolyellow){
				if (level.getTile(x, y) != Tile.torchwoolblack){
					
			this.x = xx;
			this.y = yy;
			return true;
							
				}}}}}}}}}}}}}}}}}}}}}}}}}}}
		}
		
		return false;
	}
	public boolean findStartPosDungeon(Level level) {
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		int xx = x * 16 + 8;
		int yy = y * 16 + 8;
		
		if (level.player != null) {
		
			int xd = level.player.x - xx;
			int yd = level.player.y - yy;
						
			if (xd * xd + yd * yd < 60 * 60) return false;
				
		}
		if (!ModeMenu.score){
		r = level.monsterDensity * 15;
		} else {
		r = level.monsterDensity * 22;	
		}
		if (level.getEntities(xx - r, yy - r, xx + r, yy + r).size() > 0) return false;
		
		if (level.getTile(x, y).mayPass(level, x, y, this)) {
			if (level.getTile(x, y) == Tile.o){
			
			this.x = xx;
			this.y = yy;
			return true;
			
		}
		}
		
		return false;
	}
	public boolean findStartPosCow(Level level) {
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		int xx = x * 16 + 8;
		int yy = y * 16 + 8;
		
		if (level.player != null) {
		
			int xd = level.player.x - xx;
			int yd = level.player.y - yy;
						
			if (xd * xd + yd * yd < 80 * 80) return false;
				
		}
		
		if (!ModeMenu.score){
		r = level.monsterDensity * 20;
		} else {
		r = level.monsterDensity * 27;	
		}
		if (level.getEntities(xx - r, yy - r, xx + r, yy + r).size() > 0) return false;
		
		
		//makes it so that cows only spawn on grass or flowers.
		if (level.getTile(x, y).mayPass(level, x, y, this)) {
			if (level.getTile(x, y) == Tile.grass){
			this.x = xx;
			this.y = yy;
			return true;
		}
		else if (level.getTile(x, y) == Tile.flower){
			this.x = xx;
			this.y = yy;
			return true;
		}
			if (level.getTile(x, y) == Tile.lightgrass){
				this.x = xx;
				this.y = yy;
				return true;
			}
			else if (level.getTile(x, y) == Tile.lightflower){
				this.x = xx;
				this.y = yy;
				return true;
			}
			
			
		}
		
		return false;
	}
	
	public boolean findStartPosCowLight(Level level) {
		int x = random.nextInt(level.w);
		int y = random.nextInt(level.h);
		int xx = x * 16 + 8;
		int yy = y * 16 + 8;
		
		if (level.player != null) {
		
			int xd = level.player.x - xx;
			int yd = level.player.y - yy;
						
			if (xd * xd + yd * yd < 80 * 80) return false;
				
		}
		
		if (!ModeMenu.score){
		r = level.monsterDensity * 15;
		} else {
		r = level.monsterDensity * 22;	
		}
		if (level.getEntities(xx - r, yy - r, xx + r, yy + r).size() > 0) return false;
		
		
		//makes it so that cows only spawn on grass or flowers.
		if (level.getTile(x, y).mayPass(level, x, y, this)) {
			if (level.getTile(x, y) == Tile.grass){
			this.x = xx;
			this.y = yy;
			return true;
		}
		else if (level.getTile(x, y) == Tile.flower){
			this.x = xx;
			this.y = yy;
			return true;
		}
			if (level.getTile(x, y) == Tile.lightgrass){
				this.x = xx;
				this.y = yy;
				return true;
			}
			else if (level.getTile(x, y) == Tile.lightflower){
				this.x = xx;
				this.y = yy;
				return true;
			}
			
			
		}
		
		return false;
	}
}
	