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
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.screen.AchievementsDisplay;
import minicraft.util.AdvancementElement;

public class TreeTile extends Tile {
	private static final LinkedSprite oakSprite = new LinkedSprite(SpriteType.Tile, "oak");
	private static final LinkedSprite oakSpriteFull = new LinkedSprite(SpriteType.Tile, "oak_full");
	private static final LinkedSprite spruceSprite = new LinkedSprite(SpriteType.Tile, "spruce");
	private static final LinkedSprite spruceSpriteFull = new LinkedSprite(SpriteType.Tile, "spruce_full");
	private static final LinkedSprite birchSprite = new LinkedSprite(SpriteType.Tile, "birch");
	private static final LinkedSprite birchSpriteFull = new LinkedSprite(SpriteType.Tile, "birch_full");
	private static final LinkedSprite ashSprite = new LinkedSprite(SpriteType.Tile, "ash");
	private static final LinkedSprite ashSpriteFull = new LinkedSprite(SpriteType.Tile, "ash_full");
	private static final LinkedSprite aspenSprite = new LinkedSprite(SpriteType.Tile, "aspen");
	private static final LinkedSprite aspenSpriteFull = new LinkedSprite(SpriteType.Tile, "aspen_full");
	private static final LinkedSprite firSprite = new LinkedSprite(SpriteType.Tile, "fir");
	private static final LinkedSprite firSpriteFull = new LinkedSprite(SpriteType.Tile, "fir_full");
	private static final LinkedSprite willowSprite = new LinkedSprite(SpriteType.Tile, "willow");
	private static final LinkedSprite willowSpriteFull = new LinkedSprite(SpriteType.Tile, "willow_full");

	public enum TreeType {
		OAK(oakSprite, oakSpriteFull),
		SPRUCE(spruceSprite, spruceSpriteFull),
		BIRCH(birchSprite, birchSpriteFull),
		ASH(ashSprite, ashSpriteFull),
		ASPEN(aspenSprite, aspenSpriteFull),
		FIR(firSprite, firSpriteFull),
		WILLOW(willowSprite, willowSpriteFull);

		private final LinkedSprite treeSprite;
		private final LinkedSprite treeSpriteFull;

		TreeType(LinkedSprite treeSprite, LinkedSprite treeSpriteFull) {
			this.treeSprite = treeSprite;
			this.treeSpriteFull = treeSpriteFull;
		}
	}

	protected TreeTile(String name) {
		super(name, null);
		connectsToGrass = true;
	}

	@SuppressWarnings("PointlessArithmeticExpression")
	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Grass").render(screen, level, x, y);

		TreeType thisType = level.treeTypes[x + y * level.w];
		// Checking whether the target direction has targeted the same TreeTile
		boolean isUpTileSame = level.getTile(x, y - 1) == this && thisType == level.treeTypes[x + (y - 1) * level.w];
		boolean isLeftTileSame = level.getTile(x - 1, y) == this && thisType == level.treeTypes[(x - 1) + y * level.w];
		boolean isRightTileSame = level.getTile(x + 1, y) == this && thisType == level.treeTypes[(x + 1) + y * level.w];
		boolean isDownTileSame = level.getTile(x, y + 1) == this && thisType == level.treeTypes[x + (y + 1) * level.w];
		boolean isUpLeftTileSame = level.getTile(x - 1, y - 1) == this && thisType == level.treeTypes[(x - 1) + (y - 1) * level.w];
		boolean isUpRightTileSame = level.getTile(x + 1, y - 1) == this && thisType == level.treeTypes[(x + 1) + (y - 1) * level.w];
		boolean isDownLeftTileSame = level.getTile(x - 1, y + 1) == this && thisType == level.treeTypes[(x - 1) + (y + 1) * level.w];
		boolean isDownRightTileSame = level.getTile(x + 1, y + 1) == this && thisType == level.treeTypes[(x + 1) + (y + 1) * level.w];

		Sprite sprite = level.treeTypes[x + y * level.w].treeSprite.getSprite();
		Sprite spriteFull = level.treeTypes[x + y * level.w].treeSpriteFull.getSprite();

		if (isUpTileSame && isUpLeftTileSame && isLeftTileSame) {
			screen.render(x * 16 + 0, y * 16, spriteFull.spritePixels[0][1]);
		} else {
			screen.render(x * 16 + 0, y * 16, sprite.spritePixels[0][0]);
		}

		if (isUpTileSame && isUpRightTileSame && isRightTileSame) {
			screen.render(x * 16 + 8, y * 16, spriteFull.spritePixels[0][0]);
		} else {
			screen.render(x * 16 + 8, y * 16, sprite.spritePixels[0][1]);
		}

		if (isDownTileSame && isDownLeftTileSame && isLeftTileSame) {
			screen.render(x * 16 + 0, y * 16 + 8, spriteFull.spritePixels[1][1]);
		} else {
			screen.render(x * 16 + 0, y * 16 + 8, sprite.spritePixels[1][0]);
		}

		if (isDownTileSame && isDownRightTileSame && isRightTileSame) {
			screen.render(x * 16 + 8, y * 16 + 8, spriteFull.spritePixels[1][0]);
		} else {
			screen.render(x * 16 + 8, y * 16 + 8, sprite.spritePixels[1][1]);
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

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return false;
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
			if (tool.type == ToolType.Axe) {
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
		if (random.nextInt(100) == 0)
			level.dropItem(x * 16 + 8, y * 16 + 8, Items.get("Apple"));

		int damage = level.getData(x, y) + dmg;
		int treeHealth = 20;
		if (Game.isMode("minicraft.settings.mode.creative")) dmg = damage = treeHealth;

		level.add(new SmashParticle(x * 16, y * 16));
		Sound.play("monsterhurt");

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= treeHealth) {
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3, Items.get("Wood"));
			level.dropItem(x * 16 + 8, y * 16 + 8, 0, 2, Items.get("Acorn"));
			level.setTile(x, y, Tiles.get("Grass"));
			AchievementsDisplay.setAchievement("minicraft.achievement.woodcutter", true);
		} else {
			level.setData(x, y, damage);
		}
	}
}
