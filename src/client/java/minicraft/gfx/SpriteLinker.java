package minicraft.gfx;

import minicraft.core.Renderer;
import minicraft.util.Logging;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import java.util.ArrayList;
import java.util.HashMap;

public class SpriteLinker {
	/**
	 * Buffering SpriteSheet for caching.
	 */
	private final HashMap<String, MinicraftImage> entitySheets = new HashMap<>(),
		guiSheets = new HashMap<>(), itemSheets = new HashMap<>(), tileSheets = new HashMap<>();

	/**
	 * Storing all exist in-used LinkedSprite.
	 */
	private final ArrayList<LinkedSprite> linkedSheets = new ArrayList<>();

	/**
	 * Clearing all Sprite buffers for the upcoming resource pack application.
	 */
	public void resetSprites() {
		entitySheets.clear();
		guiSheets.clear();
		itemSheets.clear();
		tileSheets.clear();
	}

	/**
	 * The safe size check which will be required for the higher resolution sprites must be used
	 * before this method invoked. But in new rendering engine.
	 * @param t The sheet type.
	 * @param key The sheet key.
	 * @param spriteSheet The sheet.
	 */
	public void setSprite(SpriteType t, String key, MinicraftImage spriteSheet) {
		switch (t) {
			case Entity:
				entitySheets.put(key, spriteSheet);
				break;
			case Gui:
				guiSheets.put(key, spriteSheet);
				break;
			case Item:
				itemSheets.put(key, spriteSheet);
				break;
			case Tile:
				tileSheets.put(key, spriteSheet);
				break;
		}
	}

	/**
	 * Getting the sprite sheet with the category and key.
	 * @param t The sprite category
	 * @param key The resource key.
	 * @return The sprite sheet. <code>null</code> if not found.
	 */
	public MinicraftImage getSheet(SpriteType t, String key) {
		switch (t) {
			case Entity:
				return entitySheets.get(key);
			case Gui:
				return guiSheets.get(key);
			case Item:
				return itemSheets.get(key);
			case Tile:
				return tileSheets.get(key);
		}

		return null;
	}

	/**
	 * Cleaing all skin sheets in entity sheets.
	 */
	public void clearSkins() {
		for (String k : new ArrayList<>(entitySheets.keySet())) {
			if (k.startsWith("skin.")) entitySheets.remove(k);
		}
	}

	/**
	 * Setting the skin in entity sheet.
	 * @param key The key of the sheet.
	 * @param spriteSheet The sheet to be added.
	 */
	public void setSkin(String key, MinicraftImage spriteSheet) {
		setSprite(SpriteType.Entity, key, spriteSheet);
	}

	/**
	 * Getting the missing texture texture with the specific sprite type.
	 * @param type The sprite category.
	 * @return The missing texture or null if invalid sprite type.
	 */
	public static LinkedSprite missingTexture(SpriteType type) {
		switch (type) {
			case Entity:
				return new LinkedSprite(SpriteType.Entity, "missing_entity");
			case Item:
				return new LinkedSprite(SpriteType.Item, "missing_item");
			case Tile:
				return new LinkedSprite(SpriteType.Tile, "missing_tile");
			default:
				return null;
		}
	}

	/**
	 * Getting the sheet of missing texture with the specific sprite type.
	 * @param type The sprite category.
	 * @return Ths missing texture sprite sheet or null if invalid sprite type.
	 */
	public MinicraftImage missingSheet(SpriteType type) {
		switch (type) { // The sheets should be found.
			case Entity:
				return entitySheets.get("missing_entity");
			case Item:
				return itemSheets.get("missing_item");
			case Tile:
				return tileSheets.get("missing_tile");
			default:
				return null;
		}
	}

	/**
	 * Updating all existing LinkedSheet for resource pack application.
	 */
	public void updateLinkedSheets() {
		Logging.SPRITE.debug("Updating all LinkedSprite.");
		linkedSheets.forEach(s -> s.reload());
	}

	/**
	 * The metadata of the sprite sheet.
	 */
	public static class SpriteMeta {
		/**
		 * The sprite animation configuration.
		 */
		public int frames = 1, // Minimum with 1.
			frametime = 0; // 0 if no animation.
		/**
		 * The sprite connector configuration.
		 */
		public String border = null, corner = null;
	}

	/**
	 * The sprite categories in the image resources. TODO Removed for the new save system
	 */
	public static enum SpriteType {
		Item, Gui, Tile, Entity; // Only for resource packs; Skin is not applied.
	}

	/**
	 * Linking the LinkedSprite into specific sheet map. This should only be used by {@link LinkedSprite}.
	 * @param sheet The sprite to be linked.
	 * @param type The sprite type to be linked.
	 */
	public void linkSpriteSheet(LinkedSprite sheet, SpriteType type) {
		// Because of the private access.
		switch (type) {
			case Entity:
				sheet.linkedMap = Renderer.spriteLinker.entitySheets;
				break;
			case Gui:
				sheet.linkedMap = Renderer.spriteLinker.guiSheets;
				break;
			case Item:
				sheet.linkedMap = Renderer.spriteLinker.itemSheets;
				break;
			case Tile:
				sheet.linkedMap = Renderer.spriteLinker.tileSheets;
				break;
		}
	}

