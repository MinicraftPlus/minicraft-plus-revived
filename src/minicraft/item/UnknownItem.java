package minicraft.item;

import minicraft.gfx.Sprite;

class UnknownItem extends Item {
	
	protected UnknownItem() {
		super("Unknown", Sprite.missingTexture(1, 1));
	}
	
	public UnknownItem clone() {
		return new UnknownItem();
	}
}
