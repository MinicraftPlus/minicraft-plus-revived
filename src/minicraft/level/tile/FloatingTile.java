package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class FloatingTile extends Tile {
    private static ConnectorSprite sprite = new ConnectorSprite(FloatingTile.class, new Sprite(9, 24, 3, 3, Color.get(-1, -1, 430, 540), 3), new Sprite(10, 22, 2, 2, Color.get(-1, -1, 430, 540), 3), ConnectorSprite.makeSprite(2, 2, Color.get(-1, -1, 540, 540), 0, true, 21));

    public FloatingTile() {
        super("Float Tile", sprite);
    }

    @Override
    public boolean mayPass(Level level, int x, int y, Entity e) {
        return true;
    }

    @Override
    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem)item;
            if (tool.type == ToolType.Shovel && player.payStamina(5)) {
                level.setTile(xt, yt, Tiles.get("Infinite Fall"));
                Sound.monsterHurt.play();
                level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Platform"));
                return true;
            }
        }
        return false;
    }
}
