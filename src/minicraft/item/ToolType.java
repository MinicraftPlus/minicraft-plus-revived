package minicraft.item;

public enum ToolType {
	Shovel (0, 24), // if there's a second number, it specifies durability.
	Hoe (1, 20),
	Sword (2, 42),
	Pickaxe (3, 28),
	Axe (4, 24),
	Bow (5, 20),
	FishingRod (6, 16),
	Claymore (7, 34);
	//Hatchet (10),
	//Spade (11),
	//Pick (12);
	
	//public String name;
	public final int sprite; // sprite location on the spritesheet
	public final int durability;
	
	ToolType(int sprite, int dur) {
		this.sprite = sprite;
		durability = dur;
	}
	ToolType(int sprite) {
		this(sprite, -1); // durability defualts to -1 if not specified (means infinite durability)
	}
}
