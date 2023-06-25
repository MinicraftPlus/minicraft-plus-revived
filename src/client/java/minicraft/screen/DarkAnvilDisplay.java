package minicraft.screen;

import minicraft.core.io.InputHandler;
import minicraft.entity.furniture.DarkAnvil;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.SlotEntry;
import minicraft.screen.entry.StringEntry;

import java.util.ArrayList;

public class DarkAnvilDisplay extends Display {
	private final int DARKER_GRAY = Color.tint(Color.DARK_GRAY, -1, true);
	private static final int padding = 10;

	private final Player player;
	private final DarkAnvil darkAnvil;
	private final DarkAnvilCarrier carrier;

	private final Menu.Builder darkAnvilMenuBuilder = new Menu.Builder(true, 0, RelPos.LEFT)
		.setPositioning(new Point(9, 9), RelPos.BOTTOM_RIGHT)
		.setSelectable(true)
		.setTitle("Dark Anvil");

	public DarkAnvilDisplay(DarkAnvil darkAnvil, Player player) {
		super(new InventoryMenu(player, player.getInventory(), "minicraft.display.menus.inventory", RelPos.RIGHT));
		this.player = player;
		this.darkAnvil = darkAnvil;
		carrier = new DarkAnvilCarrier();
		menus = new Menu[] { darkAnvilMenuBuilder.setEntries(getEntries()).createMenu(), menus[0] };
		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
	}

