package minicraft.item;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.furniture.Bed;
import minicraft.entity.furniture.Chest;
import minicraft.entity.furniture.Composter;
import minicraft.entity.furniture.Crafter;
import minicraft.entity.furniture.DungeonChest;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.furniture.Lantern;
import minicraft.entity.furniture.Spawner;
import minicraft.entity.furniture.Tnt;
import minicraft.entity.mob.Cow;
import minicraft.entity.mob.Creeper;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.Pig;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.Sheep;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Snake;
import minicraft.entity.mob.Zombie;
import minicraft.item.component.ComponentMap;
import minicraft.item.component.ComponentTypes;
import minicraft.item.component.type.FurnitureComponent;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

import java.util.ArrayList;

public class FurnitureItem extends Item {

	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();

		/// There should be a spawner for each level of mob, or at least make the level able to be changed.
		items.add(new FurnitureItem(new Spawner(new Cow())));
		items.add(new FurnitureItem(new Spawner(new Pig())));
		items.add(new FurnitureItem(new Spawner(new Sheep())));
		items.add(new FurnitureItem(new Spawner(new Slime(1))));
		items.add(new FurnitureItem(new Spawner(new Zombie(1))));
		items.add(new FurnitureItem(new Spawner(new Creeper(1))));
		items.add(new FurnitureItem(new Spawner(new Skeleton(1))));
		items.add(new FurnitureItem(new Spawner(new Snake(1))));
		items.add(new FurnitureItem(new Spawner(new Knight(1))));

		items.add(new FurnitureItem(new Chest()));
		items.add(new FurnitureItem(new DungeonChest(null, true)));

		// Add the various types of crafting furniture
		for (Crafter.Type type : Crafter.Type.values()) {
			items.add(new FurnitureItem(new Crafter(type)));
		}

		// Add the various lanterns
		for (Lantern.Type type : Lantern.Type.values()) {
			items.add(new FurnitureItem(new Lantern(type)));
		}

		// Add the various colors of bed
		for (DyeItem.DyeColor color : DyeItem.DyeColor.values()) {
			items.add(new FurnitureItem(new Bed(color)));
		}

		items.add(new FurnitureItem(new Tnt()));
		items.add(new FurnitureItem(new Composter()));

		return items;
	}

	public FurnitureItem(Furniture furniture) {
		super(furniture.name, furniture.itemSprite, ComponentMap.builder().add(ComponentTypes.FURNITURE, new FurnitureComponent(furniture)).build());
	}

	/**
	 * Determines if you can attack enemies with furniture (you can't)
	 */
	public boolean canAttack(ItemStack stack) {
		return false;
	}

	/**
	 * What happens when you press the "Attack" key with the furniture in your hands
	 */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir, ItemStack stack) {
		if (tile.mayPass(level, xt, yt, stack.get(ComponentTypes.FURNITURE).furniture())) { // If the furniture can go on the tile
			Sound.play("craft");

			Furniture furniture = stack.get(ComponentTypes.FURNITURE).furniture().copy();

			// Placed furniture's X and Y positions
			furniture.x = (xt << 4) + 8;
			furniture.y = (yt << 4) + 8;

			level.add(furniture); // Adds the furniture to the world
			if (Game.isMode("minicraft.settings.mode.creative")) {
				stack.put(ComponentTypes.FURNITURE, new FurnitureComponent(furniture.copy()));
			} else {
				// The value becomes true, which removes it from the player's active item
				stack.put(ComponentTypes.FURNITURE, new FurnitureComponent(furniture.copy(), true));
			}

			return true;
		}
		return false;
	}

	public boolean isDepleted(ItemStack stack) {
		return stack.get(ComponentTypes.FURNITURE).placed();
	}
}
