package minicraft.gfx;

import minicraft.core.Renderer;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteMeta;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.util.Logging;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

/**
 * This is not applicable for mob sprite animations. Only for generic sprite animations.
 */
public class SpriteAnimation implements Destroyable {
	private static final ArrayList<SpriteAnimation> spriteAnimations = new ArrayList<>();
	private static final HashMap<String, SpriteMeta> metas = new HashMap<>();

	public static void setMetadata(String key, SpriteMeta meta) {
		metas.put(key, meta);
	}

	public static void resetMetadata() {
		metas.clear();
	}

	public static SpriteMeta getMetadata(String key) {
		return metas.get(key);
	}

	/**
	 * Refreshing all currently registered animations.
	 */
	public static void refreshAnimations() {
		spriteAnimations.forEach(a -> a.refreshAnimation(metas.get(a.key)));
	}

	private LinkedSprite[] animations;
	private SpriteMeta metadata; // The metadata of the sprite sheet.
	private int frame = 0; // The current frame of the animation.
	private int frametick = 0; // The current tick of the current frame. It would be always 0 if no animation.
	private int lastTick = Updater.gameTime; // The last tick gained from the updater.
	private long lastMillis = System.currentTimeMillis(); // The last timestamp rendered.
	private boolean destoryed = false; // Whether this instance is still registered.

	// Border settings.
	private LinkedSprite border = null;
	private LinkedSprite corner = null;
	private BiFunction<Tile, Boolean, Boolean> connectChecker;
	private boolean singletonWithConnective = false;

	// Refreshing only data.
	private SpriteType type;
	private String key;

	/**
	 * Constructing animations with the provided key. The meta is given by default.
	 *
	 * @param type The sprite category.
	 * @param key  The sprite resource key.
	 */
	public SpriteAnimation(SpriteType type, String key) {
		this(metas.get(key), type, key);
	}

	/**
	 * Constructing animations with the provided metadata and key. It should already be validated.
	 *
	 * @param meta The metadata of the sprite sheet.
	 * @param type The sprite category.
	 * @param key  The sprite resource key.
	 */
	public SpriteAnimation(SpriteMeta meta, SpriteType type, String key) {
		this.type = type;
		this.key = key;
		refreshAnimation(meta);

		spriteAnimations.add(this);
	}

	/**
	 * Setting the tile class of this animation used for tile connector rendering.
	 *
	 * @param connectChecker The tile connection checker.
	 * @return The instance itself.
	 */
	public SpriteAnimation setConnectChecker(BiFunction<Tile, Boolean, Boolean> connectChecker) {
		this.connectChecker = connectChecker;
		return this;
	}

	/**
	 * Setting if the singleton sprite is used for other tile connective rendering.
	 *
	 * @param connective If used for connective rendering.
	 * @return The instance itself.
	 */
	public SpriteAnimation setSingletonWithConnective(boolean connective) {
		this.singletonWithConnective = connective;
		return this;
	}

	/**
	 * Setting the color of all animation frames.
	 *
	 * @param color The color of sprite.
	 * @return The instance itself.
	 */
	public SpriteAnimation setColor(int color) {
		for (LinkedSprite sprite : animations) {
			sprite.setColor(color);
		}

		return this;
	}

	/**
	 * Setting the color of the specific animation frame.
	 *
	 * @param frame The specific frame.
	 * @param color The color of sprite.
	 * @return The instance itself.
	 */
	public SpriteAnimation setColor(int frame, int color) {
		if (frame < 0) frame = 0;
		if (frame >= animations.length) frame = animations.length - 1;
		animations[frame].setColor(color);

		return this;
	}

	/**
	 * Setting the mirror of all animation frames.
	 *
	 * @param mirror The mirror of sprite/
	 * @return The instance itself.
	 */
	public SpriteAnimation setMirror(int mirror) {
		for (LinkedSprite sprite : animations) {
			sprite.setMirror(mirror);
		}

		return this;
	}

	/**
	 * Setting the mirror of the specific animation frame.
	 *
	 * @param frame  The specific frame.
	 * @param mirror The mirror of sprite.
	 * @return The instance itself.
	 */
	public SpriteAnimation setMirror(int frame, int mirror) {
		if (frame < 0) frame = 0;
		if (frame >= animations.length) frame = animations.length - 1;
		animations[frame].setMirror(mirror);

		return this;
	}

	/**
	 * Setting the sprite sheet mirror of all frames.
	 *
	 * @param mirror The mirror of sprite sheet.
	 * @return The instance itself.
	 */
	public SpriteAnimation setSpriteMirror(int mirror) {
		for (LinkedSprite sprite : animations) sprite.setFlip(mirror);
		return this;
	}

	/**
	 * Getting the current frame of animation.
	 *
	 * @return The current frame sprite.
	 */
	public LinkedSprite getCurrentFrame() {
		return animations[frame];
	}

	/**
	 * Getting the specific frame of animation.
	 *
	 * @param frame The specific frame.
	 * @return The frame sprite.
	 */
	public LinkedSprite getFrame(int frame) {
		return animations[frame];
	}

