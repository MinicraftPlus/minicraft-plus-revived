package minicraft.level.tile;

import java.util.Random;
import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.level.Level;

public abstract class Tile {
	public static int tickCount = 0; //A global tickCount used in the Lava & water tiles.
	protected Random random = new Random();
	
	protected static enum Material {
		Wood, Stone, Obsidian;
		public static final Material[] values = Material.values();
	}
	
	public final String name;
	
	public byte id;
	
	public boolean connectsToGrass = false;
	public boolean connectsToSand = false;
	public boolean connectsToLava = false;
	public boolean connectsToWater = false;
	public int light;
	public boolean maySpawn;
	
	protected Sprite sprite;
	protected ConnectorSprite csprite;
	
	{
		light = 1;
		maySpawn = false;
		sprite = null;
		csprite = null;
	}
	
	protected Tile(String name, Sprite sprite) {
		this.name = name.toUpperCase();
		this.sprite = sprite;
	}
	protected Tile(String name, ConnectorSprite sprite) {
		this.name = name.toUpperCase();
		csprite = sprite;
	}
	
	/// ?
	public String setDataChar() {
		return null;
	}
	
	/** This method is used by tiles to specify the default "data" they have in a level's data array.
		Used for starting health, color/type of tile, etc. */
	/// at least, that was the idea at first...
	public int getDefaultData() {
		return 0;
	}
	
	/** Render method, used in sub-classes */
	public void render(Screen screen, Level level, int x, int y) {
		if(sprite != null)
			sprite.render(screen, x<<4, y<<4);
		if(csprite != null)
			csprite.render(screen, level, x, y);
	}
	
	//public abstract void updateSprite();
	
	public int getConnectColor(Level level) {
		int scolor = 0;
		if(sprite != null)
			scolor = sprite.color;
		else if(csprite != null)
			scolor = csprite.sparse.color;
		else
			return DirtTile.dCol(level.depth);
		
		return Color.seperateEncodedSprite(scolor)[3];
	}
	
	/** Returns if the player can walk on it, overrides in sub-classes  */
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}

	/*public boolean canLight() {
		return false;
	}*/
	
	/** Gets the light radius of a tile, Bigger number = bigger circle */
	public int getLightRadius(Level level, int x, int y) {
		return 0;
	}
	
	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {}
	public void hurt(Level level, int x, int y, int dmg) {}
	
	/** What happens when you run into the tile (ex: run into a cactus) */
	public void bumpedInto(Level level, int xt, int yt, Entity entity) {}
	
	/** Update method */
	public void tick(Level level, int xt, int yt) {}
	
	/** What happens when you are inside the tile (ex: lava) */
	public void steppedOn(Level level, int xt, int yt, Entity entity) {}
	
	/** What happens when you hit an item on a tile (ex: Pickaxe on rock) */
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		return false;
	}
	/*
	public boolean use(Level level, int xt, int yt, Player player, int attackDir) {
		return false;
	}
	*/
	/** Sees if the tile connects to Water or Lava. */
	public boolean connectsToLiquid() {
		return connectsToWater || connectsToLava;
	}
	
	public int getData(String data) {
		try {
			return Integer.parseInt(data);
		} catch(NumberFormatException ex) {
			return 0;
		}
	}
	
	public boolean matches(Tile other) {
		return name == other.name;
	}
	
	public boolean matches(int thisData, String otherTile) {
		return name.equals(otherTile.split("_")[0]);
	}
	
	public String getName(int data) {
		return name;
	}
	
	public static String getData(int depth, int x, int y) {
		try {
			byte lvlidx = (byte) Game.lvlIdx(depth);
			Level curLevel = Game.levels[lvlidx];
			int pos = x + curLevel.w * y;
			
			int tileid = curLevel.tiles[pos];
			int tiledata = curLevel.data[pos];
			
			return lvlidx+";"+pos+";"+tileid+";"+tiledata;
		} catch(NullPointerException ex) {
		} catch(IndexOutOfBoundsException ex) {
		}
		
		return "";
	}
}
