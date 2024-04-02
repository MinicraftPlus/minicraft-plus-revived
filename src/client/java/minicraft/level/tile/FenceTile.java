package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;

public class FenceTile extends Tile {

	private static final SpriteAnimation wood = new SpriteAnimation(SpriteType.Tile, "wood_fence");
	private static final SpriteAnimation stone = new SpriteAnimation(SpriteType.Tile, "stone_fence");
	private static final SpriteAnimation obsidian = new SpriteAnimation(SpriteType.Tile, "obsidian_fence");

	protected final Material type;

	protected final SpriteAnimation top, bottom, left, right;

	public boolean connectUp = false, connectDown = false, connectLeft = false, connectRight = false;

	protected FenceTile(Material type) { this(type, null); }

	protected FenceTile(Material type, String name) {
		super(type + " " + (name == null ? "Fence" : name), null);
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
		top = new SpriteAnimation(SpriteType.Tile, type.toString().toLowerCase() + "_fence_top");
		bottom = new SpriteAnimation(SpriteType.Tile, type.toString().toLowerCase() + "_fence_bottom");
		left = new SpriteAnimation(SpriteType.Tile, type.toString().toLowerCase() + "_fence_left");
		right = new SpriteAnimation(SpriteType.Tile, type.toString().toLowerCase() + "_fence_right");
	}

	@Override
	public boolean connectsToSand(Level level, int x, int y) {
		return Tiles.get((short) level.getData(x, y)).connectsToSand(level, x, y);
	}

	@Override
	public boolean connectsToFluid(Level level, int x, int y) {
		return Tiles.get((short) level.getData(x, y)).connectsToFluid(level, x, y);
	}

	@Override
	public boolean connectsToGrass(Level level, int x, int y) {
		return Tiles.get((short) level.getData(x, y)).connectsToGrass(level, x, y);
	}

	public void updateConnections(Level level, int x, int y) {
		// TODO Tile#updateNeighbourhood
		connectUp = level.getTile(x, y - 1) instanceof FenceTile;
		connectDown = level.getTile(x, y + 1) instanceof FenceTile;
		connectLeft = level.getTile(x - 1, y) instanceof FenceTile;
		connectRight = level.getTile(x + 1, y) instanceof FenceTile;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
	}

	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get((short) level.getData(x, y)).render(screen, level, x, y);
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
		if (Game.isMode("minicraft.settings.mode.creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(xt, yt);
					Sound.play("monsterhurt");
					level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get(name));
					level.setTile(xt, yt, Tiles.get((short) level.getData(xt, yt)));
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
		if (Game.isMode("minicraft.settings.mode.creative")) {
			level.add(new SmashParticle(x * 16, y * 16));
			Sound.play("monsterhurt");
			level.dropItem(x * 16 + 8, y * 16 + 8, Items.get(name));
			level.setTile(x, y, Tiles.get((short) level.getData(x, y)));
		}
	}
}
