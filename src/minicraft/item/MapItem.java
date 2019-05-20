package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.MapDisplay;

public class MapItem extends Item {

    public MapItem() {
        super("Map", new Sprite(30, 4, Color.get(-1, 332, 115, 40)));
    }

    @Override
    public Item clone() {
        return new MapItem();
    }

    @Override
    public boolean interact(Player player, Entity entity, Direction attackDir) {
        if (!Game.isValidServer()) {
            Game.setMenu(new MapDisplay());
        }
        return false;
    }

    @Override
    public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
        return this.interact(player, (Entity)null, attackDir);
    }

    @Override
    public boolean interactsWithWorld() {
        return false;
    }

    @Override
    public boolean canAttack() {
        return false;
    }
}
