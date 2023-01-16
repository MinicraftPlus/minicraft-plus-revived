package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class PathTile extends Tile {
    private static SpriteAnimation sprite = new SpriteAnimation(SpriteType.Tile, "path");

    public PathTile(String name) {
        super(name, sprite);
        connectsToGrass = true;
        maySpawn = true;
    }

    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Shovel) {
                if (player.payStamina(4 - tool.level) && tool.payDurability()) {
                    level.setTile(xt, yt, Tiles.get("Hole"));
                    Sound.play("monster_hurt");
                    level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Dirt"));
                    return true;
                }
            }
        }
        return false;
    }
}
