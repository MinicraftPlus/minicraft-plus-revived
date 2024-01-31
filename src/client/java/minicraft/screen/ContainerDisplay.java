package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.io.InputHandler;
import minicraft.entity.ItemHolder;
import minicraft.entity.furniture.Chest;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.StackableItem;

public class ContainerDisplay extends Display {

	private static final int padding = 10;

	private Player player;
	private Chest chest;

	public ContainerDisplay(Player player, Chest chest) {
		super(
			new InventoryMenu(player, player.getInventory(), "minicraft.display.menus.inventory", RelPos.LEFT),
			new InventoryMenu(chest, chest.getInventory(), chest.name, RelPos.RIGHT)
		);

		//pInv = player.getInventory();
		//cInv = chest.getInventory();
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
						move = ((StackableItem) fromItem).count;
					}

					int moved = to.add(toSel, toItem);
					if (moved < move) {
						((StackableItem) fromItem).count -= moved;
					} else if (!transferAll) {
						((StackableItem) fromItem).count--;
					} else {
						from.remove(fromSel);
					}
					update();
				} else {
					int moved = to.add(toSel, toItem);
					if (moved == 1) {
						from.remove(fromSel);
						update();
					}
				}
			}
	}

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
