package minicraft.entity.mob;

import minicraft.core.io.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class Zombie extends EnemyMob {
	private static MobSprite[][][] sprites;
	static {
		sprites = new MobSprite[4][4][2];
		for (int i = 0; i < 4; i++) {
			MobSprite[][] list = MobSprite.compileMobSpriteAnimations(8, 0 + (i * 2));
			sprites[i] = list;
		}
	}
	
	/**
	 * Creates a zombie of the given level.
	 * @param lvl Zombie's level.
	 */
	public Zombie(int lvl) {
		super(lvl, sprites, 5, 100);
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	public void die() {
		if (Settings.get("diff").equals("Easy")) dropItem(2, 4, Items.get("cloth"));
		if (Settings.get("diff").equals("Normal")) dropItem(1, 3, Items.get("cloth"));
		if (Settings.get("diff").equals("Hard")) dropItem(1, 2, Items.get("cloth"));
		
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

		if(random.nextInt(100) < 4) {
			level.dropItem(x, y, Items.get("Potato"));
		}
		
		super.die();
	}
}
