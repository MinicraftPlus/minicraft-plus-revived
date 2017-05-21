package minicraft.level.tile;

import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.Mob;
import minicraft.entity.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.ConnectorSprite;
import minicraft.item.Item;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.Items;
import minicraft.level.Level;
import minicraft.screen.ModeMenu;

public class HardRockTile extends Tile {
	private static ConnectorSprite sprite = new ConnectorSprite(HardRockTile.class, new Sprite(4, 0, 3, 3, Color.get(001, 334, 445, 321), 3), new Sprite(7, 0, 2, 2, Color.get(001, 334, 445, 321), 3), ConnectorSprite.makeSprite(2, 2, Color.get(445, 334, 223, 223), 0, false, 0, 1, 2, 3));
	
	protected static void addInstances() {
		Tiles.add(new HardRockTile("Hard Rock"));
	}
	
	private HardRockTile(String name) {
		super(name, sprite);
	}
	/*
	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(445, 334, 223, 223);
		int colt = Color.get(001, 334, 445, 321);
		
		int transitionColor = colt;
		
		if (!u && !l) {
			if (!ul) screen.render(x * 16 + 0, y * 16 + 0, 0, col, 0);
			else screen.render(x * 16 + 0, y * 16 + 0, 7 + 0 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 0, y * 16 + 0, (l ? 6 : 5) + (u ? 2 : 1) * 32, transitionColor, 3);

		if (!u && !r) {
			if (!ur) screen.render(x * 16 + 8, y * 16 + 0, 1, col, 0);
			else screen.render(x * 16 + 8, y * 16 + 0, 8 + 0 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 8, y * 16 + 0, (r ? 4 : 5) + (u ? 2 : 1) * 32, transitionColor, 3);

		if (!d && !l) {
			if (!dl) screen.render(x * 16 + 0, y * 16 + 8, 2, col, 0);
			else screen.render(x * 16 + 0, y * 16 + 8, 7 + 1 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 0, y * 16 + 8, (l ? 6 : 5) + (d ? 0 : 1) * 32, transitionColor, 3);
		if (!d && !r) {
			if (!dr) screen.render(x * 16 + 8, y * 16 + 8, 3, col, 0);
			else screen.render(x * 16 + 8, y * 16 + 8, 8 + 1 * 32, transitionColor, 3);
		} else
			screen.render(x * 16 + 8, y * 16 + 8, (r ? 4 : 5) + (d ? 0 : 1) * 32, transitionColor, 3);
	}
	*/
	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void hurt(Level level, int x, int y, Mob source, int dmg, int attackDir) {
		hurt(level, x, y, 0);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (ModeMenu.creative) return true;
			if (tool.type == ToolType.Pickaxe && tool.level == 4) {
				if (player.payStamina(4 - tool.level)) {
					hurt(level, xt, yt, random.nextInt(10) + (tool.level) * 5 + 10);
					return true;
				}
			}
		}
		if (ModeMenu.creative) return true;
		else {
			return false;
		}
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.get(-1, 500)));
		int hrHealth;
		if (ModeMenu.creative) hrHealth = 0;
		else hrHealth = 200;
		if (damage >= hrHealth) {
			int count = random.nextInt(4) + 1;
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								Items.get("Stone"),
								x * 16 + random.nextInt(10) + 3,
								y * 16 + random.nextInt(10) + 3));
			}
			count = random.nextInt(2);
			for (int i = 0; i < count; i++) {
				level.add(
						new ItemEntity(
								Items.get("coal"),
								x * 16 + random.nextInt(10) + 3,
								y * 16 + random.nextInt(10) + 3));
			}
			level.setTile(x, y, Tiles.get("dirt"), 0);
		} else {
			level.setData(x, y, damage);
		}
	}

	public void tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) level.setData(xt, yt, damage - 1);
	}
}
