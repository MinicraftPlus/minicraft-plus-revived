package minicraft.entity;

import minicraft.Settings;
import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Items;

public class Slime extends EnemyMob {
	private static MobSprite[][] sprites;
	static {
		MobSprite[] list = MobSprite.compileSpriteList(0, 18, 2, 2, 0, 2);
		sprites = new MobSprite[1][2];
		sprites[0] = list;
	}
	private static int[] lvlcols = {
		Color.get(-1, 20, 40, 222),
		Color.get(-1, 100, 522, 555),
		Color.get(-1, 111, 444, 555),
		Color.get(-1, 000, 111, 224)
	};
	
	private int jumpTime = 0; // jumpTimer, also acts as a rest timer before the next jump
	
	public Slime(int lvl) {
		super(lvl, sprites, lvlcols, 1, true, 50, 60, 40);
	}
	
	public void tick() {
		super.tick();
		
		/// jumpTime from 0 to -10 (or less) is the slime deciding where to jump.
		/// 10 to 0 is it jumping.
		
		if(jumpTime <= -10 && (xa != 0 || ya != 0))
			jumpTime = 10;
		
		jumpTime--;
		if(jumpTime == 0) {
			xa = ya = 0;
		}
	}
	
	public void randomizeWalkDir(boolean byChance) {
		if(jumpTime > 0) return; // direction cannot be changed if slime is already jumping.
		super.randomizeWalkDir(byChance);
	}
	
	public boolean move(int xa, int ya) {
		boolean result = super.move(xa, ya);
		dir = 0;
		return result;
	}
	
	public void render(Screen screen) {
		int oldy = y;
		if(jumpTime > 0) {
			walkDist = 8; // set to jumping sprite.
			y -= 4; // raise up a bit.
		}
		else walkDist = 0; // set to ground sprite.
		
		dir = 0;
		
		super.render(screen);
		
		y = oldy;
	}
	
	protected void die() {
		dropItem(1, Game.isMode("score") ? 2 : 4 - Settings.getIdx("diff"), Items.get("slime"));
		
		super.die(); // Parent death call
	}
	
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "jumpTime,"+jumpTime;
		
		return updates;
	}
	
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		switch(field) {
			case "jumpTime":
				jumpTime = Integer.parseInt(val);
				return true;
		}
		
		return false;
	}
}
