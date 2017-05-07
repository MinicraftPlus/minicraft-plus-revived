package minicraft.entity;

import minicraft.crafting.Crafting;
import minicraft.gfx.Color;
import minicraft.screen.CraftingMenu;

public class Furnace extends Furniture {
	public Furnace() {
		super("Furnace", Color.get(-1, 000, 222, 333), 3, 3, 2);
		
		// some colors
		/*col0 = Color.get(-1, 000, 222, 333);
		col1 = Color.get(-1, 000, 333, 444);
		col2 = Color.get(-1, 000, 222, 333);
		col3 = Color.get(-1, 000, 111, 222);
		*/
		//col = Color.get(-1, 000, 222, 333);
		//sprite = 3; //spritesheet location
		//xr = 3; // in-game width
		//yr = 2; // in-game height
	}
	
	public boolean use(Player player, int attackDir) {
		player.game.setMenu(new CraftingMenu(Crafting.furnaceRecipes, player));
		return true;
	}
}
