package minicraft.item;

import minicraft.gfx.Sprite;

public class BoatItem extends Item {
    private static final Sprite boatSprite = new Sprite(0, 25, 0);

    protected BoatItem(String name) {
        super(name, boatSprite);
    }

    @Override
    public Item clone() {
        return new BoatItem("Boat");
    }
}
