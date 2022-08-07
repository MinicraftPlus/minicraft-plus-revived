package minicraft.screen;

import com.studiohartman.jamepad.ControllerButton;
import minicraft.core.Game;
import minicraft.core.io.ControllerHandler;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.entity.ItemHolder;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Font;
import minicraft.gfx.Screen;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;

public class PlayerInvDisplay extends Display {

	private static final int padding = 10;

	private final Player player;

	private final boolean creativeMode;
	private final Inventory creativeInv;

	public PlayerInvDisplay(Player player) {
		super(new InventoryMenu(player, player.getInventory(), "minicraft.display.menus.inventory"));

		creativeMode = Game.isMode("minicraft.settings.mode.creative");
		if (creativeMode) {
			creativeInv = Items.getCreativeModeInventory();
			menus = new Menu[] {
				menus[0],
				new InventoryMenu(player, creativeInv, "minicraft.displays.player_inv.container_title.items") {{
					super.creativeInv = true;
				}}
			};

			menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
			update();

			if(menus[0].getNumOptions() == 0) onSelectionChange(0, 1);
		} else creativeInv = null;

		this.player = player;
	}

	@Override
	public void tick(InputHandler input, ControllerHandler controlInput) {
		super.tick(input, controlInput);

		if(input.isClicked("menu", controlInput)) {
			Game.exitDisplay();
			return;
		}

		if (creativeMode) {
			Menu curMenu = menus[selection];
			int otherIdx = getOtherIdx();

			if (curMenu.getNumOptions() == 0) return;

			Inventory from, to;
			if (selection == 0) {
				if (input.isClicked("attack", controlInput) && menus[0].getNumOptions() > 0) {
					player.activeItem = player.getInventory().remove(menus[0].getSelection());
					Game.exitDisplay();
					return;
				}

				from = player.getInventory();
				to = creativeInv;

				int fromSel = curMenu.getSelection();
				Item fromItem = from.get(fromSel);

				boolean deleteAll;
				if (input.getKey("SHIFT-D").clicked) {
					deleteAll = true;
				} else if (input.getKey("D").clicked) {
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
				if (input.isClicked("attack", controlInput)) { // If stack limit is available, this can transfer whole stack
					transferAll = !(fromItem instanceof StackableItem) || ((StackableItem)fromItem).count == 1;
				} else return;

				Item toItem = fromItem.clone();

				if (!transferAll) {
					((StackableItem)toItem).count = 1;
				}

				to.add(toSel, toItem);
				update();
			}

		} else {
			if (input.isClicked("attack", controlInput) && menus[0].getNumOptions() > 0) {
				player.activeItem = player.getInventory().remove(menus[0].getSelection());
				Game.exitDisplay();
			}
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);

		// Searcher help text
		String text = Localization.getLocalized("minicraft.displays.player_inv.display.help", Game.input.getMapping("SEARCHER-BAR"));

		Font.draw(text, screen, 12, Screen.h/ 2 + 8, Color.WHITE);
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
			for(Menu m: menus)
				m.translate(shift, 0);
		}
	}

	private int getOtherIdx() { return (selection+1) % 2; }

	private void update() {
		menus[0] = new InventoryMenu((InventoryMenu) menus[0]);
		menus[1] = new InventoryMenu((InventoryMenu) menus[1]);
		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
		onSelectionChange(0, selection);
	}
}
