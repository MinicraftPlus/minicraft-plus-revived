package minicraft.item;

import java.util.ArrayList;
import minicraft.core.*;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class PotionItem extends StackableItem {
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<>();
		
		for(PotionType type: PotionType.values())
			items.add(new PotionItem(type));
		
		return items;
	}
	
	public PotionType type;
	
	private PotionItem(PotionType type) { this(type, 1); }
	private PotionItem(PotionType type, int count) {
		super(type.name, new Sprite(27, 4, Color.get(-1, 333, 310, type.dispColor)), count);
		this.type = type;
	}
	
	// the return value is used to determine if the potion was used, which means being discarded.
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		return super.interactOn(applyPotion(player, type, true));
	}
	
	public static boolean applyPotion(Player player, PotionType type, int time) {
		boolean result = applyPotion(player, type, time > 0);
		if(result) player.addPotionEffect(type, time);
		return result;
	}
	/// this method is seperate from the above method b/c this is called sepeately by Load.java.
	public static boolean applyPotion(Player player, PotionType type, boolean addEffect) {
		if(type == PotionType.None) return false; // regular potions don't do anything.
		
		if(player.getPotionEffects().containsKey(type) != addEffect) { // if hasEffect, and is disabling, or doesn't have effect, and is enabling...
			type.toggleEffect(player, addEffect);
			
			if (Game.isValidServer() && player instanceof RemotePlayer) // transmit the effect
				Game.server.getAssociatedThread((RemotePlayer)player).sendPotionEffect(type, addEffect);
		}
		
		if(addEffect && type.duration > 0) player.potioneffects.put(type, type.duration); // add it
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
