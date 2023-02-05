package minicraft.level.tile.farming;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.util.AdvancementElement;

public class FarmTile extends Tile {
    private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "farmland");

    public FarmTile(String name) {
        super(name, sprite);
    }
    protected FarmTile(String name, SpriteAnimation sprite) {
        super(name, sprite);
    }

    @Override
    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Shovel) {
                if (player.payStamina(4 - tool.level) && tool.payDurability()) {
					int data = level.getData(xt, yt);
                    level.setTile(xt, yt, Tiles.get("Dirt"));
                    Sound.play("monsterhurt");
					AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
						new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
							item, this, data, xt, yt, level.depth));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean tick(Level level, int xt, int yt) {
        int age = level.getData(xt, yt);
        if (age < 5) level.setData(xt, yt, age + 1);
        return true;
    }

    @Override
    public void steppedOn(Level level, int xt, int yt, Entity entity) {
        if (entity instanceof ItemEntity) return;
        if (random.nextInt(60) != 0) return;
        if (level.getData(xt, yt) < 5) return;
        level.setTile(xt, yt, Tiles.get("Dirt"));
    }
}
