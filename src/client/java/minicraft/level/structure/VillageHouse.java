package minicraft.level.structure;

import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.furniture.Chest;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.level.ChunkManager;
import minicraft.level.ChunkPoint;
import minicraft.level.structure.SimpleStructure;
import minicraft.util.Hash2;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.util.Collections;
import java.util.function.Consumer;

public class VillageHouse extends Structure {
	protected static final SimpleStructure house, ruinOverlay1, ruinOverlay2;
	protected static final Rectangle bounds;

	static {
		house = new SimpleStructure(128);
		house.setData("F:Wood Planks,W:Wood Wall",
			"WWWWW\n"+
			"WFFFW\n"+
			"WFFFW\n"+
			"WFFFW\n"+
			"WWWWW");
		ruinOverlay1 = new SimpleStructure(128);
		ruinOverlay1.setData("G:Grass,F:Wood Planks",
			"**FG*\n"+
			"F*GG*\n"+
			"*G**F\n"+
			"G*G**\n"+
			"***G*");
		ruinOverlay2 = new SimpleStructure(128);
		ruinOverlay2.setData("G:Grass,F:Wood Planks",
			"F**G*\n"+
			"*****\n"+
			"*GG**\n"+
			"F**G*\n"+
			"*F**G");
		bounds = new Rectangle(-3, -3, 3, 3, Rectangle.CORNERS);
	};
	private int gridSize;
	private boolean ruined;
	public VillageHouse(int gridSize, boolean ruined) {
		this.gridSize = gridSize;
		this.ruined = ruined;
	}

	public Set<Point> occurencesInChunk(Level level, int xc, int yc) {
		return Collections.EMPTY_SET;
	}

	public Set<ChunkPoint> affectedChunks(Level level, int xt, int yt) {
		return house.affectedChunks(level, xt, yt);
	}

	public Set<Rectangle> getBounds(Level level, int xt, int yt) {
		return Collections.singleton(new Rectangle(xt - 3, yt - 3, xt + 3, yt + 3, Rectangle.CORNERS));
	}

	public void draw(Level level, int xt, int yt) {
		Random r = new Random(Hash2.hash2(level.getSeed(), xt, yt));
		boolean hasChest = r.nextBoolean();
		int doorConfig = r.nextInt(14)+1;
		boolean overlaySwitch = r.nextBoolean();
		house.draw(level, xt, yt);
		if(hasChest) {
			Chest c = new Chest();
			c.populateInvRandom(r, "villagehouse", 1);
			level.add(c, (xt + r.nextInt(3) - 1) << 4, (yt + r.nextInt(3) - 1) << 4);
		}

		if((doorConfig & 0x1)==1)
			level.setTile(xt, yt-2, Tiles.get("Wood Door"));
		if((doorConfig & 0x2)==2)
			level.setTile(xt+2, yt, Tiles.get("Wood Door"));
		if((doorConfig & 0x4)==4)
			level.setTile(xt, yt+2, Tiles.get("Wood Door"));
		if((doorConfig & 0x8)==8)
			level.setTile(xt-2, yt, Tiles.get("Wood Door"));

		if(ruined) {
			if(overlaySwitch)
				ruinOverlay1.draw(level, xt, yt);
			else
				ruinOverlay2.draw(level, xt, yt);
		}
	}

	@Override
	public int hashCode() {
		if(ruined)
			return house.hashCode() ^ ruinOverlay1.hashCode() ^ ruinOverlay2.hashCode();
		else return house.hashCode();
	}
}
