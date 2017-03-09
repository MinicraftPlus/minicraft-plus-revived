package com.mojang.ld22.item.resource;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

public class Resource {
	public static Resource wood = new Resource("Wood", 1 + 4 * 32, Color.get(-1, 200, 531, 430));
	public static Resource stone = new Resource("Stone", 2 + 4 * 32, Color.get(-1, 111, 333, 555));
	public static Resource flower = new PlantableResource("Flower", 0 + 4 * 32, Color.get(-1, 10, 444, 330), Tile.flower, Tile.grass, Tile.lightgrass);
	public static Resource acorn = new PlantableResource("Acorn", 3 + 4 * 32, Color.get(-1, 100, 531, 320), Tile.treeSapling, Tile.grass, Tile.lightgrass);
	public static Resource torch = new TorchResource("Torch", 18 + 4 * 32, Color.get(-1, 500, 520, 320), Tile.lightgrass,Tile.dirt,Tile.lightdirt,Tile.lightplank, Tile.plank,Tile.lightsbrick, Tile.sbrick,Tile.lightwool, Tile.wool,Tile.lightrwool, Tile.redwool,Tile.lightbwool, Tile.bluewool,Tile.lightgwool, Tile.greenwool,Tile.lightywool, Tile.yellowwool,Tile.lightblwool, Tile.blackwool,Tile.lightgrass, Tile.grass, Tile.lightsand, Tile.sand);
	public static Resource leather = new Resource("Leather", 19 + 4 * 32, Color.get(-1, 100, 211, 322));
	public static Resource dirt = new PlantableResource("Dirt", 2 + 4 * 32, Color.get(-1, 100, 322, 432), Tile.dirt, Tile.hole, Tile.water, Tile.lava, Tile.lightwater,Tile.lighthole);
	public static Resource plank = new PlantableResource("Plank", 1 + 4 * 32, Color.get(-1, 200, 531, 530),Tile.plank, Tile.hole, Tile.water, Tile.lightwater,Tile.lighthole);
	public static Resource plankwall = new PlantableResource("Plank Wall", 16 + 4 * 32, Color.get(-1, 200, 531, 530),Tile.plankwall, Tile.plank, Tile.lightplank);
	public static Resource wdoor = new PlantableResource("Wood Door", 17 + 4 * 32, Color.get(-1, 200, 531, 530),Tile.wdc, Tile.plank,Tile.lightplank);
	public static Resource sdoor = new PlantableResource("Stone Door", 17 + 4 * 32, Color.get(-1, 111, 333, 444),Tile.sdc, Tile.sbrick, Tile.lightsbrick);
	public static Resource stonewall = new PlantableResource("St.BrickWall", 16 + 4 * 32, Color.get(-1, 100, 333, 444),Tile.stonewall, Tile.sbrick,Tile.lightsbrick);
	public static Resource obrick = new PlantableResource("Ob.Brick", 1 + 4 * 32, Color.get(-1, 159, 59, 59),Tile.o, Tile.hole, Tile.water, Tile.lava);
	public static Resource sbrick = new PlantableResource("St.Brick", 1 + 4 * 32, Color.get(-1, 333, 444, 444),Tile.sbrick, Tile.hole, Tile.water,Tile.lighthole,Tile.lightwater, Tile.lava);
	public static Resource wool = new PlantableResource("Wool", 2 + 4 * 32, Color.get(-1, 555, 555, 555),Tile.wool, Tile.hole,Tile.lighthole,Tile.lightwater, Tile.water);
	public static Resource redwool = new PlantableResource("Wool", 2 + 4 * 32, Color.get(-1, 100, 300, 500),Tile.redwool, Tile.hole, Tile.water,Tile.lighthole,Tile.lightwater);
	public static Resource bluewool = new PlantableResource("Wool", 2 + 4 * 32, Color.get(-1, 005, 115, 115),Tile.bluewool, Tile.hole, Tile.water,Tile.lighthole,Tile.lightwater);
	public static Resource greenwool = new PlantableResource("Wool", 2 + 4 * 32, Color.get(-1, 10, 40, 50),Tile.greenwool, Tile.hole, Tile.water,Tile.lighthole,Tile.lightwater);
	public static Resource yellowwool = new PlantableResource("Wool", 2 + 4 * 32, Color.get(-1, 110, 440, 552),Tile.yellowwool, Tile.hole, Tile.water,Tile.lighthole,Tile.lightwater);
	public static Resource blackwool = new PlantableResource("Wool", 2 + 4 * 32, Color.get(-1, 000, 111, 111),Tile.blackwool, Tile.hole, Tile.water,Tile.lighthole,Tile.lightwater);
	public static Resource sand = new PlantableResource("Sand", 2 + 4 * 32, Color.get(-1, 110, 440, 550), Tile.sand, Tile.dirt, Tile.lightdirt);
	public static Resource cactusFlower = new PlantableResource("Cactus", 4 + 4 * 32, Color.get(-1, 10, 40, 50), Tile.cactusSapling,Tile.lightsand, Tile.sand);
	public static Resource seeds = new PlantableResource("Seeds", 5 + 4 * 32, Color.get(-1, 10, 40, 50), Tile.wheat, Tile.farmland);
	public static Resource grassseeds = new PlantableResource("Grass Seeds", 5 + 4 * 32, Color.get(-1, 10, 30, 50), Tile.grass, Tile.dirt,Tile.lightgrass);
	public static Resource bone = new PlantableResource("Bone", 15 + 4 * 32, Color.get(-1, 222, 555, 555), Tile.tree, Tile.treeSapling,Tile.lightts);
	public static Resource wheat = new Resource("Wheat", 6 + 4 * 32, Color.get(-1, 110, 330, 550));
	public static Resource bread = new FoodResource("Bread", 8 + 4 * 32, Color.get(-1, 110, 330, 550), 2, 5);
	public static Resource apple = new FoodResource("Apple", 9 + 4 * 32, Color.get(-1, 100, 300, 500), 1, 5);
	public static Resource rawpork = new FoodResource("Raw Pork", 20 + 4 * 32, Color.get(-1, 211, 311, 411), 1, 5);
	public static Resource rawfish = new FoodResource("Raw Fish", 24 + 4 * 32, Color.get(-1, 660, 670, 680), 1, 5);
	public static Resource rawbeef = new FoodResource("Raw Beef", 20 + 4 * 32, Color.get(-1, 200, 300, 400), 1, 5);
	public static Resource bacon = new FoodResource("Pork Chop", 20 + 4 * 32, Color.get(-1, 220, 440, 330), 3, 5);
	public static Resource cookedfish = new FoodResource("Cooked Fish", 24 + 4 * 32, Color.get(-1, 220, 330, 440), 3, 5);
	public static Resource cookedpork = new FoodResource("Cooked Pork", 148, Color.get(-1, 220, 440, 330), 3, 5);
	public static Resource steak = new FoodResource("Steak", 20 + 4 * 32, Color.get(-1, 100, 333, 211), 3, 5);
	public static Resource goldapple = new FoodResource("G.Apple", 9 + 4 * 32, Color.get(-1, 110, 440, 550), 10, 5);
	public static Resource larmor = new ArmorResource("L.Armor", 3 + 12 * 32, Color.get(-1, 100, 211, 322), 3, 9);
	public static Resource sarmor = new ArmorResource("S.Armor", 3 + 12 * 32, Color.get(-1, 10, 20, 30), 4, 9);
	public static Resource iarmor = new ArmorResource("I.Armor", 3 + 12 * 32, Color.get(-1, 100, 322, 544), 5, 9);
	public static Resource garmor = new ArmorResource("G.Armor", 3 + 12 * 32, Color.get(-1, 110, 330, 553), 7, 9);
	public static Resource gemarmor = new ArmorResource("Gem Armor", 3 + 12 * 32, Color.get(-1, 101, 404, 545), 10, 9);
	
