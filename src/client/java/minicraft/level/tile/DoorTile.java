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
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;
import org.jetbrains.annotations.Nullable;

public class DoorTile extends Tile {
	protected Material type;
	private SpriteAnimation closedSprite;
	private SpriteAnimation openSprite;

	protected DoorTile(Material type) {
		this(type, null);
	}

	protected DoorTile(Material type, String name) {
		super(type.name() + " " + (name == null ? "Door" : name), null);
		this.type = type;
		switch (type) {
			case Wood:
				closedSprite = new SpriteAnimation(SpriteType.Tile, "wood_door");
				openSprite = new SpriteAnimation(SpriteType.Tile, "wood_door_opened");
				break;
			case Stone:
				closedSprite = new SpriteAnimation(SpriteType.Tile, "stone_door");
				openSprite = new SpriteAnimation(SpriteType.Tile, "stone_door_opened");
				break;
			case Obsidian:
				closedSprite = new SpriteAnimation(SpriteType.Tile, "obsidian_door");
				openSprite = new SpriteAnimation(SpriteType.Tile, "obsidian_door_opened");
				break;
		}
		sprite = closedSprite;
	}

	public void render(Screen screen, Level level, int x, int y) {
		boolean closed = level.getData(x, y) == 0;
		SpriteAnimation curSprite = closed ? closedSprite : openSprite;
		curSprite.render(screen, level, x, y);
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {
		level.add(new SmashParticle(x << 4, y << 4));
		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if (Game.isMode("minicraft.settings.mode.creative")) {
			level.setTile(x, y, Tiles.get((short) (id + 3))); // Will get the corresponding floor tile.
			Sound.play("monsterhurt");
			level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get(type.name() + " Door"));
			return true;
		}

		if (item instanceof ToolItem && source instanceof Player) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == type.getRequiredTool()) {
				if (((Player) source).payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					level.setTile(x, y, Tiles.get((short) (id + 3))); // Will get the corresponding floor tile.
					Sound.play("monsterhurt");
					level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get(type.name() + " Door"));
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, x, y, level.depth));
					return true;
				}
			}
		}

		handleDamage(level, x, y, source, item, 0);
		return true;
	}

	@Override
	public boolean use(Level level, int xt, int yt, Player player, @Nullable Item item, Direction attackDir) {
		boolean closed = level.getData(xt, yt) == 0;
		level.setData(xt, yt, closed ? 1 : 0);
		return true;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		boolean closed = level.getData(x, y) == 0;
		return !closed;
	}
}