	/**
	 * Rendering the animation on the screen.
	 *
	 * @param screen The screen instance.
	 * @param level  The level for rendering.
	 * @param x      The x coordinate level tile.
	 * @param y      The y coordinate level tile.
	 */
	public void render(Screen screen, Level level, int x, int y) {
		// If border and the tile class is set.
		if (connectChecker != null && (border != null || corner != null)) {
			boolean u = connectChecker.apply(level.getTile(x, y - 1), true);
			boolean d = connectChecker.apply(level.getTile(x, y + 1), true);
			boolean l = connectChecker.apply(level.getTile(x - 1, y), true);
			boolean r = connectChecker.apply(level.getTile(x + 1, y), true);

			boolean ul = connectChecker.apply(level.getTile(x - 1, y - 1), false);
			boolean dl = connectChecker.apply(level.getTile(x - 1, y + 1), false);
			boolean ur = connectChecker.apply(level.getTile(x + 1, y - 1), false);
			boolean dr = connectChecker.apply(level.getTile(x + 1, y + 1), false);

			x = x << 4;
			y = y << 4;

			Sprite full = animations[frame].getSprite(); // Singleton; Must be 2*2.
			Sprite sparse = border != null ? border.getSprite() : null; // Border; Must be 3*3.
			Sprite sides = corner != null ? corner.getSprite() : null; // Corner; Must be 2*2.

			if (u && l) {
				int connectiveColor = singletonWithConnective ? full.color : sparse.color;
				Sprite.Px connective = singletonWithConnective ? full.spritePixels[1][1] : sparse.spritePixels[1][1];
				if (ul) screen.render(x, y, connective, connectiveColor);
				else if (sides == null) screen.render(x, y, full.spritePixels[1][1], full.color);
				else screen.render(x, y, sides.spritePixels[0][0], 3, sides.color);
			} else
				screen.render(x, y, sparse.spritePixels[u ? 1 : 0][l ? 1 : 0], sparse.color);

			if (u && r) {
				int connectiveColor = singletonWithConnective ? full.color : sparse.color;
				Sprite.Px connective = singletonWithConnective ? full.spritePixels[1][0] : sparse.spritePixels[1][1];
				if (ur) screen.render(x + 8, y, connective, connectiveColor);
				else if (sides == null) screen.render(x + 8, y, full.spritePixels[1][0], full.color);
				else screen.render(x + 8, y, sides.spritePixels[0][1], 3, sides.color);
			} else
				screen.render(x + 8, y, sparse.spritePixels[u ? 1 : 0][r ? 1 : 2], sparse.color);

			if (d && l) {
				int connectiveColor = singletonWithConnective ? full.color : sparse.color;
				Sprite.Px connective = singletonWithConnective ? full.spritePixels[0][1] : sparse.spritePixels[1][1];
				if (dl) screen.render(x, y + 8, connective, connectiveColor);
				else if (sides == null) screen.render(x, y + 8, full.spritePixels[0][1], full.color);
				else screen.render(x, y + 8, sides.spritePixels[1][0], 3, sides.color);
			} else
				screen.render(x, y + 8, sparse.spritePixels[d ? 1 : 2][l ? 1 : 0], sparse.color);

			if (d && r) {
				int connectiveColor = singletonWithConnective ? full.color : sparse.color;
				Sprite.Px connective = singletonWithConnective ? full.spritePixels[0][0] : sparse.spritePixels[1][1];
				if (dr) screen.render(x + 8, y + 8, connective, connectiveColor);
				else if (sides == null) screen.render(x + 8, y + 8, full.spritePixels[0][0], full.color);
				else screen.render(x + 8, y + 8, sides.spritePixels[1][1], 3, sides.color);
			} else
				screen.render(x + 8, y + 8, sparse.spritePixels[d ? 1 : 2][r ? 1 : 2], sparse.color);

		} else
			screen.render(x << 4, y << 4, animations[frame]);

		// If there is animation.
		if (animations.length > 1) {
			if (lastMillis < World.getLastWorldEnterTime()) { // Last time rendered is before this new world entered.
				lastTick = Updater.gameTime; // Reset game time. Depends on world.
				frame = 0;
				frametick = 0;
			}

			frametick += Updater.gameTime - lastTick;
			lastTick = Updater.gameTime;
			lastMillis = System.currentTimeMillis();

			// Checking frame increment.
			if (frametick >= metadata.frametime) {
				frame += frametick / metadata.frametime;
				frametick %= metadata.frametime;
				if (frame >= animations.length - 1)
					frame %= animations.length;
			}
		}
	}

	/**
	 * Refreshing the animation data for this instance.
	 */
	public void refreshAnimation(SpriteMeta metadata) {
		frame = 0;
		frametick = 0;
		this.metadata = metadata;
		MinicraftImage sheet = Renderer.spriteLinker.getSheet(type, key);
		if (sheet == null) {
			animations = new LinkedSprite[]{SpriteLinker.missingTexture(type)};
			border = null;
			corner = null;
			return;
		}

		int width = sheet.width / 8;

		// Destroying all previous LinkedSprite.
		try {
			if (animations != null) for (LinkedSprite sprite : animations) sprite.destroy();
			if (border != null) border.destroy();
			if (corner != null) corner.destroy();
		} catch (DestroyFailedException e) {
			Logging.SPRITE.trace(e);
		}

		if (metadata != null) {
			if (metadata.frames < 1) metadata.frames = 1;
			animations = new LinkedSprite[metadata.frames];
			for (int f = 0; f < animations.length; f++) {
				animations[f] = new LinkedSprite(type, key).setSpriteDim(0, f * width, width, width);
			}

			// Tile sprite only.
			if (metadata.border != null) border = new LinkedSprite(type, metadata.border);
			if (metadata.corner != null) corner = new LinkedSprite(type, metadata.corner);
		} else {
			animations = new LinkedSprite[]{new LinkedSprite(type, key).setSpriteSize(width, width)};
			border = null;
			corner = null;
		}
	}

	@Override
	public void destroy() throws DestroyFailedException {
		spriteAnimations.remove(this);
		if (animations != null) for (LinkedSprite sprite : animations) sprite.destroy();
		if (border != null) border.destroy();
		if (corner != null) corner.destroy();
		destoryed = true;
	}

	@Override
	public boolean isDestroyed() {
		return destoryed;
	}
}
