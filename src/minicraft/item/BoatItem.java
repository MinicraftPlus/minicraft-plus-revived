package minicraft.item;

import minicraft.core.Game;
import minicraft.entity.Boat;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class BoatItem extends Item {
    private static final Sprite boatSprite = new Sprite(0, 25, 0);

    private boolean placed = false;

    public BoatItem(String name) {
        super(name, boatSprite);
    }

    public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
        if (level.getTile(xt, yt).id == 6) {
            Boat boat = new Boat();

            boat.x = xt * 16 + 8;
            boat.y = yt * 16 + 8;
            level.add(boat);
            if(Game.isMode("creative"))
                boat = boat.clone();
            else
                placed = true;

            return true;
        }
        return false;
    }

    public boolean isDepleted() {
        return placed;
    }

    @Override
    public Item clone() {
        return new BoatItem("Boat");
    }
}
