package minicraft.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import minicraft.Game;
import minicraft.InputHandler;
import minicraft.Sound;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.MobSprite;
import minicraft.gfx.Screen;
import minicraft.item.ArmorItem;
import minicraft.item.FurnitureItem;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.PowerGloveItem;
import minicraft.item.Recipes;
import minicraft.item.StackableItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.saveload.Save;
import minicraft.screen.CraftingMenu;
import minicraft.screen.LoadingMenu;
import minicraft.screen.ModeMenu;
import minicraft.screen.OptionsMenu;
import minicraft.screen.PauseMenu;
import minicraft.screen.PlayerInfoMenu;
import minicraft.screen.PlayerInvMenu;
import minicraft.screen.WorldSelectMenu;

public class Player extends Mob {
	protected InputHandler input;
	public Game game;
	
	public static final int playerHurtTime = 30;
	
	public double moveSpeed = 1; // the number of coordinate squares to move; each tile is 16x16.
	public int score; // the player's score
	
	//These 2 ints are ints saved from the first spawn - this way the spawn pos is always saved.
	public int spawnx = 0, spawny = 0; // these are stored as tile coordinates, not entity coordinates.
	//public boolean bedSpawn = false;
	
	public boolean hasSetHome = false, skinon;
	public int homeSetX, homeSetY;
	
	public static int maxHealth = 10, maxStamina = 10, maxHunger = 10, maxArmor = 100; // the maximum stats that the player can have.
	
	public static MobSprite[][] sprites =  MobSprite.compileMobSpriteAnimations(0, 14);
	public static MobSprite[][] carrySprites = MobSprite.compileMobSpriteAnimations(0, 16); // the sprites while carrying something.
	public static MobSprite[][] suitSprites = MobSprite.compileMobSpriteAnimations(18, 20); // the "airwizard suit" sprites.
	public static MobSprite[][] carrySuitSprites = MobSprite.compileMobSpriteAnimations(18, 22); // the "airwizard suit" sprites.
	
	public Inventory inventory;
	public Item activeItem, attackItem; // attackItem is useful again b/c of the power glove.
	private Item prevItem; // holds the item held before using the POW glove.
	public int attackTime, attackDir;
	
	private int onStairDelay; // the delay before changing levels.
	
	public int hunger, stamina, armor; // the current stats
	public int armorDamageBuffer;
	public ArmorItem curArmor; // the color of the armor to be displayed.
	
	public int staminaRecharge; // the ticks before charging a bolt of the player's stamina
	private static int maxStaminaRecharge = 10; // cutoff value for staminaRecharge
	public int staminaRechargeDelay; // the recharge delay ticks when the player uses up their stamina.
	
	public int hungerStamCnt, stamHungerTicks; // tiers of hunger penalties before losing a burger.
	private static final int[] maxHungerStams = {10, 7, 5}; // hungerStamCnt required to lose a burger.
	private static final int maxHungerTicks = 300; // the cutoff value for stamHungerTicks
	private int stepCount; // used to penalize hunger for movement.
	private int hungerChargeDelay; // the delay between each time the hunger bar increases your health
	private int hungerStarveDelay; // the delay between each time the hunger bar decreases your health
	
	public HashMap<PotionType, Integer> potioneffects; // the potion effects currently applied to the player
	public boolean showpotioneffects; // whether to display the current potion effects on screen
	int cooldowninfo; // prevents you from toggling the info pane on and off super fast.
	int regentick; // counts time between each time the regen potion effect heals you.
	
	int acs = 25; // default ("start") arrow count
	//public int ac; // arrow count
	public int shirtColor = 110; // player shirt color.
	
	// Note: the player's health & max health are inherited from Mob.java
	
	public Player(Player previousInstance, Game game, InputHandler input) {
		super(sprites, Player.maxHealth);
		
		x = 24;
		y = 24;
		this.game = game;
		this.input = input;
		inventory = new Inventory();
		
		//ac = acs;
		if(previousInstance == null)
			inventory.add(Items.get("arrow"), acs);
		
		potioneffects = new HashMap<PotionType, Integer>();
		showpotioneffects = true;
		
		cooldowninfo = 0;
		regentick = 0;
		shirtColor = 110;
		
		armor = 0;
		curArmor = null;
		armorDamageBuffer = 0;
		stamina = maxStamina;
		hunger = maxHunger;
		
		hungerStamCnt = maxHungerStams[OptionsMenu.diff];
		stamHungerTicks = maxHungerTicks;
		
		if (ModeMenu.creative) {
			Items.fillCreativeInv(inventory);
		}/* else {
			inventory.add(new PowerGloveItem()));
		}*/
		
		if(previousInstance != null) {
			spawnx = previousInstance.spawnx;
			spawny = previousInstance.spawny;
		}
	}
	
	public void addPotionEffect(PotionType type, int duration) {
		potioneffects.put(type, duration);
	}
	public void addPotionEffect(PotionType type) {
		addPotionEffect(type, type.duration);
	}
	public HashMap<PotionType, Integer> getPotionEffects() { return potioneffects; }
	
