package minicraft.level.tile.WoolTiles;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

public class BlueWoolTile extends Tile {

    private static Sprite sprite = Sprite.repeat(17, 0, 2, 2, Color.get(15, 115, 15, 115));

    public BlueWoolTile() {
        super("Blue Wool", sprite);
    }

    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Shovel) {
                if (player.payStamina(3 - tool.level) && tool.payDurability()) {
                    level.setTile(xt, yt, Tiles.get("hole"));
                    level.dropItem(xt*16+8, yt*16+8, Items.get("Blue Wool"));
                    Sound.monsterHurt.play();
                    return true;
                }
            }
        }
        return false;
    }
}
