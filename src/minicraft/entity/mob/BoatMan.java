package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.gfx.Font;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

import java.util.List;

// TODO: Implement a quest structure so the player is assigned the boat recipe if they deliver the hat.
//       On death the player score can be decremented. (And incremented on quest completion).

public class BoatMan extends Mob {
    private static final MobSprite[][] sprites = new MobSprite[1][1];

    static {
        sprites[0][0] = new MobSprite( 2 * 12, 0, 2, 2, 0);
    }

    // TODO: Move these to localization.
    private final String talkMessage = "I seem to be missing my hat";
    private final String shoutMessage = "Ahoy mate!";

    private final int talkWidth = Font.textWidth(talkMessage) / 2;
    private final int shoutWidth = Font.textWidth(shoutMessage) / 2;

    private final int talkRange = (int)Math.pow(2 * 16, 2);
    private final int shoutRange = (int)Math.pow(5 * 16, 2);

    private boolean isTalking = false;
    private boolean isShouting = false;
    private boolean playerInRange = false;

    public BoatMan() {
        super(BoatMan.sprites, 10);
    }

    public BoatMan(MobSprite[][] sprites, int health) {
        super(BoatMan.sprites, 10);
    }

    @Override
    public void tick() {
        super.tick();

        isTalking = false;
        isShouting = false;
        playerInRange = false;

        Player[] players = level.getPlayers();
        for (Player player : players) {
            int xdiff = player.x - x;
            int ydiff = player.y - y;
            int dist = xdiff * xdiff + ydiff * ydiff;

            if (dist <= talkRange) {
                playerInRange = true;
                isTalking = true;
                break;
            } else if (dist <= shoutRange) {
                playerInRange = true;
                isShouting = true;
                break;
            }
        }
    }

    @Override
    public void render(Screen screen) {
        sprites[0][0].render(screen, x - 8, y - 11);

        if (playerInRange) {
            if (isTalking) {
                Font.drawCompleteBackground(talkMessage, screen, x - talkWidth, y - 11 - Font.textHeight());
            } else if (isShouting) {
                Font.drawCompleteBackground(shoutMessage, screen, x - shoutWidth, y - 11 - Font.textHeight());
            }
        }
    }

    @Override
    public void die() {
        Game.unlockableRecipes.getRecipe("Boat").unlock();
        super.die();
    }

    public void setStartingPosition() {
        Point startPos = new Point(25, 25);

        List<Point> tiles = level.getMatchingTiles(Tiles.get("Sand"));
        tiles.addAll(level.getMatchingTiles(Tiles.get("Grass")));

        // Find a sand tile that is next to water.
        for (Point p : tiles) {
            Tile t = level.getTile(p.x - 1, p.y);
            if (t.id == 6) {
                startPos = p;
                break;
            }
            t = level.getTile(p.x + 1, p.y);
            if (t.id == 6) {
                startPos = p;
                break;
            }
            t = level.getTile(p.x, p.y - 1);
            if (t.id == 6) {
                startPos = p;
                break;
            }
            t = level.getTile(p.x, p.y + 1);
            if (t.id == 6) {
                startPos = p;
                break;
            }
        }

        //x = startPos.x;
        //y = startPos.y;
        System.out.println("Found starting point: " + startPos.x + ", " + startPos.y);

        System.out.println(Game.isValidServer());
        System.out.println(Game.hasConnectedClients());
        System.out.println(Game.isValidClient());
        System.out.println(Game.isConnectedClient());
        System.out.println(Game.server);
        System.out.println(Game.client);

    }
}
