package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.entity.vehicle.Boat;
import minicraft.gfx.SpriteLinker;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntitySummonItem extends StackableItem {
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		items.add(new EntitySummonItem("Boat", new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "boat"), Boat::new, Boat.RADIUS_X, Boat.RADIUS_Y, new Boat(Direction.NONE)));
		return items;
	}

	private final @NotNull Function<Direction, Entity> entitySupplier;
	private final @NotNull Entity testDummy; // Only used for tile passing test
	private final int xr, yr;

	protected EntitySummonItem(String name, SpriteLinker.LinkedSprite sprite, @NotNull Function<Direction, Entity> entitySupplier, int xr, int yr, @NotNull Entity testDummy) {
		super(name, sprite);
		this.entitySupplier = entitySupplier;
		this.xr = xr;
		this.yr = yr;
		this.testDummy = testDummy;
	}

	@Override
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (isAreaClear(level, xt, yt)) {
			level.add(entitySupplier.apply(attackDir), player.x + 12 * attackDir.getX(), player.y + 12 * attackDir.getY());
			return super.interactOn(true);
		}

		return super.interactOn(false);
	}

	private boolean isAreaClear(Level level, int xt, int yt) {
		// X/Y-Starting/Ending points
		final int xs = ((xt << 4) - xr) >> 4;
		final int xe = ((xt << 4) + xr) >> 4;
		final int ys = ((yt << 4) - yr) >> 4;
		final int ye = ((yt << 4) + yr) >> 4;
		for (int x = xs; x <= xe; x++) {
			for (int y = ys; y <= ye; y++) {
				if (!level.getTile(x, y).mayPass(level, x, y, testDummy)) return false;
			}
		}

		return true;
	}

	@Override
	public boolean interactsWithWorld() {
		return false;
	}

	@Override
	public @NotNull StackableItem copy() {
		return new EntitySummonItem(getName(), sprite, entitySupplier, xr, yr, testDummy);
	}
}
