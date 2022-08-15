package minicraft.gfx;

import java.util.ArrayList;
import java.util.Objects;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import minicraft.core.Renderer;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteMeta;
import minicraft.gfx.SpriteLinker.SpriteSheet;
import minicraft.gfx.SpriteLinker.SpriteType;

/** This is not applicable for mob sprite animations. Only for generic sprite animations. */
public class SpriteAnimation implements Destroyable {
	private static final ArrayList<SpriteAnimation> spriteAnimations = new ArrayList<>();

	public static void refreshAnimations() {
		spriteAnimations.forEach(a -> a.refreshAnimation());
	}

	private LinkedSprite[] animations;
	private SpriteMeta metadata; // The metadata of the sprite sheet.
	private int frame = 0; // The current frame of the animation.
	private int frametick = 0; // The current tick of the current frame. It would be always 0 if no animation.
	private boolean destoryed = false; // Whether this instance is still registered.

	/**
	 * Constructing animations with the provided metadata and key. It should already be validated.
	 * @param meta The metadata of the sprite sheet.
	 * @param type The sprite category.
	 * @param key The sprite resource key.
	 */
	public SpriteAnimation(SpriteMeta meta, SpriteType type, String key) {
		this.metadata = meta;
		SpriteSheet sheet = Objects.requireNonNull(Renderer.spriteLinker.getSheet(type, key), "sprite " + type + ": " + key);
		int width = sheet.width;
		if (meta.frames < 1) meta.frames = 1;
		animations = new LinkedSprite[meta.frames];
		for (int f = 0; f < animations.length; f++) {
			animations[f] = new LinkedSprite(type, key).setSpriteDim(0, f * (width / 8), width, width);
		}

		spriteAnimations.add(this);
	}

	/**
	 * Setting the color of all animation frames.
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
	 * @param frame The specific frame.
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
	 * Getting the current frame of animation.
	 * @return The current frame sprite.
	 */
	public LinkedSprite getCurrentFrame() {
		return animations[frame];
	}

	/**
	 * Getting the specific frame of animation.
	 * @param frame The specific frame.
	 * @return The frame sprite.
	 */
	public LinkedSprite getFrame(int frame) {
		return animations[frame];
	}

	/**
	 * Rendering the animation on the screen.
	 * @param screen The screen instance.
	 * @param xp The x pixel.
	 * @param yp The y pixel.
	 */
	public void render(Screen screen, int xp, int yp) {
		screen.render(xp, yp, animations[frame]);
		frametick++;

		// Checking frame increment.
		if (frametick == metadata.frametime) {
			frametick = 0;
			if (frame == animations.length - 1)
				frame = 0;
			else
				frame++;
		}
	}

	public void refreshAnimation() {
		// TODO
	}

	@Override
	public void destroy() throws DestroyFailedException {
		spriteAnimations.remove(this);
		destoryed = true;
	}

	@Override
	public boolean isDestroyed() {
		return destoryed;
	}
}
