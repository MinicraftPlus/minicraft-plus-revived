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
		items.add(new EntitySummonItem("Boat", new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "boat"), Boat::new));
		return items;
	}

	private final @NotNull Function<Direction, Entity> entitySupplier;

	protected EntitySummonItem(String name, SpriteLinker.LinkedSprite sprite, @NotNull Function<Direction, Entity> entitySupplier) {
		super(name, sprite);
		this.entitySupplier = entitySupplier;
	}

	@Override
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		level.add(entitySupplier.apply(attackDir), player.x + 12 * attackDir.getX(), player.y + 12 * attackDir.getY());
		return true;
	}

	@Override
	public boolean interactsWithWorld() {
		return false;
	}

	@Override
	public @NotNull StackableItem copy() {
		return new EntitySummonItem(getName(), sprite, entitySupplier);
	}
}
