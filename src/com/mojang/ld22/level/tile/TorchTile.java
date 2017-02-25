package com.mojang.ld22.level.tile;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;

public class TorchTile extends Tile {
	private Tile onType;
	
	public TorchTile(int id, Tile onType) {
		super(id);
		this.onType = onType;
		connectsToSand = onType.connectsToSand;
		connectsToGrass = onType.connectsToGrass;
		connectsToWater = onType.connectsToWater;
		connectsToLava = onType.connectsToLava;
	}
	

	public void render(Screen screen, Level level, int x, int y) {
		int col0 = Color.get(320, 500, 520, -1);
	    int col4 =  Color.get(320, 500, 520, -1);
	    
		onType.render(screen, level, x, y);

		if (level.dirtColor == 322){
		int col = col0;
		screen.render(x * 16 + 4, y * 16 + 4, 12 + 3 * 32, col, 0);

		}
		if (level.dirtColor != 322){
			int col = col4;
			screen.render(x * 16 + 4, y * 16 + 4, 12 + 3 * 32, col, 0);
	
	}

	}
	public void tick(Level level, int x, int y) {
			if (level.getTile(x, y - 1) == Tile.grass){
				level.setTile(x, y - 1, Tile.lightgrass, y);
			}
			if (level.getTile(x, y + 1) == Tile.grass){
				level.setTile(x, y + 1, Tile.lightgrass, y);
			}
			if (level.getTile(x - 1, y) == Tile.grass){
				level.setTile(x - 1, y, Tile.lightgrass, y);
			}
			if (level.getTile(x + 1, y) == Tile.grass){
				level.setTile(x + 1, y, Tile.lightgrass, y);
			}
			if (level.getTile(x, y - 2) == Tile.grass){
				level.setTile(x, y - 2, Tile.lightgrass, y);
			}
			if (level.getTile(x, y + 2) == Tile.grass){
				level.setTile(x, y + 2, Tile.lightgrass, y);
			}
			if (level.getTile(x - 2, y) == Tile.grass){
				level.setTile(x - 2, y, Tile.lightgrass, y);
			}
			if (level.getTile(x + 2, y) == Tile.grass){
				level.setTile(x + 2, y, Tile.lightgrass, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.grass){
				level.setTile(x - 1, y - 1, Tile.lightgrass, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.grass){
				level.setTile(x + 1, y - 1, Tile.lightgrass, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.grass){
				level.setTile(x + 1, y + 1, Tile.lightgrass, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.grass){
				level.setTile(x - 1, y + 1, Tile.lightgrass, y);
			}
			
			if (level.getTile(x, y - 1) == Tile.sand){
				level.setTile(x, y - 1, Tile.lightsand, y);
			}
			if (level.getTile(x, y + 1) == Tile.sand){
				level.setTile(x, y + 1, Tile.lightsand, y);
			}
			if (level.getTile(x - 1, y) == Tile.sand){
				level.setTile(x - 1, y, Tile.lightsand, y);
			}
			if (level.getTile(x + 1, y) == Tile.sand){
				level.setTile(x + 1, y, Tile.lightsand, y);
			}
			if (level.getTile(x, y - 2) == Tile.sand){
				level.setTile(x, y - 2, Tile.lightsand, y);
			}
			if (level.getTile(x, y + 2) == Tile.sand){
				level.setTile(x, y + 2, Tile.lightsand, y);
			}
			if (level.getTile(x - 2, y) == Tile.sand){
				level.setTile(x - 2, y, Tile.lightsand, y);
			}
			if (level.getTile(x + 2, y) == Tile.sand){
				level.setTile(x + 2, y, Tile.lightsand, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.sand){
				level.setTile(x - 1, y - 1, Tile.lightsand, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.sand){
				level.setTile(x + 1, y - 1, Tile.lightsand, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.sand){
				level.setTile(x + 1, y + 1, Tile.lightsand, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.sand){
				level.setTile(x - 1, y + 1, Tile.lightsand, y);
			}
			if (level.getTile(x, y - 1) == Tile.tree){
				level.setTile(x, y - 1, Tile.lighttree, y);
			}
			if (level.getTile(x, y + 1) == Tile.tree){
				level.setTile(x, y + 1, Tile.lighttree, y);
			}
			if (level.getTile(x - 1, y) == Tile.tree){
				level.setTile(x - 1, y, Tile.lighttree, y);
			}
			if (level.getTile(x + 1, y) == Tile.tree){
				level.setTile(x + 1, y, Tile.lighttree, y);
			}
			if (level.getTile(x, y - 2) == Tile.tree){
				level.setTile(x, y - 2, Tile.lighttree, y);
			}
			if (level.getTile(x, y + 2) == Tile.tree){
				level.setTile(x, y + 2, Tile.lighttree, y);
			}
			if (level.getTile(x - 2, y) == Tile.tree){
				level.setTile(x - 2, y, Tile.lighttree, y);
			}
			if (level.getTile(x + 2, y) == Tile.tree){
				level.setTile(x + 2, y, Tile.lighttree, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.tree){
				level.setTile(x - 1, y - 1, Tile.lighttree, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.tree){
				level.setTile(x + 1, y - 1, Tile.lighttree, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.tree){
				level.setTile(x + 1, y + 1, Tile.lighttree, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.tree){
				level.setTile(x - 1, y + 1, Tile.lighttree, y);
			}
			
			if (level.getTile(x, y - 1) == Tile.cactus){
				level.setTile(x, y - 1, Tile.lightcac, y);
			}
			if (level.getTile(x, y + 1) == Tile.cactus){
				level.setTile(x, y + 1, Tile.lightcac, y);
			}
			if (level.getTile(x - 1, y) == Tile.cactus){
				level.setTile(x - 1, y, Tile.lightcac, y);
			}
			if (level.getTile(x + 1, y) == Tile.cactus){
				level.setTile(x + 1, y, Tile.lightcac, y);
			}
			if (level.getTile(x, y - 2) == Tile.cactus){
				level.setTile(x, y - 2, Tile.lightcac, y);
			}
			if (level.getTile(x, y + 2) == Tile.cactus){
				level.setTile(x, y + 2, Tile.lightcac, y);
			}
			if (level.getTile(x - 2, y) == Tile.cactus){
				level.setTile(x - 2, y, Tile.lightcac, y);
			}
			if (level.getTile(x + 2, y) == Tile.cactus){
				level.setTile(x + 2, y, Tile.lightcac, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.cactus){
				level.setTile(x - 1, y - 1, Tile.lightcac, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.cactus){
				level.setTile(x + 1, y - 1, Tile.lightcac, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.cactus){
				level.setTile(x + 1, y + 1, Tile.lightcac, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.cactus){
				level.setTile(x - 1, y + 1, Tile.lightcac, y);
			}
			
			if (level.getTile(x, y - 1) == Tile.water){
				level.setTile(x, y - 1, Tile.lightwater, y);
			}
			if (level.getTile(x, y + 1) == Tile.water){
				level.setTile(x, y + 1, Tile.lightwater, y);
			}
			if (level.getTile(x - 1, y) == Tile.water){
				level.setTile(x - 1, y, Tile.lightwater, y);
			}
			if (level.getTile(x + 1, y) == Tile.water){
				level.setTile(x + 1, y, Tile.lightwater, y);
			}
			if (level.getTile(x, y - 2) == Tile.water){
				level.setTile(x, y - 2, Tile.lightwater, y);
			}
			if (level.getTile(x, y + 2) == Tile.water){
				level.setTile(x, y + 2, Tile.lightwater, y);
			}
			if (level.getTile(x - 2, y) == Tile.water){
				level.setTile(x - 2, y, Tile.lightwater, y);
			}
			if (level.getTile(x + 2, y) == Tile.water){
				level.setTile(x + 2, y, Tile.lightwater, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.water){
				level.setTile(x - 1, y - 1, Tile.lightwater, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.water){
				level.setTile(x + 1, y - 1, Tile.lightwater, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.water){
				level.setTile(x + 1, y + 1, Tile.lightwater, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.water){
				level.setTile(x - 1, y + 1, Tile.lightwater, y);
			}
			if (level.getTile(x, y - 1) == Tile.dirt){
				level.setTile(x, y - 1, Tile.lightdirt, y);
			}
			if (level.getTile(x, y + 1) == Tile.dirt){
				level.setTile(x, y + 1, Tile.lightdirt, y);
			}
			if (level.getTile(x - 1, y) == Tile.dirt){
				level.setTile(x - 1, y, Tile.lightdirt, y);
			}
			if (level.getTile(x + 1, y) == Tile.dirt){
				level.setTile(x + 1, y, Tile.lightdirt, y);
			}
			if (level.getTile(x, y - 2) == Tile.dirt){
				level.setTile(x, y - 2, Tile.lightdirt, y);
			}
			if (level.getTile(x, y + 2) == Tile.dirt){
				level.setTile(x, y + 2, Tile.lightdirt, y);
			}
			if (level.getTile(x - 2, y) == Tile.dirt){
				level.setTile(x - 2, y, Tile.lightdirt, y);
			}
			if (level.getTile(x + 2, y) == Tile.dirt){
				level.setTile(x + 2, y, Tile.lightdirt, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.dirt){
				level.setTile(x - 1, y - 1, Tile.lightdirt, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.dirt){
				level.setTile(x + 1, y - 1, Tile.lightdirt, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.dirt){
				level.setTile(x + 1, y + 1, Tile.lightdirt, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.dirt){
				level.setTile(x - 1, y + 1, Tile.lightdirt, y);
			}
			if (level.getTile(x, y - 1) == Tile.flower){
				level.setTile(x, y - 1, Tile.lightflower, y);
			}
			if (level.getTile(x, y + 1) == Tile.flower){
				level.setTile(x, y + 1, Tile.lightflower, y);
			}
			if (level.getTile(x - 1, y) == Tile.flower){
				level.setTile(x - 1, y, Tile.lightflower, y);
			}
			if (level.getTile(x + 1, y) == Tile.flower){
				level.setTile(x + 1, y, Tile.lightflower, y);
			}
			if (level.getTile(x, y - 2) == Tile.flower){
				level.setTile(x, y - 2, Tile.lightflower, y);
			}
			if (level.getTile(x, y + 2) == Tile.flower){
				level.setTile(x, y + 2, Tile.lightflower, y);
			}
			if (level.getTile(x - 2, y) == Tile.flower){
				level.setTile(x - 2, y, Tile.lightflower, y);
			}
			if (level.getTile(x + 2, y) == Tile.flower){
				level.setTile(x + 2, y, Tile.lightflower, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.flower){
				level.setTile(x - 1, y - 1, Tile.lightflower, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.flower){
				level.setTile(x + 1, y - 1, Tile.lightflower, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.flower){
				level.setTile(x + 1, y + 1, Tile.lightflower, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.flower){
				level.setTile(x - 1, y + 1, Tile.lightflower, y);
			}
			if (level.getTile(x, y - 1) == Tile.stairsUp){
				level.setTile(x, y - 1, Tile.lightstairsUp, y);
			}
			if (level.getTile(x, y + 1) == Tile.stairsUp){
				level.setTile(x, y + 1, Tile.lightstairsUp, y);
			}
			if (level.getTile(x - 1, y) == Tile.stairsUp){
				level.setTile(x - 1, y, Tile.lightstairsUp, y);
			}
			if (level.getTile(x + 1, y) == Tile.stairsUp){
				level.setTile(x + 1, y, Tile.lightstairsUp, y);
			}
			if (level.getTile(x, y - 2) == Tile.stairsUp){
				level.setTile(x, y - 2, Tile.lightstairsUp, y);
			}
			if (level.getTile(x, y + 2) == Tile.stairsUp){
				level.setTile(x, y + 2, Tile.lightstairsUp, y);
			}
			if (level.getTile(x - 2, y) == Tile.stairsUp){
				level.setTile(x - 2, y, Tile.lightstairsUp, y);
			}
			if (level.getTile(x + 2, y) == Tile.stairsUp){
				level.setTile(x + 2, y, Tile.lightstairsUp, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.stairsUp){
				level.setTile(x - 1, y - 1, Tile.lightstairsUp, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.stairsUp){
				level.setTile(x + 1, y - 1, Tile.lightstairsUp, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.stairsUp){
				level.setTile(x + 1, y + 1, Tile.lightstairsUp, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.stairsUp){
				level.setTile(x - 1, y + 1, Tile.lightstairsUp, y);
			}
			if (level.getTile(x, y - 1) == Tile.stairsDown){
				level.setTile(x, y - 1, Tile.lightstairsDown, y);
			}
			if (level.getTile(x, y + 1) == Tile.stairsDown){
				level.setTile(x, y + 1, Tile.lightstairsDown, y);
			}
			if (level.getTile(x - 1, y) == Tile.stairsDown){
				level.setTile(x - 1, y, Tile.lightstairsDown, y);
			}
			if (level.getTile(x + 1, y) == Tile.stairsDown){
				level.setTile(x + 1, y, Tile.lightstairsDown, y);
			}
			if (level.getTile(x, y - 2) == Tile.stairsDown){
				level.setTile(x, y - 2, Tile.lightstairsDown, y);
			}
			if (level.getTile(x, y + 2) == Tile.stairsDown){
				level.setTile(x, y + 2, Tile.lightstairsDown, y);
			}
			if (level.getTile(x - 2, y) == Tile.stairsDown){
				level.setTile(x - 2, y, Tile.lightstairsDown, y);
			}
			if (level.getTile(x + 2, y) == Tile.stairsDown){
				level.setTile(x + 2, y, Tile.lightstairsDown, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.stairsDown){
				level.setTile(x - 1, y - 1, Tile.lightstairsDown, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.stairsDown){
				level.setTile(x + 1, y - 1, Tile.lightstairsDown, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.stairsDown){
				level.setTile(x + 1, y + 1, Tile.lightstairsDown, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.stairsDown){
				level.setTile(x - 1, y + 1, Tile.lightstairsDown, y);
			}
			
			if (level.getTile(x, y - 1) == Tile.plank){
				level.setTile(x, y - 1, Tile.lightplank, y);
			}
			if (level.getTile(x, y + 1) == Tile.plank){
				level.setTile(x, y + 1, Tile.lightplank, y);
			}
			if (level.getTile(x - 1, y) == Tile.plank){
				level.setTile(x - 1, y, Tile.lightplank, y);
			}
			if (level.getTile(x + 1, y) == Tile.plank){
				level.setTile(x + 1, y, Tile.lightplank, y);
			}
			if (level.getTile(x, y - 2) == Tile.plank){
				level.setTile(x, y - 2, Tile.lightplank, y);
			}
			if (level.getTile(x, y + 2) == Tile.plank){
				level.setTile(x, y + 2, Tile.lightplank, y);
			}
			if (level.getTile(x - 2, y) == Tile.plank){
				level.setTile(x - 2, y, Tile.lightplank, y);
			}
			if (level.getTile(x + 2, y) == Tile.plank){
				level.setTile(x + 2, y, Tile.lightplank, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.plank){
				level.setTile(x - 1, y - 1, Tile.lightplank, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.plank){
				level.setTile(x + 1, y - 1, Tile.lightplank, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.plank){
				level.setTile(x + 1, y + 1, Tile.lightplank, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.plank){
				level.setTile(x - 1, y + 1, Tile.lightplank, y);
			}
			if (level.getTile(x, y - 1) == Tile.sbrick){
				level.setTile(x, y - 1, Tile.lightsbrick, y);
			}
			if (level.getTile(x, y + 1) == Tile.sbrick){
				level.setTile(x, y + 1, Tile.lightsbrick, y);
			}
			if (level.getTile(x - 1, y) == Tile.sbrick){
				level.setTile(x - 1, y, Tile.lightsbrick, y);
			}
			if (level.getTile(x + 1, y) == Tile.sbrick){
				level.setTile(x + 1, y, Tile.lightsbrick, y);
			}
			if (level.getTile(x, y - 2) == Tile.sbrick){
				level.setTile(x, y - 2, Tile.lightsbrick, y);
			}
			if (level.getTile(x, y + 2) == Tile.sbrick){
				level.setTile(x, y + 2, Tile.lightsbrick, y);
			}
			if (level.getTile(x - 2, y) == Tile.sbrick){
				level.setTile(x - 2, y, Tile.lightsbrick, y);
			}
			if (level.getTile(x + 2, y) == Tile.sbrick){
				level.setTile(x + 2, y, Tile.lightsbrick, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.sbrick){
				level.setTile(x - 1, y - 1, Tile.lightsbrick, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.sbrick){
				level.setTile(x + 1, y - 1, Tile.lightsbrick, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.sbrick){
				level.setTile(x + 1, y + 1, Tile.lightsbrick, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.sbrick){
				level.setTile(x - 1, y + 1, Tile.lightsbrick, y);
			}
			if (level.getTile(x, y - 1) == Tile.wdo){
				level.setTile(x, y - 1, Tile.lwdo, y);
			}
			if (level.getTile(x, y + 1) == Tile.wdo){
				level.setTile(x, y + 1, Tile.lwdo, y);
			}
			if (level.getTile(x - 1, y) == Tile.wdo){
				level.setTile(x - 1, y, Tile.lwdo, y);
			}
			if (level.getTile(x + 1, y) == Tile.wdo){
				level.setTile(x + 1, y, Tile.lwdo, y);
			}
			if (level.getTile(x, y - 2) == Tile.wdo){
				level.setTile(x, y - 2, Tile.lwdo, y);
			}
			if (level.getTile(x, y + 2) == Tile.wdo){
				level.setTile(x, y + 2, Tile.lwdo, y);
			}
			if (level.getTile(x - 2, y) == Tile.wdo){
				level.setTile(x - 2, y, Tile.lwdo, y);
			}
			if (level.getTile(x + 2, y) == Tile.wdo){
				level.setTile(x + 2, y, Tile.lwdo, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.wdo){
				level.setTile(x - 1, y - 1, Tile.lwdo, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.wdo){
				level.setTile(x + 1, y - 1, Tile.lwdo, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.wdo){
				level.setTile(x + 1, y + 1, Tile.lwdo, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.wdo){
				level.setTile(x - 1, y + 1, Tile.lwdo, y);
			}
			if (level.getTile(x, y - 1) == Tile.wdc){
				level.setTile(x, y - 1, Tile.lwdc, y);
			}
			if (level.getTile(x, y + 1) == Tile.wdc){
				level.setTile(x, y + 1, Tile.lwdc, y);
			}
			if (level.getTile(x - 1, y) == Tile.wdc){
				level.setTile(x - 1, y, Tile.lwdc, y);
			}
			if (level.getTile(x + 1, y) == Tile.wdc){
				level.setTile(x + 1, y, Tile.lwdc, y);
			}
			if (level.getTile(x, y - 2) == Tile.wdc){
				level.setTile(x, y - 2, Tile.lwdc, y);
			}
			if (level.getTile(x, y + 2) == Tile.wdc){
				level.setTile(x, y + 2, Tile.lwdc, y);
			}
			if (level.getTile(x - 2, y) == Tile.wdc){
				level.setTile(x - 2, y, Tile.lwdc, y);
			}
			if (level.getTile(x + 2, y) == Tile.wdc){
				level.setTile(x + 2, y, Tile.lwdc, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.wdc){
				level.setTile(x - 1, y - 1, Tile.lwdc, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.wdc){
				level.setTile(x + 1, y - 1, Tile.lwdc, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.wdc){
				level.setTile(x + 1, y + 1, Tile.lwdc, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.wdc){
				level.setTile(x - 1, y + 1, Tile.lwdc, y);
			}
			if (level.getTile(x, y - 1) == Tile.sdo){
				level.setTile(x, y - 1, Tile.lsdo, y);
			}
			if (level.getTile(x, y + 1) == Tile.sdo){
				level.setTile(x, y + 1, Tile.lsdo, y);
			}
			if (level.getTile(x - 1, y) == Tile.sdo){
				level.setTile(x - 1, y, Tile.lsdo, y);
			}
			if (level.getTile(x + 1, y) == Tile.sdo){
				level.setTile(x + 1, y, Tile.lsdo, y);
			}
			if (level.getTile(x, y - 2) == Tile.sdo){
				level.setTile(x, y - 2, Tile.lsdo, y);
			}
			if (level.getTile(x, y + 2) == Tile.sdo){
				level.setTile(x, y + 2, Tile.lsdo, y);
			}
			if (level.getTile(x - 2, y) == Tile.sdo){
				level.setTile(x - 2, y, Tile.lsdo, y);
			}
			if (level.getTile(x + 2, y) == Tile.sdo){
				level.setTile(x + 2, y, Tile.lsdo, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.sdo){
				level.setTile(x - 1, y - 1, Tile.lsdo, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.sdo){
				level.setTile(x + 1, y - 1, Tile.lsdo, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.sdo){
				level.setTile(x + 1, y + 1, Tile.lsdo, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.sdo){
				level.setTile(x - 1, y + 1, Tile.lsdo, y);
			}
			if (level.getTile(x, y - 1) == Tile.sdc){
				level.setTile(x, y - 1, Tile.lsdc, y);
			}
			if (level.getTile(x, y + 1) == Tile.sdc){
				level.setTile(x, y + 1, Tile.lsdc, y);
			}
			if (level.getTile(x - 1, y) == Tile.sdc){
				level.setTile(x - 1, y, Tile.lsdc, y);
			}
			if (level.getTile(x + 1, y) == Tile.sdc){
				level.setTile(x + 1, y, Tile.lsdc, y);
			}
			if (level.getTile(x, y - 2) == Tile.sdc){
				level.setTile(x, y - 2, Tile.lsdc, y);
			}
			if (level.getTile(x, y + 2) == Tile.sdc){
				level.setTile(x, y + 2, Tile.lsdc, y);
			}
			if (level.getTile(x - 2, y) == Tile.sdc){
				level.setTile(x - 2, y, Tile.lsdc, y);
			}
			if (level.getTile(x + 2, y) == Tile.sdc){
				level.setTile(x + 2, y, Tile.lsdc, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.sdc){
				level.setTile(x - 1, y - 1, Tile.lsdc, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.sdc){
				level.setTile(x + 1, y - 1, Tile.lsdc, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.sdc){
				level.setTile(x + 1, y + 1, Tile.lsdc, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.sdc){
				level.setTile(x - 1, y + 1, Tile.lsdc, y);
			}
			if (level.getTile(x, y - 1) == Tile.hole){
				level.setTile(x, y - 1, Tile.lighthole, y);
			}
			if (level.getTile(x, y + 1) == Tile.hole){
				level.setTile(x, y + 1, Tile.lighthole, y);
			}
			if (level.getTile(x - 1, y) == Tile.hole){
				level.setTile(x - 1, y, Tile.lighthole, y);
			}
			if (level.getTile(x + 1, y) == Tile.hole){
				level.setTile(x + 1, y, Tile.lighthole, y);
			}
			if (level.getTile(x, y - 2) == Tile.hole){
				level.setTile(x, y - 2, Tile.lighthole, y);
			}
			if (level.getTile(x, y + 2) == Tile.hole){
				level.setTile(x, y + 2, Tile.lighthole, y);
			}
			if (level.getTile(x - 2, y) == Tile.hole){
				level.setTile(x - 2, y, Tile.lighthole, y);
			}
			if (level.getTile(x + 2, y) == Tile.hole){
				level.setTile(x + 2, y, Tile.lighthole, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.hole){
				level.setTile(x - 1, y - 1, Tile.lighthole, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.hole){
				level.setTile(x + 1, y - 1, Tile.lighthole, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.hole){
				level.setTile(x + 1, y + 1, Tile.lighthole, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.hole){
				level.setTile(x - 1, y + 1, Tile.lighthole, y);
			}
			if (level.getTile(x, y - 1) == Tile.wool){
				level.setTile(x, y - 1, Tile.lightwool, y);
			}
			if (level.getTile(x, y + 1) == Tile.wool){
				level.setTile(x, y + 1, Tile.lightwool, y);
			}
			if (level.getTile(x - 1, y) == Tile.wool){
				level.setTile(x - 1, y, Tile.lightwool, y);
			}
			if (level.getTile(x + 1, y) == Tile.wool){
				level.setTile(x + 1, y, Tile.lightwool, y);
			}
			if (level.getTile(x, y - 2) == Tile.wool){
				level.setTile(x, y - 2, Tile.lightwool, y);
			}
			if (level.getTile(x, y + 2) == Tile.wool){
				level.setTile(x, y + 2, Tile.lightwool, y);
			}
			if (level.getTile(x - 2, y) == Tile.wool){
				level.setTile(x - 2, y, Tile.lightwool, y);
			}
			if (level.getTile(x + 2, y) == Tile.wool){
				level.setTile(x + 2, y, Tile.lightwool, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.wool){
				level.setTile(x - 1, y - 1, Tile.lightwool, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.wool){
				level.setTile(x + 1, y - 1, Tile.lightwool, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.wool){
				level.setTile(x + 1, y + 1, Tile.lightwool, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.wool){
				level.setTile(x - 1, y + 1, Tile.lightwool, y);
			}
			if (level.getTile(x, y - 1) == Tile.redwool){
				level.setTile(x, y - 1, Tile.lightrwool, y);
			}
			if (level.getTile(x, y + 1) == Tile.redwool){
				level.setTile(x, y + 1, Tile.lightrwool, y);
			}
			if (level.getTile(x - 1, y) == Tile.redwool){
				level.setTile(x - 1, y, Tile.lightrwool, y);
			}
			if (level.getTile(x + 1, y) == Tile.redwool){
				level.setTile(x + 1, y, Tile.lightrwool, y);
			}
			if (level.getTile(x, y - 2) == Tile.redwool){
				level.setTile(x, y - 2, Tile.lightrwool, y);
			}
			if (level.getTile(x, y + 2) == Tile.redwool){
				level.setTile(x, y + 2, Tile.lightrwool, y);
			}
			if (level.getTile(x - 2, y) == Tile.redwool){
				level.setTile(x - 2, y, Tile.lightrwool, y);
			}
			if (level.getTile(x + 2, y) == Tile.redwool){
				level.setTile(x + 2, y, Tile.lightrwool, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.redwool){
				level.setTile(x - 1, y - 1, Tile.lightrwool, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.redwool){
				level.setTile(x + 1, y - 1, Tile.lightrwool, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.redwool){
				level.setTile(x + 1, y + 1, Tile.lightrwool, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.redwool){
				level.setTile(x - 1, y + 1, Tile.lightrwool, y);
			}
			if (level.getTile(x, y - 1) == Tile.bluewool){
				level.setTile(x, y - 1, Tile.lightbwool, y);
			}
			if (level.getTile(x, y + 1) == Tile.bluewool){
				level.setTile(x, y + 1, Tile.lightbwool, y);
			}
			if (level.getTile(x - 1, y) == Tile.bluewool){
				level.setTile(x - 1, y, Tile.lightbwool, y);
			}
			if (level.getTile(x + 1, y) == Tile.bluewool){
				level.setTile(x + 1, y, Tile.lightbwool, y);
			}
			if (level.getTile(x, y - 2) == Tile.bluewool){
				level.setTile(x, y - 2, Tile.lightbwool, y);
			}
			if (level.getTile(x, y + 2) == Tile.bluewool){
				level.setTile(x, y + 2, Tile.lightbwool, y);
			}
			if (level.getTile(x - 2, y) == Tile.bluewool){
				level.setTile(x - 2, y, Tile.lightbwool, y);
			}
			if (level.getTile(x + 2, y) == Tile.bluewool){
				level.setTile(x + 2, y, Tile.lightbwool, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.bluewool){
				level.setTile(x - 1, y - 1, Tile.lightbwool, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.bluewool){
				level.setTile(x + 1, y - 1, Tile.lightbwool, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.bluewool){
				level.setTile(x + 1, y + 1, Tile.lightbwool, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.bluewool){
				level.setTile(x - 1, y + 1, Tile.lightbwool, y);
			}
			if (level.getTile(x, y - 1) == Tile.greenwool){
				level.setTile(x, y - 1, Tile.lightgwool, y);
			}
			if (level.getTile(x, y + 1) == Tile.greenwool){
				level.setTile(x, y + 1, Tile.lightgwool, y);
			}
			if (level.getTile(x - 1, y) == Tile.greenwool){
				level.setTile(x - 1, y, Tile.lightgwool, y);
			}
			if (level.getTile(x + 1, y) == Tile.greenwool){
				level.setTile(x + 1, y, Tile.lightgwool, y);
			}
			if (level.getTile(x, y - 2) == Tile.greenwool){
				level.setTile(x, y - 2, Tile.lightgwool, y);
			}
			if (level.getTile(x, y + 2) == Tile.greenwool){
				level.setTile(x, y + 2, Tile.lightgwool, y);
			}
			if (level.getTile(x - 2, y) == Tile.greenwool){
				level.setTile(x - 2, y, Tile.lightgwool, y);
			}
			if (level.getTile(x + 2, y) == Tile.greenwool){
				level.setTile(x + 2, y, Tile.lightgwool, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.greenwool){
				level.setTile(x - 1, y - 1, Tile.lightgwool, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.greenwool){
				level.setTile(x + 1, y - 1, Tile.lightgwool, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.greenwool){
				level.setTile(x + 1, y + 1, Tile.lightgwool, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.greenwool){
				level.setTile(x - 1, y + 1, Tile.lightgwool, y);
			}
			if (level.getTile(x, y - 1) == Tile.yellowwool){
				level.setTile(x, y - 1, Tile.lightywool, y);
			}
			if (level.getTile(x, y + 1) == Tile.yellowwool){
				level.setTile(x, y + 1, Tile.lightywool, y);
			}
			if (level.getTile(x - 1, y) == Tile.yellowwool){
				level.setTile(x - 1, y, Tile.lightywool, y);
			}
			if (level.getTile(x + 1, y) == Tile.yellowwool){
				level.setTile(x + 1, y, Tile.lightywool, y);
			}
			if (level.getTile(x, y - 2) == Tile.yellowwool){
				level.setTile(x, y - 2, Tile.lightywool, y);
			}
			if (level.getTile(x, y + 2) == Tile.yellowwool){
				level.setTile(x, y + 2, Tile.lightywool, y);
			}
			if (level.getTile(x - 2, y) == Tile.yellowwool){
				level.setTile(x - 2, y, Tile.lightywool, y);
			}
			if (level.getTile(x + 2, y) == Tile.yellowwool){
				level.setTile(x + 2, y, Tile.lightywool, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.yellowwool){
				level.setTile(x - 1, y - 1, Tile.lightywool, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.yellowwool){
				level.setTile(x + 1, y - 1, Tile.lightywool, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.yellowwool){
				level.setTile(x + 1, y + 1, Tile.lightywool, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.yellowwool){
				level.setTile(x - 1, y + 1, Tile.lightywool, y);
			}
			if (level.getTile(x, y - 1) == Tile.blackwool){
				level.setTile(x, y - 1, Tile.lightblwool, y);
			}
			if (level.getTile(x, y + 1) == Tile.blackwool){
				level.setTile(x, y + 1, Tile.lightblwool, y);
			}
			if (level.getTile(x - 1, y) == Tile.blackwool){
				level.setTile(x - 1, y, Tile.lightblwool, y);
			}
			if (level.getTile(x + 1, y) == Tile.blackwool){
				level.setTile(x + 1, y, Tile.lightblwool, y);
			}
			if (level.getTile(x, y - 2) == Tile.blackwool){
				level.setTile(x, y - 2, Tile.lightblwool, y);
			}
			if (level.getTile(x, y + 2) == Tile.blackwool){
				level.setTile(x, y + 2, Tile.lightblwool, y);
			}
			if (level.getTile(x - 2, y) == Tile.blackwool){
				level.setTile(x - 2, y, Tile.lightblwool, y);
			}
			if (level.getTile(x + 2, y) == Tile.blackwool){
				level.setTile(x + 2, y, Tile.lightblwool, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.blackwool){
				level.setTile(x - 1, y - 1, Tile.lightblwool, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.blackwool){
				level.setTile(x + 1, y - 1, Tile.lightblwool, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.blackwool){
				level.setTile(x + 1, y + 1, Tile.lightblwool, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.blackwool){
				level.setTile(x - 1, y + 1, Tile.lightblwool, y);
			}
			if (level.getTile(x, y - 1) == Tile.treeSapling){
				level.setTile(x, y - 1, Tile.lightts, y);
			}
			if (level.getTile(x, y + 1) == Tile.treeSapling){
				level.setTile(x, y + 1, Tile.lightts, y);
			}
			if (level.getTile(x - 1, y) == Tile.treeSapling){
				level.setTile(x - 1, y, Tile.lightts, y);
			}
			if (level.getTile(x + 1, y) == Tile.treeSapling){
				level.setTile(x + 1, y, Tile.lightts, y);
			}
			if (level.getTile(x, y - 2) == Tile.treeSapling){
				level.setTile(x, y - 2, Tile.lightts, y);
			}
			if (level.getTile(x, y + 2) == Tile.treeSapling){
				level.setTile(x, y + 2, Tile.lightts, y);
			}
			if (level.getTile(x - 2, y) == Tile.treeSapling){
				level.setTile(x - 2, y, Tile.lightts, y);
			}
			if (level.getTile(x + 2, y) == Tile.treeSapling){
				level.setTile(x + 2, y, Tile.lightts, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.treeSapling){
				level.setTile(x - 1, y - 1, Tile.lightts, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.treeSapling){
				level.setTile(x + 1, y - 1, Tile.lightts, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.treeSapling){
				level.setTile(x + 1, y + 1, Tile.lightts, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.treeSapling){
				level.setTile(x - 1, y + 1, Tile.lightts, y);
			}
			if (level.getTile(x, y - 1) == Tile.cactusSapling){
				level.setTile(x, y - 1, Tile.lightcs, y);
			}
			if (level.getTile(x, y + 1) == Tile.cactusSapling){
				level.setTile(x, y + 1, Tile.lightcs, y);
			}
			if (level.getTile(x - 1, y) == Tile.cactusSapling){
				level.setTile(x - 1, y, Tile.lightcs, y);
			}
			if (level.getTile(x + 1, y) == Tile.cactusSapling){
				level.setTile(x + 1, y, Tile.lightcs, y);
			}
			if (level.getTile(x, y - 2) == Tile.cactusSapling){
				level.setTile(x, y - 2, Tile.lightcs, y);
			}
			if (level.getTile(x, y + 2) == Tile.cactusSapling){
				level.setTile(x, y + 2, Tile.lightcs, y);
			}
			if (level.getTile(x - 2, y) == Tile.cactusSapling){
				level.setTile(x - 2, y, Tile.lightcs, y);
			}
			if (level.getTile(x + 2, y) == Tile.cactusSapling){
				level.setTile(x + 2, y, Tile.lightcs, y);
			}
			if (level.getTile(x - 1, y - 1) == Tile.cactusSapling){
				level.setTile(x - 1, y - 1, Tile.lightcs, y);
			}
			if (level.getTile(x + 1, y - 1) == Tile.cactusSapling){
				level.setTile(x + 1, y - 1, Tile.lightcs, y);
			}
			if (level.getTile(x + 1, y + 1) == Tile.cactusSapling){
				level.setTile(x + 1, y + 1, Tile.lightcs, y);
			}
			if (level.getTile(x - 1, y + 1) == Tile.cactusSapling){
				level.setTile(x - 1, y + 1, Tile.lightcs, y);
			}
	}
	
	public boolean canLight(){
		return true;
	}
	
	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		level.setTile(x, y, onType, 0);
		level.add(new ItemEntity(new ResourceItem(Resource.torch), x * 16 + random.nextInt(10) + 3, y * 16 + random.nextInt(10) + 3));
	}
}