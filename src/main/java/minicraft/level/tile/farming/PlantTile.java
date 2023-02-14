package minicraft.level.tile.farming;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.level.tile.BonemealableTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.WaterTile;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PlantTile extends FarmTile implements BonemealableTile {
	protected final @Nullable String seed;

	protected int maxStage = 7; // Must be a bit mask.

	protected PlantTile(String name, @Nullable String seed) {
		super(name, null);
		this.seed = seed;
	}

	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		harvest(level, x, y, source);
		return true;
	}

	@Override
	public boolean tick(Level level, int xt, int yt) {
		int data = level.getData(xt, yt);
		int moisture = data & 0b111;
		boolean successful = false;
		if (Arrays.stream(level.getAreaTiles(xt, yt, 4)).anyMatch(t -> t instanceof WaterTile)) { // Contains water.
			if (moisture < 7 && random.nextInt(10) == 0) { // hydrating
				level.setData(xt, yt, data = (data & ~3) + moisture++);
				successful = true;
			}
		} else if (moisture > 0 && random.nextInt(10) == 0) { // drying
			level.setData(xt, yt, data = (data & ~3) + moisture--);
			successful = true;
		}

		int stage = (data >> 3) & maxStage;
		if (stage < maxStage) {
			double points = moisture > 0 ? 4 : 2;
			for (int i = -1; i < 2; i++)
				for (int j = -1; j < 2; j++) {
					Tile t = level.getTile(xt + i, yt + j);
					if ((i != 0 || j != 0) && t instanceof FarmTile) {
						points += (level.getData(xt + i, yt + j) & 0b111) > 0 ? 0.75 : 0.25;
					}
				}

			boolean u = level.getTile(xt, yt - 1) == this;
			boolean d = level.getTile(xt, yt + 1) == this;
			boolean l = level.getTile(xt - 1, yt) == this;
			boolean r = level.getTile(xt + 1, yt) == this;
			boolean ul = level.getTile(xt - 1, yt - 1) == this;
			boolean dl = level.getTile(xt - 1, yt + 1) == this;
			boolean ur = level.getTile(xt + 1, yt - 1) == this;
			boolean dr = level.getTile(xt + 1, yt + 1) == this;
			if (u && d && l && r && ul && dl && ur && dr)
				points /= 2;
			else {
				if (u && d && l && r)
					points *= 0.75;
				if (u && (d && (l || r) || l && r) || d && l && r) // Either 3 of 4 directions.
					points *= 0.85;
				if (ul && (dr || dl || ur) || dl && (ur || dr) || ur && dr) // Either 2 of 4 directions.
					points *= 0.9;
				if (ul) points *= 0.98125;
				if (dl) points *= 0.98125;
				if (ur) points *= 0.98125;
				if (dr) points *= 0.98125;
			}

			if (random.nextInt((int) (100/points) + 1) == 0)
				level.setData(xt, yt, (data & ~(maxStage << 3)) + ((stage + 1) << 3)); // Incrementing the stage by 1.
			return true;
		}

		return successful;
	}

	/** Default harvest method, used for everything that doesn't really need any special behavior. */
	protected void harvest(Level level, int x, int y, Entity entity) {
		int data = level.getData(x, y);
		int age = (data >> 3) & maxStage;

		if (seed != null)
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, Items.get(seed));

		if (age == maxStage) {
			level.dropItem(x * 16 + 8, y * 16 + 8, random.nextInt(3) + 2, Items.get(name));
		} else if (seed == null) {
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, Items.get(name));
		}

		if (age == maxStage && entity instanceof Player) {
			((Player)entity).addScore(random.nextInt(5) + 1);
		}

		// Play sound.
		Sound.play("monsterhurt");

		level.setTile(x, y, Tiles.get("farmland"), data & 0b111);
	}

	@Override
	public boolean isValidBonemealTarget(Level level, int x, int y) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, int x, int y) {
		return true;
	}

	@Override
	public void performBonemeal(Level level, int x, int y) {
		int data = level.getData(x, y);
		int stage = (data >> 3) & maxStage;
		level.setData(x, y, (data & ~(maxStage << 3)) + (Math.min(stage + random.nextInt(4) + 2, maxStage) << 3));
	}
}
