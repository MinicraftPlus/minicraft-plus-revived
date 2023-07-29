package minicraft.item;

public enum ToolType {
	Shovel (34), // If there's a second number, it specifies durability.
	Hoe (30),
	Sword (52),
	Pickaxe (38),
	Axe (34),
	Bow (30),
	Claymore (44),
	Shears (42, true);

	public final int durability;
	public final boolean noLevel;

	/**
	 * Create a tool with five levels: wood, stone, iron, gold, and gem.
	 * All these levels are added automatically but sprites have to be added manually.
	 * @param dur Durability of the tool.
	 */
	ToolType(int dur) { this(dur, false); }

	/**
	 * Create a tool without a specified level.
	 * @param dur Durability of the tool.
	 * @param noLevel If the tool has only one level.
	 */
	ToolType(int dur, boolean noLevel) {
		durability = dur;
		this.noLevel = noLevel;
	}
}
