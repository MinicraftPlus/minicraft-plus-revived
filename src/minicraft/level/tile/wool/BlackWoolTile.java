package minicraft.level.tile.wool;

import minicraft.gfx.Sprite;

public class BlackWoolTile extends Wool {
    private static Sprite sprite = new Sprite(10, 4, 2, 2, 1);

    public BlackWoolTile(String name) {
        super(name, sprite, "Black Wool");
    }
}
