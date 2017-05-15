package minicraft.entity;

import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.screen.OptionsMenu;

public class Knight extends EnemyMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(24, 14);
	private static int[] lvlcols = {
		Color.get(-1, 000, 555, 10),
		Color.get(-1, 000, 555, 220),
		Color.get(-1, 000, 555, 5),
		Color.get(-1, 000, 555, 400),
		Color.get(-1, 000, 555, 459)
	};
	
	public Knight(int lvl) {
		super(lvl, sprites, lvlcols, 9, 100);
		/*this.col0 = Color.get(-1, 0, 555, 359);
		this.col1 = Color.get(-1, 0, 555, 359);
		this.col2 = Color.get(-1, 0, 333, 59);
		this.col3 = Color.get(-1, 0, 333, 59);
		this.col4 = Color.get(-1, 0, 333, 59);
		*/
		//col = lvlcols[lvl-1];
	}
	/*
	public void render(Screen screen) {
		col0 = Color.get(-1, 000, 555, 10);
		col1 = Color.get(-1, 000, 555, 10);
		col2 = Color.get(-1, 000, 555, 10);
		col3 = Color.get(-1, 000, 555, 10);
		col4 = Color.get(-1, 000, 555, 10);
		
		if (lvl == 2) col = Color.get(-1, 000, 555, 220);
		else if (lvl == 3) col = Color.get(-1, 000, 555, 5);
		else if (lvl == 4) col = Color.get(-1, 000, 555, 400);
		else if (lvl == 5) col = Color.get(-1, 000, 555, 459);
		
		else if (level.dirtColor == 322) {

			if (Game.time == 0) {
				col = col0;
			}
			if (Game.time == 1) {
				col = col1;
				
			}
			if (Game.time == 2) {
				col = col2;
				
			}
			if (Game.time == 3) {
				col = col3;
				
			}
		} else {
			col = col4;
		}
		
		super.render(screen);
	}
	
	public boolean canWool() {
		return true;
	}*/

	protected void die() {
		if (OptionsMenu.diff == OptionsMenu.easy) dropItem(1, 3, Items.get("shard"));
		if (OptionsMenu.diff == OptionsMenu.norm) dropItem(0, 2, Items.get("shard"));
		if (OptionsMenu.diff == OptionsMenu.hard) dropItem(0, 2, Items.get("shard"));
		
		super.die();
	}
	
	public int getMaxLevel() {
		return 5;
	}
}
