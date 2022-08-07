package minicraft.gfx;

import java.util.HashMap;

import minicraft.core.Renderer;
import minicraft.util.Logging;

public class SpriteLinker {
	private final HashMap<String, SpriteSheet> entitySheets = new HashMap<>();
	private final HashMap<String, SpriteSheet> guiSheets = new HashMap<>();
	private final HashMap<String, SpriteSheet> itemSheets = new HashMap<>();
	private final HashMap<String, SpriteSheet> tileSheets = new HashMap<>();

	public void resetSprites() {
		entitySheets.clear();
		guiSheets.clear();
		itemSheets.clear();
		tileSheets.clear();
	}

	/** The safe size check which will be required for the higher resolution sprites must be used
	 * before this method invoked. But in new rendering engine.
	 * @param t The sheet type.
	 * @param key The sheet key.
	 * @param spriteSheet The sheet.
	 */
	public void setSprite(SpriteType t, String key, SpriteSheet spriteSheet) {
		switch (t) {
			case Entity: entitySheets.put(key, spriteSheet); break;
			case Gui: guiSheets.put(key, spriteSheet); break;
			case Item: itemSheets.put(key, spriteSheet); break;
			case Tile: tileSheets.put(key, spriteSheet); break;
			default:
				break;
		}
	}

	public SpriteSheet getSpriteSheet(SpriteType t, String key) {
		switch (t) {
			case Entity: return entitySheets.get(key);
			case Gui: return guiSheets.get(key);
			case Item: return itemSheets.get(key);
			case Tile: return tileSheets.get(key);
			default: return null;
		}
	}

	public static enum SpriteType {
		Item, Gui, Tile, Entity; // Only for resource packs; Skin is not applied.
	}

	public static class LinkedSpriteSheet {
		private final String key;
		private HashMap<String, SpriteSheet> linkedMap;
		private int x, y, w, h, color = -1, mirror = 0, number = 0;
		private int[][] mirrors = null;
		private boolean onepixel = false;
		private MobSpriteType mobType = MobSpriteType.Animations;

		public LinkedSpriteSheet(SpriteType t, String key) {
			this.key = key;
			switch (t) {
				case Entity: linkedMap = Renderer.spriteLinker.entitySheets; break;
				case Gui: linkedMap = Renderer.spriteLinker.guiSheets; break;
				case Item: linkedMap = Renderer.spriteLinker.itemSheets; break;
				case Tile: linkedMap = Renderer.spriteLinker.tileSheets; break;
			}
		}

		public LinkedSpriteSheet setSpriteSize(int w, int h) {
			this.w = w;
			this.h = h;
			return this;
		}
		public LinkedSpriteSheet setSpritePos(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}
		public LinkedSpriteSheet setSpriteDim(int x, int y, int w, int h) {
			setSpriteSize(w, h);
			setSpritePos(x, y);
			return this;
		}
		public LinkedSpriteSheet setColor(int color) {
			this.color = color;
			return this;
		}
		public LinkedSpriteSheet setMirror(int mirror) {
			this.mirror = mirror;
			return this;
		}
		public LinkedSpriteSheet setMirrors(int[][] mirrors) {
			this.mirrors = mirrors;
			return this;
		}
		public LinkedSpriteSheet setOnePixel(boolean onepixel) {
			this.onepixel = onepixel;
			return this;
		}

		public SpriteSheet getSheet() { return linkedMap.get(key); }
		/** {@link #getSpriteOrMissing(SpriteType)} would be more prefered to this. */
		public Sprite getSprite() {
			SpriteSheet sheet = linkedMap.get(key);
			if (sheet != null) {
				if (w <= 0) w = sheet.width;
				if (h <= 0) h = sheet.height;
				Sprite sprite = mirrors == null ? new Sprite(x, y, w, h, sheet, mirror, onepixel) : new Sprite(x, y, w, h, sheet, onepixel, mirrors);
				sprite.color = color;
				return sprite;
			}

			Logging.SPRITE.warn("Sprite with resource ID not found: {}", key);
			return null;
		}
		/** This method would be prefered to {@link #getSprite()}. */
		public Sprite getSpriteOrMissing(SpriteType type) {
			Sprite sprite = getSprite();
			return sprite != null ? sprite : Sprite.missingTexture(type).getSprite();
		}
		public MobSprite[][] getMobSprites() {
			SpriteSheet sheet = linkedMap.get(key);
			if (sheet != null) {
				if (w <= 0) w = sheet.width;
				if (h <= 0) h = sheet.height;
				switch (mobType) {
					case Animations: return MobSprite.compileMobSpriteAnimations(x, y, sheet);
					case List: return new MobSprite[][]{MobSprite.compileSpriteList(x, y, w, h, mirror, number, sheet)};
				}
			}

			return null;
		}

		private enum MobSpriteType { Animations, List }
		public LinkedSpriteSheet setMobSpriteAnimations() {
			mobType = MobSpriteType.Animations;
			return this;
		}
		public LinkedSpriteSheet setSpriteList(int number) {
			mobType = MobSpriteType.List;
			this.number = number;
			return this;
		}
	}
}