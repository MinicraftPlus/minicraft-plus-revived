package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.Particle;
import minicraft.gfx.SpriteLinker;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.farming.CropTile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

public class FertilizerItem extends StackableItem {
	private static final SpriteLinker.LinkedSprite particleSprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "glint");

	public static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		items.add(new FertilizerItem("Fertilizer", new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "fertilizer")));
		return items;
	}

	protected FertilizerItem(String name, SpriteLinker.LinkedSprite sprite) {
		super(name, sprite);
	}

	@Override
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (tile instanceof CropTile) {
			Random random = new Random();
			for (int i = 0; i < 2; ++i) {
				double x = (double)xt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
				double y = (double)yt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
				level.add(new Particle((int) x, (int) y, 120 + random.nextInt(21) - 40, particleSprite));
			}
			int fertilization = ((CropTile) tile).getFertilization(level.getData(xt, yt));
			if (fertilization < 100) { // More fertilization, lower the buffer is applied.
				((CropTile) tile).fertilize(level, xt, yt, 40);
			} else if (fertilization < 200) {
				((CropTile) tile).fertilize(level, xt, yt, 30);
			} else if (fertilization < 300) {
				((CropTile) tile).fertilize(level, xt, yt, 25);
			} else if (fertilization < 400) {
				((CropTile) tile).fertilize(level, xt, yt, 20);
			} else {
				((CropTile) tile).fertilize(level, xt, yt, 10);
			}

			return super.interactOn(true);
		}

		return false;
	}

	@Override
	public @NotNull StackableItem copy() {
		return new FertilizerItem(getName(), sprite);
	}
}
