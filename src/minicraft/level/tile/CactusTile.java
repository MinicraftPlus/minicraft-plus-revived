package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Mob;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.gfx.Screen;
import minicraft.item.ResourceItem;
import minicraft.item.resource.Resource;
import minicraft.level.Level;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;

public class CactusTile extends Tile {
	private static Sprite sprite = new Sprite(8, 2, 2, 2);
	private static int color = Color.get(30, 40, 50, 550);
	
	public CactusTile(int id) {
		super(id, sprite, color);
		connectsToSand = true;
		data.put(Property.CONNECTSAND, (Object)new Boolean(true));
		data.put(Property.HEALTH, (Object)new Integer(10));
	}
	
	//public static int col = Color.get(30, 40, 50, 550);
	
	//public void render(Screen screen, Level level, int x, int y) {
		//sprite.render(screen, x * 16, y * 16);
		/*screen.render(x * 16 + 0, y * 16 + 0, 8 + 2 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 9 + 2 * 32, col, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 8 + 3 * 32, col, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 9 + 3 * 32, col, 0);
		*/
	//}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}
	/*
	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		int damage = level.getData(x, y) + dmg;
		if (ModeMenu.creative) damage = cHealth;
		
		/// make tile.smack() method? :P
		level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500)));
		
		if (damage >= cHealth) {
			destroy(level, x, y);
		} else {
			level.setData(x, y, damage);
		}
	}
	*/
	/** this is basically the entity die() method, for tiles. */
	public void destroy(Level level, int x, int y) {
		int count = random.nextInt(2) + 2;
		/// move random one into Level.java as well...
		level.dropResource(x<<4, y<<4, count, Resource.cactusFlower);
		/*for (int i = 0; i < count; i++) {
					new ItemEntity(
							new ResourceItem(),
							x * 16 + random.nextInt(10) + 3,
							y * 16 + random.nextInt(10) + 3));
		}*/
		level.setTile(x, y, Tile.sand, 0);
	}
	
	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if (OptionsMenu.diff == OptionsMenu.easy) {
			entity.hurt(this, x, y, 1);
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			entity.hurt(this, x, y, 1);
		}
		if (OptionsMenu.diff == OptionsMenu.hard) {
			entity.hurt(this, x, y, 2);
		}
	}

	public void tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) level.setData(xt, yt, damage - 1); // wait... the tiles regenerate?!?!? Well, that would explain why only only 50 or so are chosen randomly...
	}
}
