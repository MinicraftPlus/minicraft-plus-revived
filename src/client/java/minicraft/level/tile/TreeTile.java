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
import minicraft.gfx.SpriteManager.SpriteLink;
import minicraft.gfx.SpriteManager.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.screen.AchievementsDisplay;
import minicraft.util.AdvancementElement;

public class TreeTile extends Tile {
	private static final SpriteLink oakSprite = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "oak").createSpriteLink();
	private static final SpriteLink oakSpriteFull = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "oak_full").createSpriteLink();
	private static final SpriteLink spruceSprite = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "spruce").createSpriteLink();
	private static final SpriteLink spruceSpriteFull = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "spruce_full").createSpriteLink();
	private static final SpriteLink birchSprite = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "birch").createSpriteLink();
	private static final SpriteLink birchSpriteFull = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "birch_full").createSpriteLink();
	private static final SpriteLink ashSprite = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "ash").createSpriteLink();
	private static final SpriteLink ashSpriteFull = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "ash_full").createSpriteLink();
	private static final SpriteLink aspenSprite = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "aspen").createSpriteLink();
	private static final SpriteLink aspenSpriteFull = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "aspen_full").createSpriteLink();
	private static final SpriteLink firSprite = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "fir").createSpriteLink();
	private static final SpriteLink firSpriteFull = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "fir_full").createSpriteLink();
	private static final SpriteLink willowSprite = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "willow").createSpriteLink();
	private static final SpriteLink willowSpriteFull = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, "willow_full").createSpriteLink();

	public enum TreeType {
		OAK(oakSprite, oakSpriteFull),
		SPRUCE(spruceSprite, spruceSpriteFull),
		BIRCH(birchSprite, birchSpriteFull),
		ASH(ashSprite, ashSpriteFull),
		ASPEN(aspenSprite, aspenSpriteFull),
		FIR(firSprite, firSpriteFull),
		WILLOW(willowSprite, willowSpriteFull);

		private final SpriteLink treeSprite;
		private final SpriteLink treeSpriteFull;

		TreeType(SpriteLink treeSprite, SpriteLink treeSpriteFull) {
			this.treeSprite = treeSprite;
			this.treeSpriteFull = treeSpriteFull;
		}
	}

	protected TreeTile(String name) {
		super(name, null);
	}

	@Override
	public boolean connectsToGrass(Level level, int x, int y) {
		return true;
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
			screen.render((x << 4) + 0, (y << 4) + 0, spriteFull.spritePixels[0][1]);
		} else {
			screen.render((x << 4) + 0, (y << 4) + 0, sprite.spritePixels[0][0]);
		}

		if (isUpTileSame && isUpRightTileSame && isRightTileSame) {
			screen.render((x << 4) + 8, (y << 4) + 0, spriteFull.spritePixels[0][0]);
		} else {
			screen.render((x << 4) + 8, (y << 4) + 0, sprite.spritePixels[0][1]);
		}

		if (isDownTileSame && isDownLeftTileSame && isLeftTileSame) {
			screen.render((x << 4) + 0, (y << 4) + 8, spriteFull.spritePixels[1][1]);
		} else {
			screen.render((x << 4) + 0, (y << 4) + 8, sprite.spritePixels[1][0]);
		}

		if (isDownTileSame && isDownRightTileSame && isRightTileSame) {
			screen.render((x << 4) + 8, (y << 4) + 8, spriteFull.spritePixels[1][0]);
		} else {
			screen.render((x << 4) + 8, (y << 4) + 8, sprite.spritePixels[1][1]);
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
