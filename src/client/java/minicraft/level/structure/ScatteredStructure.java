package minicraft.level.structure;

import minicraft.entity.furniture.Furniture;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.level.Level;
import minicraft.level.ChunkManager;
import minicraft.level.ChunkPoint;
import minicraft.util.Hash2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import java.util.function.Consumer;

public class ScatteredStructure extends MultiStructure {
	private int startingSpread, minimum = -1, maximum = -1;

	private HashMap<Structure, PlacementRules> structures = new HashMap<>();

	public ScatteredStructure(int gridSize, int startingSpread, int minimum, int maximum) {
		super(gridSize);
		this.startingSpread = startingSpread;
		this.seedOffset = gridSize * startingSpread;
		this.minimum = minimum;
		this.maximum = maximum;
	}

	public void addSingle(Structure s) { add(s, 1, 1); }
	public void addOptionalSingle(Structure s) { add(s, 0, 1); }
	public void addOptional(Structure s, int maximumUses) { add(s, 0, maximumUses); }

	public void add(Structure s, int minimumUses, int maximumUses) {
		seedOffset ^= s.hashCode() << structures.size();
		structures.put(s, new PlacementRules(minimumUses, maximumUses));
	}

	// Find randomly generated placements at given point
	protected Map<Structure, Set<Point>> placementsAt(Level level, int x, int y) {

		Map<Structure, Set<Point>> out = new HashMap<>();

		// Use hashed seed for random
		Random r = new Random(Hash2.hash2(level.getSeed() ^ seedOffset, x, y));
		Structure[] structureSet = structures.keySet().toArray(new Structure[0]);
		int currentSpread = startingSpread;
		HashMap<Structure, Boolean> satisfied = new HashMap<>();
		HashSet<Rectangle> bounds = new HashSet<>();

		// Prepopulate output
		for(Structure s : structureSet)
			out.put(s, new HashSet<Point>());

		for(int numStructures = 0; numStructures < maximum; numStructures++) {
			int attempts = 1024;

			// Try to find a structure that hasn't hit its maximum occurence limit
			Structure toPlace;
			do {
				toPlace = structureSet[r.nextInt(structureSet.length)];
				attempts--;
			} while(out.get(toPlace).size() >= structures.get(toPlace).maximum && attempts > 0);

			// Find a place to put it, increasing range after enough attempts
			Point placement = new Point(0, 0);
			int doubleCountdown = 10;

			boolean placeable = false;
			attempts = 1024;
			while(!placeable && attempts > 0) {
				placement = new Point(
					r.nextInt(currentSpread) - currentSpread / 2 + x,
					r.nextInt(currentSpread) - currentSpread / 2 + y
				);
				attempts--;
				doubleCountdown--;
				if(doubleCountdown <= 0) {
					doubleCountdown = 10;
					currentSpread *= 2;
				}

				// See if it is in free space
				Set<Rectangle> boundsForPlacement = toPlace.getBounds(level, placement.x, placement.y);
				placeable = true;
				for(Rectangle current : bounds)
					for(Rectangle attempt : boundsForPlacement)
						if(current.intersects(attempt)) {
							placeable = false;
							break;
						}

				// Placement found!
				if(placeable)
					bounds.addAll(boundsForPlacement);
			}

			// Add placement
			out.get(toPlace).add(placement);
			if(out.get(toPlace).size() == structures.get(toPlace).minimum)
				satisfied.put(toPlace, true);

			// If we hit minimum required structures, we can check if enough structures were placed to obey placement rules. If they have, we are done placing
			if(numStructures > minimum) {
				boolean placementRulesMet = true;
				for(Structure s : structureSet) {
					if(!satisfied.get(s)) {
						placementRulesMet = false;
						break;
					}
				}

				if(placementRulesMet)
					break;
			}
		}

		return out;
	}

	private static class PlacementRules {
		public int minimum, maximum;
		public PlacementRules(int minimum, int maximum) {
			this.minimum = minimum;
			this.maximum = maximum;
		}
	};
}
