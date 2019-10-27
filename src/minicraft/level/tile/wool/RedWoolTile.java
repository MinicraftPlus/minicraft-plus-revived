package minicraft.level.tile.wool;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class RedWoolTile extends Tile {
    private static Sprite sprite = new Sprite(10, 0, 2, 2, 1);

    public RedWoolTile(String name) {
        super(name, sprite);
    }

    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Shovel) {
                if (player.payStamina(3 - tool.level) && tool.payDurability()) {
                    level.setTile(xt, yt, Tiles.get("hole"));
                    Sound.monsterHurt.play();
                    level.dropItem(xt*16+8, yt*16+8, Items.get("Red Wool"));
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mayPass(Level level, int x, int y, Entity e) {
        return e.canWool();
    }

}
