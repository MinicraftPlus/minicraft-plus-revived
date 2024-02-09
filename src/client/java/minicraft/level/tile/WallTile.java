package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;

public class WallTile extends Tile {
	private static SpriteAnimation wood = new SpriteAnimation(SpriteType.Tile, "wood_wall")
		.setConnectChecker((tile, side) -> tile.getClass() == WallTile.class);
	private static SpriteAnimation stone = new SpriteAnimation(SpriteType.Tile, "stone_wall")
		.setConnectChecker((tile, side) -> tile.getClass() == WallTile.class);
	private static SpriteAnimation obsidian = new SpriteAnimation(SpriteType.Tile, "obsidian_wall")
		.setConnectChecker((tile, side) -> tile.getClass() == WallTile.class);

	private static final String obrickMsg = "minicraft.notification.defeat_air_wizard_first";
	protected Material type;

	protected WallTile(Material type) {
		this(type, null);
	}

	protected WallTile(Material type, String name) {
		super(type.name() + " " + (name == null ? "Wall" : name), null);
		this.type = type;
		switch (type) {
			case Wood:
				sprite = wood;
				break;
			case Stone:
				sprite = stone;
				break;
			case Obsidian:
				sprite = obsidian;
				break;
		}
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		if (Game.isMode("minicraft.settings.mode.creative") || level.depth != -3 || type != Material.Obsidian || AirWizard.beaten) {
			hurt(level, x, y, 0);
			return true;
		} else {
			Game.notifications.add(Localization.getLocalized(obrickMsg));
			return false;
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (Game.isMode("minicraft.settings.mode.creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (level.depth != -3 || type != Material.Obsidian || AirWizard.beaten) {
					if (player.payStamina(4 - tool.level) && tool.payDurability()) {
						int data = level.getData(xt, yt);
						hurt(level, xt, yt, tool.getDamage());
						AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
							new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
								item, this, data, xt, yt, level.depth));
						return true;
					}
				} else {
					Game.notifications.add(obrickMsg);
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int sbwHealth = 100;
		if (Game.isMode("minicraft.settings.mode.creative")) dmg = damage = sbwHealth;

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.play("monsterhurt");

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= sbwHealth) {
			String itemName = "", tilename = "";
			switch (type) { // Get what tile to set and what item to drop
				case Wood: {
					itemName = "Plank";
					tilename = "Wood Planks";
					break;
				}
				case Stone: {
					itemName = "Stone Brick";
					tilename = "Stone Bricks";
					break;
				}
				case Obsidian: {
					itemName = "Obsidian Brick";
					tilename = "Obsidian";
					break;
				}
			}

			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3 - type.ordinal(), Items.get(itemName));
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
		return Material.values[data].name() + " Wall";
	}
}
