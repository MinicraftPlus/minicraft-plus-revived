package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.Particle;
import minicraft.entity.particle.WaterParticle;
import minicraft.gfx.Point;
import minicraft.gfx.SpriteLinker;
import minicraft.level.Level;
import minicraft.level.tile.DirtTile;
import minicraft.level.tile.GrassTile;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.WaterTile;
import minicraft.level.tile.farming.CropTile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

public class WateringCanItem extends Item {
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		items.add(new WateringCanItem("Watering Can"));
		return items;
	}

	private static final SpriteLinker.LinkedSprite sprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "watering_can");
	private static final SpriteLinker.LinkedSprite spriteFilled = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "watering_can_filled");
	private static final SpriteLinker.LinkedSprite particleSprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "glint");

	private static final SpriteLinker.LinkedSprite[] spriteSplash = new SpriteLinker.LinkedSprite[]{
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "splash_0"),
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "splash_1"),
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "splash_2"),
		new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "splash_3")
	};

	public final int CAPACITY = 1800;
	public int content = 0;
	private int renderingTick = 0;

	protected WateringCanItem(String name) {
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
			Random random = new Random();
			if (renderingTick >= 8) {
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
			if (tile instanceof CropTile) {
				int fertilization = ((CropTile) tile).getFertilization(level.getData(xt, yt));
				if (fertilization < 150) { // Maximum of 5 levels watering can can fertilize.
					((CropTile) tile).fertilize(level, xt, yt, 1);
				}
				if (random.nextInt(5) == 0) {
					double x = (double) xt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
					double y = (double) yt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
					level.add(new Particle((int) x, (int) y, 120 + random.nextInt(21) - 40, particleSprite));
				}
			} else if (tile instanceof DirtTile || tile instanceof GrassTile) {
				if (tile instanceof GrassTile) {
					if (random.nextInt(15) == 0) {
						double x = (double) xt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
						double y = (double) yt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
						level.add(new Particle((int) x, (int) y, 120 + random.nextInt(21) - 40, particleSprite));
					}
					if (random.nextInt(60) == 0) { // Small chance for growing flowers
						level.setTile(xt, yt, Tiles.get(2), random.nextInt(2));
					}
				}

				for (Point p : level.getAreaTilePositions(xt, yt, 1)) {
					Tile t = level.getTile(p.x, p.y);
					if (tile instanceof DirtTile) {
						if (t instanceof GrassTile) { // Grass tile exists.
							if (random.nextInt(5) == 0) {
								double x = (double) xt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
								double y = (double) yt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
								level.add(new Particle((int) x, (int) y, 120 + random.nextInt(21) - 40, particleSprite));
							}
							if (random.nextInt(10) == 0)
								level.setTile(xt, yt, Tiles.get("grass")); // Grass extends.
							break; // Operation finished.
						}
					} else { // tile instanceof GrassTile
						if (t instanceof DirtTile) { // Dirt tile exists.
							if (random.nextInt(5) == 0) {
								double x = (double) xt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
								double y = (double) yt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
								level.add(new Particle((int) x, (int) y, 120 + random.nextInt(21) - 40, particleSprite));
							}
							if (random.nextInt(15) == 0)
								level.setTile(p.x, p.y, Tiles.get("grass")); // Grass extends.
							break; // Operation finished.
						}
					}
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
	public @NotNull Item copy() {
		return new WateringCanItem(getName());
	}
}
