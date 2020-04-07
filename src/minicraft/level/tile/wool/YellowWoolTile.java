package minicraft.level.tile.wool;

import minicraft.gfx.Sprite;

public class YellowWoolTile extends Wool {
    private static Sprite sprite = new Sprite(8, 4, 2, 2, 1);

    public YellowWoolTile(String name) {
        super(name, sprite, "Yellow Wool");
    }
}
