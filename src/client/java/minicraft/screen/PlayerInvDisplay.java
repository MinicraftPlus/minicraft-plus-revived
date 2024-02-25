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
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.StringEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerInvDisplay extends Display {

	private static final int padding = 10;

	private final Player player;

	private @Nullable List<ListEntry> itemDescription = null;

	private final boolean creativeMode;
	private final Inventory creativeInv;

	public PlayerInvDisplay(Player player) {
		super(new InventoryMenu(player, player.getInventory(), new Localization.LocalizationString(
			"minicraft.display.menus.inventory"), ItemListMenu.POS_LEFT));
		this.player = player;
		Menu.Builder descriptionMenuBuilder = new Menu.Builder(true, 3, RelPos.TOP_LEFT);
		creativeMode = Game.isMode("minicraft.displays.world_create.options.game_mode.creative");
		itemDescription = getDescription();
		if (itemDescription != null) descriptionMenuBuilder.setEntries(itemDescription);
		Menu descriptionMenu = descriptionMenuBuilder.setPositioning(new Point(padding, menus[0].getBounds().getBottom() + 8), RelPos.BOTTOM_RIGHT)
			.setSelectable(false)
			.createMenu();
		if (creativeMode) {
			creativeInv = Items.getCreativeModeInventory();
			menus = new Menu[]{
				menus[0],
				new InventoryMenu(player, creativeInv, new Localization.LocalizationString(
					"minicraft.displays.player_inv.container_title.items"), ItemListMenu.POS_RIGHT) {{
					creativeInv = true;
				}},
				descriptionMenu
			};

			onSelectionChange(1, 0);
		} else {
			creativeInv = null;
			menus = new Menu[]{menus[0], descriptionMenu};
		}

		onScreenKeyboardMenu = OnScreenKeyboardMenu.checkAndCreateMenu();
		if (onScreenKeyboardMenu != null)
			onScreenKeyboardMenu.setVisible(false);
	}

	@Nullable
	private List<ListEntry> getDescription() {
		if (selection == 0) {
			Inventory inv = player.getInventory();
			return inv.invSize() == 0 ? null : inv.get(menus[0].getSelection()).getDescription().toEntries();
		} else {
			return creativeInv.invSize() == 0 ? null :
				creativeInv.get(menus[1].getSelection()).getDescription().toEntries();
		}
	}

	OnScreenKeyboardMenu onScreenKeyboardMenu;

	@Override
	public void tick(InputHandler input) {
		boolean acted = false; // Checks if typing action is needed to be handled.
		boolean mainMethod = false;

		itemDescription = getDescription();
		if (itemDescription == null) menus[creativeMode ? 2 : 1].shouldRender = false;
		else {
			menus[creativeMode ? 2 : 1].shouldRender = true;
			menus[creativeMode ? 2 : 1].setEntries(itemDescription);
			menus[creativeMode ? 2 : 1].builder()
				.setMenuSize(null)
				.setDisplayLength(0)
				.recalculateFrame(); // This resizes menu
		}

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

			if (input.getMappedKey("menu").isClicked()) { // Should not listen button press.
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
					if (input.inputPressed("SELECT") && menus[0].getNumOptions() > 0) {
						player.activeItem = player.getInventory().remove(menus[0].getSelection());
						Game.exitDisplay();
						return;
					}

					from = player.getInventory();
					to = creativeInv;

					int fromSel = curMenu.getSelection();
					Item fromItem = from.get(fromSel);

					boolean deleteAll;
					if (input.getMappedKey("SHIFT-D").isClicked() || input.buttonPressed(ControllerButton.Y)) {
						deleteAll = true;
					} else if (input.getMappedKey("D").isClicked() || input.buttonPressed(ControllerButton.X)) {
						deleteAll = !(fromItem instanceof StackableItem) || ((StackableItem) fromItem).count == 1;
					} else return;

					if (deleteAll) {
						from.remove(fromSel);
					} else {
						((StackableItem) fromItem).count--; // this is known to be valid.
					}

					update();
					((InventoryMenu) curMenu).refresh();
				} else {
					from = creativeInv;
					to = player.getInventory();

					int toSel = menus[otherIdx].getSelection();
					int fromSel = curMenu.getSelection();

					Item fromItem = from.get(fromSel);

					boolean transferAll;
					if (input.inputPressed("SELECT")) { // If stack limit is available, this can transfer whole stack
						transferAll = !(fromItem instanceof StackableItem) || ((StackableItem) fromItem).count == 1;
					} else return;

					Item toItem = fromItem.copy();

					if (!transferAll) {
						((StackableItem) toItem).count = 1;
					}

					to.add(toSel, toItem);
					update();
					((InventoryMenu) menus[otherIdx]).refresh();
				}

			} else {
				if (input.inputPressed("SELECT") && menus[0].getNumOptions() > 0) {
					player.activeItem = player.getInventory().remove(menus[0].getSelection());
					Game.exitDisplay();
				}
			}
		}
	}

	@Override
	public void render(Screen screen) {
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

			if (oldSel == newSel)
				return; // this also serves as a protection against access to menus[0] when such may not exist.
			if (newSel == 0)
				menus[2].builder().setPositioning(new Point(padding, menus[0].getBounds().getBottom() + 8), RelPos.BOTTOM_RIGHT);
			if (newSel == 1)
				menus[2].builder().setPositioning(new Point(Screen.w - padding, menus[1].getBounds().getBottom() + 8), RelPos.BOTTOM_LEFT);
		}
	}

	private int getOtherIdx() {
		return selection ^ 1;
	}

	private void update() {
		onSelectionChange(0, selection);
		itemDescription = getDescription();
	}
}
