package minicraft.level;

import java.util.HashMap;
import java.util.HashSet;

import minicraft.entity.furniture.Furniture;
import minicraft.entity.furniture.Lantern;
import minicraft.gfx.Point;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

// this stores structures that can be drawn at any location.
public class Structure {
	
	private HashSet<TilePoint> tiles;
	private HashMap<Point, Furniture> furniture;
	
	public Structure() {
		tiles = new HashSet<>();
		furniture = new HashMap<>();
	}
	
	public void setTile(int x, int y, Tile tile) {
		tiles.add(new TilePoint(x, y, tile));
	}
	public void addFurniture(int x, int y, Furniture furniture) {
		this.furniture.put(new Point(x, y), furniture);
	}
	
	public void draw(Level level, int xt, int yt) {
		for(TilePoint p: tiles)
			level.setTile(xt+p.x, yt+p.y, p.t);
		
		for(Point p: furniture.keySet())
			level.add(furniture.get(p).clone(), xt+p.x, yt+p.y, true);
	}
	
	static class TilePoint {
		int x, y;
		Tile t;
		
		public TilePoint(int x, int y, Tile tile) {
			this.x = x;
			this.y = y;
			this.t = tile;
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof TilePoint)) return false;
			TilePoint p = (TilePoint) o;
			return x == p.x && y == p.y && t.id == p.t.id;
		}
		
		@Override
		public int hashCode() {
			return x+y*51 + t.id * 131;
		}
	}
	
	static final Structure dungeonGate;
	
	static {
		Structure s = new Structure();
		s.addFurniture(-1, 1, new Lantern(Lantern.Type.IRON));
		s.setTile(-1, 0, Tiles.get("Obsidian"));
		s.setTile(+1, 0, Tiles.get("Obsidian"));
		s.setTile(+2, 0, Tiles.get("Obsidian Door"));
		s.setTile(-2, 0, Tiles.get("Obsidian Door"));
		s.setTile(0, -1, Tiles.get("Obsidian"));
		s.setTile(0, +1, Tiles.get("Obsidian"));
		s.setTile(0, +2, Tiles.get("Obsidian Door"));
		s.setTile(0, -2, Tiles.get("Obsidian Door"));
		s.setTile(-1, -1, Tiles.get("Obsidian"));
		s.setTile(-1, +1, Tiles.get("Obsidian"));
		s.setTile(+1, -1, Tiles.get("Obsidian"));
		s.setTile(+1, +1, Tiles.get("Obsidian"));
		s.setTile(+3, 0, Tiles.get("Obsidian"));
		s.setTile(-3, 0, Tiles.get("Obsidian"));
		s.setTile(+3, -1, Tiles.get("Obsidian"));
		s.setTile(-3, -1, Tiles.get("Obsidian"));
		s.setTile(+3, +1, Tiles.get("Obsidian"));
		s.setTile(-3, +1, Tiles.get("Obsidian"));
		s.setTile(+4, 0, Tiles.get("Obsidian"));
		s.setTile(-4, 0, Tiles.get("Obsidian"));
		s.setTile(+4, -1, Tiles.get("Obsidian"));
		s.setTile(-4, -1, Tiles.get("Obsidian"));
		s.setTile(+4, +1, Tiles.get("Obsidian"));
		s.setTile(-4, +1, Tiles.get("Obsidian"));
		s.setTile(0, +3, Tiles.get("Obsidian"));
		s.setTile(0, -3, Tiles.get("Obsidian"));
		s.setTile(+1, -3, Tiles.get("Obsidian"));
		s.setTile(-1, -3, Tiles.get("Obsidian"));
		s.setTile(+1, +3, Tiles.get("Obsidian"));
		s.setTile(-1, +3, Tiles.get("Obsidian"));
		s.setTile(0, +4, Tiles.get("Obsidian"));
		s.setTile(0, -4, Tiles.get("Obsidian"));
		s.setTile(+1, -4, Tiles.get("Obsidian"));
		s.setTile(-1, -4, Tiles.get("Obsidian"));
		s.setTile(+1, +4, Tiles.get("Obsidian"));
		s.setTile(-1, +4, Tiles.get("Obsidian"));
		s.setTile(-2, -2, Tiles.get("Obsidian Wall"));
		s.setTile(-3, -2, Tiles.get("Obsidian Wall"));
		s.setTile(-3, +2, Tiles.get("Obsidian Wall"));
		s.setTile(-2, +1, Tiles.get("Obsidian Wall"));
		s.setTile(+2, -2, Tiles.get("Obsidian Wall"));
		s.setTile(+4, -2, Tiles.get("Obsidian Wall"));
		s.setTile(+4, +2, Tiles.get("Obsidian Wall"));
		s.setTile(-4, -2, Tiles.get("Obsidian Wall"));
		s.setTile(-4, +2, Tiles.get("Obsidian Wall"));
		s.setTile(+1, -2, Tiles.get("Obsidian Wall"));
		s.setTile(-2, +2, Tiles.get("Obsidian Wall"));
		s.setTile(+2, +3, Tiles.get("Obsidian Wall"));
		s.setTile(+2, +4, Tiles.get("Obsidian Wall"));
		s.setTile(-2, -3, Tiles.get("Obsidian Wall"));
		s.setTile(-2, -4, Tiles.get("Obsidian Wall"));
		s.setTile(+2, -3, Tiles.get("Obsidian Wall"));
		s.setTile(+2, -4, Tiles.get("Obsidian Wall"));
		s.setTile(-2, +3, Tiles.get("Obsidian Wall"));
		s.setTile(-2, +4, Tiles.get("Obsidian Wall"));
		s.setTile(+3, -2, Tiles.get("Obsidian Wall"));
		s.setTile(+3, +2, Tiles.get("Obsidian Wall"));
		s.setTile(+2, +2, Tiles.get("Obsidian Wall"));
		s.setTile(-1, +2, Tiles.get("Obsidian Wall"));
		s.setTile(+2, -1, Tiles.get("Obsidian Wall"));
		s.setTile(+2, +1, Tiles.get("Obsidian Wall"));
		s.setTile(+1, +2, Tiles.get("Obsidian Wall"));
		s.setTile(-2, -1, Tiles.get("Obsidian Wall"));
		s.setTile(-1, -2, Tiles.get("Obsidian Wall"));
		
		dungeonGate = s;
	}
}
