package minicraft.entity;

import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.screen.OptionsMenu;

public class Skeleton extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(8, 16);
	private static int[] lvlcols = {
		Color.get(-1, 111, 40, 444),
		Color.get(-1, 100, 522, 555),
		Color.get(-1, 111, 444, 555),
		Color.get(-1, 000, 111, 555)
	};
	
	public int arrowtime;
	public int artime;
	
	public Skeleton(int lvl) {
		super(lvl, sprites, lvlcols, 6, true, 100, 45, 200);
		
		arrowtime = 300 / (lvl + 5);
		artime = arrowtime;
	}

	public void tick() {
		super.tick();
		
		Player player = getClosestPlayer();
		if (player != null && randomWalkTime == 0) {
			boolean done = false;
			artime--;
			
			int xd = player.x - x;
			int yd = player.y - y;
			if (xd * xd + yd * yd < 100 * 100) {
				if (artime < 1) {
					int xdir = 0, ydir = 0;
					if(dir == 0) ydir = 1;
					if(dir == 1) ydir = -1;
					if(dir == 2) xdir = -1;
					if(dir == 3) xdir = 1;
					level.add(new Arrow(this, xdir, ydir, lvl, done));
					artime = arrowtime;
				}
			}
		}
	}
	
	protected void die() {
		int[] diffrands = {20, 20, 30};
		int[] diffvals = {13, 18, 28};
		int diff = OptionsMenu.diff;
		
		int count = random.nextInt(3 - diff) + 1;
		int bookcount = random.nextInt(1) + 1;
		int rand = random.nextInt(diffrands[diff]);
		if (rand <= diffvals[diff])
			level.dropItem(x, y, count, Items.get("bone"), Items.get("arrow"));
		else if (diff == 0 && rand < 19 || diff != 0)
			level.dropItem(x, y, bookcount, Items.get("Antidious"), Items.get("arrow"));
		else if (diff == 0) // rare chance of 10 arrows on easy mode
			level.dropItem(x, y, 10, Items.get("arrow"));
		
		super.die();
	}
}
