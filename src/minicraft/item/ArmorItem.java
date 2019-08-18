package minicraft.item;

import java.util.ArrayList;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ArmorItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		items.add(new ArmorItem("Leather Armor", new Sprite(3, 12, Color.get(-1, 100, 211, 322)), .3f, 1));
		items.add(new ArmorItem("Snake Armor", new Sprite(3, 12, Color.get(-1, 10, 20, 30)), .4f, 2));
		items.add(new ArmorItem("Iron Armor", new Sprite(3, 12, Color.get(-1, 100, 322, 544)), .5f, 3));
		items.add(new ArmorItem("Gold Armor", new Sprite(3, 12, Color.get(-1, 110, 330, 553)), .7f, 4));
		items.add(new ArmorItem("Gem Armor", new Sprite(3, 12, Color.get(-1, 101, 404, 545)), 1f, 5));
		
		return items;
	}
	
	private final float armor;
	private final int staminaCost;
	public final int level;
	
	private ArmorItem(String name, Sprite sprite, float health, int level) { this(name, sprite, 1, health, level); }
	private ArmorItem(String name, Sprite sprite, int count, float health, int level) {
		super(name, sprite, count);
		this.armor = health;
		this.level = level;
		staminaCost = 9;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		boolean success = false;
		if (player.curArmor == null && player.payStamina(staminaCost)) {
			player.curArmor = this; // set the current armor being worn to this.
			player.armor = (int) (armor * Player.maxArmor); // armor is how many hits are left
			success = true;
		}
		
		return super.interactOn(success);
	}
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public ArmorItem clone() {
		return new ArmorItem(getName(), sprite, count, armor, level);
	}
}
