package minicraft.item;

import java.util.ArrayList;
import minicraft.Game;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.entity.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class PotionItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		
		for(PotionType type: PotionType.values())
			items.add(new PotionItem(type));
		
		return items;
	}
	
	public PotionType type;
	
	public PotionItem(PotionType type) { this(type, 1); }
	public PotionItem(PotionType type, int count) {
		super(type.name, new Sprite(27, 4, Color.get(-1, 333, 310, type.dispColor)), count);
		this.type = type;
	}
	
	//private static HashMap<String, PotionItem> potions = new HashMap<String, PotionItem>();
	
	//public String type;
	//public int duration, color;
	
	/*public PotionItem(String name, int sprite, int bottleColor, int duration, int dispColor) {
		super(name+(name.equals("Potion")?"":" Potion"), sprite, bottleColor); //color is the bottle color.
		type = name;
		this.duration = duration;
		color = dispColor; // color is the outline color when shown in the "potionEffects" display.
		
		potions.put(name, this);
	}*/
	
	// the return value is used to determine if the potion was used, which means being discarded.
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		return applyPotion(player, type, true);
	}
	
	public static boolean applyPotion(Player player, PotionType type, boolean addEffect) {
		return applyPotion(player, type, addEffect?type.duration:0);
	}
	/// this method is seperate from the above method b/c this is called sepeately by Load.java.
	public static boolean applyPotion(Player player, PotionType type, int time) {
		if(type == PotionType.None) return false; // regular potions don't do anything.
		
		boolean addEffect = time > 0;
		
		if(player.potioneffects.containsKey(type) != addEffect) { // if hasEffect, and is disabling, or doesn't have effect, and is enabling...
			type.toggleEffect(player, addEffect);
		}
		
		if(addEffect) player.potioneffects.put(type, type.duration); // add it
		else player.potioneffects.remove(type);
		
		return true;
	}
	
	public boolean matches(Item other) {
		return super.matches(other) && ((PotionItem)other).type == type;
	}
	
	public PotionItem clone() {
		return new PotionItem(type, count);
	}
}
