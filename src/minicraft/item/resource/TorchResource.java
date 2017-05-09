package minicraft.item.resource;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import minicraft.entity.Player;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.TorchTile;

public class TorchResource extends Resource {
	private List<Byte> sourceTiles;
	private Tile targetTile;

	public TorchResource(String name, int sprite, int color, Tile targetTile, Tile... sourceTiles1) {
		this(name, sprite, color, targetTile, Arrays.asList(sourceTiles1));
	}

	public TorchResource(String name, int sprite, int color, Tile targetTile, List<Tile> sourceTiles) {
		super(name, sprite, color);
		this.targetTile = targetTile;
		this.sourceTiles = new ArrayList<Byte>();
		for(Tile t: sourceTiles) {
			this.sourceTiles.add(t.id);
		}
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, int attackDir) {
		if (sourceTiles.contains((byte)tile.id)) {
			level.setTile(xt, yt, TorchTile.getTorchTile(tile), level.getData(xt, yt));
			return true;
		}
		return false;
	}
}
