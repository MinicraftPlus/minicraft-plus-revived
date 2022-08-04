package minicraft.item;

import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteLinker.SpriteType;

public class UnknownItem extends StackableItem {

	protected UnknownItem(String reqName) {
		super(reqName, Sprite.missingTexture(SpriteType.Item));
	}

	public UnknownItem clone() {
		return new UnknownItem(getName());
	}
}
