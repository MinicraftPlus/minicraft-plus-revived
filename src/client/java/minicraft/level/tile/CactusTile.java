package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.entity.vehicle.Boat;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Items;
import minicraft.level.Level;

public class CactusTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "cactus");

	protected CactusTile(String name) {
		super(name, sprite);
	}

	@Override
	public boolean connectsToSand(Level level, int x, int y) {
		return true;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		int damage = level.getData(x, y) + dmg;
		int cHealth = 10;
		if (Game.isMode("minicraft.settings.mode.creative")) dmg = damage = cHealth;
		level.add(new SmashParticle(x << 4, y << 4));
		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));

		if (damage >= cHealth) {
			//int count = random.nextInt(2) + 2;
			level.setTile(x, y, Tiles.get("sand"));
			Sound.play("monsterhurt");
			level.dropItem((x << 4) + 8, (y << 4) + 8, 2, 4, Items.get("Cactus"));
		} else {
			level.setData(x, y, damage);
		}
		return true;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Sand").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		if (entity instanceof Boat) ((Boat) entity).hurt();
		if (!(entity instanceof Mob)) return;
		Mob m = (Mob) entity;
		if (Settings.get("diff").equals("minicraft.settings.difficulty.easy")) {
			m.hurt(this, x, y, 1);
		}
		if (Settings.get("diff").equals("minicraft.settings.difficulty.normal")) {
			m.hurt(this, x, y, 1);
		}
		if (Settings.get("diff").equals("minicraft.settings.difficulty.hard")) {
			m.hurt(this, x, y, 2);
		}
	}

	public boolean tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage - 1);
			return true;
		}
		return false;
	}
}
