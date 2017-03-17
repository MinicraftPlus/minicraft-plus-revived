package com.mojang.ld22.item.resource;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import java.util.Arrays;
import java.util.List;

public class PlantableResource extends Resource {
	private List<Tile> sourceTiles;
	private Tile targetTile;

	public PlantableResource(
			String name, int sprite, int color, Tile targetTile, Tile... sourceTiles1) {
		this(name, sprite, color, targetTile, Arrays.asList(sourceTiles1));
	}

	public PlantableResource(
			String name, int sprite, int color, Tile targetTile, List<Tile> sourceTiles) {
		super(name, sprite, color);
		this.sourceTiles = sourceTiles;
		this.targetTile = targetTile;
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (sourceTiles.contains(tile)) {
			level.setTile(xt, yt, targetTile, 0);
			return true;
		}
		if (name == "Plank Wall") {
			if (!sourceTiles.contains(tile)) {
				Game.infoplank = true;
			}
		}
		if (name == "Wood Door") {
			if (!sourceTiles.contains(tile)) {
				Game.infoplank = true;
			}
		}
		if (name == "St.BrickWall") {
			if (!sourceTiles.contains(tile)) {
				Game.infosbrick = true;
			}
		}
		if (name == "Stone Door") {
			if (!sourceTiles.contains(tile)) {
				Game.infosbrick = true;
			}
		}
		return false;
	}
}
