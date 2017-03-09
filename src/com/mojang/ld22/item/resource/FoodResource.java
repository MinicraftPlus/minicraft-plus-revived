package com.mojang.ld22.item.resource;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

public class FoodResource extends Resource {
	private int heal;
	private int staminaCost;
	
	public FoodResource(String name, int sprite, int color, int heal, int staminaCost) {
		super(name, sprite, color);
		this.heal = heal;
		this.staminaCost = staminaCost;
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (player.hunger < 10) {
			if( player.payStamina(staminaCost)) {
				player.hunger = player.hunger + heal;
				if (player.hunger >10) {
					player.hunger = 10;
					return true;
				}
				else {
					return true;
				}
			}
			return false;
		}
		return false;
	}
}
	