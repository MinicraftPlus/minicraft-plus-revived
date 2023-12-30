package minicraft.entity.rideable;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PowerGloveItem;
import minicraft.item.ToolItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Boat extends RideableEntity {

	private static final SpriteLinker.LinkedSprite[][] boatSprites = new SpriteLinker.LinkedSprite[][] {
		Mob.compileSpriteList(0, 0, 3,3,0,4,"boat"), //
		Mob.compileSpriteList(0,3,3,3,0,4, "boat")
	};

	private static final SpriteLinker.LinkedSprite itemSprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "boat");
    public Player playerInBoat = null;

    private int exitTimer = 0;
    private boolean boatAnim = false;
    protected int pushTime = 0;

    private int tickTime =  0;

    private Direction pushDir = Direction.NONE; // the direction to push the furniture

    public Boat() {
        super(3, 3, "Boat", itemSprite);
    }

    public Boat(int xr, int yr) {
        super(xr, yr, "Boat", itemSprite);
    }

    @Override
    public void render(Screen screen) {
        int xo = x-8; // Horizontal
        int yo = y-8; // Vertical

        if (Game.player.equals(playerInBoat)) {
        	switch (Game.player.dir) {
				case UP: // if currently attacking upwards...
					screen.render(xo - 4, yo - 4, boatSprites[0][((playerInBoat.walkDist >> 3) & 1) + 2].getSprite());
                    playerInBoat.render(screen);
                    break;
                case LEFT: // Attacking to the left... (Same as above)
						screen.render(xo - 4, yo - 4, boatSprites[1][((playerInBoat.walkDist >> 3) & 1)].getSprite());
					playerInBoat.render(screen);
					break;

				case RIGHT: // Attacking to the right (Same as above)
							screen.render(xo - 4, yo - 4, boatSprites[1][((playerInBoat.walkDist >> 3) & 1) + 2].getSprite());
					playerInBoat.render(screen);
					break;

				case DOWN: // Attacking downwards (Same as above)
					screen.render(xo - 4, yo - 4, boatSprites[0][((playerInBoat.walkDist >> 3) & 1)].getSprite());
					playerInBoat.render(screen);
					break;

				case NONE:
					break;
			}
        } else {
			screen.render(xo - 4, yo - 4, boatSprites[0][0]);

		}
    }


    @Override
    public void tick() {
    	tickTime++;

        // moves the furniture in the correct direction.
        move(pushDir.getX(), pushDir.getY());
        pushDir = Direction.NONE;

        if (pushTime > 0) {
            pushTime--; // update pushTime by subtracting 1.
        }

        if (playerInBoat != null) {
            exitTimer--;

            if (exitTimer <= 0 && Game.input.getKey("ATTACK").down) {
                restorePlayer(playerInBoat);
				return;
            }

            double ya = 0;
            double xa = 0;

            if (Game.input.getKey("MOVE-UP").down) ya -= 1;
            if (Game.input.getKey("MOVE-DOWN").down) ya += 1;
            if (Game.input.getKey("MOVE-LEFT").down) xa -= 1;
			if (Game.input.getKey("MOVE-RIGHT").down) xa += 1;

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

    @Override
    public boolean blocks(Entity entity) {
        return true;
    }

    @Override
    public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (player.activeItem instanceof ToolItem && playerInBoat == null) {
			level.dropItem(x, y, Items.get("Boat"));
			remove();
		} else if (playerInBoat == null) {
			player.isRiding = true;
			playerInBoat = player;
			exitTimer = 10;
			playerInBoat.moveSpeed = 1.5;
			return true;
		}

		//Put whatever item the player is holding into their inventory
		if (player.activeItem != null && !(player.activeItem instanceof PowerGloveItem)) {
			player.getInventory().add(0, player.activeItem);
		}
		//make this the player's current item.
		return true;
	}

	public boolean move(double xa, double ya) {
        if (Updater.saving || (xa == 0 && ya == 0)) {
            return true; // pretend that it kept moving
        }

        // used to check if the entity has BEEN stopped, COMPLETELY; below checks for a lack of collision.
        boolean stopped = true;

        if (move2(xa, 0)) stopped = false; // becomes false if horizontal movement was successful.
        if (move2(0, ya)) stopped = false; // becomes false if vertical movement was successful.

        if (!stopped) {
            int xt = x >> 4; // the x tile coordinate that the entity is standing on.
            int yt = y >> 4; // the y tile coordinate that the entity is standing on.

            // Calls the steppedOn() method in a tile's class. (used for tiles like sand (footprints) or lava (burning))
            level.getTile(xt, yt).steppedOn(level, xt, yt, this);
        }

        return !stopped;
    }

    public boolean move2(double xa, double ya) {
        if (xa == 0 && ya == 0) {
            return true; // was not stopped
        }

        boolean interact = true;

        // gets the tile coordinate of each direction from the sprite...
        int xto0 = ((x) - 1) >> 4; // to the left
        int yto0 = ((y) - 1) >> 4; // above
        int xto1 = ((x) + 1) >> 4; // to the right
        int yto1 = ((y) + 1) >> 4; // below

        // gets same as above, but after movement.
        int xt0 = (int) ((x + xa) - 1) >> 4;
        int yt0 = (int) ((y + ya) - 1) >> 4;
        int xt1 = (int) ((x + xa) + 1) >> 4;
        int yt1 = (int) ((y + ya) + 1) >> 4;

        // boolean blocked = false; // if the next tile can block you.
        for (int yt = yt0; yt <= yt1; yt++) { // cycles through y's of tile after movement
            for (int xt = xt0; xt <= xt1; xt++) { // cycles through x's of tile after movement
                if (xt >= xto0 && xt <= xto1 && yt >= yto0 && yt <= yto1) {
                    continue; // skip this position if this entity's sprite is touching it
                }

                if (level.getTile(xt, yt).id != 6) {
                    return false;
                }
            }
        }

        // these lists are named as if the entity has already moved-- it hasn't, though.
        // gets all of the entities that are inside this entity (aka: colliding) before moving.
        List<Entity> wasInside = level.getEntitiesInRect(getBounds());

        int xr = 1;
        int yr = 1;

        // gets the entities that this entity will touch once moved.
        List<Entity> isInside = level.getEntitiesInRect(new Rectangle(x + (int) xa, y + (int) ya, xr * 2, yr * 2, Rectangle.CENTER_DIMS));
        for (int i = 0; interact && i < isInside.size(); i++) {
            /// cycles through entities about to be touched, and calls touchedBy(this) for each of them.
            Entity entity = isInside.get(i);
            if (entity == this) {
                continue; // touching yourself doesn't count XD
            }

            if (entity instanceof Player) {
                touchedBy(entity);
            } else {
                touchedBy(this); // call the method. ("touch" the entity)
            }
        }

        isInside.removeAll(wasInside); // remove all the entities that this one is already touching before moving.
        for (Entity entity : isInside) {
            if (entity == this) {
                continue; // can't interact with yourself LOL
            }

            if (entity.blocks(this)) {
                return false; // if the entity prevents this one from movement, don't move.
            }
        }

        // finally, the entity moves!
        x += (int) xa;
        y += (int) ya;

        return true; // the move was successful.
    }

    @Override
    protected void touchedBy(Entity entity) {
        if (level.getTile(this.x, this.y).id != 6) {
            return;
        }

        if (entity instanceof Player) {
            tryPush((Player) entity);
        }
    }

	public void tryPush(Player player) {
        if (level.getTile(this.x, this.y).id != 6) {
            return;
        }
		if (pushTime == 0) {
			pushDir = player.dir; // Set pushDir to the player's dir.
			pushTime = 10; // Set pushTime to 10.
		}
	}

	/**
	 * "Restores" the player to the world, in other words places player out of the boat
	 *
	 * @param player The player in the boat
	 */
	public void restorePlayer(Player player) {
		if (playerInBoat != null) {
			if (playerInBoat.getLevel() == null) {
				Game.levels[Game.currentLevel].add(player);
				player.isRiding = false;
				playerInBoat = null;
			} else {
				playerInBoat.getLevel().add(player);
				player.isRiding = false;
				playerInBoat = null;
			}
		}
	}


    @Override
    public Boat clone() {
        return new Boat();
    }
}
