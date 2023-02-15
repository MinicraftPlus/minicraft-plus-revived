package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.Particle;
import minicraft.gfx.SpriteLinker;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.WaterTile;
import minicraft.level.tile.farming.PlantTile;

import java.util.ArrayList;
import java.util.Random;

public class WateringTinItem extends Item {
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		items.add(new WateringTinItem("Watering Tin"));
		return items;
	}

	private static final SpriteLinker.LinkedSprite sprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "watering_tin");
	private static final SpriteLinker.LinkedSprite spriteFilled = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "watering_tin_filled");

	private static final SpriteLinker.LinkedSprite[] spriteSplash = new SpriteLinker.LinkedSprite[] {
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "splash_0"),
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "splash_1"),
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "splash_2"),
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "splash_3")
	};

	public final int CAPACITY = 3600;
	public int content = 0;
	private int renderingTick = 0;

	protected WateringTinItem(String name) {
		super(name, sprite);
	}

	@Override
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (tile instanceof WaterTile) {
			content = CAPACITY;
			updateSprite();
			return true;
		} else if (content > 0) {
			content--;
			updateSprite();
			renderingTick++;
			if (renderingTick == 8) {
				Random random = new Random();
				for (int i = 0; i < 4; i++) {
					SpriteLinker.LinkedSprite splash = spriteSplash[random.nextInt(spriteSplash.length)];
					// 2-pixel deviation for centering particle sprites.
					int destX = player.x - 2 + 12 * attackDir.getX() + random.nextInt(9) - 4;
					int destY = player.y - 2 + 12 * attackDir.getY() + random.nextInt(9) - 4;
					int x = player.x - 2 + 4 * attackDir.getX() + random.nextInt(5) - 2;
					int y = player.y - 2 + 4 * attackDir.getY() + random.nextInt(5) - 2;
					level.add(new WaterParticle(x, y, 80 + random.nextInt(61) - 30, splash, destX, destY));
					renderingTick = 0;
				}
			}
			if (tile instanceof PlantTile) {
				int fertilization = ((PlantTile) tile).getFertilization(level.getData(xt, yt));
				if (fertilization < 150) { // Maximum of 5 levels watering tin can fertilize.
					((PlantTile) tile).fertilize(level, xt, yt, 1);
				}
			}

			return true;
		}

		return false;
	}

	private void updateSprite() {
		super.sprite = content > 0 ? spriteFilled : sprite;
	}

	@Override
	public String getData() {
		return super.getData() + "_" + content;
	}

	@Override
	public Item clone() {
		return new WateringTinItem(getName());
	}

	private static class WaterParticle extends Particle {
		private final int destX;
		private final int destY;
		private int count;
		private boolean stopped;

		public WaterParticle(int x, int y, int lifetime, SpriteLinker.LinkedSprite sprite, int destX, int destY) {
			super(x, y, lifetime, sprite);
			this.destX = destX;
			this.destY = destY;
			count = 0;
			stopped = false;
		}

		@Override
		public void tick() {
			move:
			if (!stopped) {
				count++;
				if (x == destX && y == destY) {
					stopped = true;
					break move;
				}
				if (count == 2) {
					int diffX = destX - x;
					int diffY = destY - y;
					if (Math.abs(diffX) < 3 && Math.abs(diffY) < 3) {
						move(destX, destY);
						stopped = true;
						break move;
					}

					double phi = Math.atan2(diffY, diffX);
					double moveX = Math.cos(phi);
					double moveY = Math.sin(phi);
					int moveXI = 0;
					int moveYI = 0;
					if (Math.abs(moveX / moveY) > 1.4) moveXI = (int) Math.signum(moveX); // Difference in X is greater.
					else if (Math.abs(moveY / moveX) > 1.4) moveYI = (int) Math.signum(moveY); // Difference in Y is greater.
					else { // The difference is small.
						moveXI = (int) Math.signum(moveX);
						moveYI = (int) Math.signum(moveY);
					}

					if (!move(moveXI, moveYI))
						stopped = true;
					count = 0;
				}
			}

			super.tick();
		}
	}
}
