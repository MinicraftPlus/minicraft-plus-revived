package minicraft.entity.vehicle;

import minicraft.core.Updater;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.PlayerRideable;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.level.tile.Tiles;
import minicraft.level.tile.WaterTile;
import minicraft.util.Vector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Boat extends Entity implements PlayerRideable {
	private static final int MOVE_SPEED = 1;

	private @Nullable Entity passenger;

	private @NotNull Direction dir;

	public Boat(@NotNull Direction dir) {
		super(5, 5);
		this.dir = dir;
	}

	@Override
	public void render(Screen screen) {
		Font.draw("BOAT", screen, x - 2 * 8, y - 4);
	}

	@Override
	public void tick() {
		if (isRemoved()) return;
		if (level != null && level.getTile(x >> 4, y >> 4) == Tiles.get("lava"))
			hurt();
		// Moves the furniture in the correct direction.
		move(pushDir.getX(), pushDir.getY());
		pushDir = Direction.NONE;

		if (pushTime > 0) pushTime--; // Update pushTime by subtracting 1.
		else multiPushTime = 0;
	}

	public void hurt() {
		if (isRemoved())
			return;
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

	@Override
	protected int getPushTimeDelay() {
		return isInWater() ? 2 : 6;
	}

	@Override
	protected void touchedBy(Entity entity) {
		if (entity instanceof Player)
			tryPush((Player) entity);
	}

	private void syncPassengerState(Entity passenger) {
		passenger.x = x;
		passenger.y = y;
		if (passenger instanceof Mob)
			((Mob) passenger).dir = dir;
	}

	@Override
	public boolean canSwim() {
		return true;
	}

	@Override
	public boolean rideTick(Player passenger, Vector2 vec) {
		if (this.passenger != passenger) return false;
		if (!isInWater() && Updater.tickCount % 4 != 0) return true; // Slower when not in water.
		int xd = (int) (vec.x * MOVE_SPEED);
		int yd = (int) (vec.y * MOVE_SPEED);
		dir = Direction.getDirection(xd, yd);
		if (move(xd, yd)) {
			syncPassengerState(passenger);
		}

		return true;
	}

	@Override
	public boolean startRiding(Player player) {
		if (passenger == null) {
			passenger = player;
			syncPassengerState(passenger);
			return true;
		} else
			return false;
	}

	@Override
	public void stopRiding(Player player) {
		if (passenger == player)
			passenger = null;
	}
}
