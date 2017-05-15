package minicraft.item;

import java.util.ArrayList;
import java.util.HashMap;
import minicraft.entity.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.screen.ModeMenu;

public class BucketItem extends StackableItem {
	
	public static enum Fill {
		Empty (null, 333),
		Water (Tile.water, 005),
		Lava (Tile.lava, 400);
		
		public Tile contained;
		public int innerColor; // TODO make it so that the inside color is fetched from the tile color.
		
		private Fill(Tile contained, int innerCol) {
			this.contained = contained;
			innerColor = innerCol;
		}
	}
	
	protected static ArrayList<Item> getAllInstances() {
		ArrayList<Item> items = new ArrayList<Item>();
		
		for(Fill fill: Fill.values())
			items.add(new BucketItem(fill));
		
		return items;
	}
	
	private static final Fill getFilling(Tile tile) {
		if(tile == null) return Fill.Empty;
		for(Fill fill: Fill.values())
			if(fill.contained.id == tile.id)
				return fill;
		
		return null;
	}
	
	public Fill filling;
	
	private BucketItem(Fill fill) { this(fill, 1); }
	private BucketItem(Fill fill, int count) {
		super(fill.name() + " Bucket", new Sprite(21, 4, Color.get(-1, 222, fill.innerColor, 555)), count);
		this.filling = fill;
	}
	/*
	public int getSprite() {
		return 21 + 4 * 32;
	}
	
	public void renderIcon(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
	}
	
	public void renderInventory(Screen screen, int x, int y) {
		screen.render(x, y, getSprite(), getColor(), 0);
		Font.draw(name, screen, x + 8, y, Color.get(-1, 555));
	}
	
	public String getName() {
		if(contained == null) return "Bucket";
		
		String className = contained.getClass().getName();
		className = className.substring(className.lastIndexOf("."), className.lastIndexOf("Tile"));
		return className + " Bucket";
	}
	*/
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if(filling.contained == null) {
			Fill fill = getFilling(tile);
			if(fill != null && fill != Fill.Empty) {
				level.setTile(xt, yt, Tile.hole, 0);
				if(!ModeMenu.creative) player.activeItem = new BucketItem(fill);
				return true;
			}
		}
		else if(tile.matches(Tile.hole)) {
			level.setTile(xt, yt, filling.contained, 0);
			if(!ModeMenu.creative) player.activeItem = new BucketItem(Fill.Empty);
			return true;
		}
		
		/*
		if (tile == Tile.water) {
			level.setTile(xt, yt, Tile.hole, 0);
			item = (new BucketItem());
		}
		if (tile == Tile.lava) {
			level.setTile(xt, yt, Tile.hole, 0);
			item = (new BucketItem());
		}*/
		
		//if(ModeMenu.creative) item = this;
		//player.activeItem = item;
		
		return false;
	}
	
	public boolean matches(Item other) {
		return super.matches(other) && filling == ((BucketItem)other).filling;
	}
	
	public BucketItem clone() {
		//System.out.println("fill " + fill);
		return new BucketItem(filling, count);
	}
}
