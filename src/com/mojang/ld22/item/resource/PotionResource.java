package com.mojang.ld22.item.resource;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import java.util.HashMap;

public class PotionResource extends Resource {
	
	private static HashMap<String, PotionResource> potions = new HashMap<String, PotionResource>();
	
	public String type;
	public int duration, color;
	
	public PotionResource(String name, int sprite, int bottleColor, int duration, int dispColor) {
		super(name+(name.equals("Potion")?"":" Potion"), sprite, bottleColor); //color is the bottle color.
		type = name;
		this.duration = duration;
		color = dispColor; // color is the outline color when shown in the "potionEffects" display.
		
		potions.put(name, this);
	}
	
	// the return value is used to determine if the potion was used, which means being discarded.
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		return applyPotion(player, type, true);
	}
	
	public static boolean applyPotion(Player player, String type, boolean addEffect) {
		return applyPotion(player, type, addEffect?potions.get(type).duration:0);
	}
	public static boolean applyPotion(Player player, String type, int time) {
		if(type == "Potion") return false; // regular potions don't do anything.
		
		boolean addEffect = time > 0;
		
		if(player.potioneffects.containsKey(type) != addEffect) { // if hasEffect, and is disabling, or doesn't have effect, and is enabling...
			switch(type) {
				// these are the only effects that actually do anything not accounted for in other parts of the code:
				case "Speed": player.moveSpeed += (double)( addEffect ? 1 : (player.moveSpeed > 1 ? -1 : 0) ); break;
				case "Time": Game.gamespeed *= (addEffect ? 0.5f : 2);
			}
		}
		
		if(addEffect) player.potioneffects.put(type, time); // add it
		else player.potioneffects.remove(type);
		
		return true;
	}
	
	public static int potionColor(String name) {
		return potions.get(name).color; // it's so easy like this...
	}
}
