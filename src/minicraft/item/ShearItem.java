package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Sheep;
import minicraft.gfx.Sprite;

import java.util.ArrayList;

public class ShearItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		items.add(new ShearItem());

		return items;
	}

	private ShearItem() {
		super("Shear", new Sprite(1, 12, 0));
	}

	@Override
	public boolean interact(Player player, Entity entity, Direction attackDir) {
		if (entity instanceof Sheep) {
			if (!((Sheep) entity).cut) {
				((Sheep) entity).shear();
				return true;
			}
		}
		return false;
	}

	@Override
	public Item clone() { return new ShearItem(); }
}
