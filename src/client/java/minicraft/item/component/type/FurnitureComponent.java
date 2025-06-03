package minicraft.item.component.type;

import minicraft.entity.furniture.Furniture;

public class FurnitureComponent {
	private final Furniture furniture;
	private final boolean placed;  // Value if the furniture has been placed or not.

	public FurnitureComponent(Furniture furniture) {
		this(furniture, false);
	}

	public FurnitureComponent(Furniture furniture, boolean placed) {
		this.furniture = furniture;
		this.placed = placed;
	}

	public FurnitureComponent with(Furniture furniture) {
		return new FurnitureComponent(furniture, this.placed);
	}

	public FurnitureComponent withPlaced(boolean placed) {
		return new FurnitureComponent(this.furniture, placed);
	}

	public Furniture furniture() {
		return this.furniture;
	}

	public boolean placed() {
		return this.placed;
	}
}
