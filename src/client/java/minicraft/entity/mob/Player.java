package minicraft.entity.mob;

import minicraft.core.Game;
import minicraft.core.Updater;
import minicraft.core.World;
import minicraft.core.io.InputHandler;
import minicraft.core.io.Settings;
import minicraft.core.io.Sound;
import minicraft.entity.Arrow;
import minicraft.entity.ClientTickable;
import minicraft.entity.Direction;
import minicraft.entity.Entity;
import minicraft.entity.ItemEntity;
import minicraft.entity.ItemHolder;
import minicraft.entity.furniture.Bed;
import minicraft.entity.furniture.DeathChest;
import minicraft.entity.furniture.Furniture;
import minicraft.entity.furniture.Tnt;
import minicraft.entity.particle.Particle;
import minicraft.entity.particle.TextParticle;
import minicraft.gfx.Color;
import minicraft.gfx.Point;
import minicraft.gfx.Rectangle;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.gfx.SpriteLinker.LinkedSprite;
import minicraft.gfx.SpriteLinker.SpriteType;
import minicraft.item.ArmorItem;
import minicraft.item.FishingData;
import minicraft.item.FishingRodItem;
import minicraft.item.FurnitureItem;
import minicraft.item.Inventory;
import minicraft.item.Item;
import minicraft.item.Items;
import minicraft.item.PotionItem;
import minicraft.item.PotionType;
import minicraft.item.PowerGloveItem;
import minicraft.item.Recipes;
import minicraft.item.StackableItem;
import minicraft.item.TileItem;
import minicraft.item.ToolItem;
import minicraft.item.ToolType;
import minicraft.level.Level;
import minicraft.level.tile.Tile;
import minicraft.level.tile.Tiles;
import minicraft.network.Analytics;
import minicraft.saveload.Save;
import minicraft.screen.AchievementsDisplay;
import minicraft.screen.CraftingDisplay;
import minicraft.screen.InfoDisplay;
import minicraft.screen.LoadingDisplay;
import minicraft.screen.PauseDisplay;
import minicraft.screen.PlayerInvDisplay;
import minicraft.screen.SkinDisplay;
import minicraft.screen.WorldSelectDisplay;
import minicraft.util.AdvancementElement;
import minicraft.util.Logging;
import minicraft.util.Vector2;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class Player extends Mob implements ItemHolder, ClientTickable {
	protected InputHandler input;
	private static final int playerHurtTime = 30;
	public static final int INTERACT_DIST = 12;
	private static final int ATTACK_DIST = 20;

	private static final int mtm = 300; // Time given to increase multiplier before it goes back to 1.
	public static final int MAX_MULTIPLIER = 50; // Maximum score multiplier.

	public double moveSpeed = 1; // The number of coordinate squares to move; each tile is 16x16.
	private int score; // The player's score

	private int multipliertime = mtm; // Time left on the current multiplier.
	private int multiplier = 1; // Score multiplier

	// These 2 ints are ints saved from the first spawn - this way the spawn pos is always saved.
	public int spawnx = 0, spawny = 0; // These are stored as tile coordinates, not entity coordinates.

	// The maximum stats that the player can have.
	public static final int maxStat = 10;
	public static final int maxHealth = 30, maxStamina = maxStat, maxHunger = maxStat;
	public static int extraHealth = 0;
	public static int baseHealth = 10;
	public static final int maxArmor = 100;

	public static LinkedSprite[][] sprites;
	public static LinkedSprite[][] carrySprites;

	private final Inventory inventory;

	public Item activeItem;
	Item attackItem; // attackItem is useful again b/c of the power glove.
	private Item prevItem; // Holds the item held before using the POW glove.

	int attackTime;
	public Direction attackDir;

	private int onStairDelay; // The delay before changing levels.
	private int onFallDelay; // The delay before falling b/c we're on an InfiniteFallTile

	public int hunger, stamina, armor; // The current stats
	public int armorDamageBuffer;
	@Nullable
	public ArmorItem curArmor; // The color/type of armor to be displayed.

	private int staminaRecharge; // The ticks before charging a bolt of the player's stamina
	private static final int maxStaminaRecharge = 10; // Cutoff value for staminaRecharge
	public int staminaRechargeDelay; // The recharge delay ticks when the player uses up their stamina.

	private int hungerStamCnt, stamHungerTicks; // Tiers of hunger penalties before losing a burger.
	private static final int maxHungerTicks = 400; // The cutoff value for stamHungerTicks
	private static final int[] maxHungerStams = { 10, 7, 5 }; // TungerStamCnt required to lose a burger.
	private static final int[] hungerTickCount = { 120, 30, 10 }; // Ticks before decrementing stamHungerTicks.
	private static final int[] hungerStepCount = { 8, 3, 1 }; // Steps before decrementing stamHungerTicks.
	private static final int[] minStarveHealth = { 5, 3, 0 }; // Min hearts required for hunger to hurt you.
	private int stepCount; // Used to penalize hunger for movement.
	private int hungerChargeDelay; // The delay between each time the hunger bar increases your health
	private int hungerStarveDelay; // The delay between each time the hunger bar decreases your health

	public HashMap<PotionType, Integer> potioneffects; // The potion effects currently applied to the player
	public boolean showpotioneffects; // Whether to display the current potion effects on screen
	public boolean simpPotionEffects;
	public boolean renderGUI;
	public int questExpanding; // Lets the display keeps expanded.
	private int cooldowninfo; // Prevents you from toggling the info pane on and off super fast.
	private int regentick; // Counts time between each time the regen potion effect heals you.

	public int shirtColor = Color.get(1, 51, 51, 0); // Player shirt color.

	public boolean isFishing = false;
	public int maxFishingTicks = 120;
	public int fishingTicks = maxFishingTicks;
	public int fishingLevel;

	private LinkedSprite hudSheet;

	// Note: the player's health & max health are inherited from Mob.java

	public Player(@Nullable Player previousInstance, InputHandler input) {
		super(null, Player.baseHealth);

		x = 24;
		y = 24;
		this.input = input;
		// Since this implementation will be deleted by Better Creative Mode Inventory might not implemented correctly
		inventory = new Inventory() { // Registering all triggers to InventoryChanged.
			private void triggerTrigger() {
				AdvancementElement.AdvancementTrigger.InventoryChangedTrigger.INSTANCE.trigger(
					new AdvancementElement.AdvancementTrigger.InventoryChangedTrigger.InventoryChangedTriggerConditionHandler.InventoryChangedTriggerConditions(this)
				);
			}

			@Override
			public void clearInv() {
				super.clearInv();
				triggerTrigger();
			}

			@Override
			public Item remove(int idx) {
				Item item = super.remove(idx);
				triggerTrigger();
				return item;
			}

			@Override
			public @Nullable Item add(Item item) {
				Item res = super.add(item);
				triggerTrigger();
				return res;
			}

			@Override
			public void removeItems(Item given, int count) {
				super.removeItems(given, count);
				triggerTrigger();
			}

			@Override
			public void updateInv(String items) {
				super.updateInv(items);
				triggerTrigger();
			}
		};

		potioneffects = new HashMap<>();
		showpotioneffects = true;
		simpPotionEffects = false;
		renderGUI = true;

		cooldowninfo = 0;
		regentick = 0;
		questExpanding = 0;

		attackDir = dir;
		armor = 0;
		curArmor = null;
		armorDamageBuffer = 0;
		stamina = maxStamina;
		hunger = maxHunger;

		hungerStamCnt = maxHungerStams[Settings.getIdx("diff")];
		stamHungerTicks = maxHungerTicks;

		if (previousInstance != null) {
			spawnx = previousInstance.spawnx;
			spawny = previousInstance.spawny;
		}

		hudSheet = new LinkedSprite(SpriteType.Gui, "hud");

		updateSprites();
	}

	public int getMultiplier() {
		return Game.isMode("minicraft.settings.mode.score") ? multiplier : 1;
	}

	void resetMultiplier() {
		multiplier = 1;
		multipliertime = mtm;
	}

	public void addMultiplier(int value) {
		if (!Game.isMode("minicraft.settings.mode.score")) return;
		multiplier = Math.min(MAX_MULTIPLIER, multiplier + value);
		multipliertime = Math.max(multipliertime, mtm - 5);
	}

	public void tickMultiplier() {
		if ((!Updater.paused) && multiplier > 1) {
			if (multipliertime != 0) multipliertime--;
			if (multipliertime <= 0) resetMultiplier();
		}
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void addScore(int points) {
		score += points * getMultiplier();
	}

	/**
	 * Adds a new potion effect to the player.
	 * @param type Type of potion.
	 * @param duration How long the effect lasts.
	 */
	public void addPotionEffect(PotionType type, int duration) {
		potioneffects.put(type, duration);
	}

	/**
	 * Adds a potion effect to the player.
	 * @param type Type of effect.
	 */
	public void addPotionEffect(PotionType type) {
		addPotionEffect(type, type.duration);
	}

	/**
	 * Returns all the potion effects currently affecting the player.
	 * @return all potion effects on the player.
	 */
	public HashMap<PotionType, Integer> getPotionEffects() {
		return potioneffects;
	}

	@Override
	public void tick() {
		if (level == null || isRemoved()) return;
		if (Game.getDisplay() != null) return; // Don't tick player when menu is open
		if (input.getMappedKey("F3-Y").isClicked()) {
			World.scheduleLevelChange(1);
			return;
		} else if (input.getMappedKey("F3-H").isClicked()) {
			World.scheduleLevelChange(-1);
			return;
		}

		super.tick(); // Ticks Mob.java

		tickMultiplier();

		if ((baseHealth + extraHealth) > maxHealth) {
			extraHealth = maxHealth - 10;
			Logging.PLAYER.warn("Current Max Health is greater than Max Health, downgrading.");
		}

		if (potioneffects.size() > 0 && !Bed.inBed(this)) {
			for (PotionType potionType : potioneffects.keySet().toArray(new PotionType[0])) {
				if (potioneffects.get(potionType) <= 1) // If time is zero (going to be set to 0 in a moment)...
					PotionItem.applyPotion(this, potionType, false); // Automatically removes this potion effect.
				else
					potioneffects.put(potionType, potioneffects.get(potionType) - 1); // Otherwise, replace it with one less.
			}
		}

		if (isFishing) {
			if (!Bed.inBed(this) && !isSwimming()) {
				fishingTicks--;
				if (fishingTicks <= 0) {
					goFishing();
				}
			} else {
				isFishing = false;
				fishingTicks = maxFishingTicks;
			}
		}

		if (cooldowninfo > 0) cooldowninfo--;
		if (questExpanding > 0) questExpanding--;

		if (input.inputPressed("potionEffects") && cooldowninfo == 0) {
			cooldowninfo = 10;
			showpotioneffects = !showpotioneffects;
		}

		if (input.inputPressed("simpPotionEffects")) {
			simpPotionEffects = !simpPotionEffects;
		}

		if (input.inputPressed("toggleHUD")) {
			renderGUI = !renderGUI;
		}

		if (input.inputPressed("expandQuestDisplay")) {
			questExpanding = 30;
		}

		Tile onTile = level.getTile(x >> 4, y >> 4); // Gets the current tile the player is on.
		if (onTile == Tiles.get("Stairs Down") || onTile == Tiles.get("Stairs Up")) {
			if (onStairDelay <= 0) { // When the delay time has passed...
				World.scheduleLevelChange((onTile == Tiles.get("Stairs Up")) ? 1 : -1); // Decide whether to go up or down.
				onStairDelay = 10; // Resets delay, since the level has now been changed.
				return; // SKIPS the rest of the tick() method.
			}

			onStairDelay = 10; // Resets the delay, if on a stairs tile, but the delay is greater than 0. In other words, this prevents you from ever activating a level change on a stair tile, UNTIL you get off the tile for 10+ ticks.
		} else if (onStairDelay > 0)
			onStairDelay--; // Decrements stairDelay if it's > 0, but not on stair tile... does the player get removed from the tile beforehand, or something?

		if (onTile == Tiles.get("Infinite Fall") && !Game.isMode("minicraft.settings.mode.creative")) {
			if (onFallDelay <= 0) {
				World.scheduleLevelChange(-1);
				onFallDelay = 40;
				return;
			}
		} else if (onFallDelay > 0) onFallDelay--;

		if (Game.isMode("minicraft.settings.mode.creative")) {
			// Prevent stamina/hunger decay in creative mode.
			stamina = maxStamina;
			hunger = maxHunger;
		}

		// Remember: staminaRechargeDelay is a penalty delay for when the player uses up all their stamina.
		// staminaRecharge is the rate of stamina recharge, in some sort of unknown units.
		if (stamina <= 0 && staminaRechargeDelay == 0 && staminaRecharge == 0) {
			staminaRechargeDelay = 40; // Delay before resuming adding to stamina.
		}

		if (staminaRechargeDelay > 0 && stamina < maxStamina) staminaRechargeDelay--;

		if (staminaRechargeDelay == 0) {
			staminaRecharge++; // Ticks since last recharge, accounting for the time potion effect.

			if (isSwimming() && !potioneffects.containsKey(PotionType.Swim))
				staminaRecharge = 0; // Don't recharge stamina while swimming.

			// Recharge a bolt for each multiple of maxStaminaRecharge.
			while (staminaRecharge > maxStaminaRecharge) {
				staminaRecharge -= maxStaminaRecharge;
				if (stamina < maxStamina) stamina++; // Recharge one stamina bolt per "charge".
			}
		}

		int diffIdx = Settings.getIdx("diff");

		if (hunger < 0) hunger = 0; // Error correction

		if (stamina < maxStamina) {
			stamHungerTicks -= diffIdx; // Affect hunger if not at full stamina; this is 2 levels away from a hunger "burger".
			if (stamina == 0) stamHungerTicks -= diffIdx; // Double effect if no stamina at all.
		}

		// This if statement encapsulates the hunger system
		if (!Bed.inBed(this)) {
			if (hungerChargeDelay > 0) { // If the hunger is recharging health...
				stamHungerTicks -= 2 + diffIdx; // Penalize the hunger
				if (hunger == 0) stamHungerTicks -= diffIdx; // Further penalty if at full hunger
			}

			if (Updater.tickCount % Player.hungerTickCount[diffIdx] == 0)
				stamHungerTicks--; // hunger due to time.

			if (stepCount >= Player.hungerStepCount[diffIdx]) {
				stamHungerTicks--; // hunger due to exercise.
				stepCount = 0; // reset.
			}

			if (stamHungerTicks <= 0) {
				stamHungerTicks += maxHungerTicks; // Reset stamHungerTicks
				hungerStamCnt--; // Enter 1 level away from burger.
			}

			while (hungerStamCnt <= 0) {
				hunger--; // Reached burger level.
				hungerStamCnt += maxHungerStams[diffIdx];
			}

			/// System that heals you depending on your hunger
			if (health < (baseHealth + extraHealth) && hunger > maxHunger / 2) {
				hungerChargeDelay++;
				if (hungerChargeDelay > 20 * Math.pow(maxHunger - hunger + 2, 2)) {
					health++;
					hungerChargeDelay = 0;
				}
			} else hungerChargeDelay = 0;

			if (hungerStarveDelay == 0) {
				hungerStarveDelay = 120;
			}

			if (hunger == 0 && health > minStarveHealth[diffIdx]) {
				if (hungerStarveDelay > 0) hungerStarveDelay--;
				if (hungerStarveDelay == 0) {
					directHurt(1, Direction.NONE); // Do 1 damage to the player
				}
			}
		}

		// regen health
		if (potioneffects.containsKey(PotionType.Regen)) {
			regentick++;
			if (regentick > 60) {
				regentick = 0;
				if (health < 10) {
					health++;
				}
			}
		}

		if (Updater.savecooldown > 0 && !Updater.saving)
			Updater.savecooldown--;


		// Handle player input. Input is handled by the menu if we are in one.
		if (Game.getDisplay() == null && !Bed.inBed(this)) {
			// Create the raw movement vector.
			Vector2 vec = new Vector2(0, 0);

			// Move while we are not falling.
			if (onFallDelay <= 0) {
				// controlInput.buttonPressed is used because otherwise the player will move one even if held down.
				if (input.inputDown("move-up")) vec.y--;
				if (input.inputDown("move-down")) vec.y++;
				if (input.inputDown("move-left")) vec.x--;
				if (input.inputDown("move-right")) vec.x++;


			}

			// Executes if not saving; and... essentially halves speed if out of stamina.
			if ((vec.x != 0 || vec.y != 0) && (staminaRechargeDelay % 2 == 0 || isSwimming()) && !Updater.saving) {
				double spd = moveSpeed * (potioneffects.containsKey(PotionType.Speed) ? 1.5D : 1);
				int xd = (int) (vec.x * spd);
				int yd = (int) (vec.y * spd);

				Direction newDir = Direction.getDirection(xd, yd);
				if (newDir == Direction.NONE) newDir = dir;

				// Move the player
				boolean moved = move(xd, yd); // THIS is where the player moves; part of Mob.java
				if (moved) stepCount++;
			}


			if (isSwimming() && tickTime % 60 == 0 && !potioneffects.containsKey(PotionType.Swim)) { // If drowning... :P
				if (stamina > 0) payStamina(1); // Take away stamina
				else directHurt(1, Direction.NONE); // If no stamina, take damage.
			}

			if (activeItem != null && (input.inputPressed("drop-one") || input.inputPressed("drop-stack"))) {
				Item drop = activeItem.copy();

				if (!input.inputPressed("drop-stack") || !(drop instanceof StackableItem) || ((StackableItem) drop).count <= 1) {
					activeItem = null; // Remove it from the "inventory"
					if (isFishing) {
						isFishing = false;
						fishingTicks = maxFishingTicks;
					}
				} else {
					// Drop one from stack
					((StackableItem) activeItem).count--;
					((StackableItem) drop).count = 1;
				}

				level.dropItem(x, y, drop);
			}

			if ((activeItem == null || !activeItem.used_pending) && (input.inputPressed("attack")) && stamina != 0 && onFallDelay <= 0) { // This only allows attacks when such action is possible.
				if (!potioneffects.containsKey(PotionType.Energy)) stamina--;
				staminaRecharge = 0;

				attack();
			}

			if ((input.inputPressed("menu") || input.inputPressed("craft")) && activeItem != null) {
				tryAddToInvOrDrop(activeItem);

				activeItem = null;
				if (isFishing) {
					isFishing = false;
					fishingTicks = maxFishingTicks;
				}
			}

			if (Game.getDisplay() == null) {
				if (input.inputPressed("craft") && !use()) {
					Game.setDisplay(new CraftingDisplay(Recipes.craftRecipes, "minicraft.displays.crafting", this, true));
					return;
				} else if (input.inputPressed("menu") && !use()) { // !use() = no furniture in front of the player; this prevents player inventory from opening (will open furniture inventory instead)
					Game.setDisplay(new PlayerInvDisplay(this));
					return;
				} else if (input.inputPressed("pause")) {
					Game.setDisplay(new PauseDisplay());
					return;
				} else if (input.inputDown("info")) {
					Game.setDisplay(new InfoDisplay());
					return;
				}

				if (input.inputDown("quicksave") && !Updater.saving) {
					Updater.saving = true;
					LoadingDisplay.setPercentage(0);
					new Save(WorldSelectDisplay.getWorldName());
				}
				//debug feature:
				if (input.inputDown("F3-p")) { // Remove all potion effects
					for (PotionType potionType : potioneffects.keySet()) {
						PotionItem.applyPotion(this, potionType, false);
					}
				}

				if (input.inputPressed("pickup") && (activeItem == null || !activeItem.used_pending)) {
					if (!(activeItem instanceof PowerGloveItem)) { // If you are not already holding a power glove (aka in the middle of a separate interaction)...
						prevItem = activeItem; // Then save the current item...
						if (isFishing) {
							isFishing = false;
							fishingTicks = maxFishingTicks;
						}
						activeItem = new PowerGloveItem(); // and replace it with a power glove.
					}
					attack(); // Attack (with the power glove)
					resolveHeldItem();
				}
			}

			if (attackTime > 0) {
				attackTime--;
				if (attackTime == 0) attackItem = null; // null the attackItem once we are done attacking.
			}
		}
	}

	/**
	 * Removes an held item and places it back into the inventory.
	 * Looks complicated to so it can handle the powerglove.
	 */
	public void resolveHeldItem() {
		if (!(activeItem instanceof PowerGloveItem)) { // If you are now holding something other than a power glove...
			if (prevItem != null) { // and you had a previous item that we should care about...
				tryAddToInvOrDrop(prevItem); // Then add that previous item to your inventory so it isn't lost.
			} // If something other than a power glove is being held, but the previous item is null, then nothing happens; nothing added to inventory, and current item remains as the new one.
		} else
			activeItem = prevItem; // Otherwise, if you're holding a power glove, then the held item didn't change, so we can remove the power glove and make it what it was before.

		prevItem = null; // This is no longer of use.

		if (activeItem instanceof PowerGloveItem) // If, for some odd reason, you are still holding a power glove at this point, then null it because it's useless and shouldn't remain in hand.
			activeItem = null;
	}

	/**
	 * This method is called when we press the attack button.
	 */
	protected void attack() {
		// walkDist is not synced, so this can happen for both the client and server.
		walkDist += 8; // Increase the walkDist (changes the sprite, like you moved your arm)

		if (isFishing) {
			isFishing = false;
			fishingTicks = maxFishingTicks;
		}

		if (activeItem != null && !activeItem.interactsWithWorld()) {
			attackDir = dir; // Make the attack direction equal the current direction
			attackItem = activeItem; // Make attackItem equal activeItem
			activeItem.interactOn(Tiles.get("rock"), level, 0, 0, this, attackDir);
			if (activeItem.isDepleted()) {
				activeItem = null;
				if (isFishing) {
					isFishing = false;
					fishingTicks = maxFishingTicks;
				}
			}
			return;
		}

		attackDir = dir; // Make the attack direction equal the current direction
		attackItem = activeItem; // Make attackItem equal activeItem

		// If we are holding an item.
		if (activeItem != null) {
			attackTime = 10;
			boolean done = false;

			// Fire a bow if we have the stamina and an arrow.
			if (activeItem instanceof ToolItem && stamina - 1 >= 0) {
				ToolItem tool = (ToolItem) activeItem;
				if (tool.type == ToolType.Bow && tool.dur > 0 && inventory.count(Items.arrowItem) > 0) {

					inventory.removeItem(Items.arrowItem);
					level.add(new Arrow(this, attackDir, tool.level));
					attackTime = 10;

					if (!Game.isMode("minicraft.settings.mode.creative")) tool.dur--;

					AchievementsDisplay.setAchievement("minicraft.achievement.bow", true);

					return;
				}
			}

			// If the interaction between you and an entity is successful, then return.
			if (interact(getInteractionBox(INTERACT_DIST))) {
				if (activeItem.isDepleted())
					activeItem = null;
				return;
			}

			// Attempt to interact with the tile.
			Point t = getInteractionTile();

			// If the target coordinates are a valid tile.
			if (t.x >= 0 && t.y >= 0 && t.x < level.w && t.y < level.h) {

				// Get any entities (except dropped items and particles) on the tile.
				List<Entity> tileEntities = level.getEntitiesInTiles(t.x, t.y, t.x, t.y, false, ItemEntity.class, Particle.class);

				// If there are no other entities than us on the tile.
				if (tileEntities.size() == 0 || tileEntities.size() == 1 && tileEntities.get(0) == this) {
					Tile tile = level.getTile(t.x, t.y);

					// If the item successfully interacts with the target tile.
					if (activeItem.interactOn(tile, level, t.x, t.y, this, attackDir)) {
						done = true;

						// Returns true if the target tile successfully interacts with the item.
					} else if (tile.interact(level, t.x, t.y, this, activeItem, attackDir)) {
						done = true;
					}
				}

				if (activeItem.isDepleted()) {
					// If the activeItem has 0 items left, then "destroy" it.
					activeItem = null;
					if (isFishing) {
						isFishing = false;
						fishingTicks = maxFishingTicks;
					}
				}
			}
			if (done) return; // Skip the rest if interaction was handled
		}

		if (activeItem == null || activeItem.canAttack()) { // If there is no active item, OR if the item can be used to attack...
			attackTime = 5;
			// Attacks the enemy in the appropriate direction.
			boolean used = hurt(getInteractionBox(ATTACK_DIST));

			// Attempts to hurt the tile in the appropriate direction.
			Point t = getInteractionTile();

			// Check if tile is in bounds of the map.
			if (t.x >= 0 && t.y >= 0 && t.x < level.w && t.y < level.h) {
				Tile tile = level.getTile(t.x, t.y);
				used = tile.hurt(level, t.x, t.y, this, random.nextInt(3) + 1, attackDir) || used;
			}

			if (used && activeItem instanceof ToolItem)
				((ToolItem) activeItem).payDurability();
		}
	}

	private Rectangle getInteractionBox(int range) {
		int x = this.x, y = this.y - 2;

		//noinspection UnnecessaryLocalVariable
		int paraClose = 4, paraFar = range;
		int perpClose = 0, perpFar = 8;

		int xClose = x + dir.getX() * paraClose + dir.getY() * perpClose;
		int yClose = y + dir.getY() * paraClose + dir.getX() * perpClose;
		int xFar = x + dir.getX() * paraFar + dir.getY() * perpFar;
		int yFar = y + dir.getY() * paraFar + dir.getX() * perpFar;

		return new Rectangle(Math.min(xClose, xFar), Math.min(yClose, yFar), Math.max(xClose, xFar), Math.max(yClose, yFar), Rectangle.CORNERS);
	}

	private Point getInteractionTile() {
		int x = this.x, y = this.y - 2;

		x += dir.getX() * INTERACT_DIST;
		y += dir.getY() * INTERACT_DIST;

		return new Point(x >> 4, y >> 4);
	}

	private void goFishing() {
		int fcatch = random.nextInt(100);

		boolean caught = false;

		// Figure out which table to roll for
		List<String> data = null;
		if (fcatch > FishingRodItem.getChance(0, fishingLevel)) {
			data = FishingData.fishData;
		} else if (fcatch > FishingRodItem.getChance(1, fishingLevel)) {
			data = FishingData.junkData;
		} else if (fcatch > FishingRodItem.getChance(2, fishingLevel)) {
			data = FishingData.toolData;
		} else if (fcatch >= FishingRodItem.getChance(3, fishingLevel)) {
			data = FishingData.rareData;
		}

		if (data != null) { // If you've caught something
			for (String line : data) {

				// Check all the entries in the data
				// The number is a percent, if one fails, it moves down the list
				// For entries with a "," it chooses between the options

				int chance = Integer.parseInt(line.split(":")[0]);
				String itemData = line.split(":")[1];
				if (random.nextInt(100) + 1 <= chance) {
					if (itemData.contains(",")) { // If it has multiple items choose between them
						String[] extendedData = itemData.split(",");
						int randomChance = random.nextInt(extendedData.length);
						itemData = extendedData[randomChance];
					}
					if (itemData.startsWith(";")) {
						// For secret messages :=)
						Game.notifications.add(itemData.substring(1));
					} else {
						if (Items.get(itemData).equals(Items.get("Raw Fish"))) {
							AchievementsDisplay.setAchievement("minicraft.achievement.fish", true);
						}
						level.dropItem(x, y, Items.get(itemData));
						caught = true;
						break; // Don't let people catch more than one thing with one use
					}
				}
			}
		} else {
			caught = true; // End this fishing session
		}

		if (caught) {
			isFishing = false;
		}
		fishingTicks = maxFishingTicks; // If you didn't catch anything, try again in 120 ticks
	}

	private boolean use() {
		return use(getInteractionBox(INTERACT_DIST));
	}

	/**
	 * called by other use method; this serves as a buffer in case there is no entity in front of the player.
	 */
	private boolean use(Rectangle area) {
		List<Entity> entities = level.getEntitiesInRect(area); // Gets the entities within the 4 points
		for (Entity e : entities) {
			if (e instanceof Furniture && ((Furniture) e).use(this))
				return true; // If the entity is not the player, then call it's use method, and return the result. Only some furniture classes use this.
		}
		return false;
	}

	/**
	 * same, but for interaction.
	 */
	private boolean interact(Rectangle area) {
		List<Entity> entities = level.getEntitiesInRect(area);
		for (Entity e : entities) {
			if (e != this && e.interact(this, activeItem, attackDir))
				return true; // This is the ONLY place that the Entity.interact method is actually called.
		}
		return false;
	}

	/**
	 * same, but for attacking.
	 */
	private boolean hurt(Rectangle area) {
		List<Entity> entities = level.getEntitiesInRect(area);
		int maxDmg = 0;
		for (Entity e : entities) {
			if (e != this && e instanceof Mob) {
				int dmg = getAttackDamage(e);
				maxDmg = Math.max(dmg, maxDmg);
				((Mob) e).hurt(this, dmg, attackDir);
			}
			if (e instanceof Furniture)
				e.interact(this, null, attackDir);
		}
		return maxDmg > 0;
	}

	/**
	 * Calculates how much damage the player will do.
	 * @param e Entity being attacked.
	 * @return How much damage the player does.
	 */
	private int getAttackDamage(Entity e) {
		int dmg = random.nextInt(2) + 1;
		if (activeItem != null && activeItem instanceof ToolItem) {
			dmg += ((ToolItem) activeItem).getAttackDamageBonus(e); // Sword/Axe are more effective at dealing damage.
		}
		return dmg;
	}

	/**
	 * Updates the sprite to render on demand.
	 */
	public void updateSprites() {
		// Get the current skin we are using as a MobSprite array.
		LinkedSprite[][][] selectedSkin = SkinDisplay.getSkinAsMobSprite();

		// Assign the skin to the states.
		sprites = selectedSkin[0];
		carrySprites = selectedSkin[1];
	}

	@Override
	public void render(Screen screen) {
		/* Offset locations to start drawing the sprite relative to our position */
		int xo = x - 8; // Horizontal
		int yo = y - 11; // Vertical

		// Renders swimming
		if (isSwimming() && onFallDelay <= 0) {
			yo += 4; // y offset is moved up by 4
			if (level.getTile(x >> 4, y >> 4) == Tiles.get("water")) {

				// animation effect
				if (tickTime / 8 % 2 == 0) {
					screen.render(xo + 0, yo + 3, 5, 0, 0, hudSheet.getSheet()); // Render the water graphic
					screen.render(xo + 8, yo + 3, 5, 0, 1, hudSheet.getSheet()); // Render the mirrored water graphic to the right.
				} else {
					screen.render(xo + 0, yo + 3, 5, 1, 0, hudSheet.getSheet());
					screen.render(xo + 8, yo + 3, 5, 1, 1, hudSheet.getSheet());
				}

			} else if (level.getTile(x >> 4, y >> 4) == Tiles.get("lava")) {

				if (tickTime / 8 % 2 == 0) {
					screen.render(xo + 0, yo + 3, 6, 0, 1, hudSheet.getSheet()); // Render the lava graphic
					screen.render(xo + 8, yo + 3, 6, 0, 0, hudSheet.getSheet()); // Render the mirrored lava graphic to the right.
				} else {
					screen.render(xo + 0, yo + 3, 6, 1, 1, hudSheet.getSheet());
					screen.render(xo + 8, yo + 3, 6, 1, 0, hudSheet.getSheet());
				}
			}
		}

		// Renders indicator for what tile the item will be placed on
		if (activeItem instanceof TileItem && !isSwimming()) {
			Point t = getInteractionTile();
			screen.render(t.x * 16, t.y * 16, 3, 2, 0, hudSheet.getSheet());
			screen.render(t.x * 16 + 8, t.y * 16, 3, 2, 1, hudSheet.getSheet());
			screen.render(t.x * 16, t.y * 16 + 8, 3, 2, 2, hudSheet.getSheet());
			screen.render(t.x * 16 + 8, t.y * 16 + 8, 3, 2, 3, hudSheet.getSheet());
		}

		// Makes the player white if they have just gotten hurt
		if (hurtTime > playerHurtTime - 10) {
			col = Color.WHITE; // Make the sprite white.
		}

		LinkedSprite[][] spriteSet = activeItem instanceof FurnitureItem ? carrySprites : sprites;

		// Renders falling
		LinkedSprite curSprite;
		if (onFallDelay > 0) {
			// This makes falling look really cool.
			int spriteToUse = Math.round(onFallDelay / 2f) % carrySprites.length;
			curSprite = carrySprites[spriteToUse][(walkDist >> 3) & 1];
			screen.render(xo, yo - 4 * onFallDelay, curSprite.setColor(shirtColor));
		} else {
			curSprite = spriteSet[dir.getDir()][(walkDist >> 3) & 1]; // Gets the correct sprite to render.
			// Render each corner of the sprite
			if (isSwimming()) {
				Sprite sprite = curSprite.getSprite();
				screen.render(xo, yo, sprite.spritePixels[0][0], shirtColor);
				screen.render(xo + 8, yo, sprite.spritePixels[0][1], shirtColor);
			} else { // Don't render the bottom half if swimming.
				screen.render(xo, yo - 4 * onFallDelay, curSprite.setColor(shirtColor));
			}
		}

		// Renders slashes:
		if (attackTime > 0) {
			switch (attackDir) {
				case UP:  // If currently attacking upwards...
					screen.render(xo + 0, yo - 4, 3, 0, 0, hudSheet.getSheet()); // Render left half-slash
					screen.render(xo + 8, yo - 4, 3, 0, 1, hudSheet.getSheet()); // Render right half-slash (mirror of left).
					if (attackItem != null && !(attackItem instanceof PowerGloveItem)) { // If the player had an item when they last attacked...
						screen.render(xo + 4, yo - 4, attackItem.sprite.getSprite(), 1, false); // Then render the icon of the item, mirrored
					}
					break;
				case LEFT:  // Attacking to the left... (Same as above)
					screen.render(xo - 4, yo, 4, 0, 1, hudSheet.getSheet());
					screen.render(xo - 4, yo + 8, 4, 0, 3, hudSheet.getSheet());
					if (attackItem != null && !(attackItem instanceof PowerGloveItem)) {
						screen.render(xo - 4, yo + 4, attackItem.sprite.getSprite(), 1, false);
					}
					break;
				case RIGHT:  // Attacking to the right (Same as above)
					screen.render(xo + 8 + 4, yo, 4, 0, 0, hudSheet.getSheet());
					screen.render(xo + 8 + 4, yo + 8, 4, 0, 2, hudSheet.getSheet());
					if (attackItem != null && !(attackItem instanceof PowerGloveItem)) {
						screen.render(xo + 8 + 4, yo + 4, attackItem.sprite.getSprite());
					}
					break;
				case DOWN:  // Attacking downwards (Same as above)
					screen.render(xo + 0, yo + 8 + 4, 3, 0, 2, hudSheet.getSheet());
					screen.render(xo + 8, yo + 8 + 4, 3, 0, 3, hudSheet.getSheet());
					if (attackItem != null && !(attackItem instanceof PowerGloveItem)) {
						screen.render(xo + 4, yo + 8 + 4, attackItem.sprite.getSprite());
					}
					break;
				case NONE:
					break;
			}
		}

		// Renders the fishing rods when fishing
		if (isFishing) {
			switch (dir) {
				case UP:
					screen.render(xo + 4, yo - 4, activeItem.sprite.getSprite(), 1, false);
					break;
				case LEFT:
					screen.render(xo - 4, yo + 4, activeItem.sprite.getSprite(), 1, false);
					break;
				case RIGHT:
					screen.render(xo + 8 + 4, yo + 4, activeItem.sprite.getSprite());
					break;
				case DOWN:
					screen.render(xo + 4, yo + 8 + 4, activeItem.sprite.getSprite());
					break;
				case NONE:
					break;
			}
		}

		// Renders the furniture if the player is holding one.
		if (activeItem instanceof FurnitureItem) {
			Furniture furniture = ((FurnitureItem) activeItem).furniture;
			furniture.x = x;
			furniture.y = yo - 4;
			furniture.render(screen);
		}
	}

	/** What happens when the player interacts with a itemEntity */
	public void pickupItem(ItemEntity itemEntity) {
		boolean successful = false; // If there is any item successfully added to the player
		boolean remove = false; // Whether to remove the item entity (when empty)
		if (itemEntity.item instanceof StackableItem && ((StackableItem) itemEntity.item).stacksWith(activeItem)) { // Picked up item equals the one in your hand
			int toAdd = Math.min(((StackableItem) activeItem).count + ((StackableItem) itemEntity.item).count, ((StackableItem) activeItem).maxCount)
				- ((StackableItem) activeItem).count;
			if (toAdd > 0) {
				((StackableItem) activeItem).count += toAdd;
				((StackableItem) itemEntity.item).count -= toAdd;
				successful = true;
			}
			if (((StackableItem) itemEntity.item).count == 0) { // Empty
				remove = true; // Remove the item entity
			}
		}

		if (!(itemEntity.item instanceof StackableItem && ((StackableItem) itemEntity.item).count == 0)) {
			// Add item to inventory
			Item remaining;
			if (itemEntity.item instanceof StackableItem) {
				int orig = ((StackableItem) itemEntity.item).count;
				remaining = inventory.add(itemEntity.item);
				if (remaining != null && ((StackableItem) remaining).count != orig) {
					successful = true;
				}
			} else remaining = inventory.add(itemEntity.item);
			if (remaining == null) {
				successful = remove = true;
			}
		}

		if (remove) itemEntity.remove();
		if (successful) {
			Sound.play("pickup");
			addScore(1);
		}
	}

	// The player can swim.
	public boolean canSwim() {
		return true;
	}

	// Can walk on wool tiles..? quickly..?
	public boolean canWool() {
		return true;
	}

	/**
	 * Finds a starting position for the player.
	 * @param level Level which the player wants to start in.
	 * @param spawnSeed Spawn seed.
	 */
	public void findStartPos(Level level, long spawnSeed) {
		random.setSeed(spawnSeed);
		findStartPos(level);
	}

	/**
	 * Finds the starting position for the player in a level.
	 * @param level The level.
	 */
	public void findStartPos(Level level) {
		findStartPos(level, true);
	}

	public void findStartPos(Level level, boolean setSpawn) {
		Point spawnPos;

		List<Point> spawnTilePositions = level.getMatchingTiles(Tiles.get("grass"));

		if (spawnTilePositions.size() == 0)
			spawnTilePositions.addAll(level.getMatchingTiles((t, x, y) -> t.maySpawn()));

		if (spawnTilePositions.size() == 0)
			spawnTilePositions.addAll(level.getMatchingTiles((t, x, y) -> t.mayPass(level, x, y, Player.this)));

		// There are no tiles in the entire map which the player is allowed to stand on. Not likely.
		if (spawnTilePositions.size() == 0) {
			spawnPos = new Point(random.nextInt(level.w / 4) + level.w * 3 / 8, random.nextInt(level.h / 4) + level.h * 3 / 8);
			level.setTile(spawnPos.x, spawnPos.y, Tiles.get("grass"));
		} else { // Gets random valid spawn tile position.
			spawnPos = spawnTilePositions.get(random.nextInt(spawnTilePositions.size()));
		}

		if (setSpawn) {
			// Used to save (tile) coordinates of spawn point outside this method.
			spawnx = spawnPos.x;
			spawny = spawnPos.y;
		}

		// Set (entity) coordinates of player to the center of the tile.
		this.x = spawnPos.x * 16 + 8; // conversion from tile coords to entity coords.
		this.y = spawnPos.y * 16 + 8;
	}

	/**
	 * Finds a location where the player can respawn in a given level.
	 * @param level The level.
	 */
	public void respawn(Level level) {
		if (!level.getTile(spawnx, spawny).maySpawn()) {
			findStartPos(level); // If there's no bed to spawn from, and the stored coordinates don't point to a grass tile, then find a new point.
		}

		// Move the player to the spawn point
		this.x = spawnx * 16 + 8;
		this.y = spawny * 16 + 8;
	}

	/**
	 * Uses an amount of stamina to do an action.
	 * @param cost How much stamina the action requires.
	 * @return true if the player had enough stamina, false if not.
	 */
	public boolean payStamina(int cost) {
		if (potioneffects.containsKey(PotionType.Energy))
			return true; // If the player has the potion effect for infinite stamina, return true (without subtracting cost).
		else if (stamina <= 0) return false; // If the player doesn't have enough stamina, then return false; failure.

		if (cost < 0) cost = 0; // Error correction
		stamina -= Math.min(stamina, cost); // Subtract the cost from the current stamina
		return true; // Success
	}

	/**
	 * Gets the player's light radius underground
	 */
	@Override
	public int getLightRadius() {
		int r = 5; // The radius of the light.

		if (activeItem != null && activeItem instanceof FurnitureItem) { // If player is holding furniture
			int rr = ((FurnitureItem) activeItem).furniture.getLightRadius(); // Gets furniture light radius
			if (rr > r)
				r = rr; // Brings player light up to furniture light, if less, since the furnture is not yet part of the level and so doesn't emit light even if it should.
		}

		return r; // Return light radius
	}

	/**
	 * What happens when the player dies
	 */
	@Override
	public void die() {
		Analytics.SinglePlayerDeath.ping();

		score -= score / 3; // Subtracts score penalty (minus 1/3 of the original score)
		resetMultiplier();

		// Make death chest
		DeathChest dc = new DeathChest(this);

		if (activeItem != null) dc.getInventory().add(activeItem);
		if (curArmor != null) dc.getInventory().add(curArmor);

		Sound.play("death");

		// Add the death chest to the world.
		World.levels[Game.currentLevel].add(dc);

		super.die(); // Calls the die() method in Mob.java
	}

	@Override
	public void onExploded(Tnt tnt, int dmg) {
		super.onExploded(tnt, dmg);
		payStamina(dmg * 2);
	}

	/**
	 * Hurt the player.
	 * @param damage How much damage to do to player.
	 * @param attackDir What direction to attack.
	 */
	public void hurt(int damage, Direction attackDir) {
		doHurt(damage, attackDir);
	}

	@Override
	protected void doHurt(int damage, Direction attackDir) {
		if (Game.isMode("minicraft.settings.mode.creative") || hurtTime > 0 || Bed.inBed(this))
			return; // Can't get hurt in creative, hurt cooldown, or while someone is in bed

		int healthDam = 0, armorDam = 0;
		if (this == Game.player) {
			if (curArmor == null) { // No armor
				healthDam = damage; // Subtract that amount
			} else { // Has armor
				armorDamageBuffer += damage;
				armorDam += damage;

				while (armorDamageBuffer >= curArmor.level + 1) {
					armorDamageBuffer -= curArmor.level + 1;
					healthDam++;
				}
			}

			// Adds a text particle telling how much damage was done to the player, and the armor.
			if (armorDam > 0) {
				level.add(new TextParticle("" + damage, x, y, Color.GRAY));
				armor -= armorDam;
				if (armor <= 0) {
					healthDam -= armor; // Adds armor damage overflow to health damage (minus b/c armor would be negative)
					armor = 0;
					armorDamageBuffer = 0; // Ensures that new armor doesn't inherit partial breaking from this armor.
					curArmor = null; // Removes armor
				}
			}
		}

		if (healthDam > 0 || this != Game.player) {
			level.add(new TextParticle("" + damage, x, y, Color.get(-1, 504)));
			if (this == Game.player) super.doHurt(healthDam, attackDir); // Sets knockback, and takes away health.
		}

		Sound.play("playerhurt");
		hurtTime = playerHurtTime;
	}

	/**
	 * Hurt the player directly. Don't use the armor as a shield.
	 * @param damage Amount of damage to do to player
	 * @param attackDir The direction of attack.
	 */
	private void directHurt(int damage, Direction attackDir) {
		if (Game.isMode("minicraft.settings.mode.creative") || hurtTime > 0 || Bed.inBed(this))
			return; // Can't get hurt in creative, hurt cooldown, or while someone is in bed

		int healthDam = 0;
		if (this == Game.player) {
			healthDam = damage; // Subtract that amount
		}

		if (healthDam > 0 || this != Game.player) {
			level.add(new TextParticle("" + damage, x, y, Color.get(-1, 504)));
			if (this == Game.player) super.doHurt(healthDam, attackDir); // Sets knockback, and takes away health.
		}

		Sound.play("playerhurt");
		hurtTime = playerHurtTime;
	}

	@Override
	public void remove() {
		Logging.WORLD.trace("Removing player from level " + getLevel());
		super.remove();
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	public String getDebugHunger() {
		return hungerStamCnt + "_" + stamHungerTicks;
	}

	/**
	 * Trying to add a stack of item(s) to the top of player inventory.
	 * If there is/are no more item(s) can be added to the inventory, drop the item(s) near the player.
	 */
	public void tryAddToInvOrDrop(@Nullable Item item) {
		if (item != null) {
			if (inventory.add(item) != null) {
				getLevel().dropItem(x, y, item);
			}
		}
	}
}
