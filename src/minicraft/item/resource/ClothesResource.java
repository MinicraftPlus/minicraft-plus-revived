package minicraft.item.resource;

import minicraft.entity.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ClothesResource extends Resource {

	int red;
	int blue;
	int green;


	public ClothesResource(String name, int sprite, int color, int r, int g, int b) {
		super(name, sprite, color);
		red = r;
		blue = b;
		green = g;
	}
	
	// put on clothes
	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if(player.r == red && player.g == green && player.b == blue) {
			return false;
		} else {
			player.r = red;
			player.b = blue;
			player.g = green;
			return true;
		}
	}
}
