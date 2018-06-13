package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Settings;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Bed;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class EnemyMob extends MobAi {
	
	public int lvl;
	protected int[] lvlcols;
	public int detectDist;
	
	/**
	 * Constructor for a hostile (enemy) mob. The level determines what the mob does. sprites contains all the graphics and animations for the mob.
	 * lvlcols is the different color the mob has depending on its level. isFactor determines if the mob's health should be affected by the level and
	 * the difficulty.
	 * @param lvl The mob's level.
	 * @param sprites The mob's sprites.
	 * @param lvlcols The different colors mapped to the level.
	 * @param health How much health the mob has.
	 * @param isFactor false if maxHealth=health, true if maxHealth=health*level*level*difficulty
	 * @param detectDist The distance where the mob will detect the player and start moving towards him/her.
	 * @param lifetime How many ticks this mob will live.
	 * @param rwTime How long the mob will walk in a random direction. (random walk duration)
	 * @param rwChance The chance of this mob will walk in a random direction (random walk chance)
	 */
	public EnemyMob(int lvl, MobSprite[][] sprites, int[] lvlcols, int health, boolean isFactor, int detectDist, int lifetime, int rwTime, int rwChance) {
		super(sprites, isFactor ? (lvl==0?1:lvl * lvl) * health*((Double)(Math.pow(2, Settings.getIdx("diff")))).intValue() : health, lifetime, rwTime, rwChance);
		this.lvl = lvl == 0 ? 1 : lvl;
		this.lvlcols = java.util.Arrays.copyOf(lvlcols, lvlcols.length);
		col = lvlcols[this.lvl-1];
		this.detectDist = detectDist;
	}
	
	/**
	 * Constructor for a hostile (enemy) mob. 
	 * Lifetime will be set to 60 * Game.normSpeed.
	 * @param lvl The mob's level.
	 * @param sprites The mob's sprites.
	 * @param lvlcols The different colors mapped to the level.
	 * @param health How much health the mob has.
	 * @param isFactor false if maxHealth=health, true if maxHealth=health*level*level*difficulty
	 * @param detectDist The distance where the mob will detect the player and start moving towards him/her.
	 * @param rwTime How long the mob will walk in a random direction. (random walk duration)
	 * @param rwChance The chance of this mob will walk in a random direction (random walk chance)
	 */
	public EnemyMob(int lvl, MobSprite[][] sprites, int[] lvlcols, int health, boolean isFactor, int detectDist, int rwTime, int rwChance) {
		this(lvl, sprites, lvlcols, health, isFactor, detectDist, 60*Updater.normSpeed, rwTime, rwChance);
	}
	
	/**
	 * Constructor for a hostile (enemy) mob.
	 * isFactor=true,
	 * rwTime=60,
	 * rwChance=200.
	 * 
	 * @param lvl The mob's level.
	 * @param sprites The mob's sprites.
	 * @param lvlcols The different colors mapped to the level.
	 * @param health How much health the mob has.
	 * @param detectDist The distance where the mob will detect the player and start moving towards him/her.
	 */
	public EnemyMob(int lvl, MobSprite[][] sprites, int[] lvlcols, int health, int detectDist) {
		this(lvl, sprites, lvlcols, health, true, detectDist, 60, 200);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		Player player = getClosestPlayer();
		if (player != null && !Bed.sleeping() && randomWalkTime <= 0) { // checks if player is on zombies level and if there is no time left on randonimity timer
			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < detectDist * detectDist) {
				/// if player is less than 6.25 tiles away, then set move dir towards player
				int sig0 = 1; // this prevents too precise estimates, preventing mobs from bobbing up and down.
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
	
	@Override
	public void render(Screen screen) {
		col = lvlcols[lvl-1];
		super.render(screen);
	}
	
	@Override
	protected void touchedBy(Entity entity) { // if an entity (like the player) touches the enemy mob
		super.touchedBy(entity);
		// hurts the player, damage is based on lvl.
		if(entity instanceof Player) {
			((Player)entity).hurt(this, lvl * (Settings.get("diff").equals("Hard") ? 2 : 1));
		}
	}
	
	public void die() {
		super.die(50 * lvl, 1);
	}
	
	/**
	 * Determines if the mob can spawn at the giving position in the given map. 
	 * @param level The level which the mob wants to spawn in.
	 * @param x X map spawn coordinate.
	 * @param y Y map spawn coordinate.
	 * @return true if the mob can spawn here, false if not.
	 */
	public static boolean checkStartPos(Level level, int x, int y) { // Find a place to spawn the mob
		int r = (level.depth == -4 ? (Game.isMode("score") ? 22 : 15) : 13);
		
		if(!MobAi.checkStartPos(level, x, y, 60, r))
			return false;
		
		x = x >> 4;
		y = y >> 4;
		
		Tile t = level.getTile(x, y);
		if(level.depth == -4) {
			if (t != Tiles.get("Obsidian")) return false;
		} else if (t != Tiles.get("Stone Door") && t != Tiles.get("Wood Door") && t != Tiles.get("Obsidian Door") && t != Tiles.get("wheat") && t != Tiles.get("farmland")) {
			// prevents mobs from spawning on lit tiles, farms, or doors (unless in the dungeons)
			return !level.isLight(x, y);
		} else return false;

		return true;
	}
	
	@Override
	public int getMaxLevel() {
		return lvlcols.length;
	}
}
