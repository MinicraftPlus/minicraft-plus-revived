package minicraft.entity.furniture;

import java.util.Random;
import minicraft.Game;
import minicraft.entity.Entity;
import minicraft.entity.Inventory;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.item.Items;
import minicraft.item.StackableItem;

public class DungeonChest extends Chest {
	private static int openCol = Color.get(-1, 2, 115, 225);
	private static int lockCol = Color.get(-1, 222, 333, 555);
	
	public Random random = new Random();
	public boolean isLocked;
	
	public DungeonChest() {
		super("Dungeon Chest", lockCol);
		populateInv();
		
		isLocked = true;
	}
	
	public boolean use(Player player) {
		if (isLocked) {
			boolean activeKey = player.activeItem != null && player.activeItem.matches(Items.get("Key"));
			boolean invKey = player.inventory.count(Items.get("key")) > 0;
			if(activeKey || invKey) { // if the player has a key...
				if (!Game.isMode("creative")) { // remove the key unless on creative mode.
					if (activeKey) { // remove activeItem
						StackableItem key = (StackableItem)player.activeItem;
						key.count--;
					} else { // remove from inv
						player.inventory.removeItem(Items.get("key"));
					}
				}
				
				isLocked = false;
				col = openCol; // set to the unlocked color
				
				level.add(new SmashParticle(x * 16, y * 16));
				level.add(new TextParticle("-1 key", x, y, Color.RED));
				level.chestcount--;
				if(level.chestcount == 0) { // if this was the last chest...
					level.dropItem(x, y, 5, Items.get("Gold Apple"));
					
					Game.notifyAll("You hear a noise from the surface!", -100); // notify the player of the developments
					// add a level 2 airwizard to the middle surface level.
					AirWizard wizard = new AirWizard(true);
					wizard.x = Game.levels[Game.lvlIdx(0)].w / 2;
					wizard.y = Game.levels[Game.lvlIdx(0)].h / 2;
					Game.levels[Game.lvlIdx(0)].add(wizard);
				}
				
				return super.use(player); // the player unlocked the chest.
			}
			
			return false; // the chest is locked, and the player has no key.
		}
		else return super.use(player); // the chest was already unlocked.
	}
	
	public void render(Screen screen) {
		sprite.color = col = isLocked?lockCol:openCol;
		super.render(screen);
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
		inv.tryAdd(6, Items.get("Gold Armor"));
		inv.tryAdd(5, Items.get("Iron Armor"), 2);
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
	
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "isLocked,"+isLocked;
		
		return updates;
	}
	
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		switch(field) {
			case "isLocked":
				isLocked = Boolean.parseBoolean(val);
				return true;
		}
		
		return false;
	}
}
