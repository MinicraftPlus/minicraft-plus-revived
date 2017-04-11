package com.mojang.ld22.item.resource;

import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;

public class Resource {
	
	//public static ArrayList<Resource> resList = new ArrayList<Resource>();
	
	//format: public static Resource resource = new Resource("Name", x-sprite position + y-sprite position * 32, Color.get(-1,###,###,###));
	
	public static Resource wood = new Resource("Wood", 1 + 4 * 32, Color.get(-1, 200, 531, 430));
	public static Resource stone = new Resource("Stone", 2 + 4 * 32, Color.get(-1, 111, 333, 555));
	public static Resource flower = new PlantableResource("Flower", 0 + 4 * 32, Color.get(-1, 10, 444, 330), Tile.flower, Tile.grass, Tile.lightgrass);
	public static Resource acorn = new PlantableResource("Acorn", 3 + 4 * 32, Color.get(-1, 100, 531, 320), Tile.treeSapling, Tile.grass, Tile.lightgrass);
	
	public static Resource torch = new TorchResource("Torch", 18 + 4 * 32, Color.get(-1, 500, 520, 320), Tile.lightgrass, Tile.dirt, Tile.lightdirt, Tile.lightplank, Tile.plank, Tile.lightsbrick, Tile.sbrick, Tile.lightwool, Tile.wool, Tile.lightrwool, Tile.redwool, Tile.lightbwool, Tile.bluewool, Tile.lightgwool, Tile.greenwool, Tile.lightywool, Tile.yellowwool, Tile.lightblwool, Tile.blackwool, Tile.lightgrass, Tile.grass, Tile.lightsand, Tile.sand);
	public static Resource leather = new Resource("Leather", 19 + 4 * 32, Color.get(-1, 100, 211, 322));
	public static Resource dirt = new PlantableResource("Dirt", 2 + 4 * 32, Color.get(-1, 100, 322, 432), Tile.dirt, Tile.hole, Tile.water, Tile.lava, Tile.lightwater, Tile.lighthole);
	
	public static Resource plank = new PlantableResource("Plank", 1 + 4 * 32, Color.get(-1, 200, 531, 530), Tile.plank, Tile.hole, Tile.water, Tile.lightwater, Tile.lighthole);
	public static Resource sbrick = new PlantableResource("St.Brick", 1 + 4 * 32, Color.get(-1, 333, 444, 444), Tile.sbrick, Tile.hole, Tile.water, Tile.lighthole, Tile.lightwater, Tile.lava);
	public static Resource obrick = new PlantableResource("Ob.Brick", 1 + 4 * 32, Color.get(-1, 159, 59, 59), Tile.o, Tile.hole, Tile.water, Tile.lava);
	public static Resource plankwall = new PlantableResource("Plank Wall", 16 + 4 * 32, Color.get(-1, 200, 531, 530), Tile.plankwall, Tile.plank, Tile.lightplank);
	public static Resource stonewall = new PlantableResource("St.BrickWall", 16 + 4 * 32, Color.get(-1, 100, 333, 444), Tile.stonewall, Tile.sbrick, Tile.lightsbrick);
	public static Resource obwall = new PlantableResource("Ob.BrickWall", 16 + 4 * 32, Color.get(-1, 159, 59, 59), Tile.ow, Tile.o, Tile.lighto);
	public static Resource wdoor = new PlantableResource("Wood Door", 17 + 4 * 32, Color.get(-1, 200, 531, 530), Tile.wdc, Tile.plank, Tile.lightplank);
	public static Resource sdoor = new PlantableResource("Stone Door", 17 + 4 * 32, Color.get(-1, 111, 333, 444), Tile.sdc, Tile.sbrick, Tile.lightsbrick);
	public static Resource odoor = new PlantableResource("Obsidian Door", 17 + 4 * 32, Color.get(-1, 159, 59, 59), Tile.odc, Tile.o, Tile.lighto);
	
