package minicraft.level.tile.farming;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.Particle;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.WaterTile;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Random;

public class CropTile extends FarmTile {
	protected final @Nullable String seed;

	protected int maxAge = 0b111; // Must be a bit mask.

	protected CropTile(String name, @Nullable String seed) {
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
				level.setData(xt, yt, data = (data & ~0b111) + moisture++);
				successful = true;
			}
		} else if (moisture > 0 && random.nextInt(10) == 0) { // drying
			level.setData(xt, yt, data = (data & ~0b111) + moisture--);
			successful = true;
		}

		int fertilization = getFertilization(data);
		int stage = (data >> 3) & maxAge;
		if (stage < maxAge) {
			double points = moisture > 0 ? 4 : 2;
			for (int i = -1; i < 2; i++)
				for (int j = -1; j < 2; j++) {
					Tile t = level.getTile(xt + i, yt + j);
					if ((i != 0 || j != 0) && t instanceof FarmTile) {
						points += (level.getData(xt + i, yt + j) & 0b111) > 0 ? 0.75 : 0.25;
					}
				}

			// Checking whether the target direction has targeted the same CropTile
			boolean up = level.getTile(xt, yt - 1) == this;
			boolean down = level.getTile(xt, yt + 1) == this;
			boolean left = level.getTile(xt - 1, yt) == this;
			boolean right = level.getTile(xt + 1, yt) == this;
			boolean upLeft = level.getTile(xt - 1, yt - 1) == this;
			boolean downLeft = level.getTile(xt - 1, yt + 1) == this;
			boolean upRight = level.getTile(xt + 1, yt - 1) == this;
			boolean downRight = level.getTile(xt + 1, yt + 1) == this;
			if (up && down && left && right && upLeft && downLeft && upRight && downRight)
				points /= 2;
			else {
				if (up && down && left && right)
					points *= 0.75;
				if (up && (down && (left || right) || left && right) || down && left && right) // Either 3 of 4 directions.
					points *= 0.85;
				if (upLeft && (downRight || downLeft || upRight) || downLeft && (upRight || downRight) || upRight && downRight) // Either 2 of 4 directions.
					points *= 0.9;
				if (upLeft) points *= 0.98125;
				if (downLeft) points *= 0.98125;
				if (upRight) points *= 0.98125;
				if (downRight) points *= 0.98125;
			}

			if (random.nextInt((int) (100 / points) + 1) < (fertilization / 30 + 1)) // fertilization >= 0
				level.setData(xt, yt, data = (data & ~(maxAge << 3)) + ((stage + 1) << 3)); // Incrementing the stage by 1.
			successful = true;
		}

		if (fertilization > 0) {
			level.setData(xt, yt, (data & (0b111 + (maxAge << 3))) + ((fertilization - 1) << (3 + (maxAge + 1) / 2)));
			successful = true;
		}

		return successful;
	}

	private static final SpriteLinker.LinkedSprite particleSprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "glint");

	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof StackableItem && item.getName().equalsIgnoreCase("Fertilizer")) {
			((StackableItem) item).count--;
			Random random = new Random();
			for (int i = 0; i < 2; ++i) {
				double x = (double) xt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
				double y = (double) yt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
				level.add(new Particle((int) x, (int) y, 120 + random.nextInt(21) - 40, particleSprite));
			}
			int fertilization = getFertilization(level.getData(xt, yt));
			if (fertilization < 100) { // More fertilization, lower the buffer is applied.
				fertilize(level, xt, yt, 40);
			} else if (fertilization < 200) {
				fertilize(level, xt, yt, 30);
			} else if (fertilization < 300) {
				fertilize(level, xt, yt, 25);
			} else if (fertilization < 400) {
				fertilize(level, xt, yt, 20);
			} else {
				fertilize(level, xt, yt, 10);
			}

			return true;
		}

		return super.interact(level, xt, yt, player, item, attackDir);
	}

	/**
	 * Default harvest method, used for everything that doesn't really need any special behavior.
	 */
	protected void harvest(Level level, int x, int y, Entity entity) {
		int data = level.getData(x, y);
		int age = (data >> 3) & maxAge;

		if (seed != null)
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, Items.get(seed));

		if (age == maxAge) {
			level.dropItem(x * 16 + 8, y * 16 + 8, random.nextInt(3) + 2, Items.get(name));
		} else if (seed == null) {
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, Items.get(name));
		}

		if (age == maxAge && entity instanceof Player) {
			((Player) entity).addScore(random.nextInt(5) + 1);
		}

		// Play sound.
		Sound.play("monsterhurt");

		level.setTile(x, y, Tiles.get("farmland"), data & 0b111);
	}

	public int getFertilization(int data) {
		return data >> (3 + (maxAge + 1) / 2);
	}

	/**
	 * Fertilization: Each magnitude of fertilization (by 1) increases the chance of growth by 1/30.
	 * (The addition by fertilization is rounded down to the nearest integer in chance calculation)
	 * For example, if the chance is originally 10% (1/10), the final chance with 30 fertilization will be 20% (2/10).
	 */
	public void fertilize(Level level, int x, int y, int amount) {
		int data = level.getData(x, y);
		int fertilization = getFertilization(data);
		fertilization += amount;
		if (fertilization < 0) fertilization = 0;
		if (fertilization > 511) fertilization = 511; // The maximum possible value to be reached.
		// If this value exceeds 511, the final value would be greater than the hard maximum value that short can be.
		level.setData(x, y, (data & (0b111 + (maxAge << 3))) + (fertilization << (3 + (maxAge + 1) / 2)));
	}
}