	/**
	 * A sprite collector with resource collector.
	 */
	public static class LinkedSprite implements Destroyable {
		private final String key; // The resource key.

		/**
		 * The sprite configuration.
		 */
		private int x, y, w, h, color = -1, mirror = 0, flip = 0;

		// Sprite data.
		private HashMap<String, MinicraftImage> linkedMap;
		private SpriteType spriteType;
		private Sprite sprite;
		private boolean destoryed; // It is not linked when destoryed.
		private boolean reloaded = false; // Whether the sprite is reloaded.

		/**
		 * Create new LinkedSprite for the specific category and resource key.
		 * @param t The category of the sprite.
		 * @param key The resource key of the sprite.
		 */
		public LinkedSprite(SpriteType t, String key) {
			this.key = key;
			this.spriteType = t;
			Renderer.spriteLinker.linkSpriteSheet(this, t);
			Renderer.spriteLinker.linkedSheets.add(this);
		}

		/**
		 * Getting the sprite sheet of the linked sprite.
		 * @return The current linked sprite.
		 */
		public MinicraftImage getSheet() {
			return linkedMap.get(key);
		}

		/**
		 * Setting the sprite size.
		 * @param w The sprite width.
		 * @param h The sprite height
		 * @return The instance itself.
		 */
		public LinkedSprite setSpriteSize(int w, int h) {
			this.w = w;
			this.h = h;
			reloaded = false; // Reload this.
			return this;
		}

		/**
		 * Setting the sprite position.
		 * @param x The x position of the sprite.
		 * @param y The y position of the sprite.
		 * @return The instance itself.
		 */
		public LinkedSprite setSpritePos(int x, int y) {
			this.x = x;
			this.y = y;
			reloaded = false; // Reload this.
			return this;
		}

		/**
		 * Setting the sprite position and size.
		 * @param x The x position of the sprite.
		 * @param y The y position of the sprite.
		 * @param w The sprite width.
		 * @param h The sprite height
		 * @return The instance itself.
		 */
		public LinkedSprite setSpriteDim(int x, int y, int w, int h) {
			setSpriteSize(w, h);
			setSpritePos(x, y);
			reloaded = false; // Reload this.
			return this;
		}

		/**
		 * Setting the white tint.
		 * @param color The color of the white tint.
		 * @return The instance itself.
		 */
		public LinkedSprite setColor(int color) {
			this.color = color;
			reloaded = false; // Reload this.
			return this;
		}

		/**
		 * Setting the mirror of the sprite.
		 * @param mirror The mirror of the sprite.
		 * @return The instance itself.
		 */
		public LinkedSprite setMirror(int mirror) {
			this.mirror = mirror;
			reloaded = false; // Reload this.
			return this;
		}

		/**
		 * Setting the flip of the sprite sheet.
		 * @param flip The mirror of the sprite sheet.
		 * @return The instance itself.
		 */
		public LinkedSprite setFlip(int flip) {
			this.flip = flip;
			reloaded = false;
			return this;
		}

		/**
		 * Getting the sprite with the configuration.
		 * @return The generated sprite.
		 */
		public Sprite getSprite() {
			if (!reloaded) { // Reload if it requires to be reloaded.
				reloadSprite();
			}

			return sprite;
		}

		/**
		 * Requiring the sprite to be reloaded when the next time generated.
		 */
		public void reload() {
			reloaded = false;
		}

		/**
		 * Reloading the sprite with the configuration.
		 */
		private void reloadSprite() {
			MinicraftImage sheet = linkedMap.get(key);
			if (sheet != null) {
				if (w <= 0) w = sheet.width / 8; // Set the size as the maximum size of the sheet.
				if (h <= 0) h = sheet.height / 8; // Set the size as the maximum size of the sheet.

				boolean flipX = (0x01 & flip) > 0, flipY = (0x02 & flip) > 0;

				Sprite.Px[][] pixels = new Sprite.Px[h][w];
				for (int r = 0; r < h; r++) {
					for (int c = 0; c < w; c++) {
						// The offsets are there to determine the pixel that will be there: the one in order, or on the opposite side.
						int xOffset = flipX ? w - 1 - c : c;
						int yOffset = flipY ? h - 1 - r : r;
						pixels[r][c] = new Sprite.Px(x + xOffset, y + yOffset, mirror, sheet);
					}
				}

				sprite = new Sprite(pixels);
				sprite.color = color;
			} else {
				Logging.SPRITE.warn("SpriteSheet with resource ID not found: {}", key);
				sprite = missingTexture(spriteType).getSprite();
			}

			reloaded = true;
		}

		/**
		 * Unlink this LinkedSprite from SpriteLinker.
		 */
		@Override
		public void destroy() throws DestroyFailedException {
			Renderer.spriteLinker.linkedSheets.remove(this); // Unlink this instance.
			destoryed = true;
		}

		/**
		 * If this LinkedSprite is unlinked from SpriteLinker.
		 */
		@Override
		public boolean isDestroyed() {
			return destoryed;
		}
	}
}
