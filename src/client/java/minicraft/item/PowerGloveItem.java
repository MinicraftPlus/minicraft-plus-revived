package minicraft.item;

import org.jetbrains.annotations.NotNull;

public class PowerGloveItem extends Item {

	public PowerGloveItem() {
		super("Power Glove");
	}

	public @NotNull PowerGloveItem copy() {
		return new PowerGloveItem();
	}
}
