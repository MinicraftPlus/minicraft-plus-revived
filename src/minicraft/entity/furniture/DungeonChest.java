package minicraft.entity.furniture;

import java.util.Random;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.mob.AirWizard;
import minicraft.entity.mob.Player;
import minicraft.entity.particle.SmashParticle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.StackableItem;
import org.jetbrains.annotations.Nullable;

public class DungeonChest extends Chest {
	private static Sprite openSprite = new Sprite(14, 24, 2, 2, 2);
	private static Sprite lockSprite = new Sprite(12, 24, 2, 2, 2);
	
	public Random random = new Random();
	public boolean isLocked;
	
	/**
	 * Creates a custom chest with the name Dungeon Chest.
	 * @param populateInv
	 */
	public DungeonChest(boolean populateInv) {
		super("Dungeon Chest");
		this.sprite = lockSprite;

		if(populateInv)
			populateInv();
		
		isLocked = true;
	}
	
	public boolean use(Player player) {
		if (isLocked) {
			boolean activeKey = player.activeItem != null && player.activeItem.equals(Items.get("Key"));
			boolean invKey = player.getInventory().count(Items.get("key")) > 0;
			if(activeKey || invKey) { // if the player has a key...
				if (!Game.isMode("creative")) { // remove the key unless on creative mode.
					if (activeKey) { // remove activeItem
						StackableItem key = (StackableItem)player.activeItem;
						key.count--;
					} else { // remove from inv
						player.getInventory().removeItem(Items.get("key"));
					}
				}
				
				isLocked = false;
				this.sprite = openSprite; // set to the unlocked color
				
				level.add(new SmashParticle(x * 16, y * 16));
				level.add(new TextParticle("-1 key", x, y, Color.RED));
				level.chestCount--;
				if(level.chestCount == 0) { // if this was the last chest...
					level.dropItem(x, y, 5, Items.get("Gold Apple"));
					
					Updater.notifyAll("You hear a noise from the surface!", -100); // notify the player of the developments
					// add a level 2 airwizard to the middle surface level.
					AirWizard wizard = new AirWizard(true);
					wizard.x = World.levels[World.lvlIdx(0)].w / 2;
					wizard.y = World.levels[World.lvlIdx(0)].h / 2;
					World.levels[World.lvlIdx(0)].add(wizard);
				}
				
				return super.use(player); // the player unlocked the chest.
			}
			
			return false; // the chest is locked, and the player has no key.
		}
		else return super.use(player); // the chest was already unlocked.
	}
	
	/**
	 * Populate the inventory of the DungeonChest, psudo-randomly.
	 */
	private void populateInv() {
		Inventory inv = getInventory(); // Yes, I'm that lazy. ;P
		inv.clearInv(); // clear the inventory.
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
		
		if(inv.invSize() < 1) { // add this if none of the above was added.
			inv.add(Items.get("steak"), 6);
			inv.add(Items.get("Time Potion"));
			inv.add(Items.get("Gem Axe"));
		}
	}
	
	/** what happens if the player tries to push a Dungeon Chest. */
	@Override
	protected void touchedBy(Entity entity) {
		if(!isLocked) // can only be pushed if unlocked.
			super.touchedBy(entity);
	}

	@Override
	public boolean interact(Player player, @Nullable Item item, Direction attackDir) {
		if(!isLocked)
			return super.interact(player, item, attackDir);
		return false;
	}
	
	@Override
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "isLocked,"+isLocked;
		
		return updates;
	}
	
	@Override
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
