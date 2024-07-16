package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.Renderer;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.entity.ItemHolder;
import minicraft.entity.furniture.Chest;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.MinicraftImage;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.StackableItem;

public class ContainerDisplay extends Display {

	private static final int padding = 10;

	private final MinicraftImage counterSheet =
		Renderer.spriteLinker.getSheet(SpriteLinker.SpriteType.Gui, "inventory_counter");

	private Player player;
	private Chest chest;

	public ContainerDisplay(Player player, Chest chest) {
		menus = new Menu[] {
			new InventoryMenu(player, player.getInventory(), "minicraft.display.menus.inventory", RelPos.RIGHT, this::update),
			new InventoryMenu(chest, chest.getInventory(), chest.name, RelPos.LEFT, this::update)
		};
		this.player = player;
		this.chest = chest;

		onScreenKeyboardMenu = OnScreenKeyboardMenu.checkAndCreateMenu();
		if (onScreenKeyboardMenu != null) {
			onScreenKeyboardMenu.setVisible(false);
		}

		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);

		if (menus[1].getNumOptions() == 0) onSelectionChange(1, 0);
	}

	OnScreenKeyboardMenu onScreenKeyboardMenu;

	@Override
	protected void onSelectionChange(int oldSel, int newSel) {
		super.onSelectionChange(oldSel, newSel);

		if (oldSel == newSel)
			return; // this also serves as a protection against access to menus[0] when such may not exist.

		int shift = 0;

		if (newSel == 0) shift = padding - menus[0].getBounds().getLeft();
		if (newSel == 1) shift = (Screen.w - padding) - menus[1].getBounds().getRight();

		for (Menu m : menus) {
			m.translate(shift, 0);
		}
	}

	private int getOtherIdx() {
		return selection ^ 1;
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
		if (onScreenKeyboardMenu != null) {
			onScreenKeyboardMenu.render(screen);
		}

		// It would be better if this could be made into InventoryMenu, but not possible at the moment.
		if (selection == 0) {
			// LHS is focused
			Rectangle boundsLeft = menus[0].getBounds();
			int sizeLeft = player.getInventory().invSize();
			int capLeft = player.getInventory().getMaxSlots();
			// Expanded counter
			if (sizeLeft < 10) { // At the moment at most just 2 digits and always 2 digits for capacity (no worry yet)
				// Background
				screen.render(boundsLeft.getRight() + 2 - (23 - 5), boundsLeft.getTop() - 3,
					12, 12, 3, 13, counterSheet);
				// Skips the middle part as that is for more digits
				screen.render(boundsLeft.getRight() + 2 - 15, boundsLeft.getTop() - 3,
					20, 12, 15, 13, counterSheet);

				// Digits
				renderCounterNumber(screen, boundsLeft.getRight() + 2 - 16, boundsLeft.getTop() - 1,
					5, 5, 7, sizeLeft, colorByHeaviness(calculateHeaviness(sizeLeft, capLeft), true));
				renderCounterNumber(screen, boundsLeft.getRight() + 2 - 10, boundsLeft.getTop() + 3,
					0, 4, 5, capLeft, Color.GRAY);
			} else {
				// Background
				screen.render(boundsLeft.getRight() + 2 - 23, boundsLeft.getTop() - 3,
					12, 12, 23, 13, counterSheet);

				// Digits
				renderCounterNumber(screen, boundsLeft.getRight() + 2 - 21, boundsLeft.getTop() - 1,
					5, 5, 7, sizeLeft, colorByHeaviness(calculateHeaviness(sizeLeft, capLeft), true));
				renderCounterNumber(screen, boundsLeft.getRight() + 2 - 10, boundsLeft.getTop() + 3,
					0, 4, 5, capLeft, Color.GRAY);
			}

			// RHS is not focused
			Rectangle boundsRight = menus[1].getBounds();
			int sizeRight = chest.getInventory().invSize();
			int capRight = chest.getInventory().getMaxSlots();
			// Minimized counter
			if (sizeRight < 10) { // no worry yet, really
				// Background
				screen.render(boundsRight.getLeft() + 4, boundsRight.getTop() - 1,
					0, 12, 4, 9, counterSheet);
				// Skips the middle part as that is for more digits
				screen.render(boundsRight.getLeft() + 8, boundsRight.getTop() - 1,
					8, 12, 4, 9, counterSheet);

				// Digits
				renderCounterNumber(screen, boundsRight.getLeft() + 4 + 2, boundsRight.getTop() + 1,
					0, 4, 5, sizeRight, fadeColor(colorByHeaviness(calculateHeaviness(sizeRight, capRight), false)));
			} else {
				// Background
				screen.render(boundsRight.getLeft() + 4, boundsRight.getTop() - 1,
					0, 12, 12, 9, counterSheet);

				// Digits
				renderCounterNumber(screen, boundsRight.getLeft() + 4 + 2, boundsRight.getTop() + 1,
					0, 4, 5, sizeRight, fadeColor(colorByHeaviness(calculateHeaviness(sizeRight, capRight), false)));
			}
		} else { // assert selection == 1
			// LHS is not focused
			Rectangle boundsLeft = menus[0].getBounds();
			int sizeLeft = player.getInventory().invSize();
			int capLeft = player.getInventory().getMaxSlots();
			// Minimized counter
			if (sizeLeft < 10) {
				// Background
				screen.render(boundsLeft.getRight() - 4 - 8, boundsLeft.getTop() - 1,
					0, 12, 4, 9, counterSheet);
				// Skips the middle part as that is for more digits
				screen.render(boundsLeft.getRight() - 4 - 4, boundsLeft.getTop() - 1,
					8, 12, 4, 9, counterSheet);

				// Digits
				renderCounterNumber(screen, boundsLeft.getRight() - 4 - 6, boundsLeft.getTop() + 1,
					0, 4, 5, sizeLeft, fadeColor(colorByHeaviness(calculateHeaviness(sizeLeft, capLeft), false)));
			} else {
				// Background
				screen.render(boundsLeft.getRight() - 4 - 12, boundsLeft.getTop() - 1,
					0, 12, 12, 9, counterSheet);

				// Digits
				renderCounterNumber(screen, boundsLeft.getRight() - 4 - 10, boundsLeft.getTop() + 1,
					0, 4, 5, sizeLeft, fadeColor(colorByHeaviness(calculateHeaviness(sizeLeft, capLeft), false)));
			}

			// RHS is focused
			Rectangle boundsRight = menus[1].getBounds();
			int sizeRight = chest.getInventory().invSize();
			int capRight = chest.getInventory().getMaxSlots();
			// Expanded counter (background horizontally mirrored)
			if (sizeRight < 10) {
				// Background
				screen.render(boundsRight.getLeft() - 2 + (20 - 5), boundsRight.getTop() - 3,
					12, 12, 3, 13, counterSheet, 1);
				// Skips the middle part as that is for more digits
				screen.render(boundsRight.getLeft() - 2, boundsRight.getTop() - 3,
					20, 12, 15, 13, counterSheet, 1);

				// Digits
				renderCounterNumber(screen, boundsRight.getLeft() - 2 + 11, boundsRight.getTop() - 1,
					5, 5, 7, sizeRight, colorByHeaviness(calculateHeaviness(sizeRight, capRight), true));
				renderCounterNumber(screen, boundsRight.getLeft(), boundsRight.getTop() + 3,
					0, 4, 5, capRight, Color.GRAY);
			} else {
				// Background
				screen.render(boundsRight.getLeft() - 2, boundsRight.getTop() - 3,
					12, 12, 23, 13, counterSheet, 1);

				// Digits
				renderCounterNumber(screen, boundsRight.getLeft() - 2 + 11, boundsRight.getTop() - 1,
					5, 5, 7, sizeRight, colorByHeaviness(calculateHeaviness(sizeRight, capRight), true));
				renderCounterNumber(screen, boundsRight.getLeft(), boundsRight.getTop() + 3,
					0, 4, 5, capRight, Color.GRAY);
			}
		}
	}

	// xp, yp - x, y target pixel; ys - y source pixel; w, h - w, h of a digit sprite; n - number to render
	private void renderCounterNumber(Screen screen, int xp, int yp, int ys, int w, int h, int n, int color) {
		String display = String.valueOf(n);
		for (int i = 0; i < display.length(); ++i) {
			screen.render(xp + i * w, yp, w * (display.charAt(i) - '0'), ys, w, h, counterSheet, 0, color);
		}
	}

	// Gives a percentage of heaviness according to size and capacity.
	// n <= cap && n >= 0 && cap > 0
	private float calculateHeaviness(int n, int cap) {
		// Formula: -(x - 1)^2 + 1 # always positive as (co-)domain=[0,1]
		float inner = (float) n / cap - 1;
		return constrainDecimal(-(inner * inner) + 1);
	}

	// Constrain any possible floating point calculation error causing potential issues
	private float constrainDecimal(float val) {
		if (val > 1) return 1;
		if (val < 0) return 0;
		return val;
	}

	// Main color means more vibrant color, i.e. with greenness; else with whiteness instead
	// Colors here are calculated as two phases as of the property of RGB space.
	private int colorByHeaviness(float val, boolean main) {
		if (main) {
			if (val < .5) {
				// From green (0) to yellow (.5) linearly
				return ((int) (val / .5F * 255) << 16) | 0x100FF00;
			} else {
				// From yellow (.5) to red (1) linearly
				return ((int) ((1 - val) / .5F * 255) << 8) | 0x1FF0000;
			}
		} else {
			if (val < .3) {
				// From white (0) to yellow (.3) linearly
				return (int) (val / .3F * 255) | 0x1FFFF00;
			} else {
				// From yellow (.3) to red (1) linearly
				return ((int) ((1 - val) / .7F * 255) << 8) | 0x1FF0000;
			}
		}
	}

	/**
	 * Fading required color on counter background. Counter background here is hardcoded as #2A3299 while
	 * fading alpha (opacity) of the color is hardcoded as 35%.
	 */
	private int fadeColor(int color) {
		/*
		 * Z = X + (Y - X) * P # Source: https://stackoverflow.com/a/12228643
		 * where Z: new color value, X: background, Y: overlay color value, P: overlay opacity
		 */
		final float P = .35F;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;

		// New color values
		int nr = 0x2A + (int) ((r - 0x2A) * P);
		int ng = 0x32 + (int) ((g - 0x32) * P);
		int nb = 0x99 + (int) ((b - 0x99) * P);
		return 0x1000000 | (nr << 16) | (ng << 8) | nb;
	}

	@Override
	public void tick(InputHandler input) {
		boolean acted = false; // Checks if typing action is needed to be handled.
		boolean mainMethod = false;

		Menu curMenu = menus[selection];
		int otherIdx = getOtherIdx();
		if (onScreenKeyboardMenu == null || !curMenu.isSearcherBarActive() && !onScreenKeyboardMenu.isVisible()) {
			super.tick(input);

			if (input.inputPressed("menu") || chest.isRemoved()) {
				Game.setDisplay(null);
				return;
			}

			curMenu = menus[selection];
			mainMethod = true;
		} else {
			try {
				onScreenKeyboardMenu.tick(input);
			} catch (OnScreenKeyboardMenu.OnScreenKeyboardMenuTickActionCompleted |
			         OnScreenKeyboardMenu.OnScreenKeyboardMenuBackspaceButtonActed e) {
				acted = true;
			}

			if (!acted)
				curMenu.tick(input);

			if (input.getMappedKey("menu").isClicked() || chest.isRemoved()) {
				Game.setDisplay(null);
				return;
			}

			if (curMenu.isSearcherBarActive()) {
				if (input.buttonPressed(ControllerButton.X)) { // Hide the keyboard.
					onScreenKeyboardMenu.setVisible(!onScreenKeyboardMenu.isVisible());
				}
			} else {
				onScreenKeyboardMenu.setVisible(false);
			}
		}

		if (mainMethod || !onScreenKeyboardMenu.isVisible())
			if (input.inputPressed("attack")) {
				if (curMenu.getEntries().length == 0) return;

				// switch inventories
				Inventory from, to;
				if (selection == 0) {
					from = player.getInventory();
					to = chest.getInventory();
				} else {
					from = chest.getInventory();
					to = player.getInventory();
				}

				int toSel = menus[otherIdx].getSelection();
				int fromSel = curMenu.getSelection();

				Item fromItem = from.get(fromSel);

				boolean transferAll = input.getMappedKey("shift").isDown() || !(fromItem instanceof StackableItem) || ((StackableItem) fromItem).count == 1;

				Item toItem = fromItem.copy();

				if (fromItem instanceof StackableItem) {
					int move = 1;
					if (!transferAll) {
						((StackableItem) toItem).count = 1;
					} else {
						move = ((StackableItem) toItem).count;
					}

					if (to.add(toItem) != null) {
						((StackableItem)fromItem).count -= move - ((StackableItem) toItem).count;
					} else if (!transferAll) {
						((StackableItem) fromItem).count--;
					} else {
						from.remove(fromSel);
					}
					update();
				} else {
					if (to.add(toItem) == null) {
						from.remove(fromSel);
						update();
					}
				}
			}
	}

	/** @deprecated This method is no longer in use by the removal of multiplayer system.
	 * Also, the game is paused when the display is shown, so it is not possible for the player to pickup items during this period. */
	@Deprecated
	public void onInvUpdate(ItemHolder holder) {
		if (holder == player || holder == chest) {
			update();
		}
	}

	private void update() {
		menus[0] = new InventoryMenu((InventoryMenu) menus[0]);
		menus[1] = new InventoryMenu((InventoryMenu) menus[1]);
		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
		onSelectionChange(0, selection);
	}
}