	private ArrayList<ListEntry> getEntries() {
		ArrayList<ListEntry> entries = new ArrayList<>();

		// The first space is reserved for item sprite.
		int energy = darkAnvil.getEnergy();
		SpriteLinker.LinkedSprite sprite = Items.get("Cloud Ore").sprite;
		entries.add(new StringEntry("  " + (energy / DarkAnvil.MAX_ENERGY) + "% (" + darkAnvil.getStore() + ")",
			energy > 0 ? Color.WHITE : Color.GRAY, false) {
			@Override
			public void render(Screen screen, int x, int y, boolean isSelected) {
				super.render(screen, x, y, isSelected);
				screen.render(x, y, sprite);
			}
		});

		entries.add(carrier.toolSlot);
		entries.add(carrier.materialSlot);
		entries.add(carrier.actionEntry);
		entries.add(carrier.productSlot);

		return entries;
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

	@Override
	public void tick(InputHandler input) {
		super.tick(input);
		// Focusing on player inventory
		if (selection == 1 && (input.inputPressed("attack"))) {
			if (menus[1].getEntries().length == 0) return;
			int sel = menus[1].getSelection();
			Inventory inv = player.getInventory();
			Item item = inv.get(sel);

			boolean transferAll = input.getKey("SHIFT").down || !(item instanceof StackableItem) || ((StackableItem)item).count == 1;
			Item toItem = item.copy();
			if (item instanceof StackableItem) {
				int move = 1;
				if (!transferAll) {
					((StackableItem) toItem).count = 1;
				} else {
					move = ((StackableItem) item).count;
				}

				int moved = carrier.addToCarrier(toItem);
				if (moved != 0) {
					if (moved < move) {
						((StackableItem) item).count -= moved;
					} else if (!transferAll) {
						((StackableItem) item).count--;
					} else {
						inv.remove(sel);
					}
					update();
				}
			} else {
				int moved = carrier.addToCarrier(toItem);
				if (moved == 1) {
					inv.remove(sel);
					update();
				}
			}
		}
	}

	private void onSlotTransfer(SlotEntry slot, InputHandler input) {
		Item item = slot.getItem();
		if (item == null) return;
		boolean transferAll = input.getKey("SHIFT").down || !(item instanceof StackableItem) || ((StackableItem) item).count == 1;
		if (item instanceof StackableItem) {
			Item toItem = item.copy();
			int move = 1;
			if (!transferAll) {
				((StackableItem)toItem).count = 1;
			} else {
				move = ((StackableItem) item).count;
			}

			int moved = player.getInventory().add(toItem);
			if (moved != 0) {
				if (moved < move) {
					((StackableItem) item).count -= moved;
				} else if (!transferAll) {
					((StackableItem) item).count--;
				} else {
					slot.setItem(null);
				}
				update();
			}
		} else {
			int moved = player.getInventory().add(item);
			if (moved == 1) {
				slot.setItem(null);
				update();
			}
		}
	}

	private void update() {
		// On (slot) update; refresh actionEntry and product (for productSlot)
		Item item; // Product slot should be empty for this to work.
		if ((item = carrier.toolSlot.getItem()) != null && carrier.productSlot.getItem() == null) {
			carrier.product = (ToolItem) item.copy();
			carrier.product.dur = carrier.product.MAX_DUR;
			StackableItem material;
			carrier.actionEntry.setSelectable((material = (StackableItem) carrier.materialSlot.getItem()) != null &&
				material.count >= carrier.product.level + 1);
		} else {
			carrier.product = null;
			carrier.actionEntry.setSelectable(false);
		}

		// Generic menu update
		menus[0] = darkAnvilMenuBuilder.setEntries(getEntries()).createMenu();
		menus[1] = new InventoryMenu((InventoryMenu) menus[1]);
		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
		onSelectionChange(0, selection);
	}

	// A temporary carrier of a dark anvil (for item slots)
	private class DarkAnvilCarrier {
		private final SlotEntry toolSlot;
		private final SlotEntry materialSlot; // Cloud Ore
		private final SlotEntry.TemporarySlotEntry productSlot;
		private final SelectEntry actionEntry;
		private ToolItem product = null;

		public DarkAnvilCarrier() {
			toolSlot = new SlotEntry(new SlotEntry.SlotEntryPlaceholder("Tool Item"), DarkAnvilDisplay.this::onSlotTransfer);
			materialSlot = new SlotEntry(new SlotEntry.SingletonItemSlotEntryPlaceholder(Items.get("Cloud Ore")), DarkAnvilDisplay.this::onSlotTransfer);
			productSlot = new SlotEntry.TemporarySlotEntry(new SlotEntry.SlotEntryPlaceholder("Repaired tool") {
				@Override
				public String getDisplayString() {
					return product == null ? super.getDisplayString() : product.getDisplayName();
				}

				@Override
				public int getDisplayColor(boolean isSelected) {
					return product == null ? DARKER_GRAY : super.getDisplayColor(isSelected);
				}
			}, DarkAnvilDisplay.this::onSlotTransfer);
			actionEntry = new SelectEntry("Repair tool", this::onAction) {
				@Override
				public int getColor(boolean isSelected) {
					return isSelectable() ? super.getColor(isSelected) : Color.DARK_GRAY;
				}
			};
			actionEntry.setSelectable(false);
		}

		private void onAction() {
			ToolItem tool = (ToolItem) toolSlot.getItem();
			StackableItem material = (StackableItem) materialSlot.getItem();
			// Checking again for security
			if (tool != null && material != null && material.count >= tool.level + 1) {
				toolSlot.setItem(null);
				material.count -= tool.level + 1;
				productSlot.setItem(product);
			}

			update(); // Refreshing again
		}

		/** @return number of items transferred */
		private int addToCarrier(Item item) {
			if (item instanceof ToolItem) { // toolSlot
				if (toolSlot.getItem() == null) {
					toolSlot.setItem(item);
					return 1;
				}
			} else if (item.getName().equalsIgnoreCase("Cloud Ore")) { // materialSlot
				StackableItem stack = (StackableItem) materialSlot.getItem();
				StackableItem toItem = (StackableItem) item.copy();
				if (stack == null) {
					toItem.count = Math.min(((StackableItem) item).count, toItem.maxCount);
					materialSlot.setItem(toItem);
					((StackableItem) item).count -= toItem.count;
					return toItem.count;
				} else if (stack.count < stack.maxCount) {
					int toAdd = Math.min(((StackableItem) item).count, stack.maxCount - stack.count);
					stack.count += toAdd;
					((StackableItem) item).count -= toAdd;
					return toAdd;
				}
			}

			return 0;
		}
	}
}
