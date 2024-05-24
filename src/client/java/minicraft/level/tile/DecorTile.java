package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;

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

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof ToolItem) {
			ToolItem tool = (ToolItem) item;
			if (tool.type == mType.getRequiredTool()) {
				if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(xt, yt);
					if (level.depth == 1) {
						level.setTile(xt, yt, Tiles.get("Cloud"));
					} else {
						level.setTile(xt, yt, Tiles.get("Hole"));
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
					level.dropItem((xt << 4) + 8, (yt << 4) + 8, drop);
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, xt, yt, level.depth));
					return true;
				}
			}
		}
		return false;
	}

	public boolean mayPass(Level level, int x, int y, Entity e) {
		return true;
	}
}
