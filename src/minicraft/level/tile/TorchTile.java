package minicraft.level.tile;

import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PowerGloveItem;
import minicraft.level.Level;

public class TorchTile extends Tile {
	private static Sprite sprite = new Sprite(12, 3, Color.get(320, 500, 520, -1));
	
	private Tile onType;
	
	public static final TorchTile getTorchTile(Tile onTile) {
		int id = onTile.id;
		if(id < 128) id += 128;
		else System.out.println("tried to place torch on torch tile...");
		if(Tiles.get(id) != null)
			return (TorchTile)Tiles.get(id);
		else {
			TorchTile tile = new TorchTile(onTile);
			
			
			return tile;
		}
	}
	
	private TorchTile(Tile onType) {
		super("Torch "+ onType.name, sprite);
		this.onType = onType;
		//csprite = onType.csprite;
		this.connectsToSand = onType.connectsToSand;
		this.connectsToGrass = onType.connectsToGrass;
		this.connectsToWater = onType.connectsToWater;
		this.connectsToLava = onType.connectsToLava;
	}
	
	public void render(Screen screen, Level level, int x, int y) {
		//int col = Color.get(320, 500, 520, -1);
		
		onType.render(screen, level, x, y);
		//Items.get("Torch").sprite.render(screen, x * 16 + 4, y * 16 + 4);
		sprite.render(screen, x*16 + 4, y*16 + 4);
	}
	
	public int getLightRadius(Level level, int x, int y) {
		return 6;//level.player.potioneffects.containsKey("Light")?9:6; // this is accounted for elsewhere.
	}
	/*
	public boolean canLight() {
		return true;
	}
	*/
	public boolean interact(Level level, int xt, int yt, Player player, Item item, int attackDir) {
		if(item instanceof PowerGloveItem) {
			level.setTile(xt, yt, this.onType);
			level.dropItem(xt * 16, yt * 16, Items.get("Torch"));
			return true;
		} else {
			return false;
		}
	}
}
