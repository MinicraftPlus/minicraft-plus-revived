package minicraft.entity.rideable;

import minicraft.core.Game;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;

public class RideableEntity extends Entity {

	public Player ridingPlayer = null;
	public SpriteLinker.LinkedSprite itemSprite;
	public String name;
	public SpriteLinker.LinkedSprite sprites;
	protected int health;

	public RideableEntity(int xr, int yr, String name, int health, SpriteLinker.LinkedSprite sprite) {
		super(xr, yr);
		this.name = name;
		itemSprite = sprite;
		this.health = health;
	}

	@Override
	public void render(Screen screen) { screen.render(x-8, y-8, sprites);}

	@Override
	public void tick() {}

	@Override
	public boolean canSwim() {
		return true;
	}

	public void hurt(Mob mob, int damage) { // Hurt the mob, when the source is another mob
		if (mob instanceof Player && Game.isMode("minicraft.settings.mode.creative"))
		{
			remove();
		}
		else {
			health -= damage; // Call the method that actually performs damage, and use our provided attackDir
		}
	}
}
