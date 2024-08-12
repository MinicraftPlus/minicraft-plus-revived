package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Mob;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.entity.SignTileEntity;
import minicraft.screen.SignDisplay;
import minicraft.screen.SignDisplayMenu;
import minicraft.util.AdvancementElement;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

public class SignTile extends Tile {
	protected SignTile() {
		super("Sign", new SpriteAnimation(SpriteLinker.SpriteType.Tile, "sign"));
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

	@Override
	public boolean use(Level level, int xt, int yt, Player player, @Nullable Item item, Direction attackDir) {
		Game.setDisplay(new SignDisplay(level, xt, yt));
		return true; // TODO Add a way to lock signs
	}

	@Override
	protected void handleDamage(Level level, int x, int y, Entity source, @Nullable Item item, int dmg) {}

	@Override
	public boolean hurt(Level level, int x, int y, Entity source, @Nullable Item item, Direction attackDir, int damage) {
		if (item instanceof ToolItem && ((ToolItem) item).type == ToolType.Axe) {
			int data = level.getData(x, y);
			level.setTile(x, y, Tiles.get((short) data));
			SignDisplay.removeSign(level.depth, x, y);
			Sound.play("monsterhurt");
			level.dropItem((x << 4) + 8, (y << 4) + 8, Items.get("Sign"));
			AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
				new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
					item, this, data, x, y, level.depth));
			return true;
		}

		return false;
	}

	@Override
	public void onTileSet(Level level, int x, int y) {
		level.add(new SignTileEntity(), x, y, true);
	}
}
