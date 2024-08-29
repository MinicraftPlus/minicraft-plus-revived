package minicraft.item;

import minicraft.gfx.SpriteManager;
import minicraft.gfx.SpriteManager.SpriteType;
import org.jetbrains.annotations.NotNull;

public class UnknownItem extends StackableItem {

	protected UnknownItem(String reqName) {
		super(reqName, SpriteManager.missingTexture(SpriteType.Item));
	}

	public @NotNull UnknownItem copy() {
		return new UnknownItem(getName());
	}
}
