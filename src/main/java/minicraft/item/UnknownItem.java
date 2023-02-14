package minicraft.item;

import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.SpriteType;
import org.jetbrains.annotations.NotNull;

public class UnknownItem extends StackableItem {

	protected UnknownItem(String reqName) {
		super(reqName, SpriteLinker.missingTexture(SpriteType.Item));
	}

	public @NotNull UnknownItem copy() {
		return new UnknownItem(getName());
	}
}
