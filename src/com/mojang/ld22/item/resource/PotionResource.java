package com.mojang.ld22.item.resource;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

import java.util.HashMap;

public class PotionResource extends Resource {

	private static final String[] potionTypes = {"Potion", "Speed", "Light", "Swim", "Energy", "Regen", "Time", "Lava", "Shield", "Haste"};
	private static final Integer[] potionDurations = {0, 4200, 6000, 4800, 8400, 1800, 1800, 7200, 5400, 4800};
	private static final int[] potionColors = {0, 10, 440, 2, 510, 464, 222, 400, 115, 303};
	
	public String type;
	public int typeidx, duration, color;
	
	public PotionResource(String name, int sprite, int color, int potiontype) {
		super(name, sprite, color);
		typeidx = potiontype;
		type = potionTypes[typeidx];
		duration = potionDurations[typeidx];
		color = potionColors[typeidx];
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		
		if(!player.potioneffects.contains(type)) {
			// if the player does not yet have this potion effect...
			player.potioneffects.add(type); // add it
			player.potioneffectstime.add(duration); // add the time this effect will last
			
			switch(type) { // apply the potion effect... though, this may not be necessary, as Player.java seems to check and apply the effects anyway...
				case "Speed": player.speed = 2.0D; break;
				case "Light": player.light = 2.5D; break;
				case "Swim": player.infswim = true; break;
				case "Energy": player.infstamina = true; break;
				case "Regen": player.regen = true; break;
				case "Time": player.slowtime = true; break;
				case "Lava": player.lavaimmune = true; break;
				case "Shield": player.shield = true; break;
				case "Haste": player.haste = true; break;
				default: return false;
			}
			return true;
			
		} else { // the player already has this potion effect; refresh it.
			for(int i = 0; i < player.potioneffects.size(); i++) {
				if(((String)player.potioneffects.get(i)).equals(type)) { // find the effect in the list
					player.potioneffectstime.set(i, duration); // set the corrosponding duration to the max.
					return true;
				}
			}
		}
		
		// the code should never reach here; it would have to think it's in the array, but then not find it.
		return false;
	}

	public static int potionColor(String name) {
		for(int i = 0; i < potionTypes.length; i++)
			if(potionTypes[i] == name)
				return potionColors[i]; // because all the potion attributes are aligned, once you find the index of the name, that's the index of the color.
		
		return 0;
	}
}
