package minicraft.entity.vehicle;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.PlayerRideable;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Items;
import minicraft.level.tile.LavaTile;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.WaterTile;
import minicraft.util.Vector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Boat extends Entity implements PlayerRideable {
	private static final SpriteLinker.LinkedSprite[][] boatSprites = new SpriteLinker.LinkedSprite[][] {
		Mob.compileSpriteList(0, 0, 3, 3, 0, 4, "boat"), //
		Mob.compileSpriteList(0, 3, 3, 3, 0, 4, "boat")
	};

	private static final int MOVE_SPEED = 1;
	private static final int STAMINA_COST_TIME = 45; // a unit of stamina is consumed per this amount of time of move

	private @Nullable Entity passenger;
	private @NotNull Direction dir;

	private int walkDist = 0;
	private int unitMoveCounter = 0;

	public Boat(@NotNull Direction dir) {
		super(6, 6);
		this.dir = dir;
	}

	@Override
	public void render(Screen screen) {
		int xo = x - 8; // Horizontal
		int yo = y - 8; // Vertical

		switch (dir) {
			case UP: // if currently riding upwards...
				screen.render(xo - 4, yo - 4, boatSprites[0][((walkDist >> 3) & 1) + 2].getSprite());
				break;
			case LEFT: // Riding to the left... (Same as above)
				screen.render(xo - 4, yo - 4, boatSprites[1][((walkDist >> 3) & 1)].getSprite());
				break;
			case RIGHT: // Riding to the right (Same as above)
				screen.render(xo - 4, yo - 4, boatSprites[1][((walkDist >> 3) & 1) + 2].getSprite());
				break;
			case DOWN: // Riding downwards (Same as above)
				screen.render(xo - 4, yo - 4, boatSprites[0][((walkDist >> 3) & 1)].getSprite());
				break;
			default:
				throw new UnsupportedOperationException("dir must be defined when on world");
		}

		if (passenger != null)
			passenger.render(screen);
	}

	@Override
	public void tick() {
		if (isRemoved()) return;
		if (level != null && level.getTile(x >> 4, y >> 4) == Tiles.get("lava")) {
			hurt();
			if (isRemoved()) return;
		}

		// Moves the furniture in the correct direction.
		move(pushDir.getX(), pushDir.getY());
		pushDir = Direction.NONE;

		if (pushTime > 0) pushTime--; // Update pushTime by subtracting 1.
		else multiPushTime = 0;
	}

	public void hurt() {
		if (isRemoved())
			return;
		stopRiding((Player)passenger);
		die();
	}

	@Override
	public void die() {
		level.dropItem(x, y, Items.get("Boat"));
		Sound.play("monsterhurt");
		super.die();
	}

	public boolean isInWater() {
		return level.getTile(x >> 4, y >> 4) instanceof WaterTile;
	}

	public boolean isInLava() {
		return level.getTile(x >> 4, y >> 4) instanceof LavaTile;
	}

	@Override
	protected int getPushTimeDelay() {
		return isInWater() ? 2 : isInLava() ? 4 : 6;
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player)
			tryPush((Player) entity);
		if (entity instanceof Boat && ((Boat) entity).passenger instanceof Player)
			tryPush((Player) ((Boat) entity).passenger);
	}

	private void syncPassengerState(Entity passenger) {
		passenger.x = x;
		passenger.y = y;
		if (passenger instanceof Mob) {
			((Mob) passenger).dir = dir;
			((Mob) passenger).walkDist = walkDist;
		}
	}

	@Override
	public boolean canSwim() {
		return true;
	}

	@Override
	public boolean rideTick(Player passenger, Vector2 vec) {
		if (this.passenger != passenger) return false;

		if (unitMoveCounter >= STAMINA_COST_TIME) {
			passenger.payStamina(1);
			unitMoveCounter -= STAMINA_COST_TIME;
		}

		boolean inLava = isInLava(), inWater = isInWater();
		if (inLava) {
			if (Updater.tickCount % 2 != 0) return true; // A bit slower when in lava.
		} else if (!inWater && Updater.tickCount % 4 != 0) return true; // Slower when not in water.
		int xd = (int) (vec.x * MOVE_SPEED);
		int yd = (int) (vec.y * MOVE_SPEED);
		dir = Direction.getDirection(xd, yd);
		if (passenger.stamina > 0 && move(xd, yd)) {
			if (!(inWater || inLava) || (inLava && Updater.tickCount % 4 == 0) ||
				(inWater && Updater.tickCount % 2 != 0)) walkDist++; // Slower the animation
			syncPassengerState(passenger);
			unitMoveCounter++;
		} else {
			if (passenger.dir != this.dir) // Sync direction even not moved to render in consistent state
				syncPassengerState(passenger);
			if (unitMoveCounter > 0) // Simulation of resting
				unitMoveCounter--;
		}
		return true;
	}

	public @NotNull Direction getDir() {
		return dir;
	}

	public boolean isMoving() {
		return unitMoveCounter > 0;
	}

	@Override
	public boolean startRiding(Player player) {
		if (passenger == null) {
			passenger = player;
			unitMoveCounter = 0;
			syncPassengerState(passenger);
			return true;
		} else
			return false;
	}

	@Override
	public void stopRiding(Player player) {
		if (passenger == player) {
			passenger = null;
			unitMoveCounter = 0; // reset counters
		}
	}
}
