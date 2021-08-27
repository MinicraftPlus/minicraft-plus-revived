package minicraft.item;

import java.util.ArrayList;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
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
		super(type.name, new Sprite(0, 7, 0), count);
		this.type = type;
		this.sprite.color = type.dispColor;
	}
	
	// the return value is used to determine if the potion was used, which means being discarded.
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		return super.interactOn(applyPotion(player, type, true));
	}
	
	/// only ever called to load from file
	public static boolean applyPotion(Player player, PotionType type, int time) {
		boolean result = applyPotion(player, type, time > 0);
		if(result && time > 0) player.addPotionEffect(type, time); // overrides time
		return result;
	}
	/// main apply potion method
	public static boolean applyPotion(Player player, PotionType type, boolean addEffect) {
		if(player.getPotionEffects().containsKey(type) != addEffect) { // if hasEffect, and is disabling, or doesn't have effect, and is enabling...
			if(!type.toggleEffect(player, addEffect))
				return false; // usage failed
			
			// transmit the effect; server never uses potions without this.
			if (type.transmitEffect() && Game.isValidServer() && player instanceof RemotePlayer) 
				Game.server.getAssociatedThread((RemotePlayer)player).sendPotionEffect(type, addEffect);
		}
		
		if(addEffect && type.duration > 0) player.potioneffects.put(type, type.duration); // add it
		else player.potioneffects.remove(type);
		
		return true;
	}
	
	@Override
	public boolean equals(Item other) {
		return super.equals(other) && ((PotionItem)other).type == type;
	}
	
	@Override
	public int hashCode() { return super.hashCode() + type.name.hashCode(); }
	
	@Override
	public boolean interactsWithWorld() { return false; }
	
	public PotionItem clone() {
		return new PotionItem(type, count);
	}
}
