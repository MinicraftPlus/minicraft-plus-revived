package minicraft.level.tile;


import minicraft.entity.Entity;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;

// IMPORTANT: This tile should never be used for anything, it only exists to allow tiles right next to the edge of the world to connect to it
public class ConnectTile extends Tile {
    public ConnectTile() {
        super("connector tile", Sprite.missingTexture(SpriteType.Tile));
    }

    @Override
    public boolean mayPass(Level level, int x, int y, Entity e) {
        return false;
    }

    @Override
    public boolean maySpawn() {
        return false;
    }
}
