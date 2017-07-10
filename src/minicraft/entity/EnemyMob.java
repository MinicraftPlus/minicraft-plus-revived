package minicraft.entity;

import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;

public class EnemyMob extends MobAi {
	
	public int lvl;
	protected int[] lvlcols;
	public int detectDist;
	
	public EnemyMob(int lvl, MobSprite[][] sprites, int[] lvlcols, int health, boolean isFactor, int detectDist, int rwTime, int rwChance) {
		super(sprites, isFactor ? (lvl==0?1:lvl * lvl) * health*((Double)(Math.pow(2, OptionsMenu.diff))).intValue() : health, rwTime, rwChance);
		this.lvl = lvl == 0 ? 1 : lvl;
		this.lvlcols = java.util.Arrays.copyOf(lvlcols, lvlcols.length);
		col = lvlcols[this.lvl-1];
		this.detectDist = detectDist;
	}
	
	public EnemyMob(int lvl, MobSprite[][] sprites, int[] lvlcols, int health, int detectDist) {
		this(lvl, sprites, lvlcols, health, true, detectDist, 60, 200);
	}
	
	public void tick() {
		super.tick();
		
		Player player = getClosestPlayer();
		if (player != null && !Bed.inBed && randomWalkTime <= 0) { // checks if player is on zombies level and if there is no time left on randonimity timer
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
	
	public void render(Screen screen) {
		col = lvlcols[lvl-1];
		super.render(screen);
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
		super.die(50 * lvl, 1);
	}
	
	public static boolean checkStartPos(Level level, int x, int y) { // Find a place to spawn the mob
		int r = (level.depth == -4 ? (ModeMenu.score ? 22 : 15) : 13);
		
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
	
	public int getMaxLevel() {
		return lvlcols.length;
	}
}
