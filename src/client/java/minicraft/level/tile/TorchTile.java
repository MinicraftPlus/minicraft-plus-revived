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
import org.tinylog.Logger;

public class TorchTile extends Tile {
	private Tile onType;

	public static TorchTile getTorchTile(Tile onTile) {
		int id = onTile.id & 0xFFFF;
		if (id < 16384) id += 16384;
		else Logger.tag("TorchTile").info("Tried to place torch on torch or sign tile...");

		if (Tiles.containsTile(id))
			return (TorchTile) Tiles.get(id);
		else {
			TorchTile tile = new TorchTile(onTile);
			Tiles.add(id, tile);
			return tile;
		}
	}

	private TorchTile(Tile onType) {
		super("Torch " + onType.name, new SpriteAnimation(SpriteType.Tile, "torch"));
		this.onType = onType;
	}

	@Override
	public boolean connectsToSand(Level level, int x, int y) {
		return onType.connectsToSand(level, x, y);
	}

	@Override
	public boolean connectsToFluid(Level level, int x, int y) {
		return onType.connectsToFluid(level, x, y);
	}

	@Override
	public boolean connectsToGrass(Level level, int x, int y) {
		return onType.connectsToGrass(level, x, y);
	}

	public void render(Screen screen, Level level, int x, int y) {
		onType.render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public int getLightRadius(Level level, int x, int y) {
		return 5;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof PowerGloveItem) {
			int data = level.getData(xt, yt);
			level.setTile(xt, yt, this.onType);
			Sound.play("monsterhurt");
			level.dropItem(xt * 16 + 8, yt * 16 + 8, Items.get("Torch"));
			AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.INSTANCE.trigger(
				new AdvancementElement.AdvancementTrigger.ItemUsedOnTileTrigger.ItemUsedOnTileTriggerConditionHandler.ItemUsedOnTileTriggerConditions(
					item, this, data, xt, yt, level.depth));
			return true;
		} else {
			return false;
		}
	}
}
