package com.mojang.ld22.item.resource;

import java.util.Arrays;
import java.util.List;

import com.mojang.ld22.Game;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

public class TorchResource extends Resource {
	private List<Tile> sourceTiles;
	private Tile targetTile;
	
	public TorchResource(String name, int sprite, int color, Tile targetTile, Tile... sourceTiles1) {
		this(name, sprite, color, targetTile, Arrays.asList(sourceTiles1));
	}
	
	public TorchResource(String name, int sprite, int color, Tile targetTile, List<Tile> sourceTiles) {
		super(name, sprite, color);
		this.sourceTiles = sourceTiles;
		this.targetTile = targetTile;
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (sourceTiles.contains(tile)) {
			if (Level.depthlvl == 0){
			if (tile == Tile.grass){
				level.setTile(xt, yt, Tile.torchgrass, 0);
				return true;
			}
			if (tile == Tile.lightgrass){
				level.setTile(xt, yt, Tile.torchgrass, 0);
				return true;
			}
			if (tile == Tile.dirt){
				level.setTile(xt, yt, Tile.torchdirt, 0);
				return true;
			}
			if (tile == Tile.lightdirt){
				level.setTile(xt, yt, Tile.torchdirt, 0);
				return true;
			}
			if (tile == Tile.sand){
				level.setTile(xt, yt, Tile.torchsand, 0);
				return true;
			}
			if (tile == Tile.lightsand){
				level.setTile(xt, yt, Tile.torchsand, 0);
				return true;
			}
			if (tile == Tile.plank){
				level.setTile(xt, yt, Tile.torchplank, 0);
				return true;
			}
			if (tile == Tile.lightplank){
				level.setTile(xt, yt, Tile.torchplank, 0);
				return true;
			}
			if (tile == Tile.sbrick){
				level.setTile(xt, yt, Tile.torchsbrick, 0);
				return true;
			}
			if (tile == Tile.lightsbrick){
				level.setTile(xt, yt, Tile.torchsbrick, 0);
				return true;
			}
			if (tile == Tile.wool){
				level.setTile(xt, yt, Tile.torchwool, 0);
				return true;
			}
			if (tile == Tile.lightwool){
				level.setTile(xt, yt, Tile.torchwool, 0);
				return true;
			}
			if (tile == Tile.redwool){
				level.setTile(xt, yt, Tile.torchwoolred, 0);
				return true;
			}
			if (tile == Tile.lightrwool){
				level.setTile(xt, yt, Tile.torchwoolred, 0);
				return true;
			}
			if (tile == Tile.bluewool){
				level.setTile(xt, yt, Tile.torchwoolblue, 0);
				return true;
			}
			if (tile == Tile.lightbwool){
				level.setTile(xt, yt, Tile.torchwoolblue, 0);
				return true;
			}
			if (tile == Tile.greenwool){
				level.setTile(xt, yt, Tile.torchwoolgreen, 0);
				return true;
			}
			if (tile == Tile.lightgwool){
				level.setTile(xt, yt, Tile.torchwoolgreen, 0);
				return true;
			}
			if (tile == Tile.yellowwool){
				level.setTile(xt, yt, Tile.torchwoolyellow, 0);
				return true;
			}
			if (tile == Tile.lightywool){
				level.setTile(xt, yt, Tile.torchwoolyellow, 0);
				return true;
			}
			if (tile == Tile.blackwool){
				level.setTile(xt, yt, Tile.torchwoolblack, 0);
				return true;
			}
			if (tile == Tile.lightblwool){
				level.setTile(xt, yt, Tile.torchwoolblack, 0);
				return true;
			}
			}
		}
		return false;
	}
}
	