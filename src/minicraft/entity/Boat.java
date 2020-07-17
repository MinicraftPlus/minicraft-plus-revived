package minicraft.entity;

import minicraft.core.Game;
import minicraft.entity.mob.Player;
import minicraft.entity.mob.RemotePlayer;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.BoatItem;
import minicraft.item.PowerGloveItem;

import java.util.List;

public class Boat extends Entity {
    private static final Sprite boatSprite = new Sprite(0, 25, 0);

    private Player playerInBoat = null;

    public Boat() {
        super(1, 1);
    }

    public Boat(int xr, int yr) {
        super(xr, yr);
    }

    @Override
    public void render(Screen screen) {
        boatSprite.render(screen, x - 4, y - 4);
    }

    @Override
    public void tick() {
        if (playerInBoat != null) {
            int ya = 0;
            int xa = 0;

            if (Game.input.getKey("move-up").down) ya--;
            if (Game.input.getKey("move-down").down) ya++;
            if (Game.input.getKey("move-left").down) xa--;
            if (Game.input.getKey("move-right").down) xa++;

            move(xa, ya);
            playerInBoat.x = x;
            playerInBoat.y = y;
            playerInBoat.stamina = Player.maxStamina;
        }
    }

    @Override
    public boolean canSwim() {
        return true;
    }

    public boolean use(Player player) {
        System.out.println("use");

        if (playerInBoat == null) {
            playerInBoat = player;
            return true;
        }

        if (playerInBoat.equals(player)) {
            playerInBoat = null;
            return true;
        }

        return false;
    }

    @Override
    public boolean blocks(Entity e) {
        return true;
    }

    public void take(Player player) {
        remove(); // remove this from the world
        if(!Game.ISONLINE) {
            if (!Game.isMode("creative") && player.activeItem != null && !(player.activeItem instanceof PowerGloveItem))
                player.getInventory().add(0, player.activeItem); // put whatever item the player is holding into their inventory (should never be a power glove, since it is put in a taken out again all in the same frame).
            player.activeItem = new BoatItem("Boat"); // make this the player's current item.
        }
        else if(Game.isValidServer() && player instanceof RemotePlayer)
            Game.server.getAssociatedThread((RemotePlayer)player).updatePlayerActiveItem(new BoatItem("Boat"));
        else
            System.out.println("WARNING: undefined behavior; online game was not server and ticked furniture: "+this+"; and/or player in online game found that isn't a RemotePlayer: " + player);

        //if (Game.debug) System.out.println("set active item of player " + player + " to " + player.activeItem + "; picked up furniture: " + this);
    }

    @Override
    public boolean move2(int xa, int ya) {
        if(xa == 0 && ya == 0) return true; // was not stopped

        boolean interact = true;//!Game.isValidClient() || this instanceof ClientTickable;

        // gets the tile coordinate of each direction from the sprite...
        int xto0 = ((x) - 1) >> 4; // to the left
        int yto0 = ((y) - 1) >> 4; // above
        int xto1 = ((x) + 1) >> 4; // to the right
        int yto1 = ((y) + 1) >> 4; // below

        // gets same as above, but after movement.
        int xt0 = ((x + xa) - 1) >> 4;
        int yt0 = ((y + ya) - 1) >> 4;
        int xt1 = ((x + xa) + 1) >> 4;
        int yt1 = ((y + ya) + 1) >> 4;

        //boolean blocked = false; // if the next tile can block you.
        for (int yt = yt0; yt <= yt1; yt++) { // cycles through y's of tile after movement
            for (int xt = xt0; xt <= xt1; xt++) { // cycles through x's of tile after movement
                if (xt >= xto0 && xt <= xto1 && yt >= yto0 && yt <= yto1) continue; // skip this position if this entity's sprite is touching it
                if (level.getTile(xt, yt).id != 6) return false;
            }
        }

        // these lists are named as if the entity has already moved-- it hasn't, though.
        List<Entity> wasInside = level.getEntitiesInRect(getBounds()); // gets all of the entities that are inside this entity (aka: colliding) before moving.

        int xr = 1, yr = 1;
        List<Entity> isInside = level.getEntitiesInRect(new Rectangle(x+xa, y+ya, xr*2, yr*2, Rectangle.CENTER_DIMS)); // gets the entities that this entity will touch once moved.
        for (int i = 0; interact && i < isInside.size(); i++) {
            /// cycles through entities about to be touched, and calls touchedBy(this) for each of them.
            Entity e = isInside.get(i);
            if (e == this) continue; // touching yourself doesn't count.

            if(e instanceof Player) {
                touchedBy(e);
            }
            else
                e.touchedBy(this); // call the method. ("touch" the entity)
        }

        isInside.removeAll(wasInside); // remove all the entities that this one is already touching before moving.
        for (int i = 0; i < isInside.size(); i++) {
            Entity e = isInside.get(i);

            if (e == this) continue; // can't interact with yourself

            if (e.blocks(this)) return false; // if the entity prevents this one from movement, don't move.
        }

        // finally, the entity moves!
        x += xa;
        y += ya;

        return true; // the move was successful.
    }

    @Override
    public Boat clone() {
        return new Boat();
    }
}
