package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import minicraft.screen.entry.StringEntry;

public class PlayerInvDisplay extends Display {

	private static final int padding = 10;

	private final Player player;

	private String itemDescription = "";
	private Menu.Builder descriptionMenuBuilder;

	private final boolean creativeMode;
	private final Inventory creativeInv;

	public PlayerInvDisplay(Player player) {
		super(new InventoryMenu(player, player.getInventory(), "minicraft.display.menus.inventory", RelPos.LEFT));
		this.player = player;
		descriptionMenuBuilder = new Menu.Builder(true, 3, RelPos.TOP_LEFT);
		creativeMode = Game.isMode("minicraft.settings.mode.creative");
		itemDescription = getDescription();
		Menu descriptionMenu = descriptionMenuBuilder.setPositioning(new Point(padding, menus[0].getBounds().getBottom() + 8), RelPos.BOTTOM_RIGHT)
			.setEntries(StringEntry.useLines(Color.WHITE, false, itemDescription.split("\n")))
			.setSelectable(false)
			.createMenu();
		if (creativeMode) {
			creativeInv = Items.getCreativeModeInventory();
			menus = new Menu[] {
				menus[0],
				new InventoryMenu(player, creativeInv, "minicraft.displays.player_inv.container_title.items", RelPos.RIGHT) {{
					creativeInv = true;
				}},
				descriptionMenu
			};

			menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
			update();

			if(menus[0].getNumOptions() == 0) onSelectionChange(0, 1);
		} else {
			creativeInv = null;
			menus = new Menu[] { menus[0], descriptionMenu };
		}

		onScreenKeyboardMenu = OnScreenKeyboardMenu.checkAndCreateMenu();
		if (onScreenKeyboardMenu != null)
			onScreenKeyboardMenu.setVisible(false);
	}

	private String getDescription() {
		if (selection == 0) {
			Inventory inv = player.getInventory();
			return inv.invSize() == 0 ? "" : inv.get(menus[0].getSelection()).getDescription();
		} else {
			return creativeInv.invSize() == 0 ? "" : creativeInv.get(menus[1].getSelection()).getDescription();
		}
	}

	OnScreenKeyboardMenu onScreenKeyboardMenu;

	@Override
	public void tick(InputHandler input) {
		boolean acted = false; // Checks if typing action is needed to be handled.
		boolean mainMethod = false;

		itemDescription = getDescription();
		Menu curMenu = menus[selection];
		if (onScreenKeyboardMenu == null || !curMenu.isSearcherBarActive() && !onScreenKeyboardMenu.isVisible()) {
			super.tick(input);

			if (input.inputPressed("menu")) {
				Game.exitDisplay();
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

			if (input.getKey("menu").clicked) { // Should not listen button press.
				Game.exitDisplay();
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

		if (mainMethod || !onScreenKeyboardMenu.isVisible()) {
			if (creativeMode) {
				int otherIdx = getOtherIdx();

				if (curMenu.getNumOptions() == 0) return;

				Inventory from, to;
				if (selection == 0) {
					if (input.inputPressed("attack") && menus[0].getNumOptions() > 0) {
						player.activeItem = player.getInventory().remove(menus[0].getSelection());
						Game.exitDisplay();
						return;
					}

					from = player.getInventory();
					to = creativeInv;

					int fromSel = curMenu.getSelection();
					Item fromItem = from.get(fromSel);

					boolean deleteAll;
					if (input.getKey("SHIFT-D").clicked || input.buttonPressed(ControllerButton.Y)) {
						deleteAll = true;
					} else if (input.getKey("D").clicked || input.buttonPressed(ControllerButton.X)) {
						deleteAll = !(fromItem instanceof StackableItem) || ((StackableItem)fromItem).count == 1;
					} else return;

					if (deleteAll) {
						from.remove(fromSel);
					} else {
						((StackableItem)fromItem).count--; // this is known to be valid.
					}

					update();

				} else {
					from = creativeInv;
					to = player.getInventory();

					int toSel = menus[otherIdx].getSelection();
					int fromSel = curMenu.getSelection();

					Item fromItem = from.get(fromSel);

					boolean transferAll;
					if (input.inputPressed("attack")) { // If stack limit is available, this can transfer whole stack
						transferAll = !(fromItem instanceof StackableItem) || ((StackableItem)fromItem).count == 1;
					} else return;

					Item toItem = fromItem.copy();

					if (!transferAll) {
						((StackableItem)toItem).count = 1;
					}

					to.add(toSel, toItem);
					update();
				}

			} else {
				if (input.inputPressed("attack") && menus[0].getNumOptions() > 0) {
					player.activeItem = player.getInventory().remove(menus[0].getSelection());
					Game.exitDisplay();
				}
			}
		}
	}

	@Override
	public void render(Screen screen) {
		if (itemDescription.isEmpty()) menus[creativeMode ? 2 : 1].shouldRender = false;
		else {
			menus[creativeMode ? 2 : 1] = descriptionMenuBuilder.setEntries(StringEntry.useLines(Color.WHITE, itemDescription.split("\n")))
				.createMenu(); // This resizes menu
		}

		super.render(screen);

		// Searcher help text
		String text = Localization.getLocalized("minicraft.displays.player_inv.display.help", Game.input.getMapping("SEARCHER-BAR"));
		Font.draw(text, screen, selection == 0 ? 12 : Screen.w - 12 - Font.textWidth(text), menus[creativeMode ? 2 : 1].getBounds().getBottom() + 8, Color.WHITE);

		if (onScreenKeyboardMenu != null)
			onScreenKeyboardMenu.render(screen);
	}

	@Override
	protected void onSelectionChange(int oldSel, int newSel) {
		super.onSelectionChange(oldSel, newSel);
		if (creativeMode) {
			// Hide Items Inventory when not selecting it.
			if (selection == 0) menus[1].shouldRender = false;
			else menus[1].shouldRender = true;

			if(oldSel == newSel) return; // this also serves as a protection against access to menus[0] when such may not exist.
			int shift = 0;
			if(newSel == 0) shift = padding - menus[0].getBounds().getLeft();
			if(newSel == 1) shift = (Screen.w - padding) - menus[1].getBounds().getRight();
			menus[0].translate(shift, 0);
			menus[1].translate(shift, 0);
			if (newSel == 0) descriptionMenuBuilder.setPositioning(new Point(padding, menus[0].getBounds().getBottom() + 8), RelPos.BOTTOM_RIGHT);
			if (newSel == 1) descriptionMenuBuilder.setPositioning(new Point(Screen.w - padding, menus[1].getBounds().getBottom() + 8), RelPos.BOTTOM_LEFT);
		}
	}

	private int getOtherIdx() { return (selection+1) % 2; }

	private void update() {
		menus[0] = new InventoryMenu((InventoryMenu) menus[0]);
		menus[1] = new InventoryMenu((InventoryMenu) menus[1]) {{
			creativeInv = true;
		}};
		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
		onSelectionChange(0, selection);
		itemDescription = getDescription();
	}
}
