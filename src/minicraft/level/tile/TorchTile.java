package minicraft.level.tile;

import minicraft.entity.ItemEntity;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Item;
import minicraft.item.PowerGloveItem;
import minicraft.item.StackableItem;
import minicraft.item.Items;
import minicraft.level.Level;

public class TorchTile extends Tile {
	
	private Tile onType;
	
	public static final TorchTile getTorchTile(Tile onTile) {
		int id = onTile.id;
		if(id < 128) id += 128;
		else System.out.println("tried to place torch on torch tile...");
		if(Tile.tiles[id] != null)
			return (TorchTile)Tile.tiles[id];
		else {
			return new TorchTile(onTile);
		}
	}
	
	private TorchTile(Tile onType) {
		super(onType.id+128);
		this.onType = onType;
		this.connectsToSand = onType.connectsToSand;
		this.connectsToGrass = onType.connectsToGrass;
		this.connectsToWater = onType.connectsToWater;
		this.connectsToLava = onType.connectsToLava;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		int col = Color.get(320, 500, 520, -1);
		
		onType.render(screen, level, x, y);
		Items.get("Torch").sprite.render(screen, x * 16 + 4, y * 16 + 4);
	}
	
	public int getLightRadius(Level level, int x, int y) {
		return 6;//level.player.potioneffects.containsKey("Light")?9:6; // this is accounted for elsewhere.
	}
	
	public boolean canLight() {
		return true;
	}

	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if(item instanceof PowerGloveItem) {
			level.setTile(xt, yt, this.onType, 0);
			level.dropItem(xt * 16, yt * 16, Items.get("Torch"));
			return true;
		} else {
			return false;
		}
	}
}
