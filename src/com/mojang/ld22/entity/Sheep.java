package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.gfx.MobSprite;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;

public class Sheep extends PassiveMob {
	private static MobSprite[][] sprites = MobSprite.compileMobSpriteAnimations(10, 18);
	
	public Sheep() {
		super(sprites);
		
		col0 = Color.get(-1, 000, 444, 321);
		col1 = Color.get(-1, 000, 555, 432);
		col2 = Color.get(-1, 000, 333, 210);
		col3 = Color.get(-1, 000, 222, 100);
		col4 = Color.get(-1, 000, 444, 432);
	}
	
	public void render(Screen screen) {
		if (isLight()) {
			col0 = col1 = col2 = col3 = col4 = Color.get(-1, 000, 555, 432);
		} else {
			col0 = Color.get(-1, 000, 444, 321);
			col1 = Color.get(-1, 000, 555, 432);
			col2 = Color.get(-1, 000, 333, 210);
			col3 = Color.get(-1, 000, 222, 100);
			col4 = Color.get(-1, 000, 444, 432);
		}
		
		if (level.dirtColor == 322) {
			if (Game.time == 0) col = col0;
			if (Game.time == 1) col = col1;
			if (Game.time == 2) col = col2;
			if (Game.time == 3) col = col3;
		} else col = col4;
		
		super.render(screen);
	}

	public boolean canWool() {
		return true;
	}

	protected void die() {
		int min = 0, max = 0;
		if (OptionsMenu.diff == OptionsMenu.easy) {min = 1; max = 3;}
		if (OptionsMenu.diff == OptionsMenu.norm) {min = 1; max = 2;}
		if (OptionsMenu.diff == OptionsMenu.hard) {min = 0; max = 2;}
		
		dropResource(min, max, Resource.wool);
		
		super.die();
	}
}