	public static Resource arrow = new Resource("arrow", 13 + 5 * 32, Color.get(-1, 111, 222, 430));
	public static Resource string = new Resource("string", 25 + 4 * 32, Color.get(-1, 555, 555, 555));
	
	public static Resource coal = new Resource("Coal", 10 + 4 * 32, Color.get(-1, 000, 111, 111));
	public static Resource ironOre = new Resource("Iron Ore", 10 + 4 * 32, Color.get(-1, 100, 322, 544));
	public static Resource lapisOre = new Resource("Lapis", 10 + 4 * 32, Color.get(-1, 005, 115, 115));
	public static Resource goldOre = new Resource("Gold Ore", 10 + 4 * 32, Color.get(-1, 110, 440, 553));
	public static Resource ironIngot = new Resource("Iron", 11 + 4 * 32, Color.get(-1, 100, 322, 544));
	public static Resource goldIngot = new Resource("Gold", 11 + 4 * 32, Color.get(-1, 110, 330, 553));
	
	public static Resource rod = new ItemResource("Fish Rod", 6 + 5 * 32, Color.get(-1, 320, 320, 444));
	
	public static Resource rose = new Resource("Rose", 0 + 4 * 32, Color.get(-1, 100, 300, 500));
	public static Resource gunp = new Resource("GunPowder", 2 + 4 * 32, Color.get(-1, 111, 222, 333));
	public static Resource slime = new Resource("Slime", 10 + 4 * 32, Color.get(-1, 10, 30, 50));
	public static Resource glass = new Resource("glass", 12 + 4 * 32, Color.get(-1, 555, 555, 555));
	public static Resource cloth = new Resource("cloth", 1 + 4 * 32, Color.get(-1, 25, 252, 141));
	public static Resource book = new ItemResource("book", 14 + 4 * 32, Color.get(-1, 200, 531, 430));
	public static Resource bookant = new ItemResource("Antidious", 14 + 4 * 32, Color.get(-1, 100, 300, 500));
	public static Resource cloud = new PlantableResource("cloud", 2 + 4 * 32, Color.get(-1, 222, 555, 444), Tile.cloud, Tile.infiniteFall);
	public static Resource gem = new Resource("gem", 13 + 4 * 32, Color.get(-1, 101, 404, 545));
	public static Resource scale = new Resource("Scale", 22 + 4 * 32, Color.get(-1, 10, 30, 20));
	public static Resource shard = new Resource("Shard", 23 + 4 * 32, Color.get(-1, 222, 333, 444));
	
	public final String name;
	public final int sprite;
	public final int color;
	
	public Resource(String name, int sprite, int color) {
		if (name.length() > 20) throw new RuntimeException("Name cannot be longer than twenty characters!");
		this.name = name;
		this.sprite = sprite;
		this.color = color;
	}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		return false;
	}
	
}