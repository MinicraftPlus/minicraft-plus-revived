package minicraft.entity.furniture;

import java.util.HashMap;

import org.jetbrains.annotations.Nullable;

import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Cow;
import minicraft.entity.mob.Creeper;
import minicraft.entity.mob.Knight;
import minicraft.entity.mob.Pig;
import minicraft.entity.mob.Sheep;
import minicraft.entity.mob.Skeleton;
import minicraft.entity.mob.Slime;
import minicraft.entity.mob.Snake;
import minicraft.entity.mob.Zombie;

public class Furnitures {
  private static HashMap<String, Furniture> FURNITURE_REGISTER = new HashMap<>();

  static void register(String id, Furniture furniture) {
    FURNITURE_REGISTER.put(id, furniture);
  }

  static {
    register("bed", new Bed());
    register("chest", new Chest());
    register("death_chest", new DeathChest());
    register("dungeon_chest", new DungeonChest(false));
    register("anvil", new Crafter(Crafter.Type.Anvil));
    register("enchanter", new Crafter(Crafter.Type.Enchanter));
    register("furnace", new Crafter(Crafter.Type.Furnace));
    register("loom", new Crafter(Crafter.Type.Loom));
    register("oven", new Crafter(Crafter.Type.Oven));
    register("workbench", new Crafter(Crafter.Type.Workbench));
    register("lantern", new Lantern(Lantern.Type.NORM));
    register("iron_lantern", new Lantern(Lantern.Type.IRON));
    register("gold_lantern", new Lantern(Lantern.Type.GOLD));
    register("cow_spawner", new Spawner(new Cow()));
		register("pig_spawner", new Spawner(new Pig()));
		register("sheep_spawner", new Spawner(new Sheep()));
		register("slime_spawner", new Spawner(new Slime(1)));
		register("zombie_spawner", new Spawner(new Zombie(1)));
		register("creeper_spawner", new Spawner(new Creeper(1)));
		register("skeleton_spawner", new Spawner(new Skeleton(1)));
		register("snake_spawner", new Spawner(new Snake(1)));
		register("knight_spawner", new Spawner(new Knight(1)));
		register("airwizard_spawner", new Spawner(new AirWizard(false)));
		
    register("tnt", new Tnt());
  }

  @Nullable
  public static Furniture get(String id) {
    return FURNITURE_REGISTER.get(id);
  }
}
