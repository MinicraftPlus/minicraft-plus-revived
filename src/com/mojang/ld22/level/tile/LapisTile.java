package com.mojang.ld22.level.tile;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Mob;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.entity.particle.SmashParticle;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.screen.ModeMenu;

public class LapisTile extends Tile {
	private Resource toDrop;
	private int color;

	public LapisTile(int id, Resource toDrop) {
		super(id);
		this.toDrop = toDrop;
		this.color = toDrop.color & 0xffff00;
	}

	public void render(Screen screen, Level level, int x, int y) {
		color = (toDrop.color & 0xffffff00) + Color.get(level.dirtColor);
		screen.render(x * 16 + 0, y * 16 + 0, 17 + 1 * 32, color, 0);
		screen.render(x * 16 + 8, y * 16 + 0, 18 + 1 * 32, color, 0);
		screen.render(x * 16 + 0, y * 16 + 8, 17 + 2 * 32, color, 0);
		screen.render(x * 16 + 8, y * 16 + 8, 18 + 2 * 32, color, 0);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		int playDmg;
		if (ModeMenu.creative) playDmg = random.nextInt(4);
		else {
			playDmg = 0;
		}
		hurt(level, x, y, playDmg);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (ModeMenu.creative == true) hurt(level, xt, yt, 5);
		else {
			if (item instanceof ToolItem) {
				ToolItem tool = (ToolItem) item;
				if (tool.type == ToolType.pickaxe) {
					if (player.payStamina(2)) {
						hurt(level, xt, yt, 0);
						return true;
					}
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + 1;
		level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 005, 115, 115)));
		int lapHealth;
		int mxD;
		if (ModeMenu.creative) {
			lapHealth = 1;
			mxD = -1;
		} else {
			lapHealth = random.nextInt(10) + 3;
			mxD = 1;
		}
		if (dmg > 0) {
			int count = random.nextInt(2) + 2;
			if (damage >= lapHealth) {
				level.setTile(x, y, Tile.dirt, 0);
				count += 2;
			} else {
				level.setData(x, y, damage);
			}
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								new ResourceItem(toDrop),
								x * 16 + random.nextInt(10) + 3,
								y * 16 + random.nextInt(10) + 3));
			}
		}
	}

	public void bumpedInto(Level level, int x, int y, Entity entity) {
		entity.hurt(this, x, y, 0);
	}
}
