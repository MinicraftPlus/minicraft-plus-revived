package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;

public class BossWallTile extends Tile {

	private static final String wallMsg = "The Obsidian Knight must be defeated first.";
	protected Material type;
	private ConnectorSprite sprite;

	protected BossWallTile(Material type) {
		super(type.name() + " Boss Wall", (ConnectorSprite) null);
		this.type = type;
		if (type == Material.Obsidian) {
			sprite = new ConnectorSprite(BossWallTile.class, new Sprite(20, 14, 3, 3, 1, 3), new Sprite(23, 14, 2, 2, 1, 3), new Sprite(21, 15, 2, 2, 1, 0, true));
		}
		csprite = sprite;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		if(ObsidianKnight.active)return false;
		if (Game.isMode("Creative") || ObsidianKnight.beaten || ObsidianKnight.failed || !ObsidianKnight.active) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		if (Game.isMode("minicraft.settings.mode.creative") || ObsidianKnight.failed || !ObsidianKnight.active) {
			hurt(level, x, y, random.nextInt(6) / 6 * dmg / 2);
			return true;
		} else {
			Game.notifications.add(wallMsg);
			return false;
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (Game.isMode("Creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (ObsidianKnight.beaten || ObsidianKnight.failed || !ObsidianKnight.active) {
					if (player.payStamina(4 - tool.level) && tool.payDurability()) {
						hurt(level, xt, yt, tool.getDamage());
						return true;
					}
				} else {
					Game.notifications.add(wallMsg);
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int sbwHealth = 100;
		if (Game.isMode("Creative")) dmg = damage = sbwHealth;

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.monsterHurt.play();

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= sbwHealth) {
			String itemName = "", tilename = "";
			// Get what tile to set and what item to drop
			if (type == Material.Obsidian) {
				itemName = "Obsidian Boss Wall";
				tilename = "Obsidian Boss Wall";
			}

			//level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3 - type.ordinal(), Items.get(itemName));
			level.setTile(x, y, Tiles.get(tilename));
		} else {
			level.setData(x, y, damage);
		}
	}

	public boolean tick(Level level, int xt, int yt) {
		int damage = level.getData(xt, yt);
		if (damage > 0) {
			level.setData(xt, yt, damage - 1);
			return true;
		}
		return false;
	}

	public String getName(int data) {
		return Material.values[data].name() + " Boss Wall";
	}
}
