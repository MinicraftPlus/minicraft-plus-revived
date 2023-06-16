package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.Particle;
import minicraft.gfx.SpriteLinker;
import minicraft.level.Level;
import minicraft.level.tile.BoostablePlant;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

public class ArcaneFertilizerItem extends StackableItem {
	public static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		items.add(new ArcaneFertilizerItem("Arcane Fertilizer", new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "arcane_fertilizer")));
		return items;
	}

	protected ArcaneFertilizerItem(String name, SpriteLinker.LinkedSprite sprite) {
		super(name, sprite);
	}

	private static final SpriteLinker.LinkedSprite particleSprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "glint");

	@Override
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (tile instanceof BoostablePlant) {
			if (((BoostablePlant) tile).isValidBoostablePlantTarget(level, xt, yt)) {
				((BoostablePlant) tile).performPlantBoost(level, xt, yt);

				Random random = new Random();
				for (int i = 0; i < 5; i++) {
					double x = (double)xt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
					double y = (double)yt * 16 + 8 + (random.nextGaussian() * 0.5) * 8;
					level.add(new Particle((int) x, (int) y, 120 + random.nextInt(21) - 40, particleSprite));
				}
				return super.interactOn(true);
			}
		}

		return false;
	}

	@Override
	public @NotNull StackableItem copy() {
		return new ArcaneFertilizerItem(getName(), sprite);
	}
}
