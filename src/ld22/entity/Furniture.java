package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.FurnitureItem;
import com.mojang.ld22.item.PowerGloveItem;

public class Furniture extends Entity {
	private int pushTime = 0;
	private int pushDir = -1;
	public int col, col0, col1, col2, col3, sprite;
	public String name;
	public int lightTimer = 0;
	private Player shouldTake;

	public Furniture(String name) {
		this.name = name;
		xr = 3;
		yr = 3;
	}

	public void tick() {
		if (shouldTake != null) {
			if (shouldTake.activeItem instanceof PowerGloveItem) {
				remove();
				shouldTake.inventory.add(0, shouldTake.activeItem);
				shouldTake.activeItem = new FurnitureItem(this);
			}
			shouldTake = null;
		}
		if (pushDir == 0) move(0, +1);
		if (pushDir == 1) move(0, -1);
		if (pushDir == 2) move(-1, 0);
		if (pushDir == 3) move(+1, 0);
		pushDir = -1;
		if (pushTime > 0) pushTime--;
	}

	public void render(Screen screen) {

		if (Game.Time == 0) {
			screen.render(x - 8, y - 8 - 4, sprite * 2 + 8 * 32, col0, 0);
			screen.render(x - 0, y - 8 - 4, sprite * 2 + 8 * 32 + 1, col0, 0);
			screen.render(x - 8, y - 0 - 4, sprite * 2 + 8 * 32 + 32, col0, 0);
			screen.render(x - 0, y - 0 - 4, sprite * 2 + 8 * 32 + 33, col0, 0);
		}
		if (Game.Time == 1) {
			screen.render(x - 8, y - 8 - 4, sprite * 2 + 8 * 32, col1, 0);
			screen.render(x - 0, y - 8 - 4, sprite * 2 + 8 * 32 + 1, col1, 0);
			screen.render(x - 8, y - 0 - 4, sprite * 2 + 8 * 32 + 32, col1, 0);
			screen.render(x - 0, y - 0 - 4, sprite * 2 + 8 * 32 + 33, col1, 0);
		}
		if (Game.Time == 2) {
			screen.render(x - 8, y - 8 - 4, sprite * 2 + 8 * 32, col2, 0);
			screen.render(x - 0, y - 8 - 4, sprite * 2 + 8 * 32 + 1, col2, 0);
			screen.render(x - 8, y - 0 - 4, sprite * 2 + 8 * 32 + 32, col2, 0);
			screen.render(x - 0, y - 0 - 4, sprite * 2 + 8 * 32 + 33, col2, 0);
		}
		if (Game.Time == 3) {
			screen.render(x - 8, y - 8 - 4, sprite * 2 + 8 * 32, col3, 0);
			screen.render(x - 0, y - 8 - 4, sprite * 2 + 8 * 32 + 1, col3, 0);
			screen.render(x - 8, y - 0 - 4, sprite * 2 + 8 * 32 + 32, col3, 0);
			screen.render(x - 0, y - 0 - 4, sprite * 2 + 8 * 32 + 33, col3, 0);
		}
	}

	public boolean blocks(Entity e) {
		return true;
	}

	protected void touchedBy(Entity entity) {
		if (entity instanceof Player && pushTime == 0) {
			if (name != "D.Chest") {
				pushDir = ((Player) entity).dir;
				pushTime = 10;
			}
		}
	}

	public void take(Player player) {
		if (name != "D.Chest") {
			shouldTake = player;
		}
	}

	public boolean canWool() {
		return true;
	}
}
