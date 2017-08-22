package minicraft.entity;

import minicraft.Sound;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.level.tile.Tiles;
import minicraft.screen.Displays;
import minicraft.screen.OptionsMenu;
import minicraft.screen.entry.SettingEntry;

public class Creeper extends EnemyMob {
	private static final MobSprite[][] sprites;
	private static final MobSprite[] walking, standing;
	static {
		MobSprite[] list = MobSprite.compileSpriteList(4, 18, 2, 2, 0, 3);
		walking = new MobSprite[] {list[1], list[2]};
		standing = new MobSprite[] {list[0], list[0]};
		sprites = new MobSprite[1][2];
		sprites[0] = standing;
	}
	private static int[] lvlcols = {
		Color.get(-1, 20, 40, 30),
		Color.get(-1, 200, 262, 232),
		Color.get(-1, 200, 272, 222),
		Color.get(-1, 200, 292, 282)
	};
	
	private static final int MAX_FUSE_TIME = 60;
	private static final int BLAST_RADIUS = 60;
	private static final int BLAST_DAMAGE = 10;
	
	private int fuseTime = 0;
	private boolean fuseLit = false;
	
	public Creeper(int lvl) {
		super(lvl, sprites, lvlcols, 10, 50);
	}
	
	public boolean move(int xa, int ya) {
		boolean result = super.move(xa, ya);
		dir = 0;
		if (xa == 0 && ya == 0) walkDist = 0;
		return result;
	}
	
	public void tick() {
		super.tick();
		
		if (fuseTime > 0) {
			fuseTime--; // fuse getting shorter...
			xa = ya = 0;
		} else if (fuseLit) { // fuseLit is set to true when fuseTime is set to max, so this happens after fuseTime hits zero, while fuse is lit.
			// blow up
			xa = ya = 0;
			
			boolean hurtOne = false; // tells if any players were hurt
			
			for(Player player: level.getPlayers()) {
				int pdx = Math.abs(player.x - x);
				int pdy = Math.abs(player.y - y);
				if(pdx < BLAST_RADIUS && pdy < BLAST_RADIUS) {
					float pd = (float) Math.sqrt(pdx * pdx + pdy * pdy);
					int dmg = (int) (BLAST_DAMAGE * (1 - (pd / BLAST_RADIUS))) + OptionsMenu.diff;
					player.hurt(this, dmg, Mob.getAttackDir(this, player));
					player.payStamina(dmg * (Displays.options.getEntry("diff").getValue().equals("easy")?1:2));
					hurtOne = true;
				}
			}
			
			if (hurtOne) {
				
				Sound.explode.play();
				
				// figure out which tile the mob died on
				int xt = x >> 4;
				int yt = (y - 2) >> 4;
				
				// change tile to an appropriate crater
				if(!level.getTile(xt, yt).name.toLowerCase().contains("stairs")) {
					if (lvl == 4) {
						level.setTile(xt, yt, Tiles.get("Infinite Fall"));
					} else if (lvl == 3) {
						level.setTile(xt, yt, Tiles.get("lava"));
					} else {
						level.setTile(xt, yt, Tiles.get("hole"));
					}
				}
				
				die(); // dying now kind of kills everything. the super class will take care of it.
			} else {
				fuseTime = 0;
				fuseLit = false;
			}
		}
	}

	public void render(Screen screen) {
		if (fuseLit && fuseTime % 6 == 0) {
			super.lvlcols[lvl-1] = Color.get(-1, 252);
		}
		else
			super.lvlcols[lvl-1] = Creeper.lvlcols[lvl-1];
		
		this.sprites[0] = walkDist == 0 ? standing : walking;
		
		super.render(screen);
	}

	protected void touchedBy(Entity entity) {
		if (entity instanceof Player) {
			if (fuseTime == 0) {
				Sound.fuse.play();
				fuseTime = MAX_FUSE_TIME;
				fuseLit = true;
			}
			entity.hurt(this, 1, Mob.getAttackDir(this, entity));
		}
	}
	
	public boolean canWool() {
		return false;
	}
	
	protected void die() {
		dropItem(1, 4-OptionsMenu.diff, Items.get("Gunpowder"));
		super.die();
	}
	
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "fuseTime,"+fuseTime+
		";fuseLit,"+fuseLit;
		
		return updates;
	}
	
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		switch(field) {
			case "fuseTime":
				fuseTime = Integer.parseInt(val);
				return true;
			
			case "fuseLit":
			 	boolean wasLit = fuseLit;
				fuseLit = Boolean.parseBoolean(val);
				if(fuseLit && !wasLit)
					Sound.fuse.play();
		}
		
		return false;
	}
}
