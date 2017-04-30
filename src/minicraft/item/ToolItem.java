package minicraft.item;

import java.util.Random;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.ModeMenu;

public class ToolItem extends Item {
	private Random random = new Random();
	
	public static final int MAX_LEVEL = 5; // How many different levels of tools there are
	public static final String[] LEVEL_NAMES = {"Wood", "Rock", "Iron", "Gold", "Gem"}; // The names of the different levels. A later level means a stronger tool.
	
	public ToolType type; // Type of tool (Sword, hoe, axe, pickaxe, shovel)
	public int level = 0; // Level of said tool
	public int dur; // the durability of the tool; currently only used for fishing rod.
	// TODO implement durabilities for all tools?
	
	public static final int[] LEVEL_COLORS = { // Colors of the tools, same position as LEVEL_NAMES
		Color.get(-1, 100, 321, 431), // wood
		Color.get(-1, 100, 321, 111), // rock/stone
		Color.get(-1, 100, 321, 555), // iron
		Color.get(-1, 100, 321, 550), // gold
		Color.get(-1, 100, 321, 055), // gem
	};
	
	public static final int[] BOW_COLORS = { // Colors of the bows, specifically.
		Color.get(-1, 100, 444, 431),
		Color.get(-1, 100, 444, 111),
		Color.get(-1, 100, 444, 555),
		Color.get(-1, 100, 444, 550),
		Color.get(-1, 100, 444, 055),
	};
	
	/** Tool Item, requires a tool type (ToolType.sword, ToolType.axe, ToolType.hoe, etc) and a level (0 = wood, 2 = iron, 4 = gem, etc) */
	public ToolItem(ToolType type, int level) {
		this.type = type;
		this.level = level;
		dur = type.durability; // initial durability fetched from the ToolType
	}
	
	public int getColor() {
		if (type == ToolType.bow) {
			return BOW_COLORS[level];
		} else if(type == ToolType.rod) {
			return Color.get(-1, 320, 320, 444);
		} else {
			return LEVEL_COLORS[level];
		}
	}
	
	public int getSprite() {
		return type.sprite + 5 * 32;
	}
	
	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
	}
	
	/** Renders the icon & name of this tool for inventory/crafting purposes. */
	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
		Font.draw(getName(), screen, x + 8, y, Color.get(-1, 555, 555, 555));
	}
	
	/** Gets the name of this tool (and it's type) */
	public String getName() {
		if (type == ToolType.rod) return "Fishing Rod";
		return LEVEL_NAMES[level] + " " + type.name;
	}
	
	public void onTake(ItemEntity itemEntity) {}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (type == ToolType.rod && dur > 0) {
			if (tile == Tile.water || tile == Tile.lightwater) {
				//if(minicraft.Game.debug) System.out.println("Fishing...");
				player.goFishing(player.x - 5, player.y - 5);
				if(!ModeMenu.creative) dur--;
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
	
	/** Calculates Damage */
	public int getAttackDamageBonus(Entity e) {
		if (type == ToolType.hatchet) {
			return (level + 1) * 2 + random.nextInt(3);
		}
		if (type == ToolType.axe) {
			return (level + 1) * 2 + random.nextInt(4); // axes: (level + 1) * 2 + random number beteween 0 and 3, do slightly less damage than swords.
		}
		if (type == ToolType.sword) {
			return (level + 1) * 3 + random.nextInt(2 + level * level * 2); //swords: (level + 1) * 3 + random number between 0 and (2 + level * level * 2)
		}
		if (type == ToolType.claymore) {
			return (level + 1) * 3 + random.nextInt(4 + level * level * 3);
		}
		return 1;
	}
	
	/** Sees if this item matches another. */
	public boolean matches(Item item) {
		if (item instanceof ToolItem) {
			ToolItem other = (ToolItem) item;
			if (other.type != type) return false;
			if (other.level != level) return false;
			return true;
		}
		return false;
	}
}
