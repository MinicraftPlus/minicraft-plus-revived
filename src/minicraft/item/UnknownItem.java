package minicraft.item;

import minicraft.gfx.Sprite;

public class UnknownItem extends StackableItem {
	
	protected UnknownItem(String reqName) {
		super(reqName, Sprite.missingTexture(1, 1));
	}
	
	public UnknownItem clone() {
		return new UnknownItem(name);
	}
}
