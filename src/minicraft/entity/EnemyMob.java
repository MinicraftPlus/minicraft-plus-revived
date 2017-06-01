package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tiles;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;

public class EnemyMob extends MobAi {
	
	public int lvl;
	protected int[] lvlcols;
	public int detectDist;
	
	public EnemyMob(int lvl, MobSprite[][] sprites, int[] lvlcols, int health, boolean isFactor, int detectDist, int rwTime, int rwChance) {
		super(sprites, isFactor ? lvl * lvl * health*((Double)(Math.pow(2, OptionsMenu.diff))).intValue() : health, rwTime, rwChance);
		this.lvl = lvl;
		this.lvlcols = java.util.Arrays.copyOf(lvlcols, lvlcols.length);
		col = lvlcols[lvl-1];
		this.detectDist = detectDist;
	}
	/*public EnemyMob(int lvl, MobSprite[][] sprites, int health, boolean isFactor, int detectDist, int rwTime, int rwChance) {
		this(lvl, sprites, health, isFactor, detectDist, rwTime, rwChance);
	}*/
	public EnemyMob(int lvl, MobSprite[][] sprites, int[] lvlcols, int health, int detectDist) {
		this(lvl, sprites, lvlcols, health, true, detectDist, 60, 200);
	}
	
	public void tick() {
		super.tick();
		
		Entity[] players = level.getEntities(Player.class);
		
		if (players.length > 0 && !Bed.inBed && randomWalkTime <= 0) { // checks if player is on zombies level and if there is no time left on randonimity timer
			int xd = players[0].x - x;
			int yd = players[0].y - y;
			
			for(int i = 1; i < players.length; i++) {
				int curxd = players[i].x - x;
				int curyd = players[i].y - y;
				if(xd*xd + yd*yd > curxd*curxd + curyd*curyd) {
					xd = curxd;
					yd = curyd;
				}
			}
			
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
		//if(this instanceof Creeper) System.out.println("rendering with color " + minicraft.gfx.Color.toString(col));
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
			if (level.getTile(x, y) != Tiles.get("Obsidian")) return false;
		} else if (level.getTile(x, y) != Tiles.get("Stone Door") && level.getTile(x, y) != Tiles.get("Wood Door") && level.getTile(x, y) != Tiles.get("Obsidian Door") && level.getTile(x, y) != Tiles.get("wheat") && level.getTile(x, y) != Tiles.get("farmland"))/* && level.getTile(x, y) != Tiles.get("lightsbrick") && level.getTile(x, y) != Tiles.get("lightplank") && level.getTile(x, y) != Tiles.get("lightwool") && level.getTile(x, y) != Tiles.get("lightrwool") && level.getTile(x, y) != Tiles.get("lightbwool") && level.getTile(x, y) != Tiles.get("lightgwool") && level.getTile(x, y) != Tiles.get("lightywool") && level.getTile(x, y) != Tiles.get("lightblwool") && level.getTile(x, y) != Tiles.get("lightgrass") && level.getTile(x, y) != Tiles.get("lightsand") && level.getTile(x, y) != Tiles.get("lightdirt") && level.getTile(x, y) != Tiles.get("lightflower") && level.getTile(x, y) != Tiles.get("torchgrass") && level.getTile(x, y) != Tiles.get("torchsand") && level.getTile(x, y) != Tiles.get("torchdirt") && level.getTile(x, y) != Tiles.get("torchplank") && level.getTile(x, y) != Tiles.get("torchsbrick") && level.getTile(x, y) != Tiles.get("Torch Wool") && level.getTile(x, y) != Tiles.get("Torch Wool_red") && level.getTile(x, y) != Tiles.get("Torch Wool_blue") && level.getTile(x, y) != Tiles.get("Torch Wool_green") && level.getTile(x, y) != Tiles.get("Torch Wool_yellow") && level.getTile(x, y) != Tiles.get("Torch Wool_black")
		)*/ {  // prevents mobs from spawning on lit tiles (unless in the dungeons)
			return !level.isLight(x, y);
		} else return false;

		return true;
	}
	
	public int getMaxLevel() {
		return lvlcols.length;
	}
}
