package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

import java.util.ArrayList;
import java.util.Random;

public class FishingRodItem extends Item {

    protected static ArrayList<Item> getAllInstances() {
        ArrayList<Item> items = new ArrayList<>();

        items.add(new FishingRodItem());

        return items;
    }
    private int uses = 0; // the more uses, the higher the chance of breaking

    private Random random = new Random(System.nanoTime());

    public FishingRodItem() {
        super("Fishing Rod", new Sprite(6, 5, Color.get(-1, 210, 321, 555)));
    }

    @Override
    public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
        if (tile.equals(Tiles.get("water"))) {
            uses++;
            player.isFishing = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean canAttack() { return false; }

    @Override
    public boolean isDepleted() {
        if (random.nextInt(100) > 120 - uses) { // breaking is random
            Game.notifications.add("Your Fishing rod broke.");
            return true;
        }
        return false;
    }

    @Override
    public Item clone() {
        FishingRodItem item = new FishingRodItem();
        item.uses = this.uses;
        return item;
    }
}
