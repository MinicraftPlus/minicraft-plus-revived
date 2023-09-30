package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;
import minicraft.util.Logging;

public class FenceTile extends Tile {

	private static SpriteAnimation wood = new SpriteAnimation(SpriteType.Tile, "wood_fence");
	private static SpriteAnimation stone = new SpriteAnimation(SpriteType.Tile, "stone_fence");
	private static SpriteAnimation obsidian = new SpriteAnimation(SpriteType.Tile, "obsidian_fence");

	protected Material type;

	protected SpriteAnimation top, bottom, left, right;

	public boolean connectUp = false, connectDown = false, connectLeft = false, connectRight = false;

	protected FenceTile(Material type) { this(type, null); }
	protected FenceTile(Material type, String name) {
		super(type.name() + " " + (name == null ? "Fence" : name), null);
		this.type = type;
		switch (type)
		{
			case Wood:
				sprite = wood;
				connectsToGrass = true;
				break;
			case Stone:
				sprite = stone;
				break;
			case Obsidian:
				sprite = obsidian;
				break;
		}
		top = new SpriteAnimation(SpriteType.Tile, type.name().toLowerCase() + "_fence_top");
		bottom = new SpriteAnimation(SpriteType.Tile, type.name().toLowerCase() + "_fence_bottom");
		left = new SpriteAnimation(SpriteType.Tile, type.name().toLowerCase() + "_fence_left");
		right = new SpriteAnimation(SpriteType.Tile, type.name().toLowerCase() + "_fence_right");
	}

	public void updateConnections(Level level, int x, int y)
	{
		connectUp = level.getTile(x, y - 1).name.equals(name);
		connectDown = level.getTile(x, y + 1).name.equals(name);
		connectLeft = level.getTile(x - 1, y).name.equals(name);
		connectRight = level.getTile(x + 1, y).name.equals(name);
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void render(Screen screen, Level level, int x, int y)
	{
		switch (type)
		{
			case Wood: Tiles.get("Grass").render(screen, level, x, y); break;
			case Stone: Tiles.get("Stone Bricks").render(screen, level, x, y); break;
			case Obsidian: Tiles.get("Obsidian").render(screen, level, x, y); break;
		}

		sprite.render(screen, level, x, y);

		updateConnections(level, x, y);

		// up
		if (connectUp) {
			top.render(screen, level, x, y);
		}
		// bottom
		if (connectDown) {
			bottom.render(screen, level, x, y);
		}
		// left
		if (connectLeft) {
			left.render(screen, level, x, y);
		}
		// right
		if (connectRight) {
			right.render(screen, level, x, y);
		}
	}

	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		hurt(level, x, y, dmg);
		return true;
	}

	@Override
	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(Game.isMode("minicraft.settings.mode.creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(xt, yt);
					hurt(level, xt, yt, tool.getDamage());
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, xt, yt, level.depth));
					return true;
				}
			}
		}
		return false;
	}

	public void hurt(Level level, int x, int y, int dmg) {
		int damage = level.getData(x, y) + dmg;
		int fenceHealth = 5;
		if (Game.isMode("minicraft.settings.mode.creative")) dmg = damage = fenceHealth;

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.play("monsterhurt");

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));

		if (damage >= fenceHealth) {
			String itemName = "", tilename = "";
			switch (type) { // Get what tile to set and what item to drop
				case Wood: {
					itemName = "Wood Fence";
					tilename = "Grass";
					break;
				}
				case Stone: {
					itemName = "Stone Fence";
					tilename = "Stone Bricks";
					break;
				}
				case Obsidian: {
					itemName = "Obsidian Fence";
					tilename = "Obsidian";
					break;
				}
			}

			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 1, Items.get(itemName));
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
		return Material.values[data].name() + " Fence";
	}
}
