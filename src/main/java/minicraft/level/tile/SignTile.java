package minicraft.level.tile;

import minicraft.core.Game;
import minicraft.core.io.Sound;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PowerGloveItem;
import minicraft.level.Level;
import minicraft.screen.SignDisplay;

public class SignTile extends Tile {
	private static Sprite sprite = new Sprite(12, 3, 0);

	private Tile onType;
	private SignDisplay signDisplay;
	private boolean steppedOn = false;

	public static SignTile getSignTile(Tile onTile, SignDisplay signDisplay) {
		int id = onTile.id & 0xFFFF;
		if(id < 16384) id += 16384;
		else System.out.println("Tried to place torch on torch tile...");

		if(Tiles.containsTile(id)) {
			SignTile sign = (SignTile)Tiles.get(id);
			sign.signDisplay = signDisplay;
			return sign;
		} else {
			SignTile tile = new SignTile(onTile);
			tile.signDisplay = signDisplay;
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
		sprite.render(screen, x * 16 + 4, y * 16 + 4);
		if (steppedOn) {
			signDisplay.render(screen);
		}
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, Direction attackDir) {
		if(item instanceof PowerGloveItem) {
			level.setTile(xt, yt, this.onType);
			Sound.monsterHurt.play();
			level.dropItem(xt*16+8, yt*16+8, Items.get("Torch"));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void steppedOn(Level level, int xt, int yt, Entity entity) {
		steppedOn = true;
	}

	@Override
	public boolean tick(Level level, int xt, int yt) {
		if (Game.player.x / 16 != xt || Game.player.y / 16 != yt) {
			steppedOn = false;
		}

		return super.tick(level, xt, yt);
	}
}
