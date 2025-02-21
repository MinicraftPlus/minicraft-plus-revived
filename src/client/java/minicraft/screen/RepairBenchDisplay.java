package minicraft.screen;

import minicraft.core.io.InputHandler;
import minicraft.core.io.Localization;
import minicraft.entity.furniture.RepairBench;
import minicraft.entity.mob.Player;
import minicraft.gfx.Color;
import minicraft.gfx.Point;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.item.FishingRodItem;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.screen.entry.ListEntry;
import minicraft.screen.entry.SelectEntry;
import minicraft.screen.entry.SlotEntry;
import minicraft.util.MyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class RepairBenchDisplay extends Display {
	private final int DARKER_GRAY = Color.tint(Color.DARK_GRAY, -1, true);
	private static final int padding = 10;

	private final RepairBench repairBench;
	private final Player player;
	private final RepairBenchCarrier carrier;

	private final Menu.Builder repairBenchMenuBuilder = new Menu.Builder(true, 0, RelPos.LEFT)
		.setPositioning(new Point(9, 9), RelPos.BOTTOM_RIGHT)
		.setSelectable(true)
		.setTitle("Repair Bench");

	public RepairBenchDisplay(RepairBench repairBench, Player player) {
		menus = new Menu[] { new InventoryMenu(player, player.getInventory(), "minicraft.display.menus.inventory", RelPos.RIGHT, this::update) };
		this.repairBench = repairBench;
		this.player = player;
		carrier = new RepairBenchCarrier();
		menus = new Menu[] { repairBenchMenuBuilder.setEntries(getEntries()).createMenu(), menus[0] };
		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
	}

	private ArrayList<ListEntry> getEntries() {
		ArrayList<ListEntry> entries = new ArrayList<>();

		entries.add(carrier.inputSlot);
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

			boolean transferAll = input.getMappedKey("SHIFT").isDown() || !(item instanceof StackableItem) || ((StackableItem)item).count == 1;
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
		boolean transferAll = input.getMappedKey("SHIFT").isDown() || !(item instanceof StackableItem) || ((StackableItem) item).count == 1;
		if (item instanceof StackableItem) {
			Item toItem = item.copy();
			int move = 1;
			if (!transferAll) {
				((StackableItem)toItem).count = 1;
			} else {
				move = ((StackableItem) item).count;
			}

			if (player.getInventory().add(toItem) != null) {
				((StackableItem) item).count -= move - ((StackableItem) toItem).count;
			} else if (!transferAll) {
				((StackableItem) item).count--;
			} else {
				slot.setItem(null);
			}

			update();
		} else {
			if (player.getInventory().add(item) == null) {
				slot.setItem(null);
				update();
			}
		}
	}

	private void update() {
		// On (slot) update; refresh actionEntry and product (for productSlot)
		Item item; // Product slot should be empty for this to work.
		if ((item = carrier.inputSlot.getItem()) != null && carrier.productSlot.getItem() == null &&
			(item instanceof ToolItem || item instanceof FishingRodItem)) { // Only when the durability is not full
			StackableItem material;
			StackableItem inputMaterial = (StackableItem) carrier.materialSlot.getItem();
			if (item instanceof ToolItem) {
				ToolItem toolProduct = ((ToolItem) item).copy();
				material = (StackableItem) Items.get(toolProduct.type == ToolType.Shears ? "Iron" : ToolItem.LEVEL_NAMES[toolProduct.level]).copy();
				carrier.repairInfo = carrier.new ToolItemRepairInfo(toolProduct, material, ((ToolItem) item).dur < ((ToolItem) item).MAX_DUR && material.stacksWith(inputMaterial));
			} else { // item instanceof FishingRodItem
				FishingRodItem toolProduct = (FishingRodItem) item.copy();
				material = (StackableItem) Items.get(FishingRodItem.LEVEL_NAMES[toolProduct.level]).copy();
				carrier.repairInfo = carrier.new FishingRodItemRepairInfo(toolProduct, material, ((FishingRodItem) item).uses > 0 && material.stacksWith(inputMaterial));
			}
			if (inputMaterial != null && material.stacksWith(inputMaterial)) { // Match
				if (item instanceof ToolItem) { // Each raw material repairs 25% of the tool
					ToolItem tool = (ToolItem) carrier.repairInfo.product;
					material.count = MyUtils.clamp((int) Math.min(inputMaterial.count, Math.ceil((double) (tool.MAX_DUR - tool.dur) / tool.MAX_DUR * 4D)), 1, 4);
					tool.dur = Math.min(tool.dur + (int) (tool.MAX_DUR * (material.count / 4D)), tool.MAX_DUR);
				} else { // item instanceof FishingRodItem // Each raw material recovers (10 to 24) uses
					/* Source of the quadratic function
					 * Let L be the level of the fishing rod item.
					 * Let x be the no. of uses of the fishing rod item.
					 * Let h = 121 + L be the maximum (hard limit) no. of x.
					 * Let k = 24 be the upper bound of recovery of material (directly proportional to the no. of material).
					 * From a(x-h)^2+k, (a is coefficient (const.) of x^2 in the quadratic function)
					 *   =>	ax^2-2ahx+ah^2+k -- (1)
					 * To make it in form of ax^2+bx+c and c = 0 (to make a root of the function be 0 and starts from 0),
					 *   let k=-ah^2.
					 * Sub. k into (1),
					 *   ax^2-2ahx -- (2)
					 * From k=-ah^2, a=-k/h^2.
					 * Sub. a into (2),
					 *   -(k/h^2)x^2+2(k/h^2)hx
					 *   => -(k/h^2)x^2+2(k/h)x
					 * The lower bound of recovery of material is 10, then
					 *   recovery of material = max(-kx^2 / h^2 + 2 * kx / h, 10)
					 */
					FishingRodItem tool = (FishingRodItem) carrier.repairInfo.product;
					// Recovery of single material
					double recovery = Math.max(-24D * tool.uses*tool.uses / (121 + tool.level)*(121 + tool.level) + 2D * 24 * tool.uses / (121 + tool.level), 10);
					material.count = Math.min(inputMaterial.count, (int) Math.ceil(tool.uses / recovery));
					tool.uses = Math.max(tool.uses - (int) (material.count * recovery), 0);
				}
			}
			carrier.actionEntry.setSelectable(inputMaterial != null && material.stacksWith(inputMaterial) && inputMaterial.count >= material.count);
		} else {
			carrier.repairInfo = null;
			carrier.actionEntry.setSelectable(false);
		}

		// Generic menu update
		int oldSel = menus[0].getSelection();
		menus[0] = repairBenchMenuBuilder.setEntries(getEntries()).createMenu();
		menus[0].setSelection(oldSel);
		while (menus[0].getCurEntry() != null && !menus[0].getCurEntry().isSelectable()) {
			int sel = menus[0].getSelection();
			menus[0].setSelection(sel == 0 ? menus[0].getNumOptions() : sel - 1);
		}
		menus[1] = new InventoryMenu((InventoryMenu) menus[1]);
		menus[1].translate(menus[0].getBounds().getWidth() + padding, 0);
		onSelectionChange(0, selection);
	}

	private class RepairBenchCarrier {
		private final SlotEntry inputSlot;
		private final SlotEntry materialSlot; // Raw materials
		private final SlotEntry.TemporarySlotEntry productSlot;
		private final SelectEntry actionEntry;
		private @Nullable RepairInfo<?> repairInfo = null;
		private @Nullable RepairInfo<?> previousRepairInfo = null; // Used for productSlot

		public abstract class RepairInfo<T extends Item> {
			public final @NotNull T product;
			public final @NotNull StackableItem material;
			public final boolean repairable;

			public RepairInfo(@NotNull T product, @NotNull StackableItem material, boolean repairable) {
				this.product = product;
				this.material = material;
				this.repairable = repairable;
			}

			public @Nullable String getProductDisplayString() {
				return getItemDisplayString(product);
			}

			public abstract @Nullable String getItemDisplayString(@NotNull Item item);

			public int getProductDisplayColor(boolean isSelectable, boolean isSelected) {
				return getDisplayColor(product, isSelectable, isSelected);
			}
			public int getDisplayColor(@NotNull Item item, boolean isSelectable, boolean isSelected) {
				return isSelectable ? getDisplayColor(item, isSelected) : Color.tint(getDisplayColor(item, isSelected), -1, true);
			}
			public abstract int getDisplayColor(@NotNull Item item, boolean isSelected);
		}

		public class ToolItemRepairInfo extends RepairInfo<ToolItem> {
			public ToolItemRepairInfo(@NotNull ToolItem product, @NotNull StackableItem material, boolean repairable) {
				super(product, material, repairable);
			}

			@Override
			public @Nullable String getItemDisplayString(@NotNull Item item) {
				if (!(item instanceof ToolItem)) return null;
				int dur = ((ToolItem) item).dur * 100 / ((ToolItem) item).MAX_DUR;
				return " " + dur + "%" + item.getDisplayName();
			}

			@Override
			public int getDisplayColor(@NotNull Item item, boolean isSelected) {
				if (!(item instanceof ToolItem)) return 0;
				int dur = ((ToolItem) item).dur * 100 / ((ToolItem) item).MAX_DUR;
				int green = (int) (dur * 2.55f); // Let duration show as normal.
				int color = Color.get(1, 255 - green, green, 0);
				return isSelected ? color : Color.tint(color, -1, true);
			}
		}

		/* About fishing rod breaking chances (with prove)
		 * L ∈ { 0, 1, 2, 3 }
		 * C ∈ { x ∈ Z | 0 ≤ x ≤ 99 }
		 * n ∈ Z and n ≥ 0
		 * C > 120 - n + 6L
		 * 0 > 120 - n + 6L -- (1)
		 *   => 120 - n + 6L = -1
		 *   => n = 121 + 6L
		 * 99 < 120 - n + 6L -- (2)
		 *   => 120 - n + 6L = 98
		 *   => n = 22 + 6L
		 * When L = 0,
		 *   By (1), n = 121
		 *   By (2), n = 22
		 *   ∵ When n = 121, 100% chance breaking;
		 * 	   when n = 22, 1% chance breaking;
		 * 	   when 0 ≤ n ≤ 21, 0% chance breaking.
		 *   ∴ With this rule,
		 * 	   when n = 121 + 6L, 100% chance breaking;
		 * 	   when n = 22 + 6L, 1% chance breaking;
		 * 	   when 0 ≤ n ≤ 21 + 6L, 0% chance breaking.
		 * ∴ In conclusion,
		 * 	 when n > 21 + 6L, (n - 21 - 6L)% chance breaking;
		 * 	 else no chance breaking.
		 * Range of no chance breaking: 0 ≤ n ≤ 21 + 6L ((22 + 6L) times without any chance breaking)
		 * Range of having chance to break: 22 + 6L ≤ n ≤ 121 + 6L (100 values; incremental)
		 */

		public class FishingRodItemRepairInfo extends RepairInfo<FishingRodItem> {
			public FishingRodItemRepairInfo(@NotNull FishingRodItem product, @NotNull StackableItem material, boolean repairable) {
				super(product, material, repairable);
			}

			@Override
			public @Nullable String getItemDisplayString(@NotNull Item item) {
				if (!(item instanceof FishingRodItem)) return null;
				// dur >= 0: no chance to break; dur < 0: chance to break = dur%
				int uses = ((FishingRodItem) item).uses;
				int level = ((FishingRodItem) item).level;
				String dur;
				if (uses > 21 + level * 6) {
					dur = (uses - (21 + level * 6)) + "*";
				} else {
					dur = (int) (100 - (uses * 100D / (21 + level * 6))) + "%";
				}
				return " " + dur + item.getDisplayName();
			}

			@Override
			public int getDisplayColor(@NotNull Item item, boolean isSelected) {
				if (!(item instanceof FishingRodItem)) return 0;
				// dur >= 0: no chance to break; dur < 0: chance to break = -dur%
				int uses = ((FishingRodItem) item).uses;
				int level = ((FishingRodItem) item).level;
				int dur;
				if (uses > 21 + level * 6) {
					dur = -(uses - (21 + level * 6));
				} else {
					dur = (int) (100 - (uses * 100D / (21 + level * 6)));
				}
				dur += 50; // Shift as center
				int green = MyUtils.clamp((int) (dur * 1.7f), 0, 255); // Let duration show as normal.
				int color = Color.get(1, 255 - green, green, 0);
				return isSelected ? color : Color.tint(color, -1, true);
			}
		}

		public RepairBenchCarrier() {
			inputSlot = new SlotEntry(new SlotEntry.SlotEntryPlaceholder("Item to Repair"), RepairBenchDisplay.this::onSlotTransfer) {
				@Override
				public String toString() {
					Item item = getItem();
					if (repairInfo == null || item == null) return super.toString();
					return repairInfo.getItemDisplayString(item); // Should be not null
				}

				@Override
				public int getColor(boolean isSelected) {
					Item item = getItem();
					if (repairInfo == null || item == null) return super.getColor(isSelected);
					return repairInfo.getDisplayColor(item, isSelected); // Should be not 0
				}
			};
			materialSlot = new SlotEntry(new SlotEntry.SlotEntryPlaceholder("Raw Materials") {
				@Override
				public String getDisplayString() {
					if (repairInfo == null) return super.getDisplayString();
					return " " + Localization.getLocalized(repairInfo.material.getName());
				}

				@Override
				public int getDisplayColor(boolean isSelected) {
					return repairInfo == null ? (isSelected ? Color.tint(Color.GRAY, 1, true) : Color.DARK_GRAY) :
						super.getDisplayColor(isSelected);
				}

				@Override
				public @Nullable SpriteLinker.LinkedSprite getSprite() {
					return repairInfo == null ? null : repairInfo.material.sprite;
				}
			}, RepairBenchDisplay.this::onSlotTransfer);
			productSlot = new SlotEntry.TemporarySlotEntry(new SlotEntry.SlotEntryPlaceholder("Item Reparation") {
				@Override
				public String getDisplayString() {
					return repairInfo == null || !repairInfo.repairable ? super.getDisplayString() : repairInfo.getProductDisplayString();
				}

				@Override
				public int getDisplayColor(boolean isSelected) {
					return repairInfo == null || !repairInfo.repairable ? DARKER_GRAY : repairInfo.getProductDisplayColor(productSlot.isSelectable(), isSelected);
				}
			}, RepairBenchDisplay.this::onSlotTransfer) {
				@Override
				public String toString() {
					Item item = getItem();
					if (previousRepairInfo == null || item == null) return super.toString();
					return previousRepairInfo.getItemDisplayString(item); // Should be not null
				}

				@Override
				public int getColor(boolean isSelected) {
					Item item = getItem();
					if (previousRepairInfo == null || item == null) return super.getColor(isSelected);
					return previousRepairInfo.getDisplayColor(item, isSelected); // Should be not 0
				}
			};
			actionEntry = new SelectEntry("Repair item", this::onAction) {
				@Override
				public int getColor(boolean isSelected) {
					return isSelectable() ? super.getColor(isSelected) : Color.DARK_GRAY;
				}
			};
			actionEntry.setSelectable(false);
		}

		private void onAction() {
			Item tool = inputSlot.getItem();
			StackableItem material = (StackableItem) materialSlot.getItem();
			// Checking again for security
			if (tool != null && material != null && repairInfo != null && repairInfo.repairable &&
				material.count >= repairInfo.material.count) {
				inputSlot.setItem(null);
				material.count -= repairInfo.material.count;
				if (material.isDepleted()) materialSlot.setItem(null);
				productSlot.setItem(repairInfo.product);
				previousRepairInfo = repairInfo;
			}

			update(); // Refreshing again
		}

		/** @return number of items transferred */
		private int addToCarrier(Item item) {
			if (item instanceof ToolItem || item instanceof FishingRodItem) { // inputSlot
				if (inputSlot.getItem() == null) {
					inputSlot.setItem(item);
					return 1;
				}
			} else if (item instanceof StackableItem) { // materialSlot
				StackableItem stack = (StackableItem) materialSlot.getItem();
				if (stack != null && !stack.stacksWith(item)) return 0;
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

	@Override
	public void onExit() { // Throw all the temporarily stored items out the dark anvil.
		Item item;
		if ((item = carrier.inputSlot.getItem()) != null) repairBench.getLevel().dropItem(repairBench.x, repairBench.y, item);
		if ((item = carrier.materialSlot.getItem()) != null) repairBench.getLevel().dropItem(repairBench.x, repairBench.y, item);
		if ((item = carrier.productSlot.getItem()) != null) repairBench.getLevel().dropItem(repairBench.x, repairBench.y, item);
	}
}
