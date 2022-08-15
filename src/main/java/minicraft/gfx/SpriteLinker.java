package minicraft.gfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.awt.image.BufferedImage;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import minicraft.core.CrashHandler;
import minicraft.core.Renderer;
import minicraft.util.Logging;

public class SpriteLinker {
	/** Buffering SpriteSheet for caching. */
	private final HashMap<String, SpriteSheet> entitySheets = new HashMap<>(),
	guiSheets = new HashMap<>(), itemSheets = new HashMap<>(), tileSheets = new HashMap<>();

	/** Storing all exist in-used LinkedSprite. */
	private final ArrayList<LinkedSprite> linkedSheets = new ArrayList<>();

	static Random ran = new Random();

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
	public void setSprite(SpriteType t, String key, SpriteSheet spriteSheet) {
		switch (t) {
			case Entity: entitySheets.put(key, spriteSheet); break;
			case Gui: guiSheets.put(key, spriteSheet); break;
			case Item: itemSheets.put(key, spriteSheet); break;
			case Tile: tileSheets.put(key, spriteSheet); break;
		}
	}

	public SpriteSheet getSheet(SpriteType t, String key) {
		switch (t) {
			case Entity: return entitySheets.get(key);
			case Gui: return guiSheets.get(key);
			case Item: return itemSheets.get(key);
			case Tile: return tileSheets.get(key);
		}

		return null;
	}

	/**
	 * Getting the missing texture texture with the specific sprite type.
	 * @param type The sprite category.
	 * @return The missing texture or null if invalid sprite type.
	 */
	public static LinkedSprite missingTexture(SpriteType type) {
		switch (type) {
			case Entity: return new LinkedSprite(SpriteType.Entity, "missing_entity");
			case Item: return new LinkedSprite(SpriteType.Item, "missing_item");
			case Tile: return new LinkedSprite(SpriteType.Tile, "missing_tile");
			default: return null;
		}
	}

	/**
	 * Getting the sheet of missing texture with the specific sprite type.
	 * @param type The sprite category.
	 * @return Ths missing texture sprite sheet or null if invalid sprite type.
	 */
	public SpriteSheet missingSheet(SpriteType type) {
		switch (type) { // The sheets should be found.
			case Entity: return entitySheets.get("missing_entity");
			case Item: return itemSheets.get("missing_item");
			case Tile: return tileSheets.get("missing_tile");
			default: return null;
		}
	}

	/** Updating all existing LinkedSheet for resource pack application. */
	public void updateLinkedSheets() {
		linkedSheets.forEach(s -> s.reload());
	}

	/**
	 * Although we have SpriteLinker, we still need SpriteSheet for buffering.
	 * As BufferedImage is heavy. Our current rendering system still depends on this array.
	 */
	public static class SpriteSheet {
		/** Each sprite tile size. */
		public static final int boxWidth = 8;

		public final int width, height; // Width and height of the sprite sheet
		public final int[] pixels; // Integer array of the image's pixels

