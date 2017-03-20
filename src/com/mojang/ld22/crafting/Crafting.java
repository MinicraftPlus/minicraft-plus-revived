package com.mojang.ld22.crafting;

import com.mojang.ld22.crafting.ArrowRecipe;
import com.mojang.ld22.crafting.FurnitureRecipe;
import com.mojang.ld22.crafting.ItemRecipe;
import com.mojang.ld22.crafting.ResourceRecipe;
import com.mojang.ld22.crafting.ToolRecipe;
import com.mojang.ld22.entity.Anvil;
import com.mojang.ld22.entity.Chest;
import com.mojang.ld22.entity.Furnace;
import com.mojang.ld22.entity.GodLantern;
import com.mojang.ld22.entity.GoldLantern;
import com.mojang.ld22.entity.IronLantern;
import com.mojang.ld22.entity.Lantern;
import com.mojang.ld22.entity.Loom;
import com.mojang.ld22.entity.Oven;
import com.mojang.ld22.entity.Tnt;
import com.mojang.ld22.entity.Workbench;
import com.mojang.ld22.entity.Bed;
import com.mojang.ld22.item.BucketItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import java.util.ArrayList;
import java.util.List;

public class Crafting {

	public static final List anvilRecipes = new ArrayList();
	public static final List ovenRecipes = new ArrayList();
	public static final List furnaceRecipes = new ArrayList();
	public static final List workbenchRecipes = new ArrayList();
	public static final List godworkbenchRecipes = new ArrayList();
	public static final List enchantRecipes = new ArrayList();
	public static final List craftRecipes = new ArrayList();
	public static final List loomRecipes = new ArrayList();


