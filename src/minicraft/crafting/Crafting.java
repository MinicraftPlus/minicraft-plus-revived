package minicraft.crafting;

import java.util.ArrayList;
import java.util.List;
import minicraft.entity.Anvil;
import minicraft.entity.Bed;
import minicraft.entity.Chest;
import minicraft.entity.Furnace;
//import minicraft.entity.GodLantern;
import minicraft.entity.Lantern;
import minicraft.entity.Loom;
import minicraft.entity.Oven;
import minicraft.entity.Tnt;
import minicraft.entity.Workbench;
import minicraft.item.BucketItem;
import minicraft.item.ToolType;
import minicraft.item.resource.Resource;

public class Crafting {
	
	public static final List anvilRecipes = new ArrayList();
	public static final List ovenRecipes = new ArrayList();
	public static final List furnaceRecipes = new ArrayList();
	public static final List workbenchRecipes = new ArrayList();
	//public static final List godworkbenchRecipes = new ArrayList();
	public static final List enchantRecipes = new ArrayList();
	public static final List craftRecipes = new ArrayList();
	public static final List loomRecipes = new ArrayList();
	
	static {
		try {
			craftRecipes.add((new FurnitureRecipe(new Workbench())).addCost(Resource.wood, 10));
			craftRecipes.add((new ResourceRecipe(Resource.torch, 2)).addCost(Resource.wood, 1).addCost(Resource.coal, 1));
			/*craftRecipes.add((new ToolRecipe(ToolType.hatchet, 0)).addCost(Resource.wood, 3));
			craftRecipes.add((new ToolRecipe(ToolType.spade, 0)).addCost(Resource.wood, 3));
			craftRecipes.add((new ToolRecipe(ToolType.pick, 0)).addCost(Resource.wood, 3));
			craftRecipes.add((new ToolRecipe(ToolType.hatchet, 1)).addCost(Resource.wood, 3).addCost(Resource.stone, 3));
			craftRecipes.add((new ToolRecipe(ToolType.spade, 1)).addCost(Resource.wood, 3).addCost(Resource.stone, 3));
			craftRecipes.add((new ToolRecipe(ToolType.pick, 1)).addCost(Resource.wood, 3).addCost(Resource.stone, 3));*/
			craftRecipes.add((new ResourceRecipe(Resource.plank, 2)).addCost(Resource.wood, 1));
			craftRecipes.add((new ResourceRecipe(Resource.plankwall)).addCost(Resource.plank, 3));
			craftRecipes.add((new ResourceRecipe(Resource.wdoor)).addCost(Resource.plank, 5));
			workbenchRecipes.add((new FurnitureRecipe(new Lantern(Lantern.Type.NORM))).addCost(Resource.wood, 10).addCost(Resource.slime, 5).addCost(Resource.glass, 4));
			workbenchRecipes.add((new ResourceRecipe(Resource.sdoor)).addCost(Resource.sbrick, 5));
			workbenchRecipes.add((new ResourceRecipe(Resource.sbrick, 2)).addCost(Resource.stone, 2));
			workbenchRecipes.add((new ResourceRecipe(Resource.stonewall)).addCost(Resource.sbrick, 3));
			workbenchRecipes.add((new FurnitureRecipe(new Oven())).addCost(Resource.stone, 15));
			workbenchRecipes.add((new FurnitureRecipe(new Furnace())).addCost(Resource.stone, 20));
			workbenchRecipes.add((new FurnitureRecipe(new Chest())).addCost(Resource.wood, 20));
			workbenchRecipes.add((new FurnitureRecipe(new Anvil())).addCost(Resource.ironIngot, 5));
			workbenchRecipes.add((new FurnitureRecipe(new Tnt())).addCost(Resource.gunp, 10).addCost(Resource.sand, 8));
			workbenchRecipes.add((new FurnitureRecipe(new Loom())).addCost(Resource.wood, 10).addCost(Resource.wool, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.FishingRod, 0)).addCost(Resource.wood, 5).addCost(Resource.string, 3));
			
			loomRecipes.add((new ResourceRecipe(Resource.string, 2)).addCost(Resource.wool, 1));
			loomRecipes.add((new ResourceRecipe(Resource.redwool)).addCost(Resource.wool, 1).addCost(Resource.rose, 1));
			loomRecipes.add((new ResourceRecipe(Resource.bluewool)).addCost(Resource.wool, 1).addCost(Resource.lapisOre, 1));
			loomRecipes.add((new ResourceRecipe(Resource.greenwool)).addCost(Resource.wool, 1).addCost(Resource.cactusFlower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.yellowwool)).addCost(Resource.wool, 1).addCost(Resource.flower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.blackwool)).addCost(Resource.wool, 1).addCost(Resource.coal, 1));
			loomRecipes.add((new FurnitureRecipe(new Bed())).addCost(Resource.wood, 5).addCost(Resource.wool, 3));
			loomRecipes.add((new ResourceRecipe(Resource.redclothes)).addCost(Resource.cloth, 5).addCost(Resource.rose, 1));
			loomRecipes.add((new ResourceRecipe(Resource.blueclothes)).addCost(Resource.cloth, 5).addCost(Resource.lapisOre, 1));
			loomRecipes.add((new ResourceRecipe(Resource.greenclothes)).addCost(Resource.cloth, 5).addCost(Resource.cactusFlower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.yellowclothes)).addCost(Resource.cloth, 5).addCost(Resource.flower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.blackclothes)).addCost(Resource.cloth, 5).addCost(Resource.coal, 1));
			loomRecipes.add((new ResourceRecipe(Resource.orangeclothes)).addCost(Resource.cloth, 5).addCost(Resource.rose, 1).addCost(Resource.flower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.purpleclothes)).addCost(Resource.cloth, 5).addCost(Resource.lapisOre, 1).addCost(Resource.rose, 1));
			loomRecipes.add((new ResourceRecipe(Resource.cyanclothes)).addCost(Resource.cloth, 5).addCost(Resource.lapisOre, 1).addCost(Resource.cactusFlower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.regclothes)).addCost(Resource.cloth, 5));
			/*
			godworkbenchRecipes.add((new ToolRecipe(ToolType.Sword, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ToolRecipe(ToolType.Axe, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ToolRecipe(ToolType.Hoe, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ToolRecipe(ToolType.Pickaxe, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ToolRecipe(ToolType.Shovel, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ToolRecipe(ToolType.Bow, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new FurnitureRecipe(new GodLantern())).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new FurnitureRecipe(new Tnt())).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ResourceRecipe(Resource.goldapple)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ResourceRecipe(Resource.gemarmor)).addCost(Resource.wood, 0));
			*/
			workbenchRecipes.add((new ToolRecipe(ToolType.Sword, 0)).addCost(Resource.wood, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.Axe, 0)).addCost(Resource.wood, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.Hoe, 0)).addCost(Resource.wood, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.Pickaxe, 0)).addCost(Resource.wood, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.Shovel, 0)).addCost(Resource.wood, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.Bow, 0)).addCost(Resource.wood, 5).addCost(Resource.string, 2));
			workbenchRecipes.add((new ToolRecipe(ToolType.Sword, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.Axe, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.Hoe, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.Pickaxe, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.Shovel, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.Bow, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5).addCost(Resource.string, 2));
			workbenchRecipes.add((new ArrowRecipe(Resource.arrow, 3)).addCost(Resource.wood, 2).addCost(Resource.stone, 2));
			workbenchRecipes.add((new ResourceRecipe(Resource.larmor)).addCost(Resource.leather, 10));
			workbenchRecipes.add((new ResourceRecipe(Resource.sarmor)).addCost(Resource.scale, 15));
			loomRecipes.add((new ResourceRecipe(Resource.larmor)).addCost(Resource.leather, 10));
			anvilRecipes.add((new ResourceRecipe(Resource.iarmor)).addCost(Resource.ironIngot, 10));
			anvilRecipes.add((new ResourceRecipe(Resource.garmor)).addCost(Resource.goldIngot, 10));
			anvilRecipes.add((new ResourceRecipe(Resource.gemarmor)).addCost(Resource.gem, 65));
			anvilRecipes.add((new ItemRecipe(BucketItem.class)).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new FurnitureRecipe(new Lantern(Lantern.Type.IRON))).addCost(Resource.ironIngot, 8).addCost(Resource.slime, 5).addCost(Resource.glass, 4));
			anvilRecipes.add((new FurnitureRecipe(new Lantern(Lantern.Type.GOLD))).addCost(Resource.goldIngot, 10).addCost(Resource.slime, 5).addCost(Resource.glass, 4));
			anvilRecipes.add((new ToolRecipe(ToolType.Sword, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.Claymore, 2)).addCostTool(ToolType.Sword, 2, 1).addCost(Resource.shard, 15));
			anvilRecipes.add((new ToolRecipe(ToolType.Axe, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.Hoe, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.Pickaxe, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.Shovel, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.Bow, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5).addCost(Resource.string, 2));
			anvilRecipes.add((new ToolRecipe(ToolType.Sword, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.Claymore, 3)).addCostTool(ToolType.Sword, 3, 1).addCost(Resource.shard, 15));
			anvilRecipes.add((new ToolRecipe(ToolType.Axe, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.Hoe, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.Pickaxe, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.Shovel, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.Bow, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5).addCost(Resource.string, 2));
			anvilRecipes.add((new ToolRecipe(ToolType.Sword, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add((new ToolRecipe(ToolType.Claymore, 4)).addCostTool(ToolType.Sword, 4, 1).addCost(Resource.shard, 15));
			anvilRecipes.add((new ToolRecipe(ToolType.Axe, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add((new ToolRecipe(ToolType.Hoe, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add((new ToolRecipe(ToolType.Pickaxe, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add((new ToolRecipe(ToolType.Shovel, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add((new ToolRecipe(ToolType.Bow, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50).addCost(Resource.string, 2));
			furnaceRecipes.add((new ResourceRecipe(Resource.ironIngot)).addCost(Resource.ironOre, 4).addCost(Resource.coal, 1));
			furnaceRecipes.add((new ResourceRecipe(Resource.goldIngot)).addCost(Resource.goldOre, 4).addCost(Resource.coal, 1));
			furnaceRecipes.add((new ResourceRecipe(Resource.glass)).addCost(Resource.sand, 4).addCost(Resource.coal, 1));
			ovenRecipes.add((new ResourceRecipe(Resource.cookedpork)).addCost(Resource.rawpork, 1).addCost(Resource.coal, 1));
			ovenRecipes.add((new ResourceRecipe(Resource.steak)).addCost(Resource.rawbeef, 1).addCost(Resource.coal, 1));
			ovenRecipes.add((new ResourceRecipe(Resource.cookedfish)).addCost(Resource.rawfish, 1).addCost(Resource.coal, 1));
			ovenRecipes.add((new ResourceRecipe(Resource.bread)).addCost(Resource.wheat, 4));
			enchantRecipes.add((new ResourceRecipe(Resource.goldapple)).addCost(Resource.apple, 1).addCost(Resource.goldIngot, 10));
			enchantRecipes.add((new ResourceRecipe(Resource.grassseeds)).addCost(Resource.seeds, 1).addCost(Resource.flower, 2));
			enchantRecipes.add((new ResourceRecipe(Resource.potion)).addCost(Resource.glass, 1).addCost(Resource.lapisOre, 3));
			enchantRecipes.add((new ResourceRecipe(Resource.speedpotion)).addCost(Resource.potion, 1).addCost(Resource.cactusFlower, 5));
			enchantRecipes.add((new ResourceRecipe(Resource.lightpotion)).addCost(Resource.potion, 1).addCost(Resource.slime, 5));
			enchantRecipes.add((new ResourceRecipe(Resource.swimpotion)).addCost(Resource.potion, 1).addCost(Resource.rawfish, 5));
			enchantRecipes.add((new ResourceRecipe(Resource.hastepotion)).addCost(Resource.potion, 1).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			enchantRecipes.add((new ResourceRecipe(Resource.lavapotion)).addCost(Resource.potion, 1).addCostBucketLava(1));
			enchantRecipes.add((new ResourceRecipe(Resource.energypotion)).addCost(Resource.potion, 1).addCost(Resource.gem, 25));
			enchantRecipes.add((new ResourceRecipe(Resource.regenpotion)).addCost(Resource.potion, 1).addCost(Resource.goldapple, 1));
		} catch (Exception var1) {
			throw new RuntimeException(var1);
		}
	}

}
