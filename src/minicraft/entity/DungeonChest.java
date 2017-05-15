package minicraft.entity;

import java.util.Random;
import minicraft.Game;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.item.Items;
import minicraft.screen.ModeMenu;

public class DungeonChest extends Chest {
	private static int openCol = Color.get(-1, 2, 115, 225);
	private static int lockCol = Color.get(-1, 222, 333, 555);
	
	public Random random = new Random();
	public boolean isLocked;
	
	public DungeonChest() {
		super("Dungeon Chest", lockCol);
		populateInv();
		
		isLocked = true;
		//sprite = 1;
		/*col = Color.get();
		col0 = Color.get(-1, 111, 222, 444);
		col1 = Color.get(-1, 222, 333, 555);
		col2 = Color.get(-1, 111, 222, 444);
		col3 = Color.get(-1, 0, 111, 333);
		*/
		//col = lockCol;
	}
	
	public boolean use(Player player, int attackDir) {
		if (isLocked) {
			boolean activeKey = player.activeItem != null && player.activeItem.matches(Items.get("Key"));
			boolean invKey = player.inventory.count(Items.get("key")) > 0;
			if(activeKey || invKey) { // if the player has a key...
				if (!ModeMenu.creative) { // remove the key unless on creative mode.
					if (activeKey) { // remove activeItem
						StackableItem key = (StackableItem)player.activeItem;
						key.count--;
					} else { // remove from inv
						player.inventory.removeItem(Items.get("key"));
					}
				}
				
				isLocked = false;
				col = openCol; // set to the unlocked color
				
				level.add(new SmashParticle(x * 16 + 8, y * 16 + 8));
				level.add(new TextParticle("-1 key", x, y, Color.get(-1, 500)));
				level.chestcount--;
				if(level.chestcount == 0) { // if this was the last chest...
					level.dropItem(x, y, 5, Items.get("Gold Apple"));
					/*for(int i = 0; i < 5; i++) { // add 5 golden apples to the level
						level.add(new ItemEntity(), x, y);
					}*/
					
					Game.notifications.add("You hear a noise from the surface!"); // notify the player of the developments
					// add a level 2 airwizard to the middle surface level.
					AirWizard wizard = new AirWizard(true);
					wizard.x = Game.levels[3].w / 2;
					wizard.y = Game.levels[3].h / 2;
					Game.levels[3].add(wizard);
				}
				
				return super.use(player, attackDir); // the player unlocked the chest.
			}
			
			return false; // the chest is locked, and the player has no key.
		}
		else return super.use(player, attackDir); // the chest was already unlocked.
	}
	
	/** Populate the inventory of the DungeonChest, psudo-randomly. */
	private void populateInv() {
		inventory.clearInv(); // clear the inventory.
		Inventory inv = inventory; // Yes, I'm that lazy. ;P
		inv.tryAdd(5, Items.get("steak"), 6);
		inv.tryAdd(5, Items.get("cooked pork"), 6);
		inv.tryAdd(4, Items.get("Wood"), 20);
		inv.tryAdd(4, Items.get("Wool"), 12);
		inv.tryAdd(2, Items.get("coal"), 4);
		inv.tryAdd(5, Items.get("gem"), 7);
		inv.tryAdd(5, Items.get("gem"), 8);
		inv.tryAdd(8, Items.get("Gem Armor"));
		inv.tryAdd(6, Items.get("g.armor"));
		inv.tryAdd(5, Items.get("i.armor"), 2);
		inv.tryAdd(3, Items.get("potion"), 10);
		inv.tryAdd(4, Items.get("speed potion"), 2);
		inv.tryAdd(6, Items.get("speed potion"), 5);
		inv.tryAdd(3, Items.get("light potion"), 2);
		inv.tryAdd(4, Items.get("light potion"), 3);
		inv.tryAdd(7, Items.get("regen potion"));
		inv.tryAdd(7, Items.get("energy potion"));
		inv.tryAdd(14, Items.get("time potion"));
		inv.tryAdd(14, Items.get("shield potion"));
		inv.tryAdd(7, Items.get("lava potion"));
		inv.tryAdd(5, Items.get("haste potion"), 3);
		
		inv.tryAdd(6, Items.get("Gold Bow"));
		inv.tryAdd(7, Items.get("Gem Bow"));
		inv.tryAdd(4, Items.get("Gold Sword"));
		inv.tryAdd(7, Items.get("Gem Sword"));
		inv.tryAdd(4, Items.get("Rock Claymore"));
		inv.tryAdd(6, Items.get("Iron Claymore"));
		
		if(inventory.invSize() < 1) { // add this if none of the above was added.
			inventory.add(Items.get("steak"), 6);
			inventory.add(Items.get("Time Potion"));
			inventory.add(Items.get("Gem Axe"));
		}
	}
	
	/** what happens if the player tries to push a Dungeon Chest. */
	protected void touchedBy(Entity entity) {
		if(!isLocked) // can only be pushed if unlocked.
			super.touchedBy(entity);
	}
	
	/** what happens if the player tries to grab a Dungeon Chest. */
	public void take(Player player) {
		if(!isLocked) // can only be taken if unlocked.
			super.take(player);
	}
}
