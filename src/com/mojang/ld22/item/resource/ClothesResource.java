package com.mojang.ld22.item.resource;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

public class ClothesResource extends Resource {

	int red;
	int blue;
	int green;


	public ClothesResource(String name, int sprite, int color, int r, int g, int b) {
		super(name, sprite, color);
		red = r;
		blue = b;
		green = g;
	}
	
	// put on clothes
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if(player.r == red && player.g == green && player.b == blue) {
			return false;
		} else {
			player.r = red;
			player.b = blue;
			player.g = green;
			return true;
		}
	}
}
