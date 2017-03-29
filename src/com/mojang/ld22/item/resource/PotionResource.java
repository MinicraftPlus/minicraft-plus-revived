package com.mojang.ld22.item.resource;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import java.util.HashMap;

public class PotionResource extends Resource {

	/*private static final String[] potionTypes = {"Potion", "Speed", "Light", "Swim", "Energy", "Regen", "Time", "Lava", "Shield", "Haste"};
	private static final Integer[] potionDurations = {0, 4200, 6000, 4800, 8400, 1800, 1800, 7200, 5400, 4800};
	private static final int[] potionColors = {0, 10, 440, 2, 510, 464, 222, 400, 115, 303};
	*/
	private static HashMap<String, PotionResource> potions = new HashMap<String, PotionResource>();
	
	public String type;
	public int duration, color;
	
	public PotionResource(String name, int sprite, int bottleColor, int duration, int dispColor) {//, int potiontype) {
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
				case "Speed": player.moveSpeed += (int)( addEffect ? 1 : (player.moveSpeed > 1 ? -1 : 0) ); break;
				case "Time": Game.gamespeed *= (addEffect ? 0.5f : 2);
				//case "Light": player.light = (addEffect?2.5D:1.0D); break;
			}
		}
		
		if(addEffect) player.potioneffects.put(type, time); // add it
		else player.potioneffects.remove(type);
		
		return true;
	}
	
	public static int potionColor(String name) {
		return potions.get(name).color; // it's so easy like this...
		/*for(int i = 0; i < potionTypes.length; i++)
			if(potionTypes[i] == name)
				return potionColors[i]; // because all the potion attributes are aligned, once you find the index of the name, that's the index of the color.
		
		return 0;
		*/
	}
	/*
	public static class PotionEffect
	{
		public String type;
		public float time;
		
		public PotionEffect(int typeidx) {
			type = PotionResource.potionTypes[typeidx];
			time = PotionResource.potionDurations[typeidx];
		}
		
		public void togglePotionEffect(Player player, boolean addEffect) {
			switch(type) { // apply the potion effect... though, this may not be necessary, as Player.java seems to check and apply the effects anyway...
				case "Speed": player.moveSpeed += (addEffect?1:(player.moveSpeed>1?-1:0)); break;
				case "Light": player.light = (addEffect?2.5D:1.0D); break;
				case "Swim": player.infswim = addEffect; break;
				case "Energy": player.infstamina = addEffect; break;
				case "Regen": player.regen = addEffect; break;
				case "Time": player.slowtime = addEffect; Game.gamespeed *= (addEffect?0.5f:2); break;
				case "Lava": player.lavaimmune = addEffect; break;
				case "Shield": player.shield = addEffect; break;
				case "Haste": player.haste = addEffect; break;
				default: System.out.println("ERROR: attempted to apply invalid potion effect to : " + type);
			}
		}
	}
	*/
}
