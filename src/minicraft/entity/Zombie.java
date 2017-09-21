package minicraft.entity;

import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;
import minicraft.screen.OptionsMenu;

public class Zombie extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(0, 14);
	private static int[] lvlcols = {
		Color.get(-1, 10, 152, 40),
		Color.get(-1, 100, 522, 40),
		Color.get(-1, 111, 444, 40),
		Color.get(-1, 000, 111, 20)
	};
	
	public Zombie(int lvl) {
		super(lvl, sprites, lvlcols, 5, 100);
	}
	
	public void tick() {
		super.tick();
	}
	
	protected void die() {
		if (OptionsMenu.diff == OptionsMenu.easy) dropItem(2, 4, Items.get("cloth"));
		if (OptionsMenu.diff == OptionsMenu.norm) dropItem(2, 3, Items.get("cloth"));
		if (OptionsMenu.diff == OptionsMenu.hard) dropItem(1, 2, Items.get("cloth"));
		
		if(random.nextInt(60) == 2) {
			level.dropItem(x, y, Items.get("iron"));
		}
		
		if(random.nextInt(40) == 19) {
			int rand = random.nextInt(3);
			if(rand == 0) {
				level.dropItem(x, y, Items.get("green clothes"));
			} else if(rand == 1) {
				level.dropItem(x, y, Items.get("red clothes"));
			} else if(rand == 2) {
				level.dropItem(x, y, Items.get("blue clothes"));
			}
		}
		
		super.die();
	}
}
