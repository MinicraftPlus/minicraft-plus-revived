package com.mojang.ld22.level.tile;

import java.util.Random;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;

public class Tile {
	public static int tickCount = 0;
	protected Random random = new Random();
	
	public static Tile[] tiles = new Tile[256];
	public static Tile grass = new GrassTile(0);
	public static Tile rock = new RockTile(1);
	public static Tile water = new WaterTile(2);
	public static Tile flower = new FlowerTile(3);
	public static Tile tree = new TreeTile(4);
	public static Tile dirt = new DirtTile(5);
	public static Tile wool = new WoolTile(41);
	public static Tile redwool = new WoolRedTile(42);
	public static Tile bluewool = new WoolBlueTile(43);
	public static Tile greenwool = new WoolGreenTile(45);
	public static Tile yellowwool = new WoolYellowTile(127);
	public static Tile blackwool = new WoolBlackTile(56);
	public static Tile sand = new SandTile(6);
	public static Tile cactus = new CactusTile(7);
	public static Tile hole = new HoleTile(8);
	public static Tile treeSapling = new SaplingTile(9, grass, tree);
	public static Tile cactusSapling = new SaplingTile(10, sand, cactus);
	public static Tile farmland = new FarmTile(11);
	public static Tile wheat = new WheatTile(12);
	public static Tile lava = new LavaTile(13);
	public static Tile stairsDown = new StairsTile(14, false);
	public static Tile stairsUp = new StairsTile(15, true);
	public static Tile cloud = new CloudTile(17);
	public static Tile explode = new ExplodedTile(30);
	public static Tile plank = new PlankTile(31);
	public static Tile plankwall = new WoodWallTile(33);
	public static Tile stonewall = new StoneWallTile(34);
	public static Tile wdo = new WoodDoorOpenTile(35);
	public static Tile wdc = new WoodDoorClosedTile(36);
	public static Tile sdo = new StoneDoorOpenTile(37);
	public static Tile sdc = new StoneDoorClosedTile(38);
	public static Tile lavabrick = new LavaBrickTile(39);
	public static Tile sbrick = new StoneBrickTile(32);
	public static Tile o = new ObsidianBrick(120);
	public static Tile ow = new ObsidianWallTile(121);
	public static Tile odc = new ObsidianDoorClosedTile(122);
	public static Tile odo = new ObsidianDoorOpenTile(123);
	public static Tile hardRock = new HardRockTile(18);
	
	public static Tile lightgrass = new LightTile(100, grass, 0);
	public static Tile lightsand = new LightTile(101, sand, 1);
	public static Tile lighttree = new LightTile(102, tree, 2);
	public static Tile lightcac = new LightTile(103, cactus, 3);
	public static Tile lightwater = new LightTile(104, water, 4);
	public static Tile lightdirt = new LightTile(105, dirt, 5);
	public static Tile lightflower = new LightTile(107, flower, 6);
	public static Tile lightstairsUp = new LightTile(108, stairsUp, 7);
	public static Tile lightstairsDown = new LightTile(109, stairsDown, 8);
	public static Tile lightplank = new LightTile(110, plank, 9);
	public static Tile lightsbrick = new LightTile(111, sbrick, 10);
	public static Tile lwdo = new LightTile(112, wdo, 11);
	public static Tile lwdc = new LightTile(113, wdc, 12);
	public static Tile lsdo = new LightTile(114, sdo, 13);
	public static Tile lsdc = new LightTile(115, sdc, 14);
	public static Tile lodo = new LightTile(116, odo, 15);
	public static Tile lodc = new LightTile(117, odc, 16);
	public static Tile lighthole = new LightTile(119, hole, 17);
	public static Tile lightwool = new LightTile(57, wool, 18);
	public static Tile lightrwool = new LightTile(58, redwool, 19);
	public static Tile lightbwool = new LightTile(59, bluewool, 20);
	public static Tile lightgwool = new LightTile(60, greenwool, 21);
	public static Tile lightywool = new LightTile(61, yellowwool, 22);
	public static Tile lightblwool = new LightTile(62, blackwool, 23);
	public static Tile lighto = new LightTile(63, o, 24);
	public static Tile lightts = new LightTile(64, treeSapling, 25);
	public static Tile lightcs = new LightTile(65, cactusSapling, 26);
	
	public static Tile torchgrass = new TorchTile(44, lightgrass);
	public static Tile torchsand = new TorchTile(40, lightsand);
	public static Tile torchdirt = new TorchTile(46, lightdirt);
	public static Tile torchplank = new TorchTile(47, lightplank);
	public static Tile torchsbrick = new TorchTile(48, lightsbrick);
	public static Tile torchlo = new TorchTile(49, lighto);
	public static Tile torchwool = new TorchTile(50, wool);
	public static Tile torchwoolred = new TorchTile(51, lightrwool);
	public static Tile torchwoolblue = new TorchTile(52, lightbwool);
	public static Tile torchwoolgreen = new TorchTile(53, lightgwool);
	public static Tile torchwoolyellow = new TorchTile(54, lightywool);
	public static Tile torchwoolblack = new TorchTile(55, lightblwool);
	
	public static Tile ironOre = new OreTile(19, Resource.ironOre);
	public static Tile lapisOre = new OreTile(24, Resource.lapisOre);
	public static Tile goldOre = new OreTile(20, Resource.goldOre);
	public static Tile gemOre = new OreTile(21, Resource.gem);
	public static Tile cloudCactus = new CloudCactusTile(22);
	public static Tile infiniteFall = new InfiniteFallTile(16);
	
	public final byte id;
	
	public boolean connectsToGrass = false;
	public boolean connectsToSand = false;
	public boolean connectsToLava = false;
	public boolean connectsToWater = false;
	public int light = 1;
	
	public Tile(int id) {
		this.id = (byte) id;
		if (tiles[id] != null) throw new RuntimeException("Duplicate tile ids!");
		tiles[id] = this;
	}
	
	public String setDataChar(){
		return null;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
	}
	
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
	
	public boolean canLight() {
		return false;
	}
	
	
	public int getLightRadius(Level level, int x, int y) {
		return 0;
	}
	
	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
	}
	
	public void bumpedInto(Level level, int xt, int yt, Entity entity) {
	}
	
	public void tick(Level level, int xt, int yt) {
	}
	
	public void steppedOn(Level level, int xt, int yt, Entity entity) {
	}
	
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		return false;
	}
	
	public boolean use(Level level, int xt, int yt, Player player, int attackDir) {
		return false;
	}
	
	public boolean connectsToLiquid() {
		return connectsToWater || connectsToLava;
	}
}