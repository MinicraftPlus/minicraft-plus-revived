package minicraft.level.structure;

import minicraft.entity.furniture.Furniture;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.level.Level;
import minicraft.level.ChunkManager;
import minicraft.level.ChunkPoint;
import minicraft.util.Hash2;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

public class JigsawStructure extends MultiStructure {
	private final int minimumPieces, maximumPieces;

	public static enum Direction {
		NORTH("North", 0, -1), EAST("East", 1, 0), SOUTH("South", 0, 1), WEST("West", -1, 0);

		String name;
		int x, y;
		Direction(String name, int x, int y) {
			this.name = name;
			this.x = x;
			this.y = y;
		}

		public int getX() { return x; }
		public int getY() { return y; }

		@Override
		public String toString() {
			return name;
		}
	};

	private HashMap<Structure, PlacementRule> pieces = new HashMap<>();
	private HashMap<Direction, HashSet<Structure>> byDirection = new HashMap<>();
	private Structure centerPiece = null;

	public JigsawStructure(int gridSize, int minimumPieces, int maximumPieces) {
		super(gridSize);
		this.minimumPieces = minimumPieces;
		this.maximumPieces = maximumPieces;
		this.seedOffset = gridSize * maximumPieces ^ minimumPieces;
		byDirection.put(Direction.NORTH, new HashSet<Structure>());
		byDirection.put(Direction.SOUTH, new HashSet<Structure>());
		byDirection.put(Direction.WEST, new HashSet<Structure>());
		byDirection.put(Direction.EAST, new HashSet<Structure>());
	}

	public void setCenterPiece(Structure s, Set<Connector> connectors, int maximumUses) {
		addPiece(s, connectors, 1, maximumUses);
		centerPiece = s;
	}

	public void addOptionalPiece(Structure s, Set<Connector> connectors) {
		addPiece(s, connectors, 0);
	}
	public void addOptionalPiece(Structure s, Set<Connector> connectors, int maximumUses) {
		addPiece(s, connectors, 0, maximumUses);
	}
	public void addRequiredPiece(Structure s, Set<Connector> connectors) {
		addPiece(s, connectors, 1);
	}
	public void addRequiredPiece(Structure s, Set<Connector> connectors, int maximumUses) {
		addPiece(s, connectors, 1, maximumUses);
	}

	public void addPiece(Structure s, Set<Connector> connectors, int minimumUses) {
		addPiece(s, connectors, minimumUses, this.maximumPieces);
	}

	public void addPiece(Structure s, Set<Connector> connectors, int minimumUses, int maximumUses) {
		seedOffset ^= s.hashCode() << pieces.size();
		pieces.put(s, new PlacementRule(connectors, minimumUses, maximumUses));
		for(Connector c : connectors)
			byDirection.get(c.oppositeDirection).add(s);
	}

	public static class Connector {
		public Point connectionOffset;
		public Direction direction, oppositeDirection;
		public Connector(Direction direction, Point connectionOffset) {
			this.direction = direction;
			this.connectionOffset = connectionOffset;
			switch(direction) {
			default:
			case NORTH:
				oppositeDirection = Direction.SOUTH;
				break;
			case SOUTH:
				oppositeDirection = Direction.NORTH;
				break;
			case EAST:
				oppositeDirection = Direction.WEST;
				break;
			case WEST:
				oppositeDirection = Direction.EAST;
				break;
			}
		}

		public Connector(Direction direction, int x, int y) {
			this(direction, new Point(x, y));
		}

		public Connector(Connector other) {
			this.connectionOffset = new Point(other.connectionOffset);
			switch(other.direction) {
			default:
			case NORTH:
				direction = Direction.NORTH;
				oppositeDirection = Direction.SOUTH;
				break;
			case SOUTH:
				direction = Direction.SOUTH;
				oppositeDirection = Direction.NORTH;
				break;
			case EAST:
				direction = Direction.EAST;
				oppositeDirection = Direction.WEST;
				break;
			case WEST:
				direction = Direction.WEST;
				oppositeDirection = Direction.EAST;
				break;
			}
		}

		@Override
		public String toString() {
			return direction.toString().charAt(0) + "-" + oppositeDirection.toString().charAt(0)+"-("+connectionOffset.x + ", " + connectionOffset.y + ")";
		}
	}

	public static class StructureWrapper {
		public Set<Connector> connectors;
		public Structure structure;
		public StructureWrapper(Structure structure, Connector... connectors) {
			this.structure = structure;
			this.connectors = new HashSet<Connector>();
			for(Connector c : connectors)
				this.connectors.add(c);
		}

		public StructureWrapper(String palette, String data, Connector... connectors) {
			SimpleStructure s = new SimpleStructure(4096);
			s.setData(palette, data);
			this.structure = s;
			this.connectors = new HashSet<Connector>();
			for(Connector c : connectors)
				this.connectors.add(c);
		}
	}

	private static class PlacementRule {
		public Set<Connector> connectors;
		int minimumUses, maximumUses;
		public PlacementRule(Set<Connector> connectors, int minu, int maxu) {
			this.connectors = connectors;
			this.minimumUses = minu;
			this.maximumUses = maxu;
		}
	}

