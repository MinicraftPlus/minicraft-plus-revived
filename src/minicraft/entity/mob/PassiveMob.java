package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Settings;
import minicraft.core.Updater;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class PassiveMob extends MobAi {
	protected int color;
	
	public PassiveMob(MobSprite[][] sprites, int color) {this(sprites, color, 3);}
	public PassiveMob(MobSprite[][] sprites, int color, int healthFactor) {
		super(sprites, 5 + healthFactor * Settings.getIdx("diff"), 5*60*Updater.normSpeed, 45, 40);
		this.color = color;
		col = color;
	}
	
	public void render(Screen screen) {
		col = color;
		super.render(screen);
	}
	
	public void randomizeWalkDir(boolean byChance) {
		if(xa == 0 && ya == 0 && random.nextInt(5) == 0 || byChance || random.nextInt(randomWalkChance) == 0) {
			randomWalkTime = randomWalkDuration;
			// multiple at end ups the chance of not moving by 50%.
			xa = (random.nextInt(3) - 1) * random.nextInt(2);
			ya = (random.nextInt(3) - 1) * random.nextInt(2);
		}
	}
	
	protected void die() {
		super.die(15);
	}
	
	/** Tries once to find an appropriate spawn location for friendly mobs. */
	public static boolean checkStartPos(Level level, int x, int y) {
		
		int r = (Game.isMode("score") ? 22 : 15) + (Updater.getTime() == Updater.Time.Night ? 0 : 5); // get no-mob radius by
		
		if(!MobAi.checkStartPos(level, x, y, 80, r))
			return false;
		
		Tile tile = level.getTile(x >> 4, y >> 4);
		return tile == Tiles.get("grass") || tile == Tiles.get("flower");
		
	}
	
	public int getMaxLevel() {
		return 1;
	}
}