	public static Resource wool = new PlantableResource("Wool", 2 + 4 * 32, Color.get(-1, 555, 555, 555), Tile.wool, Tile.hole, Tile.lighthole, Tile.lightwater, Tile.water);
	public static Resource redwool = new PlantableResource("Red Wool", 2 + 4 * 32, Color.get(-1, 100, 300, 500), Tile.redwool, Tile.hole, Tile.water, Tile.lighthole, Tile.lightwater);
	public static Resource bluewool = new PlantableResource("Blue Wool", 2 + 4 * 32, Color.get(-1, 005, 115, 115), Tile.bluewool, Tile.hole, Tile.water, Tile.lighthole, Tile.lightwater);
	public static Resource greenwool = new PlantableResource("Green Wool", 2 + 4 * 32, Color.get(-1, 10, 40, 50), Tile.greenwool, Tile.hole, Tile.water, Tile.lighthole, Tile.lightwater);
	public static Resource yellowwool = new PlantableResource("Yellow Wool", 2 + 4 * 32, Color.get(-1, 110, 440, 552), Tile.yellowwool, Tile.hole, Tile.water, Tile.lighthole, Tile.lightwater);
	public static Resource blackwool = new PlantableResource("Black Wool", 2 + 4 * 32, Color.get(-1, 000, 111, 111), Tile.blackwool, Tile.hole, Tile.water, Tile.lighthole, Tile.lightwater);
	
	public static Resource sand = new PlantableResource("Sand", 2 + 4 * 32, Color.get(-1, 110, 440, 550), Tile.sand, Tile.dirt, Tile.lightdirt);
	
	public static Resource cactusFlower = new PlantableResource("Cactus", 4 + 4 * 32, Color.get(-1, 10, 40, 50), Tile.cactusSapling, Tile.lightsand, Tile.sand);
	public static Resource seeds = new PlantableResource("Seeds", 5 + 4 * 32, Color.get(-1, 10, 40, 50), Tile.wheat, Tile.farmland);
	public static Resource grassseeds = new PlantableResource("Grass Seeds", 5 + 4 * 32, Color.get(-1, 10, 30, 50), Tile.grass, Tile.dirt, Tile.lightgrass);
	public static Resource bone = new PlantableResource("Bone", 15 + 4 * 32, Color.get(-1, 222, 555, 555), Tile.tree, Tile.treeSapling, Tile.lightts);
	
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
	
	public static Resource larmor = new ArmorResource("L.Armor", 3 + 12 * 32, Color.get(-1, 100, 211, 322), 3, 1, 9);
	public static Resource sarmor = new ArmorResource("S.Armor", 3 + 12 * 32, Color.get(-1, 10, 20, 30), 4, 2, 9);
	public static Resource iarmor = new ArmorResource("I.Armor", 3 + 12 * 32, Color.get(-1, 100, 322, 544), 5, 3, 9);
	public static Resource garmor = new ArmorResource("G.Armor", 3 + 12 * 32, Color.get(-1, 110, 330, 553), 7, 4, 9);
	public static Resource gemarmor = new ArmorResource("Gem Armor", 3 + 12 * 32, Color.get(-1, 101, 404, 545), 10, 5, 9);
	public static Resource redclothes = new ClothesResource("Red Clothes", 390, Color.get(-1, Color.rgb(50, 0, 0), Color.rgb(200, 0, 0), Color.rgb(250, 0, 0)), 200, 0, 0);
	public static Resource blueclothes = new ClothesResource("Blue Clothes", 390, Color.get(-1, Color.rgb(0, 0, 50), Color.rgb(0, 0, 200), Color.rgb(0, 0, 250)), 0, 0, 200);
	public static Resource greenclothes = new ClothesResource("Green Clothes", 390, Color.get(-1, Color.rgb(0, 50, 0), Color.rgb(0, 200, 0), Color.rgb(0, 250, 0)), 0, 200, 0);
	public static Resource yellowclothes = new ClothesResource("Yellow Clothes", 390, Color.get(-1, Color.rgb(50, 50, 0), Color.rgb(200, 200, 0), Color.rgb(250, 250, 0)), 200, 200, 0);
	public static Resource blackclothes = new ClothesResource("Black Clothes", 390, Color.get(-1, Color.rgb(0, 0, 0), Color.rgb(50, 50, 50), Color.rgb(100, 100, 100)), 50, 50, 50);
	public static Resource orangeclothes = new ClothesResource("Orange Clothes", 390, Color.get(-1, Color.rgb(100, 50, 0), Color.rgb(200, 150, 0), Color.rgb(250, 200, 0)), 150, 100, 0);
	public static Resource purpleclothes = new ClothesResource("Purple Clothes", 390, Color.get(-1, Color.rgb(50, 0, 100), Color.rgb(100, 0, 150), Color.rgb(200, 0, 250)), 100, 0, 150);
	public static Resource cyanclothes = new ClothesResource("Cyan Clothes", 390, Color.get(-1, Color.rgb(0, 50, 100), Color.rgb(0, 100, 150), Color.rgb(0, 200, 250)), 0, 100, 150);
	public static Resource regclothes = new ClothesResource("Reg Clothes", 390, Color.get(-1, Color.rgb(50, 50, 50), Color.rgb(200, 200, 200), Color.rgb(250, 250, 250)), 50, 50, 0);
	
