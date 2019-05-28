package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;

public class SpikeTile extends Tile {
    private static Sprite sprite = new Sprite(20, 1, Color.get(-1, 30, 141, 252));

    public SpikeTile(String name) {
        super(name, (Sprite)null);
        connectsToSand = true;
        connectsToWater = true;
        connectsToLava = true;
    }

    @Override
    public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
        int numSpikes = level.getData(xt, yt);
        if (item.equals(Items.get("Spike"))) {
            if (numSpikes < 4) {
                numSpikes++;
                level.setData(xt, yt, numSpikes);
                Game.notifications.add("Spike upgraded");
                return true;
            } else {
                Game.notifications.add("Spike at max level");
            }
        }
        if (item instanceof ToolItem) {
            ToolItem tool = (ToolItem) item;
            if (tool.type == ToolType.Shovel) {
                if (player.payStamina(4 - tool.level) && tool.payDurability()) {
                    level.setTile(xt, yt, Tiles.get("Hole"));
                    level.dropItem(xt * 16 + 8, yt * 16 + 8, numSpikes, Items.get("Spike"));
                    Sound.monsterHurt.play();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void render(Screen screen, Level level, int x, int y) {
        Tiles.get("Hole").render(screen, level, x, y);

        x = x << 4;
        y = y << 4;

        screen.render(x - 4, y, 21 + 1 * 32, Color.get(-1, 30, 141, 252), 0);
        screen.render(x + 4, y, 21 + 1 * 32, Color.get(-1, 30, 141, 252), 1);
        screen.render(x + 4, y, 21 + 1 * 32, Color.get(-1, 30, 141, 252), 0);
        screen.render(x + 12, y, 21 + 1 * 32, Color.get(-1, 30, 141, 252), 1);
        screen.render(x - 4, y + 8, 21 + 1 * 32, Color.get(-1, 30, 141, 252), 0);
        screen.render(x + 4, y + 8, 21 + 1 * 32, Color.get(-1, 30, 141, 252), 1);
        screen.render(x + 4, y + 8, 21 + 1 * 32, Color.get(-1, 30, 141, 252), 0);
        screen.render(x + 12, y + 8, 21 + 1 * 32, Color.get(-1, 30, 141, 252), 1);
    }

    @Override
    public int getMobDamage(int xt, int yt) {
        return Game.levels[Game.currentLevel].getData(xt, yt);
    }

    @Override
    public int getDefaultData() {
        return 1;
    }

    @Override
    public boolean canHurtMob() {
        return true;
    }
}
