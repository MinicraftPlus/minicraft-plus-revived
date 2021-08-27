package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;

public class PowerGloveItem extends Item {
	
	public PowerGloveItem() {
		super("Power Glove");
	}
	
	public PowerGloveItem clone() {
		return new PowerGloveItem();
	}
}