	static {
		try {
			craftRecipes.add((new FurnitureRecipe(Workbench.class)).addCost(Resource.wood, 10));
			craftRecipes.add((new ResourceRecipe(Resource.torch)).addCost(Resource.wood, 1).addCost(Resource.coal, 1));
			craftRecipes.add((new ToolRecipe(ToolType.hatchet, 0)).addCost(Resource.wood, 3));
			craftRecipes.add((new ToolRecipe(ToolType.spade, 0)).addCost(Resource.wood, 3));
			craftRecipes.add((new ToolRecipe(ToolType.pick, 0)).addCost(Resource.wood, 3));
			craftRecipes.add((new ToolRecipe(ToolType.hatchet, 1)).addCost(Resource.wood, 3).addCost(Resource.stone, 3));
			craftRecipes.add((new ToolRecipe(ToolType.spade, 1)).addCost(Resource.wood, 3).addCost(Resource.stone, 3));
			craftRecipes.add((new ToolRecipe(ToolType.pick, 1)).addCost(Resource.wood, 3).addCost(Resource.stone, 3));
			craftRecipes.add((new ResourceRecipe(Resource.plank)).addCost(Resource.wood, 1));
			craftRecipes.add((new ResourceRecipe(Resource.plankwall)).addCost(Resource.plank, 3));
			craftRecipes.add((new ResourceRecipe(Resource.wdoor)).addCost(Resource.plank, 5));
			workbenchRecipes.add((new FurnitureRecipe(Lantern.class)).addCost(Resource.wood, 10).addCost(Resource.slime, 5).addCost(Resource.glass, 4));
			workbenchRecipes.add((new ResourceRecipe(Resource.sdoor)).addCost(Resource.sbrick, 5));
			workbenchRecipes.add((new ResourceRecipe(Resource.sbrick)).addCost(Resource.stone, 2));
			workbenchRecipes.add((new ResourceRecipe(Resource.stonewall)).addCost(Resource.sbrick, 3));
			workbenchRecipes.add((new FurnitureRecipe(Oven.class)).addCost(Resource.stone, 15));
			workbenchRecipes.add((new FurnitureRecipe(Furnace.class)).addCost(Resource.stone, 20));
			workbenchRecipes.add((new FurnitureRecipe(Chest.class)).addCost(Resource.wood, 20));
			workbenchRecipes.add((new FurnitureRecipe(Anvil.class)).addCost(Resource.ironIngot, 5));
			workbenchRecipes.add((new FurnitureRecipe(Tnt.class)).addCost(Resource.gunp, 10).addCost(Resource.sand, 8));
			workbenchRecipes.add((new FurnitureRecipe(Loom.class)).addCost(Resource.wood, 10).addCost(Resource.wool, 5));
			workbenchRecipes.add((new ResourceRecipe(Resource.rod)).addCost(Resource.wood, 5).addCost(Resource.string, 3));
			
			loomRecipes.add((new ResourceRecipe(Resource.string)).addCost(Resource.wool, 1));
			loomRecipes.add((new ResourceRecipe(Resource.redwool)).addCost(Resource.wool, 1).addCost(Resource.rose, 1));
			loomRecipes.add((new ResourceRecipe(Resource.bluewool)).addCost(Resource.wool, 1).addCost(Resource.lapisOre, 1));
			loomRecipes.add((new ResourceRecipe(Resource.greenwool)).addCost(Resource.wool, 1).addCost(Resource.cactusFlower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.yellowwool)).addCost(Resource.wool, 1).addCost(Resource.flower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.blackwool)).addCost(Resource.wool, 1).addCost(Resource.coal, 1));
			loomRecipes.add((new FurnitureRecipe(Bed.class)).addCost(Resource.wood, 5).addCost(Resource.wool, 3));
			loomRecipes.add((new ResourceRecipe(Resource.redclothes)).addCost(Resource.cloth, 5).addCost(Resource.rose, 1));
			loomRecipes.add((new ResourceRecipe(Resource.blueclothes)).addCost(Resource.cloth, 5).addCost(Resource.lapisOre, 1));
			loomRecipes.add((new ResourceRecipe(Resource.greenclothes)).addCost(Resource.cloth, 5).addCost(Resource.cactusFlower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.yellowclothes)).addCost(Resource.cloth, 5).addCost(Resource.flower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.blackclothes)).addCost(Resource.cloth, 5).addCost(Resource.coal, 1));
			loomRecipes.add((new ResourceRecipe(Resource.orangeclothes)).addCost(Resource.cloth, 5).addCost(Resource.rose, 1).addCost(Resource.flower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.purpleclothes)).addCost(Resource.cloth, 5).addCost(Resource.lapisOre, 1).addCost(Resource.rose, 1));
			loomRecipes.add((new ResourceRecipe(Resource.cyanclothes)).addCost(Resource.cloth, 5).addCost(Resource.lapisOre, 1).addCost(Resource.cactusFlower, 1));
			loomRecipes.add((new ResourceRecipe(Resource.regclothes)).addCost(Resource.cloth, 5));
			
			godworkbenchRecipes.add((new ToolRecipe(ToolType.sword, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ToolRecipe(ToolType.axe, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ToolRecipe(ToolType.hoe, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ToolRecipe(ToolType.pickaxe, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ToolRecipe(ToolType.shovel, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ToolRecipe(ToolType.bow, 4)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new FurnitureRecipe(GodLantern.class)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new FurnitureRecipe(Tnt.class)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ResourceRecipe(Resource.goldapple)).addCost(Resource.wood, 0));
			godworkbenchRecipes.add((new ResourceRecipe(Resource.gemarmor)).addCost(Resource.wood, 0));
			
			workbenchRecipes.add((new ToolRecipe(ToolType.sword, 0)).addCost(Resource.wood, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.axe, 0)).addCost(Resource.wood, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.hoe, 0)).addCost(Resource.wood, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.pickaxe, 0)).addCost(Resource.wood, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.shovel, 0)).addCost(Resource.wood, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.bow, 0)).addCost(Resource.wood, 5).addCost(Resource.string, 2));
			workbenchRecipes.add((new ToolRecipe(ToolType.sword, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.axe, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.hoe, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.pickaxe, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.shovel, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add((new ToolRecipe(ToolType.bow, 1)).addCost(Resource.wood, 5).addCost(Resource.stone, 5).addCost(Resource.string, 2));
			workbenchRecipes.add((new ArrowRecipe(Resource.arrow, 3)).addCost(Resource.wood, 2).addCost(Resource.stone, 2));
			workbenchRecipes.add((new ResourceRecipe(Resource.larmor)).addCost(Resource.leather, 10));
			workbenchRecipes.add((new ResourceRecipe(Resource.sarmor)).addCost(Resource.scale, 15));
			loomRecipes.add((new ResourceRecipe(Resource.larmor)).addCost(Resource.leather, 10));
			anvilRecipes.add((new ResourceRecipe(Resource.iarmor)).addCost(Resource.ironIngot, 10));
			anvilRecipes.add((new ResourceRecipe(Resource.garmor)).addCost(Resource.goldIngot, 10));
			anvilRecipes.add((new ResourceRecipe(Resource.gemarmor)).addCost(Resource.gem, 65));
			anvilRecipes.add((new ItemRecipe(BucketItem.class)).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new FurnitureRecipe(IronLantern.class)).addCost(Resource.ironIngot, 8).addCost(Resource.slime, 5).addCost(Resource.glass, 4));
			anvilRecipes.add((new FurnitureRecipe(GoldLantern.class)).addCost(Resource.goldIngot, 10).addCost(Resource.slime, 5).addCost(Resource.glass, 4));
			anvilRecipes.add((new ToolRecipe(ToolType.sword, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.claymore, 2)).addCostTool(ToolType.sword, 2, 1).addCost(Resource.shard, 15));
			anvilRecipes.add((new ToolRecipe(ToolType.axe, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.hoe, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.pickaxe, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.shovel, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.bow, 2)).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5).addCost(Resource.string, 2));
			anvilRecipes.add((new ToolRecipe(ToolType.sword, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.claymore, 3)).addCostTool(ToolType.sword, 3, 1).addCost(Resource.shard, 15));
			anvilRecipes.add((new ToolRecipe(ToolType.axe, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.hoe, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.pickaxe, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.shovel, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add((new ToolRecipe(ToolType.bow, 3)).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5).addCost(Resource.string, 2));
			anvilRecipes.add((new ToolRecipe(ToolType.sword, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add((new ToolRecipe(ToolType.claymore, 4)).addCostTool(ToolType.sword, 4, 1).addCost(Resource.shard, 15));
			anvilRecipes.add((new ToolRecipe(ToolType.axe, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add((new ToolRecipe(ToolType.hoe, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add((new ToolRecipe(ToolType.pickaxe, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add((new ToolRecipe(ToolType.shovel, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add((new ToolRecipe(ToolType.bow, 4)).addCost(Resource.wood, 5).addCost(Resource.gem, 50).addCost(Resource.string, 2));
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