	// Find randomly structured placement at given position
	protected Map<Structure, Set<Point>> placementsAt(Level level, int x, int y) {
		Map<Structure, Set<Point>> out = new HashMap<>();

		// Used hashed seed for random
		Random r = new Random(Hash2.hash2(level.getSeed() ^ seedOffset, x, y));
		Structure[] structures = pieces.keySet().toArray(new Structure[0]);

		// Prepopulate output
		int requiredPieces = 0;
		for(Structure s : structures) {
			out.put(s, new HashSet<Point>());
			requiredPieces += pieces.get(s).minimumUses;
		}

		// Get initial piece & populate availableConnectors
		ArrayList<Connector> availableConnectors = new ArrayList<>();
		HashSet<Rectangle> placedBounds = new HashSet<>();
		Structure initialPiece = centerPiece;
		if(initialPiece == null)
			initialPiece = structures[r.nextInt(structures.length)];
		if(pieces.get(initialPiece).minimumUses > 0)
			requiredPieces--;
		out.get(initialPiece).add(new Point(x, y));
		for(Connector c : pieces.get(initialPiece).connectors) {
			Connector moved = new Connector(c);
			moved.connectionOffset.translate(x, y);
			availableConnectors.add(moved);
		}
		placedBounds.addAll(initialPiece.getBounds(level, x, y));

		Direction[] directions = Direction.values();
		HashMap<Direction, ArrayList<Structure>> directionLists = new HashMap<>();
		directionLists.put(Direction.NORTH, new ArrayList<Structure>(byDirection.get(Direction.NORTH)));
		directionLists.put(Direction.SOUTH, new ArrayList<Structure>(byDirection.get(Direction.SOUTH)));
		directionLists.put(Direction.WEST, new ArrayList<Structure>(byDirection.get(Direction.WEST)));
		directionLists.put(Direction.EAST, new ArrayList<Structure>(byDirection.get(Direction.EAST)));


		// Ideally, "requiredPieces" hits zero before maximumPieces is reached
		for(int numPieces = 1; numPieces < maximumPieces + requiredPieces && !availableConnectors.isEmpty(); numPieces++) {
			// If all requirements are met, 25% chance of being done
			if(requiredPieces == 0 && numPieces >= minimumPieces && r.nextInt(4) == 0)
				break;

			// Whether we are strictly searching for required pieces only
			boolean strict = numPieces >= (maximumPieces - requiredPieces);

			// Pick out a random connector to build off of
			Connector base = availableConnectors.get(0);
			ArrayList<Structure> targetList = directionLists.get(base.direction);
			Structure structure = targetList.get(0);
			Connector structureConnector = base;
			Set<Rectangle> bounds = new HashSet<>();
			Point placement = new Point();
			for(int cAttempts = 0; cAttempts < 32; cAttempts++) {
				base = availableConnectors.get(r.nextInt(availableConnectors.size()));
				if(directionLists.get(base.direction).isEmpty())
					continue;

				// Try to find a structure that hasn't hit its maximum occurence limit
				// or, one that needs to meet a minimum within the remaining pieces allowed
				targetList = directionLists.get(base.direction);
				for(int sAttempts = 0; sAttempts < 32; sAttempts++) {
					structure = targetList.get(r.nextInt(targetList.size()));
					structureConnector = null;
					for(Connector c : pieces.get(structure).connectors)
						if(c.oppositeDirection == base.direction) {
							structureConnector = c;
							break;
						}
					if(structureConnector == null) // Shouldn't happen! but skip if it does
						continue;
					int occurences = out.get(structure).size() + 1;
					int maxAllowed = pieces.get(structure).maximumUses;
					if(occurences >= maxAllowed)
						continue;
					int pathsOut = pieces.get(structure).connectors.size() - 1;
					if(pathsOut == 0 && availableConnectors.isEmpty() && (numPieces < minimumPieces || requiredPieces > 0))
						continue;
					int leftRequired = pieces.get(structure).minimumUses - occurences;
					if(leftRequired <= 0 && strict)
						continue;
					break;
				}

				// Calculate hypothetical placement
				placement = new Point(base.connectionOffset);
				Point off = structureConnector.connectionOffset;
				placement.translate(-off.x, -off.y);
				placement.translate(base.direction.x, base.direction.y);

				// check if chosen connector & structure intersect anything
				boolean validPlacement = true;
				bounds = structure.getBounds(level, placement.x, placement.y);
				for(Rectangle a : placedBounds) {
					for(Rectangle b : bounds)
						if(a.intersects(b)) {
							validPlacement = false;
							break;
						}
					if(!validPlacement) break;
				}

				if(validPlacement)
					break;
			}

			// Handle chosen structure
			availableConnectors.remove(base);
			out.get(structure).add(placement);
			placedBounds.addAll(bounds);

			// Add next connectors minus what we're connecting with
			for(Connector c : pieces.get(structure).connectors) {
				if(c.oppositeDirection == base.direction)
					continue;
				Connector moved = new Connector(c);
				moved.connectionOffset.translate(placement.x, placement.y);
				availableConnectors.add(moved);
			}
		}

		return out;
	}
}
