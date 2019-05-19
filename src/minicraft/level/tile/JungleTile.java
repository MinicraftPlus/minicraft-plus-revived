package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.ConnectorSprite;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

/** this is just to differentiate normal grass and jungles
 *  used for world gen
 */
public class JungleTile extends Tile {
    private static ConnectorSprite sprite = new ConnectorSprite(GrassTile.class, new Sprite(11, 0, 3, 3, Color.get(210, 210, 320, 321), 3), Sprite.dots(Color.get(210, 210, 40, 321)))
    {
        public boolean connectsTo(Tile tile, boolean isSide) {
            if(!isSide) return true;
            return tile.connectsToJungle;
        }
    };

    public JungleTile(String name) {
        super(name, sprite);
        csprite.sides = csprite.sparse;
        connectsToJungle = true;
        maySpawn = true;
    }

    public void tick(Level level, int xt, int yt) {
        // TODO revise this method.
        if (random.nextInt(40) != 0) return;

        int xn = xt;
        int yn = yt;

        if (random.nextBoolean()) xn += random.nextInt(2) * 2 - 1;
        else yn += random.nextInt(2) * 2 - 1;

        if (level.getTile(xn, yn) == Tiles.get("dirt")) {
            level.setTile(xn, yn, this);
        }
    }

    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Shovel) {
                if (player.payStamina(4 - tool.level) && tool.payDurability()) {
                    level.setTile(xt, yt, Tiles.get("dirt"));
                    Sound.monsterHurt.play();
                    return true;
                }
            }
            if (tool.type == ToolType.Hoe) {
                if (player.payStamina(4 - tool.level) && tool.payDurability()) {
                    level.setTile(xt, yt, Tiles.get("dirt"));
                    Sound.monsterHurt.play();
                    return true;
                }
            }
        }
        return false;
    }
}
