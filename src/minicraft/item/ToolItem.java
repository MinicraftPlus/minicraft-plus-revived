package minicraft.item;

import java.util.ArrayList;
import java.util.Random;

import minicraft.Game;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class ToolItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		items.add(new ToolItem(ToolType.FishingRod, 0));
		for(ToolType tooltype: ToolType.values()) {
			if(tooltype == ToolType.FishingRod) continue;
			for(int lvl = 0; lvl <= 4; lvl++)
				items.add(new ToolItem(tooltype, lvl));
		}
		
		return items;
	}
	
	private Random random = new Random();
	
	public static final int MAX_LEVEL = 5; // How many different levels of tools there are
	public static final String[] LEVEL_NAMES = {"Wood", "Rock", "Iron", "Gold", "Gem"}; // The names of the different levels. A later level means a stronger tool.
	
	public ToolType type; // Type of tool (Sword, hoe, axe, pickaxe, shovel)
	public int level; // Level of said tool
	public int dur; // the durability of the tool; currently only used for fishing rod.
	// TODO implement durabilities for all tools?
	
	public static final int[] LEVEL_COLORS = { // Colors of the tools, same position as LEVEL_NAMES
		Color.get(-1, 100, 321, 431), // wood
		Color.get(-1, 100, 321, 111), // rock/stone
		Color.get(-1, 100, 321, 555), // iron
		Color.get(-1, 100, 321, 550), // gold
		Color.get(-1, 100, 321, 55), // gem
	};
	
	public static final int[] BOW_COLORS = { // Colors of the bows, specifically.
		Color.get(-1, 100, 444, 431),
		Color.get(-1, 100, 444, 111),
		Color.get(-1, 100, 444, 555),
		Color.get(-1, 100, 444, 550),
		Color.get(-1, 100, 444, 55),
	};
	
	/** Tool Item, requires a tool type (ToolType.Sword, ToolType.Axe, ToolType.Hoe, etc) and a level (0 = wood, 2 = iron, 4 = gem, etc) */
	public ToolItem(ToolType type, int level) {
		super(type.name().equals("FishingRod")?"Fishing Rod":LEVEL_NAMES[level]+" "+type.name(), new Sprite(type.sprite, 5, getColor(type, level)));
		
		this.type = type;
		this.level = level;
		
		dur = type.durability; // initial durability fetched from the ToolType
	}
	
	private static int getColor(ToolType type, int level) {
		int col;
		if (type == ToolType.Bow)
			col = BOW_COLORS[level];
		else if(type == ToolType.FishingRod)
			col = Color.get(-1, 320, 320, 444);
		else
			col = LEVEL_COLORS[level];
		
		return col;
	}
	
	/** Gets the name of this tool (and it's type) */
	public String getName() {
		if (type == ToolType.FishingRod) return "Fishing Rod";
		return LEVEL_NAMES[level] + " " + type;
	}
	
	@Override
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		if (type == ToolType.FishingRod && dur > 0) {
			if (tile == Tiles.get("water")) {
				player.goFishing(player.x - 5, player.y - 5);
				if(!Game.isMode("creative")) dur--;
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isDepleted() {
		return dur <= 0 && type.durability > 0;
	}
	
	/** You can attack mobs with tools. */
	public boolean canAttack() {
		return true;
	}
	
	/** Gets the attack damage bonus from an item/tool (sword/axe) */
	public int getAttackDamageBonus(Entity e) {
		
		if(e instanceof Mob) {
			if (type == ToolType.Axe) {
				return (level + 1) * 2 + random.nextInt(4); // wood axe damage: 2-5; gem axe damage: 10-13.
			}
			if (type == ToolType.Sword) {
				return (level + 1) * 3 + random.nextInt(2 + level * level); // wood: 3-5 damage; gem: 15-32 damage.
			}
			if (type == ToolType.Claymore) {
				return (level + 1) * 3 + random.nextInt(4 + level * level * 3); // wood: 3-6 damage; gem: 15-66 damage.
			}
			return 1; // all other tools do very little damage to mobs.
		}
		
		//if(e instanceof Spawner)
		
		return 0;
	}
	
	/** Sees if this item matches another. */
	public boolean matches(Item item) {
		if (item instanceof ToolItem) {
			ToolItem other = (ToolItem) item;
			return other.type == type && other.level == level;
		}
		return false;
	}
	
	public ToolItem clone() {
		ToolItem ti = new ToolItem(type, level);
		ti.dur = dur;
		return ti;
	}
}
