package minicraft.level.tile.wool;

import minicraft.gfx.Sprite;

public class BlueWoolTile extends Wool {
    private static Sprite sprite = new Sprite(8, 2, 2, 2, 1);

    public BlueWoolTile(String name) {
        super(name, sprite, "Blue Wool");
    }
}
