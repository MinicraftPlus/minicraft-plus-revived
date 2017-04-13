package com.mojang.ld22.item.resource;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

public class ArmorResource extends Resource {
	//private int heal;
	private int armor;
	private int staminaCost;
	public int level;

	public ArmorResource(String name, int sprite, int color, int health, int level, int staminaCost) {
		super(name, sprite, color);
		//this.heal = heal;
		this.armor = health;
		this.level = level;
		this.staminaCost = staminaCost;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (player.curArmor == null && player.payStamina(staminaCost)) {
			player.curArmor = this; // set the current armor being worn to this.
			player.armor = ((Double)(armor/10.0*player.maxArmor)).intValue();//level * (50 + level * 10); // armor is how many hits are left
			return true;
		}
		return false;
	}
}
