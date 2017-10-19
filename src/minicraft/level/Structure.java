package minicraft.level;

import java.util.HashSet;

import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;

// this stores structures that can be drawn at any location.
public class Structure {
	
	private HashSet<Point> tiles;
	
	public Structure() {
		tiles = new HashSet<>();
	}
	
	public void setTile(int x, int y, Tile tile) {
		tiles.add(new Point(x, y, tile));
	}
	
	public void draw(Level level, int xt, int yt) {
		for(Point p: tiles) {
			level.setTile(xt+p.x, yt+p.y, p.t);
		}
	}
	
	static class Point {
		int x, y;
		Tile t;
		
		public Point(int x, int y, Tile tile) {
			this.x = x;
			this.y = y;
			this.t = tile;
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof Point)) return false;
			Point p = (Point) o;
			return x == p.x && y == p.y && t.id == p.t.id;
		}
		
		public int hashCode() {
			return x+y*51 + t.id * 131;
		}
	}
	
	static final Structure dungeonGate;
	
	static {
		Structure s = new Structure();
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
