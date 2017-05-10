package minicraft.crafting;

import minicraft.entity.Player;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;

public class ToolRecipe extends Recipe {
	
	/** The type of tool, example: (ToolType.Sword, ToolType.Axe, ToolType.Hoe) */
	private ToolType type;
	
	/** The level of the tool (0 = wood, 1 = rock/stone, 2 = iron, 3 = gold, 4 = gem */
	private int level;
	
	/** Adds a tool recipe, given a ToolType and it's level.
	 Example: ToolRecipe(ToolType.Sword , 2) will create a iron sword.
	*/
	public ToolRecipe(ToolType type, int level) {
		super(new ToolItem(type, level)); //this goes through Recipe.java to be put on a list.
		this.type = type;
		this.level = level;
	}
	
	// add tool to inventory
	public void craft(Player player) {
		player.inventory.add(0, new ToolItem(type, level));
	}
}
