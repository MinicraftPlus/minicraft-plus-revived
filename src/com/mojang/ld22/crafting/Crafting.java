package com.mojang.ld22.crafting;

import java.util.ArrayList;
import java.util.List;

import com.mojang.ld22.entity.Anvil;
import com.mojang.ld22.entity.Chest;
import com.mojang.ld22.entity.Furnace;
import com.mojang.ld22.entity.GodLantern;
import com.mojang.ld22.entity.GoldLantern;
import com.mojang.ld22.entity.IronLantern;
import com.mojang.ld22.entity.Loom;
import com.mojang.ld22.entity.Oven;
import com.mojang.ld22.entity.Lantern;
import com.mojang.ld22.entity.Tnt;
import com.mojang.ld22.entity.Workbench;
import com.mojang.ld22.entity.bed;
import com.mojang.ld22.item.BucketItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.screen.ModeMenu;
public class Crafting {
	public static final List<Recipe> anvilRecipes = new ArrayList<Recipe>();
	public static final List<Recipe> ovenRecipes = new ArrayList<Recipe>();
	public static final List<Recipe> furnaceRecipes = new ArrayList<Recipe>();
	public static final List<Recipe> workbenchRecipes = new ArrayList<Recipe>();
	public static final List<Recipe> godworkbenchRecipes = new ArrayList<Recipe>();
	public static final List<Recipe> enchantRecipes = new ArrayList<Recipe>();
	public static final List<Recipe> craftRecipes = new ArrayList<Recipe>();
	public static final List<Recipe> loomRecipes = new ArrayList<Recipe>();
	
