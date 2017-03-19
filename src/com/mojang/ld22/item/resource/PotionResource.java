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
			
			switch(type) { // apply the potion effect
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
		// the code should never reach here; it would have to think it's in the array, but then not find it
		return false;
		/*
		if(type == 1) {
			if(!player.potioneffects.contains("Speed")) {
				player.speed = 2.0D;
				player.potioneffects.add("Speed");
				player.potioneffectstime.add(Integer.valueOf(4200));
				return true;
			}

			for(int i = 0; i < player.potioneffects.size(); ++i) {
				if(((String)player.potioneffects.get(i)).equals("Speed")) {
					player.potioneffectstime.set(player.potioneffects.indexOf(player.potioneffects.get(i)), Integer.valueOf(4200));
					return true;
				}
			}
		}

		if(type == 2) {
			if(!player.potioneffects.contains("Light")) {
				player.light = 2.5D;
				player.potioneffects.add("Light");
				player.potioneffectstime.add(Integer.valueOf(6000));
				return true;
			}

			for(int i = 0; i < player.potioneffects.size(); ++i) {
				if(((String)player.potioneffects.get(i)).equals("Light")) {
					player.potioneffectstime.set(player.potioneffects.indexOf(player.potioneffects.get(i)), Integer.valueOf(6000));
					return true;
				}
			}
		}

		if(type == 3) {
			if(!player.potioneffects.contains("Swim")) {
				player.infswim = true;
				player.potioneffects.add("Swim");
				player.potioneffectstime.add(Integer.valueOf(4800));
				return true;
			}

			for(int i = 0; i < player.potioneffects.size(); ++i) {
				if(((String)player.potioneffects.get(i)).equals("Swim")) {
					player.potioneffectstime.set(player.potioneffects.indexOf(player.potioneffects.get(i)), Integer.valueOf(4800));
					return true;
				}
			}
		}

		if(type == 4) {
			if(!player.potioneffects.contains("Energy")) {
				player.infstamina = true;
				player.potioneffects.add("Energy");
				player.potioneffectstime.add(Integer.valueOf(8400));
				return true;
			}

			for(int i = 0; i < player.potioneffects.size(); ++i) {
				if(((String)player.potioneffects.get(i)).equals("Energy")) {
					player.potioneffectstime.set(player.potioneffects.indexOf(player.potioneffects.get(i)), Integer.valueOf(8400));
					return true;
				}
			}
		}

		if(type == 5) {
			if(!player.potioneffects.contains("Regen")) {
				player.regen = true;
				player.potioneffects.add("Regen");
				player.potioneffectstime.add(Integer.valueOf(1800));
				return true;
			}

			for(int i = 0; i < player.potioneffects.size(); ++i) {
				if(((String)player.potioneffects.get(i)).equals("Regen")) {
					player.potioneffectstime.set(player.potioneffects.indexOf(player.potioneffects.get(i)), Integer.valueOf(1800));
					return true;
				}
			}
		}

		if(type == 6) {
			if(!player.potioneffects.contains("Time")) {
				player.slowtime = true;
				player.potioneffects.add("Time");
				player.potioneffectstime.add(Integer.valueOf(1800));
				return true;
			}

			for(int i = 0; i < player.potioneffects.size(); ++i) {
				if(((String)player.potioneffects.get(i)).equals("Time")) {
					player.potioneffectstime.set(player.potioneffects.indexOf(player.potioneffects.get(i)), Integer.valueOf(1800));
					return true;
				}
			}
		}

		if(type == 7) {
			if(!player.potioneffects.contains("Lava")) {
				player.lavaimmune = true;
				player.potioneffects.add("Lava");
				player.potioneffectstime.add(Integer.valueOf(7200));
				return true;
			}

			for(int i = 0; i < player.potioneffects.size(); ++i) {
				if(((String)player.potioneffects.get(i)).equals("Lava")) {
					player.potioneffectstime.set(player.potioneffects.indexOf(player.potioneffects.get(i)), Integer.valueOf(7200));
					return true;
				}
			}
		}

		if(type == 8) {
			if(!player.potioneffects.contains("Shield")) {
				player.shield = true;
				player.potioneffects.add("Shield");
				player.potioneffectstime.add(Integer.valueOf(5400));
				return true;
			}

			for(int i = 0; i < player.potioneffects.size(); ++i) {
				if(((String)player.potioneffects.get(i)).equals("Shield")) {
					player.potioneffectstime.set(player.potioneffects.indexOf(player.potioneffects.get(i)), Integer.valueOf(3600));
					return true;
				}
			}
		}

		if(type == 9) {
			if(!player.potioneffects.contplayer.haste = true; ains("Haste")) {
				player.haste = true;
				player.potioneffects.add("Haste");
				player.potioneffectstime.add(Integer.valueOf(4800));
				return true;
			}

			for(int i = 0; i < player.potioneffects.size(); ++i) {
				if(((String)player.potioneffects.get(i)).equals("Haste")) {
					player.potioneffectstime.set(player.potioneffects.indexOf(player.potioneffects.get(i)), Integer.valueOf(4800));
					return true;
				}
			}
		}
		return false;
		*/
	}

	public static int potionColor(String name) {
		for(int i = 0; i < potionTypes.length; i++)
			if(potionTypes[i] == name)
				return potionColors[i]; // because all the potion attributes are aligned, once you find the index of the name, that's the index of the color.
		
		return 0;
		//name.equals("Speed")?10:(name.equals("Light")?440:(name.equals("Swim")?2:(name.equals("Energy")?510:(name.equals("Regen")?464:(name.equals("Time")?222:(name.equals("Lava")?400:(name.equals("Shield")?115:(name.equals("Haste")?303:0))))))));
	}
}
