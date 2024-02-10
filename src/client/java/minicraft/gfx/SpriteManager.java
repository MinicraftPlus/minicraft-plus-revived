package minicraft.gfx;

import minicraft.core.Renderer;
import minicraft.util.Logging;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import java.util.ArrayList;
import java.util.HashMap;

public class SpriteManager {
	/** Buffering SpriteSheet for caching. */
	private final HashMap<String, MinicraftImage> entitySheets = new HashMap<>(),
	guiSheets = new HashMap<>(), itemSheets = new HashMap<>(), tileSheets = new HashMap<>();

	/** Storing all exist in-used LinkedSprite. */
	private final ArrayList<SpriteLink> linkedSheets = new ArrayList<>();

	/** Clearing all Sprite buffers for the upcoming resource pack application. */
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
			case Entity: entitySheets.put(key, spriteSheet); break;
			case Gui: guiSheets.put(key, spriteSheet); break;
			case Item: itemSheets.put(key, spriteSheet); break;
			case Tile: tileSheets.put(key, spriteSheet); break;
		}
	}

	/**
	 * Getting the sprite sheet with the category and key.
	 * @param spriteType The sprite category
	 * @param key The resource key.
	 * @return The sprite sheet. <code>null</code> if not found.
	 */
	public MinicraftImage getSheet(SpriteType spriteType, String key) {
		switch (spriteType) {
			case Entity: return entitySheets.get(key);
			case Gui: return guiSheets.get(key);
			case Item: return itemSheets.get(key);
			case Tile: return tileSheets.get(key);
		}

		return null;
	}

	/** Clearing all skin sheets in entity sheets. */
	public void clearSkins() {
		entitySheets.keySet().removeIf(k -> k.startsWith("skin."));
	}

	/**
	 * Setting the skin in entity sheet.
	 * @param key The key of the sheet.
	 * @param spriteSheet The sheet to be added.
	 */
	public void setSkin(String key, MinicraftImage spriteSheet) {
		setSprite(SpriteType.Entity, key, spriteSheet);
	}

	public void refreshSkins() {
		linkedSheets.forEach(s -> {
			if (s.key.startsWith("skin."))
				s.reloadSprite();
		});
	}

	private static final String MISSING_ENTITY_KEY = "missing_entity", MISSING_ITEM_KEY = "missing_item", MISSING_TILE_KEY = "missing_tile";

	/**
	 * Getting the missing texture with the specific sprite category.
	 * @param type The sprite category.
	 * @return The missing texture or null if invalid sprite category.
	 */
	public static SpriteLink missingTexture(SpriteType type) {
		switch (type) {
			case Entity: return SpriteLink.MISSING_ENTITY_TEXTURE;
			case Item: return SpriteLink.MISSING_ITEM_TEXTURE;
			case Tile: return SpriteLink.MISSING_TILE_TEXTURE;
			default: return null;
		}
	}

	/**
	 * Getting the sheet of missing texture with the specific sprite type.
	 * @param type The sprite category.
	 * @return Ths missing texture sprite sheet or null if invalid sprite type.
	 */
	public MinicraftImage missingSheet(SpriteType type) {
		switch (type) { // The sheets should be found.
			case Entity: return entitySheets.get(MISSING_ENTITY_KEY);
			case Item: return itemSheets.get(MISSING_ITEM_KEY);
			case Tile: return tileSheets.get(MISSING_TILE_KEY);
			default: return null;
		}
	}

	/** Updating all existing LinkedSheet for resource pack application. */
	public void updateLinkedSheets() {
		Logging.SPRITE.debug("Updating all LinkedSprite.");
		linkedSheets.forEach(SpriteLink::reloadSprite);
	}

	/** The metadata of the sprite sheet. */
	public static class SpriteMeta {
		/** The sprite animation configuration. */
		public int frames = 1, // Minimum with 1.
		frametime = 0; // 0 if no animation.
		/** The sprite connector configuration. */
		public String border = null, corner = null;
	}

	/** The sprite categories in the image resources. TODO Removed for the new save system */
	public enum SpriteType {
		Item, Gui, Tile, Entity // Only for resource packs; Skin is not applied.
	}

	public HashMap<String, MinicraftImage> getMappingByType(SpriteType type) {
		switch (type) {
			case Entity: return entitySheets;
			default: case Gui: return guiSheets;
			case Item: return itemSheets;
			case Tile: return tileSheets;
		}
	}

	/** A sprite collector with resource collector. */
	public static class SpriteLink implements Destroyable {
		/** Textures for missing textures for entity, item or tile */
		public static final SpriteLink
			MISSING_ENTITY_TEXTURE = new SpriteLink.SpriteLinkBuilder(SpriteType.Entity, MISSING_ENTITY_KEY).createSpriteLink(true),
			MISSING_ITEM_TEXTURE = new SpriteLink.SpriteLinkBuilder(SpriteType.Item, MISSING_ITEM_KEY).createSpriteLink(true),
			MISSING_TILE_TEXTURE = new SpriteLink.SpriteLinkBuilder(SpriteType.Tile, MISSING_TILE_KEY).createSpriteLink(true);

		private final String key; // The resource key.
		private final SpriteType spriteType;
		private final HashMap<String, MinicraftImage> linkedMap;

		/** Sprite configurations. */
		private final int x, y, w, h, mirror, flip;
		private int color; // Modifiable due to the property of Sprite#color
		private final boolean immutable; // Whether the color field is immutable.

		// Sprite data.
		private Sprite sprite = null;
		private boolean destroyed = false; // It is not linked when destroyed.

		private SpriteLink(SpriteType spriteType, String key, HashMap<String, MinicraftImage> linkedMap,
		                   int x, int y, int w, int h, int color, int mirror, int flip, boolean immutable) {
			this.key = key;
			this.spriteType = spriteType;
			this.linkedMap = linkedMap;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.color = color;
			this.mirror = mirror;
			this.flip = flip;
			this.immutable = immutable;
			reloadSprite(true); // First attempt to load a sprite. A failure (missing resources) would be expected before resource initialization.
		}

		/**
		 * Getting the sprite sheet of the linked sprite.
		 * @return The current linked sprite.
		 */
		public MinicraftImage getSheet() {
			return linkedMap.get(key);
		}

		/**
		 * Setting the white tint. The value is unchanged if the object is set as immutable.
		 * @param color The color of the white tint.
		 * @return The instance itself.
		 */
		public SpriteLink setColor(int color) {
			if (immutable) {
				this.color = color;
				if (sprite != null) sprite.color = color;
			}
			return this;
		}

		/**
		 * Getting the sprite with the configuration.
		 * @return The generated sprite.
		 */
		public Sprite getSprite() {
			return sprite;
		}

		/** Reloading the sprite with the configuration. */
		private void reloadSprite() { reloadSprite(false); }
		private void reloadSprite(boolean onInitialize) {
			MinicraftImage sheet = linkedMap.get(key);
			if (sheet != null) {
				int h = this.h == 0 ? sheet.height/8 : this.h;
				int w = this.w == 0 ? sheet.width/8 : this.w;
				if ((x + w) * 8 <= sheet.width && (y + h) * 8 <= sheet.height) {
					boolean flipX = (0x01 & flip) > 0, flipY = (0x02 & flip) > 0;
					Sprite.Px[][] pixels = new Sprite.Px[h][w];
					for (int r = 0; r < h; r++) {
						for (int c = 0; c < w; c++) {
							// The offsets are there to determine the pixel that will be there: the one in order, or on the opposite side.
							int xOffset = flipX ? w-1 - c : c;
							int yOffset = flipY ? h-1 - r : r;
							pixels[r][c] = new Sprite.Px(x + xOffset, y + yOffset, mirror, sheet);
						}
					}

					sprite = new Sprite(pixels);
					sprite.color = color;
				} else {
					Logging.SPRITE.warn("SpriteSheet or required sprite with resource ID is invalid: {}", key);
					Logging.SPRITE.debug("Invalid sprite: x: {}; y: {}; w: {}, h: {}", x, y, w, h);
					sprite = missingTexture(spriteType).getSprite();
				}
			} else if (!onInitialize) {
				Logging.SPRITE.warn("SpriteSheet with resource ID not found: {}", key);
				sprite = missingTexture(spriteType).getSprite();
			}
		}

		/** Unlink this LinkedSprite from SpriteLinker. */
		@Override
		public void destroy() throws DestroyFailedException {
			Renderer.spriteManager.linkedSheets.remove(this); // Unlink this instance.
			destroyed = true;
		}
		/** If this {@link SpriteLink} is unlinked from {@link SpriteManager}. */
		@Override
		public boolean isDestroyed() {
			return destroyed;
		}

		public static class SpriteLinkBuilder {
			private final String key; // The resource key.
			private final SpriteType spriteType;

			private int x, y, w, h, color = -1, mirror = 0, flip = 0;

			/**
			 * Create new {@link SpriteLinkBuilder} for the creation of {@link SpriteLink} with the specific category and resource key.
			 * @param spriteType The category of the sprite.
			 * @param key The resource key of the sprite.
			 */
			public SpriteLinkBuilder(SpriteType spriteType, String key) {
				this.key = key;
				this.spriteType = spriteType;
			}

			/**
			 * Setting the sprite size.
			 * @param w The sprite width.
			 * @param h The sprite height
			 * @return The instance itself.
			 */
			public SpriteLinkBuilder setSpriteSize(int w, int h) {
				this.w = w;
				this.h = h;
				return this;
			}

			/**
			 * Setting the sprite position.
			 * @param x The x position of the sprite.
			 * @param y The y position of the sprite.
			 * @return The instance itself.
			 */
			public SpriteLinkBuilder setSpritePos(int x, int y) {
				this.x = x;
				this.y = y;
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
			public SpriteLinkBuilder setSpriteDim(int x, int y, int w, int h) {
				setSpriteSize(w, h);
				setSpritePos(x, y);
				return this;
			}

			/**
			 * Setting the white tint.
			 * @param color The color of the white tint.
			 * @return The instance itself.
			 */
			public SpriteLinkBuilder setColor(int color) {
				this.color = color;
				return this;
			}

			/**
			 * Setting the mirror of the sprite.
			 * @param mirror The mirror of the sprite.
			 * @return The instance itself.
			 */
			public SpriteLinkBuilder setMirror(int mirror) {
				this.mirror = mirror;
				return this;
			}

			/**
			 * Setting the flip of the sprite sheet.
			 * @param flip The mirror of the sprite sheet.
			 * @return The instance itself.
			 */
			public SpriteLinkBuilder setFlip(int flip) {
				this.flip = flip;
				return this;
			}

			public SpriteLink createSpriteLink() { return createSpriteLink(true); }
			public SpriteLink createSpriteLink(boolean immutable) {
				HashMap<String, MinicraftImage> linkedMap = Renderer.spriteManager.getMappingByType(spriteType);
				SpriteLink sprite = new SpriteLink(spriteType, key, linkedMap, x, y, w, h, color, mirror, flip, immutable);
				Renderer.spriteManager.linkedSheets.add(sprite);
				return sprite;
			}
		}
	}
}
