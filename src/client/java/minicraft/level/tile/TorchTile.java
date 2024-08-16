package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.level.Level;
import org.jetbrains.annotations.Nullable;

public class TorchTile extends Tile {
	protected TorchTile() {
		super("Torch", new SpriteAnimation(SpriteType.Tile, "torch"));
	}

	@Override
	public boolean connectsToSand(Level level, int x, int y) {
		return Tiles.get((short) level.getData(x, y)).connectsToSand(level, x, y);
	}

	@Override
	public boolean connectsToFluid(Level level, int x, int y) {
		return Tiles.get((short) level.getData(x, y)).connectsToFluid(level, x, y);
	}

	@Override
	public boolean connectsToGrass(Level level, int x, int y) {
		return Tiles.get((short) level.getData(x, y)).connectsToGrass(level, x, y);
	}

	public void render(Screen screen, Level level, int x, int y) {
		Tiles.get((short) level.getData(x, y)).render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public int getLightRadius(Level level, int x, int y) {
		return 5;
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {
		level.add(new SmashParticle(x << 4, y << 4));
		level.add(new TextParticle("" + dmg, (x << 4) + 8, (y << 4) + 8, Color.RED));
	}

	@Override
	public @Nullable Item take(Level level, int x, int y, Player player) {
		int data = level.getData(x, y);
		level.setTile(x, y, Tiles.get((short) data));
		Sound.play("monsterhurt");
		return Items.get("Torch");
	}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		handleDamage(level, x, y, source, item, 0);
		return true;
	}
}
