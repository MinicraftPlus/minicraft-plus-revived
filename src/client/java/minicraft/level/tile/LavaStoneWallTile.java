package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Entity;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Items;
import minicraft.level.Level;

public class LavaStoneWallTile extends Tile {

	private static final SpriteAnimation sprite = new SpriteAnimation(SpriteLinker.SpriteType.Tile, "lava_stone_wall")
		.setConnectionChecker((level, x, y, tile, side) -> tile instanceof LavaStoneWallTile);

	protected LavaStoneWallTile() {
		super("Lava Stone Wall", sprite);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int sbwHealth = 100;
		if (Game.isMode("minicraft.settings.mode.creative")) dmg = damage = sbwHealth;

		level.add(new SmashParticle(x << 4, y << 4));
		Sound.play("monsterhurt");

		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
		if (damage >= sbwHealth) {

			level.dropItem((x << 4) + 8, (y << 4) + 8, 1, 3, Items.get("Lava Stone"));
			level.setTile(x, y, Tiles.get("Lava Stone"));
		} else {
			level.setData(x, y, damage);
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

	public String getName(int data) {
		return "Lava Stone Wall";
	}
}
