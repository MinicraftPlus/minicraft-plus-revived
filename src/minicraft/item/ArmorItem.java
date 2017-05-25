package minicraft.item;

import java.util.ArrayList;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ArmorItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		
		items.add(new ArmorItem("Leather Armor", new Sprite(3, 12, Color.get(-1, 100, 211, 322)), 3, 1));
		items.add(new ArmorItem("Snake Armor", new Sprite(3, 12, Color.get(-1, 10, 20, 30)), 4, 2));
		items.add(new ArmorItem("Iron Armor", new Sprite(3, 12, Color.get(-1, 100, 322, 544)), 5, 3));
		items.add(new ArmorItem("Gold Armor", new Sprite(3, 12, Color.get(-1, 110, 330, 553)), 7, 4));
		items.add(new ArmorItem("Gem Armor", new Sprite(3, 12, Color.get(-1, 101, 404, 545)), 10, 5));
		
		return items;
	}
	
	private int armor;
	private int staminaCost;
	public int level;
	
	private ArmorItem(String name, Sprite sprite, int health, int level) { this(name, sprite, 1, health, level); }
	private ArmorItem(String name, Sprite sprite, int count, int health, int level) {
		super(name, sprite, count);
		this.armor = health;
		this.level = level;
		staminaCost = 9;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (player.curArmor == null && player.payStamina(staminaCost)) {
			player.curArmor = this; // set the current armor being worn to this.
			player.armor = ((Double)(armor/10.0*player.maxArmor)).intValue(); // armor is how many hits are left
			return true;
		}
		return false;
	}
	
	public ArmorItem clone() {
		return new ArmorItem(name, sprite, count, armor, level);
	}
}
