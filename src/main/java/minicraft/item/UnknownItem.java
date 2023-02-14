package minicraft.item;

import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.SpriteType;

public class UnknownItem extends StackableItem {

	protected UnknownItem(String reqName) {
		super(reqName, SpriteLinker.missingTexture(SpriteType.Item));
	}

	public UnknownItem copy() {
		return new UnknownItem(getName());
	}
}
