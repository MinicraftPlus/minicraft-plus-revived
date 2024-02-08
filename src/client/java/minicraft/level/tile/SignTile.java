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
import org.tinylog.Logger;

public class SignTile extends Tile {
	private static final SpriteAnimation sprite = new SpriteAnimation(SpriteLinker.SpriteType.Tile, "sign");

	private final Tile onType;

	public static SignTile getSignTile(Tile onTile) {
		int id = onTile.id & 0xFFFF;
		if(id < 16384) id += 16384;
		else Logger.tag("SignTile").info("Tried to place torch on torch or sign tile...");

		if(Tiles.containsTile(id)) {
			return (SignTile)Tiles.get(id);
		} else {
			SignTile tile = new SignTile(onTile);
			Tiles.add(id, tile);
			return tile;
		}
	}

	private SignTile(Tile onType) {
		super("Sign "+ onType.name, sprite);
		this.onType = onType;
		this.connectsToSand = onType.connectsToSand;
		this.connectsToGrass = onType.connectsToGrass;
		this.connectsToFluid = onType.connectsToFluid;
	}

	public void render(Screen screen, Level level, int x, int y) {
		onType.render(screen, level, x, y);
		sprite.render(screen, level, x, y);
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if (item != null) {
			if (item instanceof ToolItem && ((ToolItem) item).type == ToolType.Axe) {
				level.setTile(xt, yt, this.onType);
				SignDisplay.removeSign(level.depth, xt, yt);
				Sound.play("monsterhurt");
				level.dropItem(xt*16+8, yt*16+8, Items.get("Sign"));
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
