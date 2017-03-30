package com.mojang.ld22.level.tile;

import com.mojang.ld22.entity.AirWizard;
import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

public class InfiniteFallTile extends Tile {
	public InfiniteFallTile(int id) {
		super(id);
	}

	public void render(Screen screen, Level level, int x, int y) {}

	public void tick(Level level, int xt, int yt) {}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return e instanceof AirWizard?true:e instanceof Player && Player.skinon;
	}
}