	public static Resource key = new Resource("Key", 154, Color.get(-1, -1, 444, 550));
	
	/// the PotionResources are mapped as: (name, spriteLoc, bottleColor, effectDuration, displayColor).
	public static Resource potion = new PotionResource("Potion", 155, Color.get(-1, 333, 310, 5), 0, 000);
	public static Resource speedpotion = new PotionResource("Speed", 155, Color.get(-1, 333, 310, 10), 4200, 010);
	public static Resource lightpotion = new PotionResource("Light", 155, Color.get(-1, 333, 310, 440), 6000, 440);
	public static Resource swimpotion = new PotionResource("Swim", 155, Color.get(-1, 333, 310, 3), 4800, 002);
	public static Resource energypotion = new PotionResource("Energy", 155, Color.get(-1, 333, 310, 510), 8400, 510);
	public static Resource regenpotion = new PotionResource("Regen", 155, Color.get(-1, 333, 310, 464), 1800, 464);
	public static Resource timepotion = new PotionResource("Time", 155, Color.get(-1, 333, 310, 222), 1800, 222);
	public static Resource lavapotion = new PotionResource("Lava", 155, Color.get(-1, 333, 310, 400), 7200, 400);
	public static Resource shieldpotion = new PotionResource("Shield", 155, Color.get(-1, 333, 310, 115), 5400, 115);
	public static Resource hastepotion = new PotionResource("Haste", 155, Color.get(-1, 333, 310, 303), 4800, 303);
	
	public static Resource arrow = new Resource("arrow", 13 + 5 * 32, Color.get(-1, 111, 222, 430));
	public static Resource string = new Resource("string", 25 + 4 * 32, Color.get(-1, 555, 555, 555));

	public static Resource coal = new Resource("Coal", 10 + 4 * 32, Color.get(-1, 000, 111, 111));
	public static Resource ironOre = new Resource("Iron Ore", 10 + 4 * 32, Color.get(-1, 100, 322, 544));
	public static Resource lapisOre = new Resource("Lapis", 10 + 4 * 32, Color.get(-1, 005, 115, 115));
	public static Resource goldOre = new Resource("Gold Ore", 10 + 4 * 32, Color.get(-1, 110, 440, 553));
	public static Resource ironIngot = new Resource("Iron", 11 + 4 * 32, Color.get(-1, 100, 322, 544));
	public static Resource goldIngot = new Resource("Gold", 11 + 4 * 32, Color.get(-1, 110, 330, 553));
	
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

	public final String name; // the name of the resource
	public final int sprite; // the sprite location of the resource
	public final int color; // the color of the resource

	public Resource(String name, int sprite, int color) {
		if (name.length() > 20)
			throw new RuntimeException("Name cannot be longer than twenty characters!");
		this.name = name;
		this.sprite = sprite;
		this.color = color;
	}
	
	/** Determines what happens when the resource is used on a certain tile; used in subclasses. */
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		return false;
	}
}
