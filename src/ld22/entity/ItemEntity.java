package com.mojang.ld22.entity;

import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.sound.Sound;

public class ItemEntity extends Entity {
	private int lifeTime;
	protected int walkDist = 0;
	protected int dir = 0;
	public int hurtTime = 0;
	protected int xKnockback, yKnockback;
	public double xa, ya, za;
	public double xx, yy, zz;
	public Item item;
	private int time = 0;

	public ItemEntity(Item item, int x, int y) {
		this.item = item;
		xx = this.x = x;
		yy = this.y = y;
		xr = 3;
		yr = 3;

		zz = 2;
		xa = random.nextGaussian() * 0.3;
		ya = random.nextGaussian() * 0.2;
		za = random.nextFloat() * 0.7 + 1;

		lifeTime = 60 * 10 + random.nextInt(70);
	}

	public void tick() {
		time++;
		if (time >= lifeTime) {
			remove();
			return;
		}
		xx += xa;
		yy += ya;
		zz += za;
		if (zz < 0) {
			zz = 0;
			za *= -0.5;
			xa *= 0.6;
			ya *= 0.6;
		}
		za -= 0.15;
		int ox = x;
		int oy = y;
		int nx = (int) xx;
		int ny = (int) yy;
		int expectedx = nx - x;
		int expectedy = ny - y;
		move(nx - x, ny - y);
		int gotx = x - ox;
		int goty = y - oy;
		xx += gotx - expectedx;
		yy += goty - expectedy;

		if (hurtTime > 0) hurtTime--;
	}

	public boolean isBlockableBy(Mob mob) {
		return false;
	}

	public void render(Screen screen) {
		if (time >= lifeTime - 6 * 20) {
			if (time / 6 % 2 == 0) return;
		}
		screen.render(x - 4, y - 4, item.getSprite(), Color.get(-1, 0, 0, 0), 0);
		screen.render(x - 4, y - 4 - (int) (zz), item.getSprite(), item.getColor(), 0);
	}

	protected void touchedBy(Entity entity) {
		if (time > 30) entity.touchItem(this);
	}

	public void take(Player player) {
		Sound.pickup.play();
		item.onTake(this);
		remove();
	}
}
