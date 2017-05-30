package minicraft.item;

public enum ToolType {
	Shovel (0),
	Hoe (1),
	Sword (2),
	Pickaxe (3),
	Axe (4),
	Bow (5),
	FishingRod (6, 15), // if there's a second number, it specifies durability.
	Claymore (7);
	//Hatchet (10),
	//Spade (11),
	//Pick (12);
	
	//public String name;
	public int sprite; // sprite location on the spritesheet
	public int durability;
	
	private ToolType(int sprite, int dur) {
		this.sprite = sprite;
		durability = dur;
	}
	private ToolType(int sprite) {
		this(sprite, -1); // durability defualts to -1 if not specified (means infinite durability)
	}
}
