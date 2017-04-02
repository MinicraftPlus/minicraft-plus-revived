package com.mojang.ld22.item;

import com.mojang.ld22.entity.Entity;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.entity.Player;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import java.util.Random;

public class ToolItem extends Item {
	private Random random = new Random();
	//public int counts = 1;

	public static final int MAX_LEVEL = 5;
	public static final String[] LEVEL_NAMES = {"Wood", "Rock", "Iron", "Gold", "Gem"};

	public ToolType type;
	public int level = 0;
	public int dur;
	//public ToolType tool;

	public static final int[] LEVEL_COLORS = {
		Color.get(-1, 100, 321, 431),
		Color.get(-1, 100, 321, 111),
		Color.get(-1, 100, 321, 555),
		Color.get(-1, 100, 321, 550),
		Color.get(-1, 100, 321, 055),
	};

	public static final int[] BOW_COLORS = {
		Color.get(-1, 100, 444, 431),
		Color.get(-1, 100, 444, 111),
		Color.get(-1, 100, 444, 555),
		Color.get(-1, 100, 444, 550),
		Color.get(-1, 100, 444, 055),
	};

	public ToolItem(ToolType type, int level) {
		this.type = type;
		this.level = level;
		dur = type.durability;
	}

	public int getColor() {
		if (type == ToolType.bow) {
			return BOW_COLORS[level];
		} else if(type == ToolType.rod) {
			return Color.get(-1, 320, 320, 444);
		} else {
			return LEVEL_COLORS[level];
		}
	}

	public int getSprite() {
		return type.sprite + 5 * 32;
	}

	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
	}

	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
		Font.draw(getName(), screen, x + 8, y, Color.get(-1, 555, 555, 555));
	}

	public String getName() {
		if (type == ToolType.rod) return "Fishing Rod";
		return LEVEL_NAMES[level] + " " + type.name;
	}

	public void onTake(ItemEntity itemEntity) {}
	
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (dur <= 0) return false;
		//if (dur == -1) return true;
		if (type == ToolType.rod) {
			if (tile == Tile.water || tile == Tile.lightwater) {
				if(com.mojang.ld22.Game.debug) System.out.println("Fishing...");
				player.goFishing(player.x - 5, player.y - 5);
				if(!ModeMenu.creative) dur--;
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isDepleted() {
		return dur <= 0 && type.durability > 0;
	}

	public boolean canAttack() {
		return true;
	}

	public int getAttackDamageBonus(Entity e) {
		if (type == ToolType.hatchet) {
			return (level + 1) * 2 + random.nextInt(3);
		}
		if (type == ToolType.axe) {
			return (level + 1) * 2 + random.nextInt(4);
		}
		if (type == ToolType.sword) {
			return (level + 1) * 3 + random.nextInt(2 + level * level * 2);
		}
		if (type == ToolType.claymore) {
			return (level + 1) * 3 + random.nextInt(4 + level * level * 3);
		}
		return 1;
	}

	public boolean matches(Item item) {
		if (item instanceof ToolItem) {
			ToolItem other = (ToolItem) item;
			if (other.type != type) return false;
			if (other.level != level) return false;
			return true;
		}
		return false;
	}
}
