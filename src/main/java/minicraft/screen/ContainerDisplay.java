package minicraft.screen;

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
		super(new InventoryMenu(chest, chest.getInventory(), chest.name), new InventoryMenu(player, player.getInventory(), "minicraft.display.menus.inventory"));
		//pInv = player.getInventory();
		//cInv = chest.getInventory();
		this.player = player;
		this.chest = chest;

		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);

		if(menus[0].getNumOptions() == 0) onSelectionChange(0, 1);
	}

	@Override
	protected void onSelectionChange(int oldSel, int newSel) {
		super.onSelectionChange(oldSel, newSel);
		if(oldSel == newSel) return; // this also serves as a protection against access to menus[0] when such may not exist.
		int shift = 0;
		if(newSel == 0) shift = padding - menus[0].getBounds().getLeft();
		if(newSel == 1) shift = (Screen.w - padding) - menus[1].getBounds().getRight();
		for(Menu m: menus)
			m.translate(shift, 0);
	}

	private int getOtherIdx() { return (selection+1) % 2; }

	@Override
	public void tick(InputHandler input) {
		super.tick(input);

		if(input.isClicked("menu") || chest.isRemoved()) {
			Game.setDisplay(null);
			return;
		}

		Menu curMenu = menus[selection];
		int otherIdx = getOtherIdx();

		if((input.isClicked("attack")) || input.getKey("shift-enter").clicked) {
			if (curMenu.getEntries().length == 0) return;
			// switch inventories
			Inventory from, to;
			if(selection == 0) {
				from = chest.getInventory();
				to = player.getInventory();
			} else {
				from = player.getInventory();
				to = chest.getInventory();
			}

			int toSel = menus[otherIdx].getSelection();
			int fromSel = curMenu.getSelection();

			Item fromItem = from.get(fromSel);

			boolean transferAll = input.getKey("shift-enter").clicked || !(fromItem instanceof StackableItem) || ((StackableItem)fromItem).count == 1;

			Item toItem = fromItem.clone();

			if (fromItem instanceof StackableItem) {
				int move = 1;
				if (!transferAll) {
					((StackableItem)toItem).count = 1;
				} else {
					move = ((StackableItem)fromItem).count;
				}

				int moved = to.add(toSel, toItem);
				if (moved < move) {
					((StackableItem)fromItem).count -= moved;
				} else if (!transferAll) {
					((StackableItem)fromItem).count--;
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
		if(holder == player || holder == chest)
			update();
	}

	private void update() {
		menus[0] = new InventoryMenu((InventoryMenu) menus[0]);
		menus[1] = new InventoryMenu((InventoryMenu) menus[1]);
		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
		onSelectionChange(0, selection);
	}
}
