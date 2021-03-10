package minicraft.item;

public enum ToolType {
	Shovel (0, 24), // if there's a second number, it specifies durability.
	Hoe (1, 20),
	Sword (2, 42),
	Pickaxe (3, 28),
	Axe (4, 24),
	Bow (5, 20),
	Claymore (6, 34),
	Shear (0, 42, true);

	public final int xPos; // X Position of origin
	public final int yPos; // Y position of origin
	public final int durability;
	public final boolean noLevel;
	
	ToolType(int xPos, int dur) {
		this.xPos = xPos;
		yPos = 13;
		durability = dur;
		noLevel = false;
	}

	ToolType(int xPos, int dur, boolean noLevel) {
		yPos = 12;
		this.xPos = xPos;
		durability = dur;
		this.noLevel = noLevel;
	}
}
