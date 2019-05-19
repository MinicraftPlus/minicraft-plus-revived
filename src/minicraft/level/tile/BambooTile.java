package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

/** this class is only ever used in world generation,
 *  it is never placed by the player, like cloud cacti
 **/
public class BambooTile extends Tile {
    private static Sprite spriteLeft = new Sprite(20, 1, 1, 2, Color.get(-1, 30, 141, 252));
    private static Sprite spriteRight = new Sprite(20, 1, 1, 2, Color.get(-1, 30, 141, 252), 1);

    public BambooTile(String name) {
        super(name, (Sprite)null);
        this.connectsToJungle = true;
    }

    @Override
    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem)item;
            if (tool.type == ToolType.Axe) {
                if (player.payStamina(4 - tool.level) && tool.payDurability()) {
                    level.setTile(xt, yt, Tiles.get("Jungle"));
                    Sound.monsterHurt.play();
                    int count = random.nextInt(2) + 1;
                    level.dropItem(xt * 16 + 8, yt * 16 + 8, count, Items.get("Bamboo"));
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public boolean mayPass(Level level, int x, int y, Entity e) {
        return false;
    }

    @Override
    public void render(Screen screen, Level level, int x, int y) {
        Tiles.get("Jungle").render(screen, level, x, y);

        x = x << 4;
        y = y << 4;

        spriteLeft.render(screen, x, y);
        spriteRight.render(screen, x + 8, y);
    }
}