	public void tick() {
		if(level == null) return;
		
		if(!Bed.inBed && input.getKey("drop-one").clicked || input.getKey("drop-stack").clicked) {
			Item itemToDrop = null;
			if(game.menu instanceof PlayerInvMenu)
				itemToDrop = ((PlayerInvMenu)game.menu).getSelectedItem();
			else if(game.menu == null)
				itemToDrop = activeItem;
			
			if(itemToDrop != null) {
				Item toEntity = null;
				if(itemToDrop instanceof StackableItem && !input.getKey("drop-stack").clicked && ((StackableItem)itemToDrop).count > 1) {
					// just drop one from the stack
					toEntity = itemToDrop.clone();
					((StackableItem)toEntity).count = 1;
					((StackableItem)itemToDrop).count--;
					if(game.menu instanceof PlayerInvMenu)
						((PlayerInvMenu)game.menu).updateSelectedItem();
				} else {
					// drop the whole item.
					toEntity = itemToDrop;
					if(!ModeMenu.creative) {
						if(game.menu instanceof PlayerInvMenu)
							((PlayerInvMenu)game.menu).removeSelectedItem();
						else if(game.menu == null)
							activeItem = null;
					}
				}
				
				if(toEntity != null && level != null) {
					ItemEntity ie = level.dropItem(x, y, toEntity);
					if(Game.isValidClient())
						Game.client.dropItem(ie);
				}
			}
		}
		
		if(game.menu != null && !Game.ISONLINE) return; // don't tick player when menu is open
		
		super.tick(); // ticks Mob.java
		
		if(potioneffects.size() > 0 && !Bed.inBed) {
			for(PotionType potionType: potioneffects.keySet().toArray(new PotionType[0])) {
				if(potioneffects.get(potionType) <= 1) // if time is zero (going to be set to 0 in a moment)...
					PotionItem.applyPotion(this, potionType, false); // automatically removes this potion effect.
				else potioneffects.put(potionType, potioneffects.get(potionType) - 1); // otherwise, replace it with one less.
			}
		}
		
		if(cooldowninfo > 0) cooldowninfo--;
		
		if(input.getKey("potionEffects").clicked && cooldowninfo == 0) {
			cooldowninfo = 10;
			showpotioneffects = !showpotioneffects;
		}
		
		Tile onTile = level.getTile(x >> 4, y >> 4); // gets the current tile the player is on.
		if (onTile == Tiles.get("Stairs Down") || onTile == Tiles.get("Stairs Up")) {
			if (onStairDelay <= 0) { // when the delay time has passed...
				game.scheduleLevelChange((onTile == Tiles.get("Stairs Up")) ? 1 : -1); // decide whether to go up or down.
				onStairDelay = 10; // resets delay, since the level has now been changed.
				return; // SKIPS the rest of the tick() method.
			}
			
			onStairDelay = 10; //resets the delay, if on a stairs tile, but the delay is greater than 0. In other words, this prevents you from ever activating a level change on a stair tile, UNTIL you get off the tile for 10+ ticks.
		} else if (onStairDelay > 0) onStairDelay--; // decrements stairDelay if it's > 0, but not on stair tile... does the player get removed from the tile beforehand, or something?

		if (ModeMenu.creative) {
			// prevent stamina/hunger decay in creative mode.
			stamina = 10;
			hunger = 10;
		}
		
		// remember: staminaRechargeDelay is a penalty delay for when the player uses up all their stamina.
		// staminaRecharge is the rate of stamina recharge, in some sort of unknown units.
		if (stamina <= 0 && staminaRechargeDelay == 0 && staminaRecharge == 0) {
			staminaRechargeDelay = 40; // delay before resuming adding to stamina.
		}
		
		if (staminaRechargeDelay > 0 && stamina < maxStamina) staminaRechargeDelay--;
		
		if (staminaRechargeDelay == 0) {
			staminaRecharge++; // ticks since last recharge, accounting for the time potion effect.
			
			if (isSwimming() && !potioneffects.containsKey(PotionType.Swim)) staminaRecharge = 0; //don't recharge stamina while swimming.
			
			// recharge a bolt for each multiple of maxStaminaRecharge.
			while (staminaRecharge > maxStaminaRecharge) {
				staminaRecharge -= maxStaminaRecharge;
				if (stamina < maxStamina) stamina++; // recharge one stamina bolt per "charge".
			}
		}
		
		if (hunger < 0) hunger = 0; // error correction
		
		if(stamina < maxStamina) {
			stamHungerTicks--; // affect hunger if not at full stamina; this is 2 levels away from a hunger "burger".
			if(stamina == 0) stamHungerTicks--; // double effect if no stamina at all.
		}
		
		/// this if statement encapsulates the hunger system
		if(!Bed.inBed) {
			if(hungerChargeDelay > 0) { // if the hunger is recharging health...
				stamHungerTicks -= 2+OptionsMenu.diff; // penalize the hunger
				if(hunger == maxHunger) stamHungerTicks -= OptionsMenu.diff; // further penalty if at full hunger
			}
			
			if(stamHungerTicks >= maxHungerTicks) {
				stamHungerTicks -= maxHungerTicks; // reset stamHungerTicks
				hungerStamCnt--; // enter 1 level away from burger.
			}
			
			// on easy mode, hunger doesn't deplete from walking or from time.
			
			if (OptionsMenu.diff == OptionsMenu.norm) {
				if(game.tickCount % 5000 == 0 && hunger > 3) hungerStamCnt--; // hunger due to time.
				
				if (stepCount >= 800) { // hunger due to exercise.
					hungerStamCnt--;
					stepCount = 0; // reset.
				}
			}
			if (OptionsMenu.diff == OptionsMenu.hard) {
				if(game.tickCount % 3000 == 0 && hunger > 0) hungerStamCnt--; // hunger due to time.
				
				if (stepCount >= 400) { // hunger due to exercise.
					hungerStamCnt--;
					stepCount = 0; // reset.
				}
			}
			
			while (hungerStamCnt <= 0) {
				hunger--; // reached burger level.
				hungerStamCnt += maxHungerStams[OptionsMenu.diff];
			}
		}
		
		/// system that heals you depending on your hunger
		if (health < maxHealth && hunger > maxHunger/2) {
			hungerChargeDelay++;
			if (hungerChargeDelay > 20*Math.pow(maxHunger-hunger+2, 2)) {
				health++;
				hungerChargeDelay = 0;
			}
		}
		else hungerChargeDelay = 0;
		
		
		if(!Bed.inBed) {
			if (hungerStarveDelay == 0) {
				hungerStarveDelay = 120;
			}
			
			int[] healths = {5, 3, 0};
			if (hunger == 0 && health > healths[OptionsMenu.diff]) {
				if (hungerStarveDelay > 0) hungerStarveDelay--;
				if (hungerStarveDelay == 0) {
					hurt(this, 1, attackDir); // do 1 damage to the player
				}
			}
		
			
			// this is where movement detection occurs.
			int xa = 0, ya = 0;
			if(game.menu == null) {
				if (input.getKey("up").down) ya--;
				if (input.getKey("down").down) ya++;
				if (input.getKey("left").down) xa--;
				if (input.getKey("right").down) xa++;
			}
			if (game.savecooldown > 0 && !game.saving) {
				game.savecooldown--;
			}
			
			//executes if not saving; and... essentially halves speed if out of stamina.
			if ((xa != 0 || ya != 0) && (staminaRechargeDelay % 2 == 0 || isSwimming()) && game.savecooldown == 0 && !game.saving) {
				double spd = moveSpeed * (potioneffects.containsKey(PotionType.Speed) ? 1.5D : 1);
				int oldDir = dir;
				boolean moved = move((int) (xa * spd), (int) (ya * spd)); // THIS is where the player moves; part of Mob.java
				if (moved) stepCount++;
				if ((moved || oldDir != dir) && Game.isValidClient() && this == game.player)
					Game.client.move(this);
			}
			
			if (isSwimming() && tickTime % 60 == 0 && !potioneffects.containsKey(PotionType.Swim)) { // if drowning... :P
				if (stamina > 0) stamina--; // take away stamina
				else hurt(this, 1, dir ^ 1); // if no stamina, take damage.
			}
		}
		
		if (potioneffects.containsKey(PotionType.Regen)) {
			regentick++;
			if (regentick > 60) {
				regentick = 0;
				if (health < 10) {
					health++;
				}
			}
		}
		
		if (game.menu == null) {
			if (!Bed.inBed && (activeItem == null || !activeItem.used_pending) && (input.getKey("attack").clicked || input.getKey("pickup").clicked) && stamina != 0) {
				if (!potioneffects.containsKey(PotionType.Energy)) stamina--;
				staminaRecharge = 0;
				
				if (input.getKey("pickup").clicked) {
					if(!(activeItem instanceof PowerGloveItem)) {
						prevItem = activeItem;
						activeItem = new PowerGloveItem();
					}
					attack();
					if(!Game.ISONLINE)
						resolveHeldItem();
				}
				else
					attack();
				
				if(activeItem != null && !(activeItem instanceof ToolItem))
					activeItem.used_pending = true;
			}
			
			if (input.getKey("menu").clicked && !use()) // !use() = no furniture in front of the player; this prevents player inventory from opening (will open furniture inventory instead)
				game.setMenu(new PlayerInvMenu(this));
			if (input.getKey("pause").clicked)
				game.setMenu(new PauseMenu(null));
			if (input.getKey("craft").clicked && !use())
				game.setMenu(new CraftingMenu(Recipes.craftRecipes, this, true));
			if (input.getKey("sethome").clicked) setHome();
			if (input.getKey("home").clicked && !Bed.inBed) goHome();
			
			if (input.getKey("info").clicked) game.setMenu(new PlayerInfoMenu());
			
			if (input.getKey("r").clicked && !game.saving && !(this instanceof RemotePlayer) && !Game.isValidClient()) {
				game.saving = true;
				LoadingMenu.percentage = 0;
				new Save(this, WorldSelectMenu.worldname);
			}
			//debug feature:
			if (Game.debug && input.getKey("shift-p").clicked) { // remove all potion effects
				for(PotionType potionType: potioneffects.keySet()) {
					PotionItem.applyPotion(this, potionType, false);
					if(Game.isValidClient() && this == game.player)
						Game.client.sendPotionEffect(potionType, false);
				}
			}
		}
		
		if (attackTime > 0) {
			attackTime--;
			if(attackTime == 0) attackItem = null; // null the attackItem once we are done attacking.
		}
		
		if(Game.isValidClient() && this == game.player) Game.client.sendPlayerUpdate(this);
	}
	
