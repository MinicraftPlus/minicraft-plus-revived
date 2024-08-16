package minicraft.level.tile;

import minicraft.core.World;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.item.Item;
import minicraft.item.ToolType;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public abstract class Tile {
	public static int tickCount = 0; // A global tickCount used in the Lava & water tiles.
	protected Random random = new Random();

	/**
	 * This is used by wall tiles to get what material they're made of.
	 */
	protected enum Material {
		Wood(ToolType.Axe),
		Stone(ToolType.Pickaxe),
		Obsidian(ToolType.Pickaxe);

		public static final Material[] values = Material.values();
		private final ToolType requiredTool;

		Material(ToolType requiredTool) {
			this.requiredTool = requiredTool;
		}

		public ToolType getRequiredTool() {
			return requiredTool;
		}
	}

	public final String name;

	public short id;

	public int light = 1;
	protected boolean maySpawn = false;

	protected SpriteAnimation sprite = null;

	protected Tile(String name, SpriteAnimation sprite) {
		this.name = name.toUpperCase();
		this.sprite = sprite;
	}


	/**
	 * This method is used by tiles to specify the default "data" they have in a level's data array.
	 * Used for starting health, color/type of tile, etc.
	 */
	// At least, that was the idea at first...
	public int getDefaultData() {
		return 0;
	}

	/**
	 * Render method, used in sub-classes
	 */
	public void render(Screen screen, Level level, int x, int y) {
		sprite.render(screen, level, x, y);
	}

	public boolean maySpawn() {
		return maySpawn;
	}

	public void onTileSet(Level level, int x, int y) {}

	/**
	 * Returns if the player can walk on it, overrides in sub-classes
	 */
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}

	/**
	 * Gets the light radius of a tile, Bigger number = bigger circle
	 */
	public int getLightRadius(Level level, int x, int y) {
		return 0;
	}

	/**
	 * Hurt the tile with a specified amount of damage directly. This is supposed to be used internally.
	 * Using {@link #hurt(Level, int, int, Entity, Item, Direction, int)} is more recommended.
	 * @param level The level this happened on.
	 * @param x X position of the tile.
	 * @param y Y position of the tile.
	 * @param source The entity performed the interaction.
	 * @param item The item the entity is holding.
	 * @param dmg The damage taken.
	 */
	protected abstract void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg);

	/**
	 * What happens when you run into the tile (ex: run into a cactus)
	 */
	public void bumpedInto(Level level, int xt, int yt, Entity entity) {
	}

	/**
	 * Update method
	 */
	public boolean tick(Level level, int xt, int yt) {
		return false;
	}

	/**
	 * What happens when you are inside the tile (ex: lava)
	 */
	public void steppedOn(Level level, int xt, int yt, Entity entity) {
	}

	/**
	 * Called when you hit an item on a tile (ex: Pickaxe on rock).
	 * @param level The level the player is on.
	 * @param x X position of the player in tile coordinates (32x per tile).
	 * @param y Y position of the player in tile coordinates (32px per tile).
	 * @param source The entity attacking.
	 * @param item The item the player is currently holding.
	 * @param attackDir The direction of the player attacking.
	 * @param damage The amount of damage intended to emit this time
	 * @return Was the operation successful?
	 */
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		return false;
	}

	/**
	 * Called when you use an item on a tile (ex: Pickaxe on rock).
	 * @param level The level the player is on.
	 * @param xt X position of the player in tile coordinates (32x per tile).
	 * @param yt Y position of the player in tile coordinates (32px per tile).
	 * @param player The player who called this method.
	 * @param item The item the player is currently holding.
	 * @param attackDir The direction of the player attacking.
	 * @return Was the operation successful?
	 */
	public boolean use(Level level, int xt, int yt, Player player, @Nullable Item item, Direction attackDir) {
		return false;
	}

	/**
	 * Picks up this tile
	 * @param level
	 * @param x
	 * @param y
	 * @param player The player interacting
	 * @return the item picked up; {@code null} if picking up failed
	 */
	public @Nullable Item take(Level level, int x, int y, Player player) {
		return null;
	}

	/**
	 * Executed when the tile is exploded.
	 * The call for this method is done just before the tiles are changed to exploded tiles.
	 * @param level The level we are on.
	 * @param xt X position of the tile.
	 * @param yt Y position of the tile.
	 * @return true if successful.
	 */
	public boolean onExplode(Level level, int xt, int yt) {
		return false;
	}

	/** Whether the tile connects to grass tile in appearance. */
	public boolean connectsToGrass(Level level, int x, int y) { return false; }

	/** Whether the tile connects to sand tile in appearance. */
	public boolean connectsToSand(Level level, int x, int y) { return false; }

	/** Whether the tile connects to fluid tile in appearance. */
	public boolean connectsToFluid(Level level, int x, int y) { return false; }

	/**
	 * @deprecated This should be planned to be removed as this method is not ideally used.
	 * 	The current only usage is in {@link Level#setTile(int, int, String)}.
	 */
	@Deprecated
	public int getData(String data) {
		try {
			return Integer.parseInt(data);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	/**
	 * @deprecated Similar to {@link #getData(String)}. Also, param {@code thisData} is unused.
	 * 	The current only usage is in {@link minicraft.item.TileItem#useOn(Tile, Level, int, int, Player, Direction)}.
	 */
	@Deprecated
	public boolean matches(int thisData, String tileInfo) {
		return name.equals(tileInfo.split("_")[0]);
	}

	public String getName(int data) {
		return name;
	}

	public static String getData(int depth, int x, int y) {
		try {
			byte lvlidx = (byte) World.lvlIdx(depth);
			Level curLevel = World.levels[lvlidx];
			int pos = x + curLevel.w * y;

			int tileid = curLevel.tiles[pos];
			int tiledata = curLevel.data[pos];

			return lvlidx + ";" + pos + ";" + tileid + ";" + tiledata;
		} catch (NullPointerException | IndexOutOfBoundsException ignored) {
		}

		return "";
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Tile)) return false;
		Tile o = (Tile) other;
		return name.equals(o.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
