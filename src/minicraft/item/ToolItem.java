package minicraft.item;

import java.util.Random;
import java.util.ArrayList;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.ModeMenu;

public class ToolItem extends Item {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		
		/// hmm... ToolType is going me ideas... TODO perhaps all item types should have something like this.
		/// yeah, yeah! And then they define all the nitty-gritty stuff like making each and every version, and this class can just be a bounch of loops!
		/// ... I hope that will work...
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
		Color.get(-1, 100, 321, 055), // gem
	};
	
	public static final int[] BOW_COLORS = { // Colors of the bows, specifically.
		Color.get(-1, 100, 444, 431),
		Color.get(-1, 100, 444, 111),
		Color.get(-1, 100, 444, 555),
		Color.get(-1, 100, 444, 550),
		Color.get(-1, 100, 444, 055),
	};
	
	/** Tool Item, requires a tool type (ToolType.Sword, ToolType.Axe, ToolType.Hoe, etc) and a level (0 = wood, 2 = iron, 4 = gem, etc) */
	public ToolItem(ToolType type, int level) {
		super(type.name().equals("FishingRod")?"Fishing Rod":LEVEL_NAMES[level]+" "+type.name(), new Sprite(type.sprite, 5, getColor(type, level)));
		
		this.type = type;
		this.level = level;
		
		dur = type.durability; // initial durability fetched from the ToolType
	}
	
	private static int getColor(ToolType type, int level) {
		int col = 0;
		if (type == ToolType.Bow)
			col = BOW_COLORS[level];
		else if(type == ToolType.FishingRod)
			col = Color.get(-1, 320, 320, 444);
		else
			col = LEVEL_COLORS[level];
		
		return col;
	}
	/*
	public int getSprite() {
		return type.sprite + 5 * 32;
	}
	*/
	/*public void renderIcon(Screen screen, int x, int y) {
		sprite.render(screen, x, y);//screen.render(x, y, getSprite(), getColor(), 0);
	}*/
	
	/** Renders the icon & name of this tool for inventory/crafting purposes. */
	/*public void renderInventory(Screen screen, int x, int y) {
		sprite.render(screen, x, y);
		Font.draw(name, screen, x + 8, y, Color.get(-1, 555));
	}*/
	
	/** Gets the name of this tool (and it's type) */
	public String getName() {
		if (type == ToolType.FishingRod) return "Fishing Rod";
		return LEVEL_NAMES[level] + " " + type;
	}
	
	//public void onTake(ItemEntity itemEntity) {}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (type == ToolType.FishingRod && dur > 0) {
			if (tile == Tile.water) {
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
	
	/** Gets the attack damage bonus from an item/tool (sword/axe) */
	public int getAttackDamageBonus(Entity e) {
		if (type == ToolType.Axe) {
			return (level + 1) * 2 + random.nextInt(4); // axes: (level + 1) * 2 + random number beteween 0 and 3, do slightly less damage than swords.
		}
		if (type == ToolType.Sword) {
			return (level + 1) * 3 + random.nextInt(2 + level * level * 2); //swords: (level + 1) * 3 + random number between 0 and (2 + level * level * 2)
		}
		if (type == ToolType.Claymore) {
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
	
	public ToolItem clone() {
		ToolItem ti = new ToolItem(type, level);
		ti.dur = dur;
		return ti;
	}
}