	public void resolveHeldItem() {
		//if(Game.debug) System.out.println("prev item: " + prevItem + "; curItem: " + activeItem);
		if(!(activeItem instanceof PowerGloveItem)) {
			if(prevItem != null && !ModeMenu.creative)
				inventory.add(0, prevItem);
			// if something other than a power glove is being held, but the previous item is null, then nothing happens; nothing added to inventory, and current item remains as the new one.
		} else
			activeItem = prevItem; // the held item didn't change, so remove the power glove and make it what it was before.
		
		if(activeItem instanceof PowerGloveItem)
			activeItem = null;
	}
	
	/* This actually ends up calling another use method down below. */
	private boolean use() {
		
		// if an entity in the direction the player is facing has a use() method, call it, then return true.
		int yo = -2;
		if (dir == 0 && use(x - 8, y + 4 + yo, x + 8, y + 12 + yo)) return true;
		if (dir == 1 && use(x - 8, y - 12 + yo, x + 8, y - 4 + yo)) return true;
		if (dir == 3 && use(x + 4, y - 8 + yo, x + 12, y + 8 + yo)) return true;
		if (dir == 2 && use(x - 12, y - 8 + yo, x - 4, y + 8 + yo)) return true;
		
		/*
		// otherwise, if there is no entity, check if the current tile has a use method.
			//...which, as it happens, never does...
		// round off player coordinates to Tile coordinates.
		int xt = x >> 4;
		int yt = (y + yo) >> 4;
		int r = 12;
		if (attackDir == 0) yt = (y + r + yo) >> 4;
		if (attackDir == 1) yt = (y - r + yo) >> 4;
		if (attackDir == 2) xt = (x - r) >> 4;
		if (attackDir == 3) xt = (x + r) >> 4;
		// do the check
		if (xt >= 0 && yt >= 0 && xt < level.w && yt < level.h) {
			if (level.getTile(xt, yt).use(level, xt, yt, this, attackDir)) return true;
		}*/
		
		return false;
	}
	
