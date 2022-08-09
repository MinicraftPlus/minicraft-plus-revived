package minicraft.gfx;

import java.util.ArrayList;
import java.util.HashMap;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import minicraft.core.Renderer;
import minicraft.util.Logging;

public class SpriteLinker {
	private final HashMap<String, SpriteSheet> entitySheets = new HashMap<>();
	private final HashMap<String, SpriteSheet> guiSheets = new HashMap<>();
	private final HashMap<String, SpriteSheet> itemSheets = new HashMap<>();
	private final HashMap<String, SpriteSheet> tileSheets = new HashMap<>();

	private final ArrayList<LinkedSpriteSheet> linkedSheets = new ArrayList<>();

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

	public void updateLinkedSheets() {
		linkedSheets.forEach(s -> s.reload());
	}

	public static enum SpriteType {
		Item, Gui, Tile, Entity; // Only for resource packs; Skin is not applied.
	}

	public void linkSpriteSheet(LinkedSpriteSheet sheet, SpriteType type) {
		switch (type) {
			case Entity: sheet.linkedMap = Renderer.spriteLinker.entitySheets; break;
			case Gui: sheet.linkedMap = Renderer.spriteLinker.guiSheets; break;
			case Item: sheet.linkedMap = Renderer.spriteLinker.itemSheets; break;
			case Tile: sheet.linkedMap = Renderer.spriteLinker.tileSheets; break;
		}
	}

	public static class LinkedSpriteSheet implements Destroyable {
		private final String key;
		private int x, y, w, h, color = -1, mirror = 0, number = 0;
		private int[][] mirrors = null;
		private boolean onepixel = false;

		private HashMap<String, SpriteSheet> linkedMap;
		private MobSpriteType mobType = MobSpriteType.Animations;
		private SpriteType spriteType;
		private MobSprite[][] mobSprites;
		private Sprite sprite;
		private boolean destoryed;
		private boolean useMobSprites = false;
		private boolean reloaded = false;

		public LinkedSpriteSheet(SpriteType t, String key) {
			this.key = key;
			this.spriteType = t;
			Renderer.spriteLinker.linkSpriteSheet(this, t);
			Renderer.spriteLinker.linkedSheets.add(this);
		}

		public LinkedSpriteSheet setSpriteSize(int w, int h) {
			this.w = w;
			this.h = h;
			reloaded = false;
			return this;
		}
		public LinkedSpriteSheet setSpritePos(int x, int y) {
			this.x = x;
			this.y = y;
			reloaded = false;
			return this;
		}
		public LinkedSpriteSheet setSpriteDim(int x, int y, int w, int h) {
			setSpriteSize(w, h);
			setSpritePos(x, y);
			reloaded = false;
			return this;
		}
		public LinkedSpriteSheet setColor(int color) {
			this.color = color;
			reloaded = false;
			return this;
		}
		public LinkedSpriteSheet setMirror(int mirror) {
			this.mirror = mirror;
			reloaded = false;
			return this;
		}
		public LinkedSpriteSheet setMirrors(int[][] mirrors) {
			this.mirrors = mirrors;
			reloaded = false;
			return this;
		}
		public LinkedSpriteSheet setOnePixel(boolean onepixel) {
			this.onepixel = onepixel;
			reloaded = false;
			return this;
		}

		public SpriteSheet getSheet() { return linkedMap.get(key); }
		public Sprite getSprite() {
			if (!reloaded) {
				reloadSprite();
			}

			return sprite;
		}
		public MobSprite[][] getMobSprites() {
			if (!useMobSprites) {
				reloaded = true;
				useMobSprites = true;
			}

			if (!reloaded) {
				reloadSprite();
			}

			return mobSprites;
		}

		public void reload() { reloaded = false; }
		private void reloadSprite() {
			SpriteSheet sheet = linkedMap.get(key);
			if (sheet != null) {
				if (w <= 0) w = sheet.width;
				if (h <= 0) h = sheet.height;
				sprite = mirrors == null ? new Sprite(x, y, w, h, sheet, mirror, onepixel) : new Sprite(x, y, w, h, sheet, onepixel, mirrors);
				sprite.color = color;
				mobSprites = null;
				if (useMobSprites) {
					switch (mobType) {
						case Animations: mobSprites = MobSprite.compileMobSpriteAnimations(x, y, sheet);
						case List: mobSprites = new MobSprite[][]{MobSprite.compileSpriteList(x, y, w, h, mirror, number, sheet)};
					}
				}
			} else {
				Logging.SPRITE.warn("Sprite with resource ID not found: {}", key);
				sprite = Sprite.missingTexture(spriteType).getSprite();
			}

			reloaded = true;
		}

		private enum MobSpriteType { Animations, List }
		public LinkedSpriteSheet setMobSpriteAnimations() {
			mobType = MobSpriteType.Animations;
			reloaded = false;
			useMobSprites = true;
			return this;
		}
		public LinkedSpriteSheet setSpriteList(int number) {
			mobType = MobSpriteType.List;
			this.number = number;
			reloaded = false;
			useMobSprites = true;
			return this;
		}

		@Override
		public void destroy() throws DestroyFailedException {
			Renderer.spriteLinker.linkedSheets.remove(this);
			destoryed = true;
		}
		@Override
		public boolean isDestroyed() {
			return destoryed;
		}
	}
}
