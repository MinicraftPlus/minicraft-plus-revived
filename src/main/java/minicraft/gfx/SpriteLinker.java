package minicraft.gfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
			default:
				break;
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
			AtomicInteger tmpWidth = new AtomicInteger(width); // Argument check. Might be pointless.
			AtomicInteger tmpHeight = new AtomicInteger(height);
			if (width % 8 != 0)
				CrashHandler.errorHandle(new IllegalArgumentException("Invalid width of SpriteSheet."), new CrashHandler.ErrorInfo(
						"Invalid SpriteSheet argument.", CrashHandler.ErrorInfo.ErrorType.HANDLED,
						String.format("Invalid width: {}, SpriteSheet width should be a multiple of 8.")
					), () -> tmpWidth.set(width % 8));
			if (height % 8 != 0)
					CrashHandler.errorHandle(new IllegalArgumentException("Invalid height of SpriteSheet."), new CrashHandler.ErrorInfo(
							"Invalid SpriteSheet argument.", CrashHandler.ErrorInfo.ErrorType.HANDLED,
							String.format("Invalid height: {}, SpriteSheet height should be a multiple of 8.")
						), () -> tmpHeight.set(height % 8));

			// Sets width and height to that of the image
			this.width = tmpWidth.get();
			this.height = tmpHeight.get();
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

	/** The sprite categories in the image resources. */
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
		private int x, y, w, h, color = -1, mirror = 0, number = 0;
		private int[][] mirrors = null;
		private boolean onepixel = false;

		// Sprite data.
		private HashMap<String, SpriteSheet> linkedMap;
		private MobSpriteType mobType = MobSpriteType.Animations;
		private SpriteType spriteType;
		private MobSprite[][] mobSprites;
		private Sprite sprite;
		private boolean destoryed; // It is not linked when destoryed.
		private boolean useMobSprites = false;
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
		 * @deprecated because of the standization. Setting the sprite position.
		 * @param x The x position of the sprite.
		 * @param y The y position of the sprite.
		 * @return The same instance.
		 */
		@Deprecated
		public LinkedSprite setSpritePos(int x, int y) {
			this.x = x;
			this.y = y;
			reloaded = false; // Reload this.
			return this;
		}
		/**
		 * @deprecated because of the standization. Setting the sprite position and size.
		 * @param x The x position of the sprite.
		 * @param y The y position of the sprite.
		 * @param w The sprite width.
		 * @param h The sprite height
		 * @return The same instance.
		 */
		@Deprecated
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
		 * @deprecated because it should not be handled like this. Setting the mirrors of each {@link Sprite.Px Px}.
		 * @param mirrors The mirrors for each {@link Sprite.Px Px}.
		 * @return The same instance.
		 */
		public LinkedSprite setMirrors(int[][] mirrors) {
			this.mirrors = mirrors;
			reloaded = false; // Reload this.
			return this;
		}
		/**
		 * @deprecated because of the standardization. {@code true} if all {@link Sprite.Px Px} are generated by the one pixel.
		 * @param onepixel Whether all {@link Sprite.Px Px} generated by the one pixel.
		 * @return The same instance.
		 */
		public LinkedSprite setOnePixel(boolean onepixel) {
			this.onepixel = onepixel;
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
				// TODO Standardization
				// sprite = mirrors == null ? new Sprite(x, y, w, h, sheet, mirror, onepixel) : new Sprite(x, y, w, h, sheet, onepixel, mirrors);
				// sprite.color = color;
				// mobSprites = null;
				// if (useMobSprites) {
				// 	switch (mobType) {
				// 		case Animations: mobSprites = MobSprite.compileMobSpriteAnimations(x, y, sheet); break;
				// 		case List: mobSprites = new MobSprite[][]{MobSprite.compileSpriteList(x, y, w, h, mirror, number, sheet)}; break;
				// 	}
				// }
			} else {
				Logging.SPRITE.warn("Sprite with resource ID not found: {}", key);
				sprite = Sprite.missingTexture(spriteType).getSprite();
			}

			reloaded = true;
		}

		// TODO Standardization
		private enum MobSpriteType { Animations, List }
		/**
		 * Setting the mobsprite as MobSprite with animation.
		 * @return The same instance.
		 */
		public LinkedSprite setMobSpriteAnimations() {
			mobType = MobSpriteType.Animations;
			reloaded = false; // Reload this.
			useMobSprites = true;
			return this;
		}
		/**
		 * Setting the mobsprite as MobSprite with list.
		 * @param number The horizontal number of list.
		 * @return The same instance.
		 */
		public LinkedSprite setSpriteList(int number) {
			mobType = MobSpriteType.List;
			this.number = number;
			reloaded = false; // Reload this.
			useMobSprites = true;
			return this;
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