		/**
		 * Default with maximum size of image.
		 * @param image The image to be added.
		 */
		public SpriteSheet(BufferedImage image) { this(image, image.getWidth(), image.getHeight()); }
		/**
		 * Custom size.
		 * @param image The image to be added.
		 * @param width The width of the {@link SpriteSheet} to be applied to the {@link LinkedSprite}.
		 * @param height The height of the {@link SpriteSheet} to be applied to the {@link LinkedSprite}.
		*/
		public SpriteSheet(BufferedImage image, int width, int height) {
			if (width % 8 != 0)
				CrashHandler.errorHandle(new IllegalArgumentException("Invalid width of SpriteSheet."), new CrashHandler.ErrorInfo(
					"Invalid SpriteSheet argument.", CrashHandler.ErrorInfo.ErrorType.HANDLED,
					String.format("Invalid width: {}, SpriteSheet width should be a multiple of 8.")
				));
			if (height % 8 != 0)
				CrashHandler.errorHandle(new IllegalArgumentException("Invalid height of SpriteSheet."), new CrashHandler.ErrorInfo(
					"Invalid SpriteSheet argument.", CrashHandler.ErrorInfo.ErrorType.HANDLED,
					String.format("Invalid height: {}, SpriteSheet height should be a multiple of 8.")
				));

			// Sets width and height to that of the image
			this.width = width - width % 8;
			this.height = height - height % 8;
			pixels = image.getRGB(0, 0, width, height, null, 0, width); // Gets the color array of the image pixels

			// Applying the RGB array into Minicraft rendering system 25 bits RBG array.
			for (int i = 0; i < pixels.length; i++) { // Loops through all the pixels
				int red;
				int green;
				int blue;

				// This should be a number from 0-255 that is the red of the pixel
				red = (pixels[i] & 0xff0000);

				// Same, but green
				green = (pixels[i] & 0xff00);

				// Same, but blue
				blue = (pixels[i] & 0xff);

				// This stuff is to figure out if the pixel is transparent or not
				int transparent = 1;

				// A value of 0 means transparent, a value of 1 means opaque
				if (pixels[i] >> 24 == 0x00) {
					transparent = 0;
				}

				// Actually put the data in the array
				// Uses 25 bits to store everything (8 for red, 8 for green, 8 for blue, and 1 for alpha)
				pixels[i] = (transparent << 24) + red + green + blue;
			}
		}
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
			case Entity: sheet.linkedMap = Renderer.spriteLinker.entitySheets; break;
			case Gui: sheet.linkedMap = Renderer.spriteLinker.guiSheets; break;
			case Item: sheet.linkedMap = Renderer.spriteLinker.itemSheets; break;
			case Tile: sheet.linkedMap = Renderer.spriteLinker.tileSheets; break;
		}
	}

	/** A sprite collector with resource collector. */
	public static class LinkedSprite implements Destroyable {
		private final String key; // The resource key.

		/** The sprite configuration. */
		private int x, y, w, h, color = -1, mirror = 0;

		// Sprite data.
		private HashMap<String, SpriteSheet> linkedMap;
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
		public SpriteSheet getSheet() {
			return linkedMap.get(key);
		}

		/**
		 * Setting the sprite size.
		 * @param w The sprite width.
		 * @param h The sprite height
		 * @return The same instance.
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
		 * @return The same instance.
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
		 * @return The same instance.
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
		 * @return The same instance.
		 */
		public LinkedSprite setColor(int color) {
			this.color = color;
			reloaded = false; // Reload this.
			return this;
		}
		/**
		 * Setting the mirror of the sprite.
		 * @param mirror The mirror of the sprite.
		 * @return The same instance.
		 */
		public LinkedSprite setMirror(int mirror) {
			this.mirror = mirror;
			reloaded = false; // Reload this.
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

		/** Requiring the sprite to be reloaded when the next time generated. */
		public void reload() { reloaded = false; }
		/** Reloading the sprite with the configuration. */
		private void reloadSprite() {
			SpriteSheet sheet = linkedMap.get(key);
			if (sheet != null) {
				if (w <= 0) w = sheet.width / 8; // Set the size as the maximum size of the sheet.
				if (h <= 0) h = sheet.height / 8; // Set the size as the maximum size of the sheet.

				Sprite.Px[][] pixels = new Sprite.Px[h][w];
				for (int r = 0; r < h; r++) {
					for (int c = 0; c < w; c++) {
						pixels[r][c] = new Sprite.Px(x + c, y + r, mirror, sheet);
					}
				}

				sprite = new Sprite(pixels);
				sprite.color = color;
			} else {
				Logging.SPRITE.warn("Sprite with resource ID not found: {}", key);
				sprite = missingTexture(spriteType).getSprite();
			}

			reloaded = true;
		}

		/** Unlink this LinkedSprite from SpriteLinker. */
		@Override
		public void destroy() throws DestroyFailedException {
			Renderer.spriteLinker.linkedSheets.remove(this); // Unlink this instance.
			destoryed = true;
		}
		/** If this LinkedSprite is unlinked from SpriteLinker. */
		@Override
		public boolean isDestroyed() {
			return destoryed;
		}
	}
}
