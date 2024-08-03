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

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item != null) {
			if (item instanceof ToolItem && ((ToolItem) item).type == ToolType.Axe) {
				int data = level.getData(xt, yt);
				level.setTile(xt, yt, Tiles.get((short) data));
				SignDisplay.removeSign(level.depth, xt, yt);
				Sound.play("monsterhurt");
				level.dropItem(xt*16+8, yt*16+8, Items.get("Sign"));
				AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
					new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
						item, this, data, xt, yt, level.depth));
				return true;
			}
		} else { // TODO Add a way to lock signs
			Game.setDisplay(new SignDisplay(level, xt, yt));
			return true;
		}

		return false;
	}

	@Override
	public boolean hurt(Level level, int x, int y, Mob source, int dmg, Direction attackDir) {
		if (source instanceof Player) {
			Game.setDisplay(new SignDisplay(level, x, y));
			return true;
		}

		return false;
	}

	@Override
	public void onTileSet(Level level, int x, int y) {
		level.add(new SignTileEntity(), x, y, true);
	}
}
