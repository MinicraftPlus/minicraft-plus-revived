package minicraft.entity;

import minicraft.Settings;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.item.Items;

public class Snake extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(18, 18);
	private static int[] lvlcols = {
		Color.get(-1, 000, 444, 30),
		Color.get(-1, 000, 555, 220),
		Color.get(-1, 000, 555, 5),
		Color.get(-1, 000, 555, 400),
		Color.get(-1, 000, 555, 459)
	};
	
	public Snake(int lvl) {
		super(lvl, sprites, lvlcols, lvl>1?8:7, 100);
	}
	
	protected void touchedBy(Entity entity) {
		if(entity instanceof Player) {
			int damage = lvl + Settings.getIdx("diff");
			entity.hurt(this, damage, Mob.getAttackDir(this, entity));
		}
	}
	
	protected void die() {
		int num = Settings.get("diff").equals("Hard") ? 0 : 1;
		dropItem(num, num+1, Items.get("scale"));
		
		super.die();
	}
}