	static {
		try {
			if (ModeMenu.creative) {
				
				craftRecipes.add(new FurnitureRecipe(Workbench.class).addCost(Resource.wood, 0));
				craftRecipes.add(new ResourceRecipe(Resource.torch).addCost(Resource.wood, 0).addCost(Resource.coal, 0));
				craftRecipes.add(new ToolRecipe(ToolType.hatchet, 0).addCost(Resource.wood, 0));
				craftRecipes.add(new ToolRecipe(ToolType.spade, 0).addCost(Resource.wood, 0));
				craftRecipes.add(new ToolRecipe(ToolType.pick, 0).addCost(Resource.wood, 0));
				craftRecipes.add(new ToolRecipe(ToolType.hatchet, 0).addCost(Resource.wood, 0).addCost(Resource.stone, 0));
				craftRecipes.add(new ToolRecipe(ToolType.spade, 0).addCost(Resource.wood, 0).addCost(Resource.stone, 0));
				craftRecipes.add(new ToolRecipe(ToolType.pick, 0).addCost(Resource.wood, 0).addCost(Resource.stone, 0));
				craftRecipes.add(new ResourceRecipe(Resource.plank).addCost(Resource.wood, 0));
				craftRecipes.add(new ResourceRecipe(Resource.plankwall).addCost(Resource.plank, 0));
				craftRecipes.add(new ResourceRecipe(Resource.wdoor).addCost(Resource.plank, 0));
				
				workbenchRecipes.add(new FurnitureRecipe(Lantern.class).addCost(Resource.wood, 0).addCost(Resource.slime, 0).addCost(Resource.glass, 0));
				
				workbenchRecipes.add(new FurnitureRecipe(Oven.class).addCost(Resource.stone, 0));
				workbenchRecipes.add(new FurnitureRecipe(Furnace.class).addCost(Resource.stone, 0));
				workbenchRecipes.add(new FurnitureRecipe(Chest.class).addCost(Resource.wood, 0));
				workbenchRecipes.add(new FurnitureRecipe(Anvil.class).addCost(Resource.ironIngot, 0));
				workbenchRecipes.add(new FurnitureRecipe(bed.class).addCost(Resource.wool, 0).addCost(Resource.wood, 0));
				
				workbenchRecipes.add(new ResourceRecipe(Resource.rod).addCost(Resource.wood, 0).addCost(Resource.string, 0));
				workbenchRecipes.add(new ToolRecipe(ToolType.sword, 0).addCost(Resource.wood, 0));
				workbenchRecipes.add(new ToolRecipe(ToolType.axe, 0).addCost(Resource.wood, 0));
				workbenchRecipes.add(new ToolRecipe(ToolType.hoe, 0).addCost(Resource.wood, 0));
				workbenchRecipes.add(new ToolRecipe(ToolType.pickaxe, 0).addCost(Resource.wood, 0));
				workbenchRecipes.add(new ToolRecipe(ToolType.shovel, 0).addCost(Resource.wood, 0));
				workbenchRecipes.add(new ToolRecipe(ToolType.sword, 1).addCost(Resource.wood, 0).addCost(Resource.stone, 0));
				workbenchRecipes.add(new ToolRecipe(ToolType.axe, 1).addCost(Resource.wood, 0).addCost(Resource.stone, 0));
				workbenchRecipes.add(new ToolRecipe(ToolType.hoe, 1).addCost(Resource.wood, 0).addCost(Resource.stone, 0));
				workbenchRecipes.add(new ToolRecipe(ToolType.pickaxe, 1).addCost(Resource.wood, 0).addCost(Resource.stone, 0));
				workbenchRecipes.add(new ToolRecipe(ToolType.shovel, 1).addCost(Resource.wood, 0).addCost(Resource.stone, 0));
				workbenchRecipes.add(new FurnitureRecipe(Tnt.class).addCost(Resource.gunp, 0).addCost(Resource.sand, 0));
				
				loomRecipes.add(new ResourceRecipe(Resource.string).addCost(Resource.wool, 0));
				loomRecipes.add(new ResourceRecipe(Resource.redwool).addCost(Resource.wool, 0).addCost(Resource.rose, 0));
				loomRecipes.add(new ResourceRecipe(Resource.bluewool).addCost(Resource.wool, 0).addCost(Resource.lapisOre, 0));
				loomRecipes.add(new ResourceRecipe(Resource.greenwool).addCost(Resource.wool, 0).addCost(Resource.cactusFlower, 0));
				loomRecipes.add(new ResourceRecipe(Resource.yellowwool).addCost(Resource.wool, 0).addCost(Resource.flower, 0));
				loomRecipes.add(new ResourceRecipe(Resource.blackwool).addCost(Resource.wool, 0).addCost(Resource.coal, 0));
				loomRecipes.add(new FurnitureRecipe(bed.class).addCost(Resource.wood, 0).addCost(Resource.wool, 0));
				
				anvilRecipes.add(new ToolRecipe(ToolType.sword, 2).addCost(Resource.wood, 0).addCost(Resource.ironIngot, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.axe, 2).addCost(Resource.wood, 0).addCost(Resource.ironIngot, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.hoe, 2).addCost(Resource.wood, 0).addCost(Resource.ironIngot, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.pickaxe, 2).addCost(Resource.wood, 0).addCost(Resource.ironIngot, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.shovel, 2).addCost(Resource.wood, 0).addCost(Resource.ironIngot, 0));
				
				anvilRecipes.add(new ToolRecipe(ToolType.sword, 3).addCost(Resource.wood, 0).addCost(Resource.goldIngot, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.axe, 3).addCost(Resource.wood, 0).addCost(Resource.goldIngot, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.hoe, 3).addCost(Resource.wood, 0).addCost(Resource.goldIngot, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.pickaxe, 3).addCost(Resource.wood, 0).addCost(Resource.goldIngot, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.shovel, 3).addCost(Resource.wood, 0).addCost(Resource.goldIngot, 0));
				
				anvilRecipes.add(new ToolRecipe(ToolType.sword, 4).addCost(Resource.wood, 0).addCost(Resource.gem, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.axe, 4).addCost(Resource.wood, 0).addCost(Resource.gem, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.hoe, 4).addCost(Resource.wood, 0).addCost(Resource.gem, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.pickaxe, 4).addCost(Resource.wood, 0).addCost(Resource.gem, 0));
				anvilRecipes.add(new ToolRecipe(ToolType.shovel, 4).addCost(Resource.wood, 0).addCost(Resource.gem, 0));
				
				furnaceRecipes.add(new ResourceRecipe(Resource.ironIngot).addCost(Resource.ironOre, 0).addCost(Resource.coal, 0));
				furnaceRecipes.add(new ResourceRecipe(Resource.goldIngot).addCost(Resource.goldOre, 0).addCost(Resource.coal, 0));
				furnaceRecipes.add(new ResourceRecipe(Resource.glass).addCost(Resource.sand, 0).addCost(Resource.coal, 0));
				
				ovenRecipes.add(new ResourceRecipe(Resource.bacon).addCost(Resource.rawpork, 1).addCost(Resource.coal, 1));
				ovenRecipes.add(new ResourceRecipe(Resource.steak).addCost(Resource.rawbeef, 1).addCost(Resource.coal, 1));
				
				enchantRecipes.add(new ResourceRecipe(Resource.goldapple).addCost(Resource.apple, 0).addCost(Resource.goldIngot, 0));
				enchantRecipes.add(new ResourceRecipe(Resource.grassseeds).addCost(Resource.seeds, 0).addCost(Resource.flower, 0));
				ovenRecipes.add(new ResourceRecipe(Resource.bread).addCost(Resource.wheat, 0));
				}
			else {
			craftRecipes.add(new FurnitureRecipe(Workbench.class).addCost(Resource.wood, 10));
			craftRecipes.add(new ResourceRecipe(Resource.torch).addCost(Resource.wood, 1).addCost(Resource.coal, 1));
			craftRecipes.add(new ToolRecipe(ToolType.hatchet, 0).addCost(Resource.wood, 3));
			craftRecipes.add(new ToolRecipe(ToolType.spade, 0).addCost(Resource.wood, 3));
			craftRecipes.add(new ToolRecipe(ToolType.pick, 0).addCost(Resource.wood, 3));
			craftRecipes.add(new ToolRecipe(ToolType.hatchet, 1).addCost(Resource.wood, 3).addCost(Resource.stone, 3));
			craftRecipes.add(new ToolRecipe(ToolType.spade, 1).addCost(Resource.wood, 3).addCost(Resource.stone, 3));
			craftRecipes.add(new ToolRecipe(ToolType.pick, 1).addCost(Resource.wood, 3).addCost(Resource.stone, 3));
			craftRecipes.add(new ResourceRecipe(Resource.plank).addCost(Resource.wood, 1));
			craftRecipes.add(new ResourceRecipe(Resource.plankwall).addCost(Resource.plank, 3));
			craftRecipes.add(new ResourceRecipe(Resource.wdoor).addCost(Resource.plank, 5));
			
			workbenchRecipes.add(new FurnitureRecipe(Lantern.class).addCost(Resource.wood, 10).addCost(Resource.slime, 5).addCost(Resource.glass, 4));
			
			workbenchRecipes.add(new ResourceRecipe(Resource.sdoor).addCost(Resource.sbrick, 5));
			workbenchRecipes.add(new ResourceRecipe(Resource.sbrick).addCost(Resource.stone, 2));
			workbenchRecipes.add(new ResourceRecipe(Resource.stonewall).addCost(Resource.sbrick, 3));
			workbenchRecipes.add(new FurnitureRecipe(Oven.class).addCost(Resource.stone, 15));
			workbenchRecipes.add(new FurnitureRecipe(Furnace.class).addCost(Resource.stone, 20));
			workbenchRecipes.add(new FurnitureRecipe(Chest.class).addCost(Resource.wood, 20));
			workbenchRecipes.add(new FurnitureRecipe(Anvil.class).addCost(Resource.ironIngot, 5));
			workbenchRecipes.add(new FurnitureRecipe(Tnt.class).addCost(Resource.gunp, 10).addCost(Resource.sand, 8));
			workbenchRecipes.add(new FurnitureRecipe(Loom.class).addCost(Resource.wood, 10).addCost(Resource.wool, 5));
			
			workbenchRecipes.add(new ResourceRecipe(Resource.rod).addCost(Resource.wood, 5).addCost(Resource.string, 5));
			
			loomRecipes.add(new ResourceRecipe(Resource.string).addCost(Resource.wool, 1));
			
			loomRecipes.add(new ResourceRecipe(Resource.redwool).addCost(Resource.wool, 1).addCost(Resource.rose, 1));
			loomRecipes.add(new ResourceRecipe(Resource.bluewool).addCost(Resource.wool, 1).addCost(Resource.lapisOre, 1));
			loomRecipes.add(new ResourceRecipe(Resource.greenwool).addCost(Resource.wool, 1).addCost(Resource.cactusFlower, 1));
			loomRecipes.add(new ResourceRecipe(Resource.yellowwool).addCost(Resource.wool, 1).addCost(Resource.flower, 1));
			loomRecipes.add(new ResourceRecipe(Resource.blackwool).addCost(Resource.wool, 1).addCost(Resource.coal, 1));
			loomRecipes.add(new FurnitureRecipe(bed.class).addCost(Resource.wood, 5).addCost(Resource.wool, 3));
			
			godworkbenchRecipes.add(new ToolRecipe(ToolType.sword, 4).addCost(Resource.wood, 0));
			godworkbenchRecipes.add(new ToolRecipe(ToolType.axe, 4).addCost(Resource.wood, 0));
			godworkbenchRecipes.add(new ToolRecipe(ToolType.hoe, 4).addCost(Resource.wood, 0));
			godworkbenchRecipes.add(new ToolRecipe(ToolType.pickaxe, 4).addCost(Resource.wood, 0));
			godworkbenchRecipes.add(new ToolRecipe(ToolType.shovel, 4).addCost(Resource.wood, 0));
			godworkbenchRecipes.add(new ToolRecipe(ToolType.bow, 4).addCost(Resource.wood, 0));
			godworkbenchRecipes.add(new FurnitureRecipe(GodLantern.class).addCost(Resource.wood, 0));
			godworkbenchRecipes.add(new FurnitureRecipe(Tnt.class).addCost(Resource.wood, 0));
			godworkbenchRecipes.add(new ResourceRecipe(Resource.goldapple).addCost(Resource.wood,  0));
			godworkbenchRecipes.add(new ResourceRecipe(Resource.gemarmor).addCost(Resource.wood, 0));
			
			workbenchRecipes.add(new ToolRecipe(ToolType.sword, 0).addCost(Resource.wood, 5));
			workbenchRecipes.add(new ToolRecipe(ToolType.axe, 0).addCost(Resource.wood, 5));
			workbenchRecipes.add(new ToolRecipe(ToolType.hoe, 0).addCost(Resource.wood, 5));
			workbenchRecipes.add(new ToolRecipe(ToolType.pickaxe, 0).addCost(Resource.wood, 5));
			workbenchRecipes.add(new ToolRecipe(ToolType.shovel, 0).addCost(Resource.wood, 5));
			workbenchRecipes.add(new ToolRecipe(ToolType.bow, 0).addCost(Resource.wood, 5).addCost(Resource.string, 2));
			workbenchRecipes.add(new ToolRecipe(ToolType.sword, 1).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add(new ToolRecipe(ToolType.axe, 1).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add(new ToolRecipe(ToolType.hoe, 1).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add(new ToolRecipe(ToolType.pickaxe, 1).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add(new ToolRecipe(ToolType.shovel, 1).addCost(Resource.wood, 5).addCost(Resource.stone, 5));
			workbenchRecipes.add(new ToolRecipe(ToolType.bow, 1).addCost(Resource.wood, 5).addCost(Resource.stone, 5).addCost(Resource.string, 2));
			
			workbenchRecipes.add(new ResourceRecipe(Resource.larmor).addCost(Resource.leather, 10));
			workbenchRecipes.add(new ResourceRecipe(Resource.sarmor).addCost(Resource.scale, 15));
			loomRecipes.add(new ResourceRecipe(Resource.larmor).addCost(Resource.leather, 10));
			anvilRecipes.add(new ResourceRecipe(Resource.iarmor).addCost(Resource.ironIngot, 10));
			anvilRecipes.add(new ResourceRecipe(Resource.garmor).addCost(Resource.goldIngot, 10));
			anvilRecipes.add(new ResourceRecipe(Resource.gemarmor).addCost(Resource.gem, 65));
			
			anvilRecipes.add(new ItemRecipe(BucketItem.class).addCost(Resource.ironIngot, 5));
			
			anvilRecipes.add(new FurnitureRecipe(IronLantern.class).addCost(Resource.ironIngot, 8).addCost(Resource.slime, 5).addCost(Resource.glass, 4));
			anvilRecipes.add(new FurnitureRecipe(GoldLantern.class).addCost(Resource.goldIngot, 10).addCost(Resource.slime, 5).addCost(Resource.glass, 4));
			anvilRecipes.add(new ToolRecipe(ToolType.sword, 2).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add(new ToolRecipe(ToolType.claymore, 2).addCostTool(ToolType.sword, 2, 1).addCost(Resource.shard, 15));
			anvilRecipes.add(new ToolRecipe(ToolType.axe, 2).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add(new ToolRecipe(ToolType.hoe, 2).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add(new ToolRecipe(ToolType.pickaxe, 2).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add(new ToolRecipe(ToolType.shovel, 2).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5));
			anvilRecipes.add(new ToolRecipe(ToolType.bow, 2).addCost(Resource.wood, 5).addCost(Resource.ironIngot, 5).addCost(Resource.string, 2));
			
			anvilRecipes.add(new ToolRecipe(ToolType.sword, 3).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add(new ToolRecipe(ToolType.claymore, 3).addCostTool(ToolType.sword, 3, 1).addCost(Resource.shard, 15));
			anvilRecipes.add(new ToolRecipe(ToolType.axe, 3).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add(new ToolRecipe(ToolType.hoe, 3).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add(new ToolRecipe(ToolType.pickaxe, 3).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add(new ToolRecipe(ToolType.shovel, 3).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5));
			anvilRecipes.add(new ToolRecipe(ToolType.bow, 3).addCost(Resource.wood, 5).addCost(Resource.goldIngot, 5).addCost(Resource.string, 2));
			
			anvilRecipes.add(new ToolRecipe(ToolType.sword, 4).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add(new ToolRecipe(ToolType.claymore, 4).addCostTool(ToolType.sword, 4, 1).addCost(Resource.shard, 15));
			anvilRecipes.add(new ToolRecipe(ToolType.axe, 4).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add(new ToolRecipe(ToolType.hoe, 4).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add(new ToolRecipe(ToolType.pickaxe, 4).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add(new ToolRecipe(ToolType.shovel, 4).addCost(Resource.wood, 5).addCost(Resource.gem, 50));
			anvilRecipes.add(new ToolRecipe(ToolType.bow, 4).addCost(Resource.wood, 5).addCost(Resource.gem, 50).addCost(Resource.string, 2));
			
			furnaceRecipes.add(new ResourceRecipe(Resource.ironIngot).addCost(Resource.ironOre, 4).addCost(Resource.coal, 1));
			furnaceRecipes.add(new ResourceRecipe(Resource.goldIngot).addCost(Resource.goldOre, 4).addCost(Resource.coal, 1));
			furnaceRecipes.add(new ResourceRecipe(Resource.glass).addCost(Resource.sand, 4).addCost(Resource.coal, 1));
			ovenRecipes.add(new ResourceRecipe(Resource.bacon).addCost(Resource.rawpork, 1).addCost(Resource.coal, 1));
			ovenRecipes.add(new ResourceRecipe(Resource.steak).addCost(Resource.rawbeef, 1).addCost(Resource.coal, 1));
			ovenRecipes.add(new ResourceRecipe(Resource.cookedfish).addCost(Resource.rawfish, 1));//.addCost(Resource.coal, 1));
			
			ovenRecipes.add(new ResourceRecipe(Resource.bread).addCost(Resource.wheat, 4));
			
			enchantRecipes.add(new ResourceRecipe(Resource.goldapple).addCost(Resource.apple, 1).addCost(Resource.goldIngot, 10));
			enchantRecipes.add(new ResourceRecipe(Resource.grassseeds).addCost(Resource.seeds, 1).addCost(Resource.flower, 2));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}