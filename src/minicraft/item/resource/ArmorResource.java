package minicraft.item.resource;

import minicraft.entity.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ArmorResource extends Resource {
	private int armor;
	private int staminaCost;
	public int level;

	public ArmorResource(String name, int sprite, int color, int health, int level, int staminaCost) {
		super(name, sprite, color);
		this.armor = health;
		this.level = level;
		this.staminaCost = staminaCost;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (player.curArmor == null && player.payStamina(staminaCost)) {
			player.curArmor = this; // set the current armor being worn to this.
			player.armor = ((Double)(armor/10.0*player.maxArmor)).intValue(); // armor is how many hits are left
			return true;
		}
		return false;
	}
}
