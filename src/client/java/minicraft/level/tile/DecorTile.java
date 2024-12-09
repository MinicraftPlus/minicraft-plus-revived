package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
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

public class DecorTile extends Tile {
	private static final SpriteAnimation stoneSprite = new SpriteAnimation(SpriteType.Tile, "ornate_stone");
	private static final SpriteAnimation obsidianSprite = new SpriteAnimation(SpriteType.Tile, "ornate_obsidian");
	private static final SpriteAnimation woodSprite = new SpriteAnimation(SpriteType.Tile, "ornate_wood");
	private static final SpriteAnimation sandStoneSprite = new SpriteAnimation(SpriteType.Tile, "sandstone");
	private static final SpriteAnimation rawEtherealSprite = new SpriteAnimation(SpriteType.Tile, "cloud");//"raw_ethereal");

	public enum decorType {
		ORNATE_OBSIDIAN(obsidianSprite, "Ornate Obsidian", Material.Obsidian),
		ORNATE_STONE(stoneSprite, "Ornate Stone", Material.Stone),
		ORNATE_WOOD(woodSprite, "Ornate Wood", Material.Wood),
		SANDSTONE(sandStoneSprite, "Sandstone", Material.Stone),
		RAW_ETHEREAL(rawEtherealSprite, "Raw Ethereal", Material.Obsidian),;

		private final SpriteAnimation decorSprite;
		private final String name;
		private final Material mType;

		decorType (SpriteAnimation sprite, String name, Material mType) {
			this.decorSprite = sprite;
			this.name = name;
			this.mType = mType;
		}
	}

	private final decorType thisType;
	private final Material mType;

	protected DecorTile(decorType type) {
		super(type.name, null);
		maySpawn = true;
		thisType = type;
		sprite = thisType.decorSprite;
		mType = thisType.mType;
	}

	@Override
	public void render(Screen screen, Level level, int x, int y) {
		super.render(screen, level, x, y);
		screen.render(x * 16 + 0, y * 16, sprite.getCurrentFrame().getSprite().spritePixels[0][0]);
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {
		level.add(new SmashParticle(x << 4, y << 4));
		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if (Game.isMode("minicraft.settings.mode.creative")) {
			if (level.depth == 1) {
				level.setTile(x, y, Tiles.get("Cloud"));
			} else {
				level.setTile(x, y, Tiles.get("Hole"));
			}
			Item drop;
			switch (thisType) {
				case ORNATE_STONE:
					drop = Items.get("Ornate Stone");
					break;
				case ORNATE_OBSIDIAN:
					drop = Items.get("Ornate Obsidian");
					break;
				case ORNATE_WOOD:
					drop = Items.get("Ornate Wood");
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + thisType);
			}
			Sound.play("monsterhurt");
			level.dropItem((x << 4) + 8, (y << 4) + 8, drop);
			return true;
		}

		if (item instanceof ToolItem && source instanceof Player) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == mType.getRequiredTool()) {
				if (((Player) source).payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(x, y);
					if (level.depth == 1) {
						level.setTile(x, y, Tiles.get("Cloud"));
					} else {
						level.setTile(x, y, Tiles.get("Hole"));
					}
					Item drop;
					switch (thisType) {
						case ORNATE_STONE:
							drop = Items.get("Ornate Stone");
							break;
						case ORNATE_OBSIDIAN:
							drop = Items.get("Ornate Obsidian");
							break;
						case ORNATE_WOOD:
							drop = Items.get("Ornate Wood");
							break;
						default:
							throw new IllegalStateException("Unexpected value: " + thisType);
					}
					Sound.play("monsterhurt");
					level.dropItem((x << 4) + 8, (y << 4) + 8, drop);
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

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
