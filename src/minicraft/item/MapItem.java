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

import java.util.ArrayList;

public class MapItem extends Item {

    protected static ArrayList<Item> getAllInstances() {
        ArrayList<Item> items = new ArrayList<>();

        items.add(new MapItem("Map", false));
        items.add(new MapItem("Ore Map", true));

        return items;
    }

    public MapItem(String name, boolean showOre) {
        super(name, new Sprite(30, 4, Color.get(-1, 443, 115, 40)));
        this.showOre = showOre;
    }

    private boolean showOre;

    @Override
    public Item clone() {
        return new MapItem(super.getName(), showOre);
    }

    @Override
    public boolean interact(Player player, Entity entity, Direction attackDir) {
        if (!Game.isValidServer()) {
            Game.setMenu(new MapDisplay(showOre));
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
