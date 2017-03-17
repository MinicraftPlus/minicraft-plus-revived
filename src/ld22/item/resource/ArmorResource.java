package com.mojang.ld22.item.resource;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

public class ArmorResource extends Resource {
	private int heal;
	private int armor;
	private int staminaCost;

	public ArmorResource(String name, int sprite, int color, int heal, int staminaCost) {
		super(name, sprite, color);
		this.heal = heal;
		this.armor = heal;
		this.staminaCost = staminaCost;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (player.maxArmor < 1 && player.payStamina(staminaCost)) {
			player.maxArmor = heal;
			return true;
		}
		return false;
	}
}
