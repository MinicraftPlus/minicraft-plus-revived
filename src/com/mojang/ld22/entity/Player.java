package com.mojang.ld22.entity;

import com.mojang.ld22.Game;
import com.mojang.ld22.InputHandler;
import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.entity.ItemEntity;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.FurnitureItem;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.ListItems;
import com.mojang.ld22.item.PowerGloveItem;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.item.resource.ArmorResource;
import com.mojang.ld22.item.resource.PotionResource;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.saveload.Save;
import com.mojang.ld22.screen.CraftInvMenu;
import com.mojang.ld22.screen.InventoryMenu;
import com.mojang.ld22.screen.LoadingMenu;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.OptionsMenu;
import com.mojang.ld22.screen.PauseMenu;
import com.mojang.ld22.screen.PlayerInfoMenu;
import com.mojang.ld22.screen.WorldSelectMenu;
import com.mojang.ld22.sound.Sound;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class Player extends Mob {
	private InputHandler input;
	public Game game;
	
	public static final int playerHurtTime = 30;
	public static double moveSpeed = 1; // the number of coordinate squares to move; each tile is 16x16.
	public static int score; // the player's score
	public static boolean hasSetHome = false, skinon;
	//These 2 ints are ints saved from the first spawn - this way the spawn pos is always saved.
	public static int spawnx = 0, spawny = 0; // these are stored as tile coordinates, not entity coordinates.
	public static int maxStamina = 10, maxHunger = 10, maxArmor = 100; // the maximum stats that the player can have.
	
	public Inventory inventory;
	public Item attackItem, activeItem;
	public boolean energy;
	public int attackTime, attackDir;
	public int homeSetX, homeSetY;
	public boolean bedSpawn;
	
	private int onStairDelay; // the delay before changing levels.
	public int stepCount;
	int tickCounter;
	int timesTick;
	
	public int hunger, stamina, armor; // the current stats
	public int armorDamageBuffer;
	public ArmorResource curArmor; // the color of the armor to be displayed.
	public int staminaRecharge;  // the recharge rate of the player's stamina
	public int staminaRechargeDelay; // the recharge delay when the player uses up their stamina.
	public int hungStamCnt;
	int hungerChargeDelay; // the delay between each time the hunger bar increases your health
	int hungerStarveDelay; // the delay between each time the hunger bar decreases your health
	boolean alreadyLostHunger;
	boolean repeatHungerCyc;
	
	public boolean showinfo;
	public int px, py;
	
	public HashMap<String, Integer> potioneffects; // the potion effects currently applied to the player
	public boolean showpotioneffects; // whether to display the current potion effects on screen
	int cooldowninfo; // prevents you from toggling the info pane on and off super fast.
	int regentick; // counts time between each time the regen potion effect heals you.
	
	int acs = 25; // default ("start") arrow count
	public int ac; // arrow count
	public int r = 50, g = 50, b; // player shirt color.
	
	// Note: the player's health & max health are inherited from Mob.java
	
	public Player(Game game, InputHandler input) {
		this.game = game;
		this.input = input;
		inventory = new Inventory();
		ac = acs;
		
		// these come from Mob.java
		x = 24;
		y = 24;
		tickCounter = 0;
		
		energy = false;
		//maxStamina = 10;
		//maxHunger = 10;
		
		repeatHungerCyc = false;
		
		px = this.x;
		py = this.y;
		
		potioneffects = new HashMap<String, Integer>();
		showpotioneffects = true;
		
		showinfo = false;
		cooldowninfo = 0;
		regentick = 0;
		r = 50;
		g = 50;
		b = 0;
		
		armor = 0;
		curArmor = null;
		armorDamageBuffer = 0;
		stamina = maxStamina;
		hunger = maxHunger;
		
		if (ModeMenu.creative) {
			for (int i = 0; i < ListItems.items.size(); i++) {
				inventory.add((Item) ListItems.items.get(i));
			}
		} else {
			inventory.add(new FurnitureItem(new Enchanter()));
			inventory.add(new FurnitureItem(new Workbench()));
			inventory.add(new PowerGloveItem());
		}
	}
	
	public void tick() {
		super.tick(); // ticks Mob.java
		isenemy = false;
		tickCounter++;
		
		if(potioneffects.size() > 0 && !Bed.hasBedSet) {
			for(String potionType: potioneffects.keySet().toArray(new String[0])) {
				if(potioneffects.get(potionType) <= 1) // if time is zero (going to be set to 0 in a moment)...
					PotionResource.applyPotion(this, potionType, false); // automatically removes this potion effect.
				else potioneffects.put(potionType, potioneffects.get(potionType) - 1); // otherwise, replace it with one less.
			}
		}
		
		if(cooldowninfo > 0) cooldowninfo--;
		
		if(input.getKey("F3").clicked && cooldowninfo == 0) { // shows debug info in upper-left
			cooldowninfo = 10;
			showinfo = !showinfo;
		}
		
		if(input.getKey("potionEffects").clicked && cooldowninfo == 0) {
			cooldowninfo = 10;
			showpotioneffects = !showpotioneffects;
		}
		
		Tile onTile = level.getTile(x >> 4, y >> 4); // gets the current tile the player is on.
		if (onTile == Tile.stairsDown || onTile == Tile.stairsUp || onTile == Tile.lightstairsDown || onTile == Tile.lightstairsUp) {
			if (onStairDelay == 0) { // when the delay time has passed...
				changeLevel((onTile == Tile.stairsUp || onTile == Tile.lightstairsUp) ? 1 : -1); // decide whether to go up or down.
				onStairDelay = 10; // resets delay, since the level has now been changed.
				return;
			}
			
			onStairDelay = 10; //resets the delay, if on a stairs tile, but the delay is not 0.
		} else if (onStairDelay > 0) onStairDelay--; // decrements stairDelay if it's > 0, but not on stair tile... does the player get removed from the tile beforehand, or something?

		if (ModeMenu.creative) {
			// prevent stamina/hunger decay in creative mode.
			if (stamina <= 10) stamina = 10;
			if (hunger < 10) hunger = 10;
		}
		
		// TODO this whole thing below needs comments; but more importantly, it needs optimizing.
		if (hunger < 0) hunger = 0; // error correction?
		else {
			// remember: staminaRechargeDelay is a penalty delay for when the player uses up all their stamina.
			// staminaRecharge is the rate of stamina recharge, in some sort of unknown units.
			if (stamina <= 0 && staminaRechargeDelay == 0 && staminaRecharge == 0) {
				staminaRechargeDelay = 40; // some sort of duration.
				hungStamCnt++;
				//if (isSwimming()) hungStamCnt--;
				if (OptionsMenu.diff == OptionsMenu.easy && hungStamCnt == 10) {
					hunger--;
					hungStamCnt = 0;
				}
				if (OptionsMenu.diff == OptionsMenu.norm && hungStamCnt == 7) {
					hunger--;
					hungStamCnt = 0;
				}
				if (OptionsMenu.diff == OptionsMenu.hard && hungStamCnt == 5) {
					hunger--;
					hungStamCnt = 0;
				}
			}

			if (staminaRechargeDelay > 0) staminaRechargeDelay--;

			if (staminaRechargeDelay == 0) {
				staminaRecharge++; // this is used to determine the time between each bolt recharge.

				if (isSwimming() && !potioneffects.containsKey("Swim")) staminaRecharge = 0; //
				
				int charge = potioneffects.containsKey("Time") ? 5 : 10;
				while (staminaRecharge > charge) {
					staminaRecharge -= charge;
					if (stamina < maxStamina) stamina++;
				}
			}
		}

		/*if (hungerChargeDelay == 0) {
			hungerChargeDelay = 100;
		}*/
		
		/// system that heals you depending on your hunger
		if (health < maxHealth && hunger > maxHunger/2) {
			//if (hungerChargeDelay > 0) hungerChargeDelay--;
			hungerChargeDelay++;
			if (hungerChargeDelay > 50*Math.pow(maxHunger-hunger+1, 2)) {
				health++;
				hungerChargeDelay = 0;
			}
		}
		else hungerChargeDelay = 0;

		if (hungerStarveDelay == 0) {
			hungerStarveDelay = 120;
		}
		if (OptionsMenu.diff == OptionsMenu.norm) {
			if (stepCount >= 10000) {
				hunger--;
				stepCount = 0;
			}
		}
		// on easy mode, hunger doesn't deplete?
		if (OptionsMenu.diff == OptionsMenu.hard) {
			if (stepCount >= 5000) {
				hunger--;
				stepCount = 0;
			}
		}

		if (OptionsMenu.diff == OptionsMenu.norm) {
			if (game.tickCount == 6000) {
				timesTick++;
				if (timesTick == random.nextInt(5)) hunger--;
			}
		}

		if (OptionsMenu.diff == OptionsMenu.hard) {
			if (game.tickCount == 6000) {
				timesTick++;
				if (timesTick == random.nextInt(2)) hunger--;
			}
		}

		if (OptionsMenu.diff == OptionsMenu.easy) {
			if (hunger == 0 && health > 5) {
				if (hungerStarveDelay > 0) hungerStarveDelay--;
				if (hungerStarveDelay == 0) {
					hurt(this, 1, attackDir); // do 1 damage to the player
				}
			}
		}

		if (OptionsMenu.diff == OptionsMenu.norm) {
			if (hunger == 0 && health > 3) {
				if (hungerStarveDelay > 0) hungerStarveDelay--;
				if (hungerStarveDelay == 0) {
					hurt(this, 1, attackDir);
				}
			}
		}

		if (OptionsMenu.diff == OptionsMenu.hard) {
			if (hunger == 0 && health > 0) {
				if (hungerStarveDelay > 0) hungerStarveDelay--;
				if (hungerStarveDelay == 0) {
					hurt(this, 1, attackDir);
				}
			}
		}
		
		// this is where movement detection occurs.
		int xa = 0, ya = 0;
		if (input.getKey("up").down) {
			ya--;
			stepCount++;
		}
		if (input.getKey("down").down) {
			ya++;
			stepCount++;
		}
		if (input.getKey("left").down) {
			xa--;
			stepCount++;
		}
		if (input.getKey("right").down) {
			xa++;
			stepCount++;
		}
		
		if (isSwimming() && tickTime % 60 == 0 && !potioneffects.containsKey("Swim")) { // if drowning... :P
			if (stamina > 0) stamina--; // take away stamina
			else hurt(this, 1, dir ^ 1); // if no stamina, take damage.
		}

		if (game.saving && game.savecooldown > 0) {
			xa = 0;
			ya = 0;
		}
		
		if (potioneffects.containsKey("Regen")) {
			regentick++;
			if (regentick > 60) {
				regentick = 0;
				if (health < 10) {
					health++;
				}
			}
		}
		
		if (game.savecooldown > 0 && !game.saving) {
			game.savecooldown--;
		}
		
		//executes if not saving; and... essentially halves speed if out of stamina.
		if (staminaRechargeDelay % 2 == 0 && game.savecooldown == 0 && !game.saving) {
			double spd = moveSpeed * (potioneffects.containsKey("Time") ? (potioneffects.containsKey("Speed") ? 1.5D : 2) : 1);
			move((int) (xa * spd), (int) (ya * spd)); // THIS is where the player moves; part of Mob.java
		}
		
		if (input.getKey("attack").clicked && stamina != 0) {
			if (!potioneffects.containsKey("Energy")) stamina--;
			staminaRecharge = 0;
			attack();
		}

		if (input.getKey("menu").clicked && !use()) // !use() = no furniture in front of the player; this prevents player inventory from opening (will open furniture inventory instead)
			game.setMenu(new InventoryMenu(this));
		if (input.getKey("pause").clicked) game.setMenu(new PauseMenu(this));
		if (input.getKey("craft").clicked && !use())
			game.setMenu(new CraftInvMenu(Crafting.craftRecipes, this));
		if (input.getKey("sethome").clicked) setHome();
		if (input.getKey("home").clicked) goHome();
		
		if (input.getKey("info").clicked) game.setMenu(new PlayerInfoMenu());
		
		if (input.getKey("r").clicked && !game.saving) {
			game.saving = true;
			new Save(this, WorldSelectMenu.worldname);
			LoadingMenu.percentage = 0;
		}
		//debug feature:
		if (Game.debug && input.getKey("shift-p").clicked) { // remove all potion effects
			for(String potionType: potioneffects.keySet().toArray(new String[0])) {
				PotionResource.applyPotion(this, potionType, false);
			}
		}
		
		if (attackTime > 0) attackTime--;
	}
	
	/* This actually ends up calling another use method down below. */
	private boolean use() {
		
		// if an entity in the direction the player is facing has a use() method, call it, then return true.
		int yo = -2;
		if (dir == 0 && use(x - 8, y + 4 + yo, x + 8, y + 12 + yo)) return true;
		if (dir == 1 && use(x - 8, y - 12 + yo, x + 8, y - 4 + yo)) return true;
		if (dir == 3 && use(x + 4, y - 8 + yo, x + 12, y + 8 + yo)) return true;
		if (dir == 2 && use(x - 12, y - 8 + yo, x - 4, y + 8 + yo)) return true;
		
		// otherwise, if there is no entity, check if the current tile has a use method:
		
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
		}
		
		return false;
	}
	
	/** This method is called when we press the attack button. */
	private void attack() {
		walkDist += 8; // increase the walkDist (changes the sprite)
		attackDir = dir; // make the attack direction equal the current direction
		attackItem = activeItem; // make attackItem equal activeItem
		boolean done = false; // we're not done yet (we just started!)
		
		if ((attackItem instanceof ToolItem) && stamina - 1 >= 0) {
			// the player is holding a tool, and has stamina available.
			ToolItem tool = (ToolItem) attackItem;
			
			if (tool.type == ToolType.bow && ac > 0) { // if the player is holding a bow, and has arrows...
				if (!energy) stamina -= 0; // must be a leftover.
				//...then shoot the arrow in the right direction.
				int spx = 0, spy = 0;
				switch (attackDir) {
					case 0: spx = 0; spy = 1; break;
					case 1: spx = 0; spy = -1; break;
					case 2: spx = -1; spy = 0; break;
					case 3: spx = 1; spy = 0; break;
				}
				if (ModeMenu.creative == false) ac--;
				level.add(new Arrow(this, spx, spy, tool.level, done));
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
				if (activeItem.isDepleted()) {
					// if the activeItem has 0 resources left, then "destroy" it.
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
			if (dir == 3) hurt(x + 4, y - 8 + yo, x + range, y + 8 + yo);
			if (dir == 2) hurt(x - range, y - 8 + yo, x - 4, y + 8 + yo);
			
			// attempts to hurt the tile in the appropriate direction.
			
			int xt = x >> 4;
			int yt = (y + yo) >> 4;
			int r = 12;
			if (attackDir == 0) yt = (y + r + yo) >> 4;
			if (attackDir == 1) yt = (y - r + yo) >> 4;
			if (attackDir == 2) xt = (x - r) >> 4;
			if (attackDir == 3) xt = (x + r) >> 4;

			if (xt >= 0 && yt >= 0 && xt < level.w && yt < level.h) {
				level.getTile(xt, yt).hurt(level, xt, yt, this, random.nextInt(3) + 1, attackDir);
			}
		}
	}
	
	public void goFishing(int x, int y) {
		int fcatch = random.nextInt(90);
		
		if (fcatch <= 8) {
			level.add(new ItemEntity(new ResourceItem(Resource.rawfish), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
		}
		
		if (fcatch == 25 || fcatch == 43 || fcatch == 32 || fcatch == 15 || fcatch == 42) {
			level.add(new ItemEntity(new ResourceItem(Resource.slime), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
		}

		if (fcatch == 56) {
			level.add(new ItemEntity(new ResourceItem(Resource.larmor), x + random.nextInt(11) - 5, y + random.nextInt(11) - 5));
		} else {
			if(Game.debug) System.out.println("Nothing caught...");
			if(random.nextInt(200) == 42) System.out.println("CHUCKNORRIS got away...");
		}
	}
	
	/** called by other use method; this serves as a buffer in case there is no entity in front of the player. */
	private boolean use(int x0, int y0, int x1, int y1) {
		List<Entity> entities = level.getEntities(x0, y0, x1, y1); // gets the entities within the 4 points
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e != this) if (e.use(this, attackDir)) return true; // if the entity is not the player, then call it's use method, and return the result.
		}
		return false;
	}
	
	/** same, but for interaction. */
	private boolean interact(int x0, int y0, int x1, int y1) {
		List<Entity> entities = level.getEntities(x0, y0, x1, y1);
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if ( e != this && e.interact(this, activeItem, attackDir) ) return true;
		}
		return false;
	}
	
	/** same, but for attacking. */
	private void hurt(int x0, int y0, int x1, int y1) {
		List<Entity> entities = level.getEntities(x0, y0, x1, y1);
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e != this) e.hurt(this, getAttackDamage(e), attackDir); // note: this actually DO the actions.
		}
	}
	
	/** Gets the attack damage the player will deal. */
	private int getAttackDamage(Entity e) {
		int dmg = random.nextInt(3) + 1;
		if (attackItem != null) {
			dmg += attackItem.getAttackDamageBonus(e); // sword/axe are more effective at dealing damage.
		}
		return dmg;
	}
	
	/** Draws the player on the screen */
	public void render(Screen screen) {
		// set / get colors.
		int r2 = r - 50, b2 = b - 50, g2 = g - 50;
		if(r == 50 && g == 50 && (b == 0 || b == 50)) {
			r2 = r < 0 ? 0 : r;
			g2 = g < 0 ? 0 : g;
			b2 = b < 0 ? 0 : b;
		}
		
		int col0 = Color.get(-1, 100, Color.rgb(r, g, b), 531);
		int col1 = Color.get(-1, 100, Color.rgb(r, g, b), 532);
		int col2 = Color.get(-1, 100, Color.rgb(r2, g2, b2), 421);
		int col3 = Color.get(-1, 0, Color.rgb(r2, g2, b2), 321);
		int col4 = Color.get(-1, 100, Color.rgb(r, g, b), 532);
		if(isLight()) { // lighter versions
			col0 = Color.get(-1, 100, Color.rgb(r, g, b), 532);
			col2 = Color.get(-1, 100, Color.rgb(r, g, b), 532);
			col3 = Color.get(-1, 100, Color.rgb(r, g, b), 532);
		}

		int xt = 0; // X tile coordinate in the sprite-sheet
		int yt = 14; // Y tile coordinate in the sprite-sheet
		if (skinon) {
			// draw the airwizard suit instead.
			xt = 18;
			yt = 20;
		}
		
		// This will either be a 1 or a 0 depending on the walk distance.
			//(Used to make walking animation by mirroring the sprite)
		int flip1 = (walkDist >> 3) & 1;
		int flip2 = (walkDist >> 3) & 1;
		// similar statements to above have same explanation

		if (dir == 1) { // if the direction is 1 (Up)
			xt += 2; // then move the sprite over 2 tiles
		}
		if (dir > 1) { // if the direction is larger than 1 (left or right)...
			flip1 = 0;
			flip2 = ((walkDist >> 4) & 1);
			if (dir == 2) { // if the direction is 2 (left)
				flip1 = 1; // mirror the sprite
			}
			xt += 4 + ((walkDist >> 3) & 1) * 2; // animation based on walk distance
		}
		
		/* offset locations to start drawing the sprite relative to our position */
		int xo = x - 8; // horizontal
		int yo = y - 11; // vertical
		
		if (isSwimming()) {
			yo += 4; // y offset is moved up by 4
			int liquidColor = 0; // color of water / lava circle
			if (level.getTile(x / 16, y / 16) == Tile.water) {
				liquidColor = Color.get(-1, -1, 115, 335);
				if (tickTime / 8 % 2 == 0) liquidColor = Color.get(-1, 335, 5, 115);
			} else if (level.getTile(x / 16, y / 16) == Tile.lava) {
				liquidColor = Color.get(-1, -1, 500, 300);
				if (tickTime / 8 % 2 == 0) liquidColor = Color.get(-1, 300, 400, 500);
			}

			screen.render(xo + 0, yo + 3, 5 + 13 * 32, liquidColor, 0); // render the water graphic
			screen.render(xo + 8, yo + 3, 5 + 13 * 32, liquidColor, 1); // render the mirrored water graphic to the right.
		}
		
		if (attackTime > 0 && attackDir == 1) { // if currently attacking upwards...
			screen.render(xo + 0, yo - 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 0); //render left half-slash
			screen.render(xo + 8, yo - 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 1); //render right half-slash (mirror of left).
			if (attackItem != null) { // if the player has an item
				attackItem.renderIcon(screen, xo + 4, yo - 4); // then render the icon of the item.
			}
		}
		
		int col = 0; // color of the player
		if (level.dirtColor == 322) {
			if(Game.time == 0) col = col0;
			if(Game.time == 1) col = col1;
			if(Game.time == 2) col = col2;
			if(Game.time == 3) col = col3;
		} else col = col4;
		
		if (hurtTime > playerHurtTime - 10) { // if the player has just gotten hurt...
			col = Color.get(-1, 555, 555, 555); // make the sprite white.
		}
		
		if (activeItem instanceof FurnitureItem) { // if holding a piece of furniture
			yt += 2; // moves the y tile up 2. (for the player holding his hands up)
		}
		
		// render each corner of the sprite
		screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
		screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
		if (!isSwimming()) { // don't render the bottom half if swimming.
			screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
			screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
		}

		if (attackTime > 0 && attackDir == 2) { // if attacking to the left.... (same as above)
			screen.render(xo - 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 1);
			screen.render(xo - 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo - 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 3) { // attacking to the right
			screen.render(xo + 8 + 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 0);
			screen.render(xo + 8 + 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 8 + 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 0) { // attacking downwards
			screen.render(xo + 0, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			screen.render(xo + 8, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 4, yo + 8 + 4);
			}
		}
		
		 // renders the furniture if the player is holding one.
		if (activeItem instanceof FurnitureItem) {
			Furniture furniture = ((FurnitureItem) activeItem).furniture;
			furniture.x = x;
			furniture.y = yo;
			furniture.render(screen);
		}
	}
	
	/** What happens when the player interacts with a itemEntity */
	public void touchItem(ItemEntity itemEntity) {
		itemEntity.take(this); // calls the take() method in ItemEntity
		if (itemEntity.item.getName() == "arrow") {
			ac++; // if it's an arrow, then just add to arrow count, not inventory.
		} else if(activeItem != null && activeItem.getName() == itemEntity.item.getName() && activeItem instanceof ResourceItem && itemEntity.item instanceof ResourceItem) {
			// picked up item matches the one in your hand
			((ResourceItem)activeItem).count += ((ResourceItem)itemEntity.item).count;
		} else {
			inventory.add(itemEntity.item); // add item to inventory
		}
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
			if (level.getTile(x, y) == Tile.grass) { // player will only spawn on a grass tile.
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
		if (Game.currentLevel == 3) { // if on surface
			// set home coordinates
			homeSetX = this.x;
			homeSetY = this.y;
			hasSetHome = true; // confirm that home coordinates are indeed set
			Game.notifications.add("Set your home!"); // give success message
		} else { // can only set home on surface
			Game.notifications.add("Can't set home here!"); // give failure message
		}
	}

	public void goHome() {
		if (Game.currentLevel == 3) { // if on surface
			if (hasSetHome == true) {
				// move player to home coordinates
				this.x = homeSetX;
				this.y = homeSetY;
				if (ModeMenu.hardcore) hurt(this, 2, attackDir); // give penalty for using home if in hardcore mode.
				stamina = 0; // teleportation uses up all your stamina.
				Game.notifications.add("Home Sweet Home!"); // give success message
				if (ModeMenu.hardcore) Game.notifications.add("Mode penalty: -2 health"); // give penalty message
			} else {
				//can go home, but no home set.
				Game.notifications.add("You don't have a home!");
			}
		} else { // can only go home from surface
			Game.notifications.add("You can't go home from here!");
		}
	}
	
	/** finds a location to respawn the player after death. */
	public boolean respawn(Level level) {
		if (!(bedSpawn || level.getTile(spawnx, spawny) == Tile.grass))
			findStartPos(level); // if there's no bed to spawn from, and the stored coordinates don't point to a grass tile, then find a new point.
		
		// move the player to the spawnpoint
		this.x = spawnx * 16 + 8;
		this.y = spawny * 16 + 8;
		return true; // again, why the "return true"'s for methods that never return false?
	}
	
	/** Pays the stamina used for an action */
	public boolean payStamina(int cost) {
		if (potioneffects.containsKey("Energy")) return true; // if the player has the potion effect for infinite stamina, return true (without subtracting cost).
		else if (cost > stamina) return false; // if the player doesn't have enough stamina, then return false; failure.

		if (cost < 0) cost = 0; // error correction
		stamina -= cost; // subtract the cost from the current stamina
		return true; // success
	}
	
	/** What to call to change the level, properly. */
	public void changeLevel(int dir) {
		game.scheduleLevelChange(dir); // schedules a level change.
	}
	
	/** Gets the player's light radius underground */
	public int getLightRadius() {
		//if (Game.currentLevel == 3) return 0; // I don't want the player to have an automatic halo on the surface.
		
		float light = potioneffects.containsKey("Light") ? 2.5f : 1; // multiplier for the light potion effect.
		float r = 3 * light; // the radius of the light.

		if (Game.currentLevel == 5) r = 5 * light; // more light than usual on dungeon level.

		if (ModeMenu.creative) r = 12 * light; // creative mode light radius is much bigger; whole screen.
		
		if (activeItem != null && activeItem instanceof FurnitureItem) { // if player is holding furniture
			int rr = ((FurnitureItem) activeItem).furniture.getLightRadius(); // gets furniture light radius
			if (rr > r) r = rr; // brings player light up to furniture light, if less, since the furnture is not yet part of the level and so doesn't emit light even if it should.
		}

		return (int) r; // return light radius
	}
	
	/** What happens when the player dies */
	protected void die() {
		super.die(); // calls the die() method in Mob.java
		int lostscore = score / 3; // finds score penalty
		score -= lostscore; // subtracts score penalty
		game.setMultiplier(1);
		
		//make death chest
		Chest dc = new DeathChest();
		dc.x = this.x;
		dc.y = this.y;
		dc.inventory = this.inventory;
		if (activeItem != null) {
			dc.inventory.add(activeItem);
		}
		dc.inventory.removeItem(new PowerGloveItem());
		
		Game.levels[Game.currentLevel].add(dc);

		Sound.playerDeath.play();
	}
	
	/** What happens when the player touches an entity */
	protected void touchedBy(Entity entity) {
		if (!(entity instanceof Player)) { // prevents stack-overflow
			entity.touchedBy(this); // calls the other entity's touchedBy method.
		}
	}
	
	/** What happens when the player is hurt */
	protected void doHurt(int damage, int attackDir) {
		if (ModeMenu.creative) return; // can't get hurt in creative
		if (hurtTime > 0) return; // currently in hurt cooldown
		
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
			level.add(new TextParticle("" + damage, x, y, Color.get(-1, 333, 333, 333)));
			armor -= armorDam;
			if(armor <= 0) {
				healthDam -= armor; // adds armor damage overflow to health damage (minus b/c armor would be negative)
				armor = 0;
				armorDamageBuffer = 0; // ensures that new armor doesn't inherit partial breaking from this armor.
				curArmor = null; // removes armor
			}
		}
		if(healthDam > 0) {
			level.add(new TextParticle("" + damage, x, y, Color.get(-1, 504, 504, 504)));
			health -= healthDam;
		}
		
		// apply the appropriate knockback
		if (attackDir == 0) yKnockback = +6;
		if (attackDir == 1) yKnockback = -6;
		if (attackDir == 2) xKnockback = -6;
		if (attackDir == 3) xKnockback = +6;
		// set hurt and invulnerable times
		hurtTime = playerHurtTime;
	}
	
	
	/** What happens when the player wins */
	public void gameWon() {
		hurtTime = 60 * 5; // sets the invulnerable time to 300
		game.won(); // win the game
	}
}
