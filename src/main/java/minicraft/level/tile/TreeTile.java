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
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.screen.AchievementsDisplay;
import minicraft.screen.QuestsDisplay;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

	private final TreeType type;

	public static final Set<Short> treeIDs = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		(short) 8, (short) 47, (short) 48, (short) 49, (short) 50, (short) 51, (short) 52)));

	public enum TreeType {
		OAK(oakSprite, oakSpriteFull),
		SPRUCE(spruceSprite, spruceSpriteFull),
		BIRCH(birchSprite, birchSpriteFull),
		ASH(ashSprite, ashSpriteFull),
		ASPEN(aspenSprite, aspenSpriteFull),
		FIR(firSprite, firSpriteFull),
		WILLOW(willowSprite, willowSpriteFull),
		;

		private final LinkedSprite treeSprite;
		private final LinkedSprite treeSpriteFull;

		TreeType(LinkedSprite treeSprite, LinkedSprite treeSpriteFull) {
			this.treeSprite = treeSprite;
			this.treeSpriteFull = treeSpriteFull;
		}
	}

	protected TreeTile(TreeType type) {
		super(type.name().toLowerCase(), null);
		this.type = type;
		connectsToGrass = true;
	}

	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get("Grass").render(screen, level, x, y);

		boolean u = level.getTile(x, y - 1) == this;
		boolean l = level.getTile(x - 1, y) == this;
		boolean r = level.getTile(x + 1, y) == this;
		boolean d = level.getTile(x, y + 1) == this;
		boolean ul = level.getTile(x - 1, y - 1) == this;
		boolean ur = level.getTile(x + 1, y - 1) == this;
		boolean dl = level.getTile(x - 1, y + 1) == this;
		boolean dr = level.getTile(x + 1, y + 1) == this;

		Sprite sprite = type.treeSprite.getSprite();
		Sprite spriteFull = type.treeSpriteFull.getSprite();

		if (u && ul && l) {
			screen.render(x * 16, y * 16, spriteFull.spritePixels[0][1]);
		} else {
			screen.render(x * 16, y * 16, sprite.spritePixels[0][0]);
		}

		if (u && ur && r) {
			screen.render(x * 16 + 8, y * 16, spriteFull.spritePixels[0][0]);
		} else {
			screen.render(x * 16 + 8, y * 16, sprite.spritePixels[0][1]);
		}

		if (d && dl && l) {
			screen.render(x * 16, y * 16 + 8, spriteFull.spritePixels[1][1]);
		} else {
			screen.render(x * 16, y * 16 + 8, sprite.spritePixels[1][0]);
		}

		if (d && dr && r) {
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
		if(Game.isMode("minicraft.settings.mode.creative"))
			return false; // Go directly to hurt method
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == ToolType.Axe) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					hurt(level, xt, yt, tool.getDamage());
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

		level.add(new SmashParticle(x*16, y*16));
		Sound.play("monsterhurt");

		level.add(new TextParticle("" + dmg, x * 16 + 8, y * 16 + 8, Color.RED));
		if (damage >= treeHealth) {
			level.dropItem(x * 16 + 8, y * 16 + 8, 1, 3, Items.get("Wood"));
			level.dropItem(x * 16 +  8, y * 16 + 8, 0, 2, Items.get("Acorn"));
			level.setTile(x, y, Tiles.get("Grass"));
			AchievementsDisplay.setAchievement("minicraft.achievement.woodcutter", true);
			QuestsDisplay.questCompleted("minicraft.quest.tutorial.get_started.kill_tree");
		} else {
			level.setData(x, y, damage);
		}
	}
}
