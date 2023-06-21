package minicraft.entity.furniture;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.item.BucketItem;
import minicraft.item.DyeItem;
import minicraft.item.Item;
import minicraft.item.Items;
import org.jetbrains.annotations.Nullable;

public class Cauldron extends Furniture {
	private static final SpriteLinker.LinkedSprite sprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "cauldron");
	private static final SpriteLinker.LinkedSprite spriteFilled = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "cauldron_filled");
	private static final SpriteLinker.LinkedSprite spriteColored = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "cauldron_colored");
	private static final SpriteLinker.LinkedSprite itemSprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "cauldron");
	private static final int BACKGROUND_COLOR = 0x424242; // Used for dynamical rendering with transparent colors

	public Cauldron() {
		super("Cauldron", sprite, itemSprite);
	}

	private boolean filled = false; // whether the cauldron is filled with water or not
	private int color = -1; // -1 if transparent, 24-bit RGB value otherwise

	@Override
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if (item != null) {
			if (item instanceof BucketItem) {
				if (item.getName().equals("Water Bucket")) {
					if (!filled) {
						filled = true;
						color = -1;
						return true;
					}
				} else if (item.getName().equals("Empty Bucket")) {
					if (color == -1 && filled) {
						player.activeItem = Items.get("Water Bucket").copy();
						return true;
					}
				}
			} else if (item instanceof DyeItem) {
				((DyeItem) item).count--;
				if (color == -1) { // Transparent; initialize color with original colors
					color = ((DyeItem) item).color.color;
				} else { // Already dyed
					color = combineColors(color, ((DyeItem) item).color.color);
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public void render(Screen screen) {
		if (!filled) screen.render(x-8, y-8, sprite);
		else if (color == -1) screen.render(x-8, y-8, spriteFilled);
		else screen.render(x-8, y-8, spriteColored.setColor(combineTransparentColor(0.8, color, BACKGROUND_COLOR)));
	}

	/**
	 * Combines 2 colors and calculates the resultant color by using the algorithm used in Minecraft.
	 * @param color0 a valid 24-bit RGB value
	 * @param color1 a valid 24-bit RGB value; might be a dye color
	 * @return the resultant 24-bit RGB value after the combination
	 */
	public static int combineColors(int color0, int color1) {
		int totalRed = (color0 >> 16) + (color1 >> 16);
		int totalGreen = ((color0 >> 8) & 0xFF) + ((color1 >> 8) & 0xFF);
		int totalBlue = (color0 & 0xFF) + (color1 & 0xFF);
		int totalMaximum = Math.max(Math.max(color0 >> 16, (color0 >> 8) & 0xFF), color0 & 0xFF)
			+ Math.max(Math.max(color1 >> 16, (color1 >> 8) & 0xFF), color1 & 0xFF);

		double averageRed = totalRed / 2.0;
		double averageGreen = totalGreen / 2.0;
		double averageBlue = totalBlue / 2.0;
		double averageMaximum = totalMaximum / 2.0;

		double maximumOfAverage = Math.max(Math.max(averageRed, averageGreen), averageBlue);
		double gainFactor = averageMaximum / maximumOfAverage;

		return ((int) (averageRed * gainFactor) << 16) + ((int) (averageGreen * gainFactor) << 8) + (int) (averageBlue * gainFactor);
	}

	/**
	 * Calculates the resultant color by a color with transparency covering on an opaque color.
	 * Reference: https://stackoverflow.com/a/3506190
	 * @param alpha alpha value [0-1] of {@code color0}
	 * @param color0 24-bit RGB value sticking with the alpha value
	 * @param color1 24-bit RGB value with opaque color
	 * @return the resultant 24-bit RGB value
	 */
	public static int combineTransparentColor(double alpha, int color0, int color1) {
		return ((int) (Math.ceil((color0 >> 16) * alpha) + Math.ceil((color1 >> 16) * (1 - alpha)))) +
			((int) (Math.ceil(((color0 >> 8) & 0xFF) * alpha) + Math.ceil(((color1 >> 8) & 0xFF) * (1 - alpha)))) +
			((int) (Math.ceil((color0 & 0xFF) * alpha) + Math.ceil((color1 & 0xFF) * (1 - alpha))));
	}
}