	/** This method is called when we press the attack button. */
	protected void attack() {
		// walkDist is not synced, so this can happen for both the client and server.
		walkDist += 8; // increase the walkDist (changes the sprite, like you moved your arm)
		
		if(Game.isValidClient()) {
			// if this is a multiplayer game, than the server will execute the full method instead.
			attackDir = dir;
			if(activeItem != null)
				attackTime = 10;
			else
				attackTime = 5;
			
			attackItem = activeItem;
			
			Game.client.requestInteraction(this);
			if(activeItem instanceof ToolItem && stamina - 1 >= 0 && ((ToolItem)activeItem).type == ToolType.Bow && inventory.count(Items.get("arrow")) > 0) // we are going to use an arrow.
				inventory.removeItem(Items.get("arrow")); // do it here so we don't need a response.
			
			return;
		}
		
		attackDir = dir; // make the attack direction equal the current direction
		attackItem = activeItem; // make attackItem equal activeItem
		boolean done = false; // we're not done yet (we just started!)
		
		if (activeItem instanceof ToolItem && stamina - 1 >= 0) {
			// the player is holding a tool, and has stamina available.
			ToolItem tool = (ToolItem) activeItem;
			
			if (tool.type == ToolType.Bow && inventory.count(Items.get("arrow")) > 0) { // if the player is holding a bow, and has arrows...
				//if (!energy) stamina -= 0; // must be a leftover.
				//...then shoot the arrow in the right direction.
				int spx = 0, spy = 0;
				switch (attackDir) {
					case 0: spx = 0; spy = 1; break;
					case 1: spx = 0; spy = -1; break;
					case 2: spx = -1; spy = 0; break;
					case 3: spx = 1; spy = 0; break;
				}
				if (ModeMenu.creative == false) inventory.removeItem(Items.get("arrow"));
				level.add(new Arrow(this, spx, spy, tool.level));
				done = true; // we have attacked!
			}
		}
		
		// if we are simply holding an item...
		if (activeItem != null) {
			attackTime = 10; // attack time will be set to 10.
			int yo = -2; // y-offset
			int range = 12; // range (distance?) from an object
			
			/* if the interaction between you and an entity is successful then return. */
			if (dir == 0 && interact(x - 8, y + 4 + yo, x + 8, y + range + yo)) done = true;
			if (dir == 1 && interact(x - 8, y - range + yo, x + 8, y - 4 + yo)) done = true;
			if (dir == 3 && interact(x + 4, y - 8 + yo, x + range, y + 8 + yo)) done = true;
			if (dir == 2 && interact(x - range, y - 8 + yo, x - 4, y + 8 + yo)) done = true;
			if (done) return;
			
			int xt = x >> 4; // current x-tile coordinate you are on.
			int yt = (y + yo) >> 4; // current y-tile coordinate you are on.
			int r = 12; // radius
			// gets the tile on the side of you that you're attacking.
			if (attackDir == 0) yt = (y + r + yo) >> 4;
			if (attackDir == 1) yt = (y - r + yo) >> 4;
			if (attackDir == 2) xt = (x - r) >> 4;
			if (attackDir == 3) xt = (x + r) >> 4;
			
			if (xt >= 0 && yt >= 0 && xt < level.w && yt < level.h) { // if the target coordinates are a valid tile...
				if (activeItem.interactOn(level.getTile(xt, yt), level, xt, yt, this, attackDir)) { // returns true if your held item successfully interacts with the target tile.
					done = true;
				} else { // item can't interact with tile
					if (level.getTile(xt, yt).interact(level, xt, yt, this, activeItem, attackDir)) { // returns true if the target tile successfully interacts with the item.
						done = true;
					}
				}
				
				if(Game.isValidServer() && this instanceof RemotePlayer) {// only do this if no interaction was actually made; b/c a tile update packet will generally happen then anyway.
					minicraft.network.MinicraftServerThread thread = Game.server.getAssociatedThread((RemotePlayer)this);
					if(thread != null)
						thread.sendTileUpdate(level, xt, yt); /// FIXME this part is as a semi-temporary fix for those odd tiles that don't update when they should; instead of having to make another system like the entity additions and removals (and it wouldn't quite work as well for this anyway), this will just update whatever tile the player interacts with (and fails, since a successful interaction changes the tile and therefore updates it anyway).
				}
				
				if (activeItem.isDepleted() && !ModeMenu.creative) {
					// if the activeItem has 0 items left, then "destroy" it.
					activeItem = null;
				}
			}
		}

		if (done) return; // skip the rest if interaction was handled.

		if (activeItem == null || activeItem.canAttack()) { // if there is no active item, OR if the item can be used to attack...
			attackTime = 5;
			int yo = -2;
			int range = 20;
			// attacks the enemy in the appropriate direction.
			if (dir == 0) hurt(x - 8, y + 4 + yo, x + 8, y + range + yo);
			if (dir == 1) hurt(x - 8, y - range + yo, x + 8, y - 4 + yo);
			if (dir == 2) hurt(x - range, y - 8 + yo, x - 4, y + 8 + yo);
			if (dir == 3) hurt(x + 4, y - 8 + yo, x + range, y + 8 + yo);
			
			// attempts to hurt the tile in the appropriate direction.
			
			int xt = x >> 4;
			int yt = (y + yo) >> 4;
			int r = 12;
			if (attackDir == 0) yt = (y + r + yo) >> 4; // down
			if (attackDir == 1) yt = (y - r + yo) >> 4; // up
			if (attackDir == 2) xt = (x - r) >> 4; // left
			if (attackDir == 3) xt = (x + r) >> 4; // right

			if (xt >= 0 && yt >= 0 && xt < level.w && yt < level.h) {
				Tile t = level.getTile(xt, yt);
				//if (Game.debug) System.out.println(Game.onlinePrefix()+"attacking tile " + xt+","+yt + ": "+t.name + " (facing " + dir+"/"+attackDir+" - " + Direction.values[dir]+"/"+Direction.values[attackDir] + ")");
				t.hurt(level, xt, yt, this, random.nextInt(3) + 1, attackDir);
			}
		}
	}
	
