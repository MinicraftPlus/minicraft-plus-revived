package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;

// This is the normal stone you see underground and on the surface, that drops coal and stone.

public class RockTile extends Tile {
	private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "rock")
		.setConnectionChecker((level, x, y, tile, side) -> tile instanceof RockTile)
		.setSingletonWithConnective(true);

	private boolean dropCoal = false;
	private int maxHealth = 50;

	private int damage;

	protected RockTile(String name) {
		super(name, sprite);
	}

	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("dirt").render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		dropCoal = false; // Can only be reached when player hits w/o pickaxe, so remove ability to get coal
		hurt(level, x, y, dmg);
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Pickaxe && player.payStamina(5 - tool.level) && tool.payDurability()) {
				int data = level.getData(xt, yt);
				// Drop coal since we use a pickaxe.
				dropCoal = true;
				hurt(level, xt, yt, tool.getDamage());
				AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
					new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
						item, this, data, xt, yt, level.depth));
				return true;
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		damage = level.getData(x, y) + dmg;

		if (Game.isMode("minicraft.settings.mode.creative")) {
			dmg = damage = maxHealth;
			dropCoal = true;
		}

		level.add(new SmashParticle(x << 4, y << 4));
		Sound.play("monsterhurt");

		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
		if (damage >= maxHealth) {
			int stone = 1;
			if (dropCoal) {
				stone += random.nextInt(3) + 1;

				int coal = 1;
				if (!Settings.get("diff").equals("minicraft.settings.difficulty.hard")) {
					coal += 1;
				}

				level.dropItem((x << 4) + 8, (y << 4) + 8, 0, coal, Items.get("Coal"));
			}

			level.dropItem((x << 4) + 8, (y << 4) + 8, stone, Items.get("Stone"));
			level.setTile(x, y, Tiles.get("Dirt"));
		} else {
			level.setData(x, y, damage);
		}
	}

	public boolean tick(Level level, int xt, int yt) {
		damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage - 1);
			return true;
		}
		return false;
	}
}
