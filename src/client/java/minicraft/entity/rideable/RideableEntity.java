package minicraft.entity.rideable;

import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;

public class RideableEntity extends Entity {

	public Player ridingPlayer = null;
	public SpriteLinker.LinkedSprite itemSprite;
	public String name;
	public SpriteLinker.LinkedSprite sprites;

	public RideableEntity(int xr, int yr, String name, SpriteLinker.LinkedSprite sprite) {
		super(xr, yr);
		this.name = name;
		itemSprite = sprite;
	}

	@Override
	public void render(Screen screen) { screen.render(x-8, y-8, sprites);}

	@Override
	public void tick() {}

	@Override
	public boolean canSwim() {
		return super.canSwim();
	}

	@Override
	public boolean canBurn() {
		return super.canBurn();
	}
}