	public void goFishing(int x, int y) {
		int fcatch = random.nextInt(90);
		
		if (fcatch < 10) level.dropItem(x, y, Items.get("raw fish"));
		else if (fcatch < 15) level.dropItem(x, y, Items.get("slime"));
		else if (fcatch == 15) level.dropItem(x, y, Items.get("Leather Armor"));
		else if (fcatch == 42 && random.nextInt(5) == 0) System.out.println("FISHNORRIS got away... just kidding, FISHNORRIS din't get away from you, you got away from FISHNORRIS...");
	}
	
	/** called by other use method; this serves as a buffer in case there is no entity in front of the player. */
	private boolean use(int x0, int y0, int x1, int y1) {
		List<Entity> entities = level.getEntitiesInRect(x0, y0, x1, y1); // gets the entities within the 4 points
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e != this) if (e.use(this, attackDir)) return true; // if the entity is not the player, then call it's use method, and return the result. Only some furniture classes use this.
		}
		return false;
	}
	
	/** same, but for interaction. */
	private boolean interact(int x0, int y0, int x1, int y1) {
		List<Entity> entities = level.getEntitiesInRect(x0, y0, x1, y1);
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if ( e != this && e.interact(this, activeItem, attackDir) ) return true;
		}
		return false;
	}
	
	/** same, but for attacking. */
	private void hurt(int x0, int y0, int x1, int y1) {
		List<Entity> entities = level.getEntitiesInRect(x0, y0, x1, y1);
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e != this) e.hurt(this, getAttackDamage(e), attackDir); // note: this really only does something for mobs.
		}
	}
	
	/** Gets the attack damage the player will deal. */
	private int getAttackDamage(Entity e) {
		int dmg = random.nextInt(2) + 1;
		if (activeItem != null && activeItem instanceof ToolItem) {
			dmg += ((ToolItem)activeItem).getAttackDamageBonus(e); // sword/axe are more effective at dealing damage.
		}
		return dmg;
	}
	
	/** Draws the player on the screen */
	public void render(Screen screen) {
		col = Color.get(-1, 100, shirtColor, 532);
		
		MobSprite[][] spriteSet; // the default, walking sprites.
		
		if(activeItem instanceof FurnitureItem) {
			spriteSet = skinon ? carrySuitSprites : carrySprites;
		} else {
			spriteSet = skinon ? suitSprites : sprites;
		}
		
		/* offset locations to start drawing the sprite relative to our position */
		int xo = x - 8; // horizontal
		int yo = y - 11; // vertical
		
		if (isSwimming()) {
			yo += 4; // y offset is moved up by 4
			int liquidColor = 0; // color of water / lava circle
			if (level.getTile(x / 16, y / 16) == Tiles.get("water")) {
				liquidColor = Color.get(-1, -1, 115, 335);
				if (tickTime / 8 % 2 == 0) liquidColor = Color.get(-1, 335, 5, 115);
			} else if (level.getTile(x / 16, y / 16) == Tiles.get("lava")) {
				liquidColor = Color.get(-1, -1, 500, 300);
				if (tickTime / 8 % 2 == 0) liquidColor = Color.get(-1, 300, 400, 500);
			}

			screen.render(xo + 0, yo + 3, 5 + 13 * 32, liquidColor, 0); // render the water graphic
			screen.render(xo + 8, yo + 3, 5 + 13 * 32, liquidColor, 1); // render the mirrored water graphic to the right.
		}
		
		if (attackTime > 0 && attackDir == 1) { // if currently attacking upwards...
			screen.render(xo + 0, yo - 4, 6 + 13 * 32, Color.get(-1, 555), 0); //render left half-slash
			screen.render(xo + 8, yo - 4, 6 + 13 * 32, Color.get(-1, 555), 1); //render right half-slash (mirror of left).
			if (attackItem != null) { // if the player had an item when they last attacked...
				attackItem.sprite.render(screen, xo + 4, yo - 4); // then render the icon of the item.
			}
		}
		
		if (hurtTime > playerHurtTime - 10) { // if the player has just gotten hurt...
			col = Color.get(-1, 555); // make the sprite white.
		}
		
		MobSprite curSprite = spriteSet[dir][(walkDist >> 3) & 1]; // gets the correct sprite to render.
		
		// render each corner of the sprite
		if (!isSwimming()) { // don't render the bottom half if swimming.
			curSprite.render(screen, xo, yo, col);
		} else {
			curSprite.renderRow(0, screen, xo, yo, col);
		}
		
		// renders slashes:
		
		if (attackTime > 0 && attackDir == 2) { // if attacking to the left.... (same as above)
			screen.render(xo - 4, yo, 7 + 13 * 32, Color.get(-1, 555), 1);
			screen.render(xo - 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555), 3);
			if (attackItem != null) {
				attackItem.sprite.render(screen, xo - 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 3) { // attacking to the right
			screen.render(xo + 8 + 4, yo, 7 + 13 * 32, Color.get(-1, 555), 0);
			screen.render(xo + 8 + 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555), 2);
			if (attackItem != null) {
				attackItem.sprite.render(screen, xo + 8 + 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 0) { // attacking downwards
			screen.render(xo + 0, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555), 2);
			screen.render(xo + 8, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555), 3);
			if (attackItem != null) {
				attackItem.sprite.render(screen, xo + 4, yo + 8 + 4);
			}
		}
		
		 // renders the furniture if the player is holding one.
		if (activeItem instanceof FurnitureItem) {
			Furniture furniture = ((FurnitureItem) activeItem).furniture;
			furniture.x = x;
			furniture.y = yo-4;
			furniture.render(screen);
		}
	}
	
	/** What happens when the player interacts with a itemEntity */
	public void touchItem(ItemEntity itemEntity) {
		itemEntity.take(this); // calls the take() method in ItemEntity
		if(ModeMenu.creative) return; // we shall not bother the inventory on creative mode.
		
		if(activeItem != null && activeItem.name == itemEntity.item.name && activeItem instanceof StackableItem && itemEntity.item instanceof StackableItem) // picked up item matches the one in your hand
			((StackableItem)activeItem).count += ((StackableItem)itemEntity.item).count;
		else
			inventory.add(itemEntity.item); // add item to inventory
	}
	
	public boolean canSwim() {
		return true; // the player can swim.
	}
	
	public boolean canWool() {
		return true; // can... something..?
	}
	
	public boolean canLight() {
		return true; // can be lit up? has a lighter version?
	}
	
	/** Finds a start position for the player to start in. */
	public boolean findStartPos(Level level) {
		while (true) { // will loop until it returns
			// gets coordinates of a random tile (in tile coordinates)
			int x = random.nextInt(level.w);
			int y = random.nextInt(level.h);
			if (level.getTile(x, y) == Tiles.get("grass")) { // player will only spawn on a grass tile.
				// used to save (tile) coordinates of spawnpoint outside of this method.
				spawnx = x;
				spawny = y;
				// set (entity) coordinates of player to the center of the tile.
				this.x = spawnx * 16 + 8; // conversion from tile coords to entity coords.
				this.y = spawny * 16 + 8;
				return true; // why bother returning anything..? it's always true...
			}
		}
	}
	
	/** Set player's home coordinates. */
	public void setHome() {
		if (game.currentLevel == 3) { // if on surface
			// set home coordinates
			homeSetX = this.x;
			homeSetY = this.y;
			hasSetHome = true; // confirm that home coordinates are indeed set
			game.notifications.add("Set your home!"); // give success message
		} else { // can only set home on surface
			game.notifications.add("Can't set home here!"); // give failure message
		}
	}

	public void goHome() {
		if (game.currentLevel == 3) { // if on surface
			if (hasSetHome == true) {
				// move player to home coordinates
				this.x = homeSetX;
				this.y = homeSetY;
				if (ModeMenu.hardcore) hurt(this, 2, attackDir); // give penalty for using home if in hardcore mode.
				stamina = 0; // teleportation uses up all your stamina.
				game.notifications.add("Home Sweet Home!"); // give success message
				if (ModeMenu.hardcore) game.notifications.add("Mode penalty: -2 health"); // give penalty message
			} else {
				//can go home, but no home set.
				game.notifications.add("You don't have a home!");
			}
		} else { // can only go home from surface
			game.notifications.add("You can't go home from here!");
		}
	}
	
	/** finds a location to respawn the player after death. */
	public boolean respawn(Level level) {
		if (!level.getTile(spawnx, spawny).maySpawn)
			findStartPos(level); // if there's no bed to spawn from, and the stored coordinates don't point to a grass tile, then find a new point.
		
		// move the player to the spawnpoint
		this.x = spawnx * 16 + 8;
		this.y = spawny * 16 + 8;
		return true; // again, why the "return true"'s for methods that never return false?
	}
	
	/** Pays the stamina used for an action */
	public boolean payStamina(int cost) {
		if (potioneffects.containsKey(PotionType.Energy)) return true; // if the player has the potion effect for infinite stamina, return true (without subtracting cost).
		else if (cost > stamina) return false; // if the player doesn't have enough stamina, then return false; failure.

		if (cost < 0) cost = 0; // error correction
		stamina -= cost; // subtract the cost from the current stamina
		return true; // success
	}
	
	/** Gets the player's light radius underground */
	public int getLightRadius() {
		//if (game.currentLevel == 3) return 0; // I don't want the player to have an automatic halo on the surface.
		
		float light = potioneffects.containsKey(PotionType.Light) ? 2.5f : 1; // multiplier for the light potion effect.
		float r = 3 * light; // the radius of the light.
		if (game.currentLevel == 3) r = (light-1) * 3;
		
		if (game.currentLevel == 5) r = 5 * light; // more light than usual on dungeon level.
		
		//if (ModeMenu.creative) r = 12 * light; // creative mode light radius is much bigger; whole screen.
		
		
		if (activeItem != null && activeItem instanceof FurnitureItem) { // if player is holding furniture
			int rr = ((FurnitureItem) activeItem).furniture.getLightRadius(); // gets furniture light radius
			if (rr > r) r = rr; // brings player light up to furniture light, if less, since the furnture is not yet part of the level and so doesn't emit light even if it should.
		}
		
		return (int) r; // return light radius
	}
	
	/** What happens when the player dies */
	protected void die() {
		int lostscore = score / 3; // finds score penalty
		score -= lostscore; // subtracts score penalty
		game.setMultiplier(1);
		
		//make death chest
		DeathChest dc = new DeathChest();
		dc.x = this.x;
		dc.y = this.y;
		dc.inventory = this.inventory;
		if (activeItem != null) {
			dc.inventory.add(activeItem);
		}
		if(curArmor != null) {
			//ArmorItem armorItem = new ArmorItem(curArmor);
			//armorItem.amount = armor;
			dc.inventory.add(curArmor);
		}
		//dc.inventory.removeItem(new PowerGloveItem());
		
		Sound.playerDeath.play();
		
		if(!Game.ISONLINE)
			Game.levels[game.currentLevel].add(dc);
		else if(Game.isValidClient())
			Game.client.sendPlayerDeath(this, dc);
		
		super.die(); // calls the die() method in Mob.java
	}
	
	/** What happens when the player touches an entity */
	protected void touchedBy(Entity entity) {
		if (!(entity instanceof Player)) { // prevents stack-overflow
			entity.touchedBy(this); // calls the other entity's touchedBy method.
		}
	}
	
	public void hurt(int damage, int attackDir) { doHurt(damage, attackDir); }
	/** What happens when the player is hurt */
	protected void doHurt(int damage, int attackDir) {
		if (ModeMenu.creative || hurtTime > 0 || Bed.inBed) return; // can't get hurt in creative, hurt cooldown, or while someone is in bed
		
		if(Game.isValidServer() && this instanceof RemotePlayer) {
			// let the clients deal with it.
			Game.server.getAssociatedThread((RemotePlayer)this).sendPlayerHurt(damage, attackDir);
			return;
		}
		
		int healthDam = 0, armorDam = 0;
		Sound.playerHurt.play();
		if (curArmor == null) { // no armor
			health -= damage; // subtract that amount
		} else { // has armor
			armorDamageBuffer += damage;
			armorDam += damage;
			
			while (armorDamageBuffer >= curArmor.level+1) {
				armorDamageBuffer -= curArmor.level+1;
				healthDam++;
			}
		}
		
		// adds a text particle telling how much damage was done to the player, and the armor.
		if(armorDam > 0) {
			level.add(new TextParticle("" + damage, x, y, Color.get(-1, 333)));
			armor -= armorDam;
			if(armor <= 0) {
				healthDam -= armor; // adds armor damage overflow to health damage (minus b/c armor would be negative)
				armor = 0;
				armorDamageBuffer = 0; // ensures that new armor doesn't inherit partial breaking from this armor.
				curArmor = null; // removes armor
			}
		}
		if(healthDam > 0) {
			level.add(new TextParticle("" + damage, x, y, Color.get(-1, 504)));
			health -= healthDam;
		}
		
		// apply the appropriate knockback
		if (attackDir == 0) yKnockback = +6;
		if (attackDir == 1) yKnockback = -6;
		if (attackDir == 2) xKnockback = -6;
		if (attackDir == 3) xKnockback = +6;
		// set invulnerability time
		hurtTime = playerHurtTime;
	}
	
	protected String getUpdateString() {
		String updates = super.getUpdateString() + ";";
		updates += "skinon,"+skinon+
		";shirtColor,"+shirtColor+
		//";activeItem,"+(activeItem==null?"null":activeItem.name)+
		";armor,"+armor+
		";attackTime,"+attackTime+
		";attackDir,"+attackDir+
		";attackItem,"+(attackItem==null?"null":attackItem.name);
		/*";potioneffects,";
		if(potioneffects.size() == 0)
			updates += "null";
		else {
			for(PotionType p: potioneffects.keySet().toArray(new PotionType[0]))
				updates += p.ordinal() + "_" + potioneffects.get(p) + ":";
			
			updates = updates.substring(0, updates.length()-1);
		}*/
		
		return updates;
	}
	
	protected boolean updateField(String field, String val) {
		if(super.updateField(field, val)) return true;
		switch(field) {
			case "skinon": skinon = Boolean.parseBoolean(val); return true;
			case "shirtColor": shirtColor = Integer.parseInt(val); return true;
			//case "activeItem": activeItem = Items.get(val); return true;
			case "armor": armor = Integer.parseInt(val); return true;
			case "attackTime": attackTime = Integer.parseInt(val); return true;
			case "attackDir": attackDir = Integer.parseInt(val); return true;
			case "attackItem": attackItem = Items.get(val); return true;
			case "potioneffects":
				potioneffects.clear();
				for(String potion: val.split(":")) {
					String[] parts = potion.split("_");
					potioneffects.put(PotionType.values[Integer.parseInt(parts[0])], Integer.parseInt(parts[1]));
				}
				return true;
		}
		
		return false;
	}
	
	public String getPlayerData() {
		List<String> datalist = new ArrayList<String>();
		String playerdata = "";
		
		Save.writePlayer(this, datalist);
		for(String str: datalist)
			if(str.length() > 0)
				playerdata += str + ",";
		playerdata = playerdata.substring(0, playerdata.length()-1) + "\n";
		
		Save.writeInventory(this, datalist);
		for(String str: datalist)
			if(str.length() > 0)
				playerdata += str + ",";
		if(datalist.size() == 0)
			playerdata += "null";
		else
			playerdata = playerdata.substring(0, playerdata.length()-1);
		
		return playerdata;
	}
}
