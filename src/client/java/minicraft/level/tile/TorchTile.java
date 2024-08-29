package minicraft.level.tile;

import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteAnimation;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PowerGloveItem;
import minicraft.level.Level;
import minicraft.util.AdvancementElement;

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

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof PowerGloveItem) {
			int data = level.getData(xt, yt);
			level.setTile(xt, yt, Tiles.get((short) data));
			Sound.play("monsterhurt");
			level.dropItem((xt << 4) + 8, (yt << 4) + 8, Items.get("Torch"));
			AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
				new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
					item, this, data, xt, yt, level.depth));
			return true;
		} else {
			return false;
		}
	}
}
