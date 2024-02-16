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

import java.util.HashMap;

public class TorchTile extends Tile {
	public static final TorchTile DELEGATE = new TorchTile(new ConnectTile()); // ConnectTile is used for placeholder.

	private static final HashMap<Integer, TorchTile> instances = new HashMap<>();

	private final Tile onType;

	/** @param onTile The tile identified by tile data. */
	public static TorchTile getTorchTile(Tile onTile) {
		if (onTile instanceof TorchTile) return (TorchTile) onTile;
		int id = onTile.id & 0xFFFF;
		if (instances.containsKey(id)) return instances.get(id);
		TorchTile tile = new TorchTile(onTile);
		instances.put(id, tile);
		return tile;
	}

	private TorchTile(Tile onType) {
		super("Torch", new SpriteAnimation(SpriteType.Tile, "torch"));
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

	public short getOnType() {
		return onType.id;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item instanceof PowerGloveItem) {
			int data = level.getData(xt, yt);
			level.setTile(xt, yt, onType);
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
