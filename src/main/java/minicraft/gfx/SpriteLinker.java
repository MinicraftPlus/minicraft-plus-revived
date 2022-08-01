package minicraft.gfx;

import java.util.ArrayList;
import java.util.HashMap;

import minicraft.core.Renderer;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.level.tile.Tiles;

public class SpriteLinker {
	/* Keys are without .png */
	private static final ArrayList<String> itemKeys = new ArrayList<>();
	private static final ArrayList<String> guiKeys = new ArrayList<>();
	private static final ArrayList<String> tileKeys = new ArrayList<>();
	private static final ArrayList<String> entityKeys = new ArrayList<>();

	static {
		for (Item item : Items.getAll()) {
			itemKeys.add(item.getName());
		}

		for (short tile : Tiles.getAll().keySet()) {
			tileKeys.add(String.valueOf(tile));
		}

		guiKeys.add("font");
		guiKeys.add("title");
		guiKeys.add("hud");

		entityKeys.add("arrow");
		entityKeys.add("spark");
		entityKeys.add("bed");
		entityKeys.add("chest");
		entityKeys.add("workbench");
		entityKeys.add("oven");
		entityKeys.add("furnace");
		entityKeys.add("anvil");
		entityKeys.add("enchanter");
		entityKeys.add("loom");
		entityKeys.add("deathchest");
		entityKeys.add("dungeonchest");
		entityKeys.add("lantern");
		entityKeys.add("iron lantern");
		entityKeys.add("gold lantern");
		entityKeys.add("spawner");
		entityKeys.add("tnt");
		entityKeys.add("airwizard");
		entityKeys.add("cow");
		entityKeys.add("creeper");
		entityKeys.add("knight");
		entityKeys.add("pig");
		entityKeys.add("sheep");
		entityKeys.add("skeleton");
		entityKeys.add("slime");
		entityKeys.add("snake");
		entityKeys.add("zombie");
		entityKeys.add("cow");
		entityKeys.add("fireparticle");
		entityKeys.add("smashparticle");
	}

	private final HashMap<String, Sprite> itemSprites = new HashMap<>();
	private final HashMap<String, Sprite> guiSprites = new HashMap<>();
	private final HashMap<String, Sprite> tileSprites = new HashMap<>();
	private final HashMap<String, MobSprite[][]> entitySprites = new HashMap<>();

	public ArrayList<String> getSpriteKeys(SpriteType t) {
		switch (t) {
			case Entity: return new ArrayList<>(entityKeys);
			case Gui: return new ArrayList<>(guiKeys);
			case Item: return new ArrayList<>(itemKeys);
			case Tile: return new ArrayList<>(tileKeys);
			default: return new ArrayList<>();
		}
	}

	public void setSprite(SpriteType t, String key, Sprite sprite) {
		switch (t) {
			case Gui: guiSprites.put(key, sprite); break;
			case Item: itemSprites.put(key, sprite); break;
			case Tile: tileSprites.put(key, sprite); break;
			default:
				break;
		}
	}
	public void setMobSprites(String key, MobSprite[][] sprite) {
		entitySprites.put(key, sprite);
	}

	public Sprite getSprite(SpriteType t, String key) {
		switch (t) {
			case Gui: return guiSprites.get(key);
			case Item: return itemSprites.get(key);
			case Tile: return tileSprites.get(key);
			default: return null;
		}
	}
	public MobSprite[][] getMobSprites(String key) {
		return entitySprites.get(key);
	}

	public static enum SpriteType {
		Item, Gui, Tile, Entity; // Only for resource packs; Skin is not applied.
	}

	public static class LinkedSprite {
		private final String key;
		private HashMap<String, Sprite> linkedMap;
		private HashMap<String, MobSprite[][]> linkedEntityMap;

		public LinkedSprite(SpriteType t, String key) {
			this.key = key;
			switch (t) {
				case Entity: linkedEntityMap = Renderer.spriteLinker.entitySprites; break;
				case Gui: linkedMap = Renderer.spriteLinker.guiSprites; break;
				case Item: linkedMap = Renderer.spriteLinker.itemSprites; break;
				case Tile: linkedMap = Renderer.spriteLinker.tileSprites; break;
			}
		}

		public Sprite getSprite() {
			if (linkedMap != null)
				return linkedMap.get(key);
			return null;
		}
		public MobSprite[][] getMobSprites() {
			if (linkedEntityMap != null)
				return linkedEntityMap.get(key);
			return null;
		}
	}
}
