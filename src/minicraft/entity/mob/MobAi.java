package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.PotionType;
import minicraft.level.Level;

public abstract class MobAi extends Mob {
	
	int randomWalkTime, randomWalkChance, randomWalkDuration;
	int xmov, ymov;
	private int lifetime;
	protected int age = 0; // Not private because it is used in Sheep.java.
	
	private boolean slowtick = false;
	
	/**
	 * Constructor for a mob with an ai.
	 * @param sprites All of this mob's sprites.
	 * @param maxHealth Maximum health of the mob.
	 * @param lifetime How many ticks this mob can live before its removed.
	 * @param rwTime How long the mob will walk in a random direction. (random walk duration)
	 * @param rwChance The chance of this mob will walk in a random direction (random walk chance)
	 */
	protected MobAi(MobSprite[][] sprites, int maxHealth, int lifetime, int rwTime, int rwChance) {
		super(sprites, maxHealth);
		this.lifetime = lifetime;
		randomWalkTime = 0;
		randomWalkDuration = rwTime;
		randomWalkChance = rwChance;
		xmov = 0;
		ymov = 0;
		walkTime = 2;
	}
	
	/**
	 * Checks if the mob should sleep this tick.
	 * @return true if mob should sleep, false if not.
	 */
	protected boolean skipTick() {
		return slowtick && (tickTime+1) % 4 == 0;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if(lifetime > 0) {
			age++;
			if (age > lifetime) {
				remove();
				return;
			}
		}
		
		if(getLevel() != null) {
			boolean foundPlayer = false;
			for(Player p: level.getPlayers()) {
				if(p.isWithin(8, this) && p.potioneffects.containsKey(PotionType.Time)) {
					foundPlayer = true;
					break;
				}
			}
			
			slowtick = foundPlayer;
		}
		
		if(skipTick()) return;
		
		if(!move(xmov * speed, ymov * speed)) {
			xmov = 0;
			ymov = 0;
		}
		
		if (random.nextInt(randomWalkChance) == 0) { // if the mob could not or did not move, or a random small chance occurred...
			randomizeWalkDir(true); // set random walk direction.
		}
		
		if (randomWalkTime > 0) randomWalkTime--;
	}
	
	@Override
	public void render(Screen screen) {
		int xo = x - 8;
		int yo = y - 11;
		
		MobSprite curSprite = sprites[dir.getDir()][(walkDist >> 3) % sprites[dir.getDir()].length];
		if (hurtTime > 0) {
			curSprite.render(screen, xo, yo, true);
		} else {
			curSprite.render(screen, xo, yo);
		}
	}
	
	@Override
	public boolean move(int xmov, int ymov) {
		//noinspection SimplifiableIfStatement
		if(Game.isValidClient()) return false; // client mobAi's should not move at all.
		
		return super.move(xmov, ymov);
	}
	
	@Override
	public void doHurt(int damage, Direction attackDir) {
		if (isRemoved() || hurtTime > 0) return; // If the mob has been hurt recently and hasn't cooled down, don't continue
		
		Player player = getClosestPlayer();
		if (player != null) { // If there is a player in the level
			/// play the hurt sound only if the player is less than 80 entity coordinates away; or 5 tiles away.
			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < 80 * 80) {
				Sound.monsterHurt.play();
			}
		}
		level.add(new TextParticle("" + damage, x, y, Color.RED)); // Make a text particle at this position in this level, bright red and displaying the damage inflicted
		
		super.doHurt(damage, attackDir);
	}
	
	@Override
	public boolean canWool() {
		return true;
	}
	
	/**
	 * Sets the mob to walk in a random direction for a given amount of time.
	 * @param byChance true if the mob should always get a new direction to walk, false if 
	 * there should be a chance that the mob moves.
	 */
	public void randomizeWalkDir(boolean byChance) { // boolean specifies if this method, from where it's called, is called every tick, or after a random chance.
		if(!byChance && random.nextInt(randomWalkChance) != 0) return;
		
		randomWalkTime = randomWalkDuration; // set the mob to walk about in a random direction for a time
		
		// set the random direction; randir is from -1 to 1.
		xmov = (random.nextInt(3) - 1);
		ymov = (random.nextInt(3) - 1);
	}
	
	/**
	 * Adds some items to the level.
	 * @param mincount Least amount of items to add.
	 * @param maxcount Most amount of items to add.
	 * @param items Which items should be added.
	 */
	protected void dropItem(int mincount, int maxcount, Item... items) {
		int count = random.nextInt(maxcount-mincount+1) + mincount;
		for (int i = 0; i < count; i++)
			level.dropItem(x, y, items);
	}
	
	/**
	 * Determines if a friendly mob can spawn here.
	 * @param level The level the mob is trying to spawn in.
	 * @param x X map coordinate of spawn.
	 * @param y Y map coordinate of spawn.
	 * @param playerDist Max distance from the player the mob can be spawned in.
	 * @param soloRadius How far out can there not already be any entities.
	 * This is multiplied by the monster density of the level
	 * @return true if the mob can spawn, false if not.
	 */
	protected static boolean checkStartPos(Level level, int x, int y, int playerDist, int soloRadius) {
		Player player = level.getClosestPlayer(x, y);
		if (player != null) {
			int xd = player.x - x;
			int yd = player.y - y;
			
			if (xd * xd + yd * yd < playerDist * playerDist) return false;
		}
		
		int r = level.monsterDensity * soloRadius; // get no-mob radius
		
		//noinspection SimplifiableIfStatement
		if (level.getEntitiesInRect(new Rectangle(x, y, r*2, r*2, Rectangle.CENTER_DIMS)).size() > 0) return false;
		
		return level.getTile(x >> 4, y >> 4).maySpawn(); // the last check.
	}
	
	/**
	 * Returns the maximum level of this mob.
	 * @return max level of the mob.
	 */
	public abstract int getMaxLevel();
	
	protected void die(int points) { die(points, 0); }
	protected void die(int points, int multAdd) {
		for(Player p: level.getPlayers()) {
			p.addScore(points); // add score for mob death
			if(multAdd != 0)
				p.addMultiplier(multAdd);
		}
		
		super.die();
	}
}
