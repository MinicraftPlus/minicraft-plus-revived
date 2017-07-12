package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.PotionType;
import minicraft.level.Level;
import minicraft.screen.ModeMenu;

public abstract class MobAi extends Mob {
	
	int randomWalkTime, randomWalkChance, randomWalkDuration;
	int xa, ya;
	
	private boolean slowtick = false;
	
	public MobAi(MobSprite[][] sprites, int maxHealth, int rwTime, int rwChance) {
		super(sprites, maxHealth);
		randomWalkTime = 0;
		randomWalkDuration = rwTime;
		randomWalkChance = rwChance;
		xa = 0;
		ya = 0;
		walkTime = 2;
	}
	
	protected boolean skipTick() {
		return slowtick && (tickTime+1) % 4 == 0;
	}
	
	public void tick() {
		super.tick();
		
		if(getLevel() != null) {
			boolean foundPlayer = false;
			for(Entity e: level.getEntitiesOfClass(Player.class)) {
				if(e.isWithin(8, this) && ((Player)e).potioneffects.containsKey(PotionType.Time)) {
					foundPlayer = true;
					break;
				}
			}
			
			slowtick = foundPlayer;
		}
		
		if(skipTick()) return;
		
		if(!move(xa * speed, ya * speed)) {
			xa = 0;
			ya = 0;
		}
		
		if (random.nextInt(randomWalkChance) == 0) { // if the mob could not or did not move, or a random small chance occurred...
			randomizeWalkDir(true); // set random walk direction.
		}
		
		if (randomWalkTime > 0) randomWalkTime--;
	}
	
	public void render(Screen screen) {
		int xo = x - 8;
		int yo = y - 11;
		
		int color = col;
		if (hurtTime > 0) {
			color = Color.get(-1, 555);
		}
		
		MobSprite curSprite = sprites[dir][(walkDist >> 3) % sprites[dir].length];
		curSprite.render(screen, xo, yo, color);
	}
	
	public boolean move(int xa, int ya) {
		if(Game.isValidClient()) return false; // client mobAi's should not move at all.
		
		return super.move(xa, ya);
	}
	
	public boolean canWool() {
		return true;
	}
	
	public void randomizeWalkDir(boolean byChance) { // boolean specifies if this method, from where it's called, is called every tick, or after a random chance.
		if(!byChance && random.nextInt(randomWalkChance) != 0) return;
		
		randomWalkTime = randomWalkDuration; // set the mob to walk about in a random direction for a time
		
		// set the random direction; randir is from -1 to 1.
		xa = (random.nextInt(3) - 1);
		ya = (random.nextInt(3) - 1);
	}
	
	protected void dropItem(int mincount, int maxcount, Item... items) {
		int count = random.nextInt(maxcount-mincount+1) + mincount;
		for (int i = 0; i < count; i++)
			level.dropItem(x, y, items);
	}
	
	/** Determines if the given spawn location is appropriate for friendly mobs. */
	protected static boolean checkStartPos(Level level, int x, int y, int playerDist, int soloRadius) {
		Player player = level.getClosestPlayer(x, y);
		if (player != null) {
			int xd = player.x - x;
			int yd = player.y - y;
			
			if (xd * xd + yd * yd < playerDist * playerDist) return false;
		}
		
		int r = level.monsterDensity * soloRadius; // get no-mob radius
		
		if (level.getEntitiesInRect(x - r, y - r, x + r, y + r).size() > 0) return false;
		
		return level.getTile(x >> 4, y >> 4).maySpawn; // the last check.
	}
	
	public abstract int getMaxLevel();
	
	public void die(int points) { die(points, 0); }
	public void die(int points, int multAdd) {
		for(Entity e: level.getEntitiesOfClass(Player.class)) {
			Player p = (Player)e;
			p.score += points * (ModeMenu.score ? p.game.multiplier : 1); // add score for zombie death
			if(multAdd != 0 && ModeMenu.score)
				p.game.addMultiplier(multAdd);
		}
		
		super.die();
	}
}
