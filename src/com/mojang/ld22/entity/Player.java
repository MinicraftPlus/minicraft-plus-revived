//respawn mod +dillyg10+
//LOOK AT DECOMP PLAYER.java! ESPECIALLY AROUND LINE 500 there, and 400 here! lots of stuff.

package com.mojang.ld22.entity;

import java.util.List;

import com.mojang.ld22.Game;
import com.mojang.ld22.InputHandler;
import com.mojang.ld22.crafting.Crafting;
import com.mojang.ld22.entity.particle.TextParticle;
import com.mojang.ld22.gfx.Color;
import com.mojang.ld22.gfx.Font;
import com.mojang.ld22.gfx.Screen;
import com.mojang.ld22.item.BookItem;
import com.mojang.ld22.item.BucketItem;
import com.mojang.ld22.item.BucketLavaItem;
import com.mojang.ld22.item.BucketWaterItem;
import com.mojang.ld22.item.FurnitureItem;
import com.mojang.ld22.item.Item;
import com.mojang.ld22.item.PowerGloveItem;
import com.mojang.ld22.item.ResourceItem;
import com.mojang.ld22.item.ToolItem;
import com.mojang.ld22.item.ToolType;
import com.mojang.ld22.item.resource.ItemResource;
import com.mojang.ld22.item.resource.Resource;
import com.mojang.ld22.item.ListItems;
import com.mojang.ld22.level.Level;
import com.mojang.ld22.level.tile.Tile;
import com.mojang.ld22.screen.CraftInvMenu;
import com.mojang.ld22.screen.HomeMenu;
import com.mojang.ld22.screen.InfoMenu;
import com.mojang.ld22.screen.InventoryMenu;
import com.mojang.ld22.screen.PauseMenu;
import com.mojang.ld22.screen.StartMenu;
import com.mojang.ld22.screen.LoadingMenu;
import com.mojang.ld22.screen.WorldSelectMenu;
import com.mojang.ld22.sound.Sound;
import com.mojang.ld22.screen.ModeMenu;
import com.mojang.ld22.screen.PlayerInfoMenu;
import com.mojang.ld22.saveload.Save;

public class Player extends Mob {
	private InputHandler input;
	public int attackTime, attackDir;
	public boolean energy = false;
	public Game game;
	public Inventory inventory = new Inventory();
	public static Inventory Sinventory;
	public Item attackItem;
	public Item activeItem;
	public int stamina;
	public int staminaRecharge;
	public int staminaRechargeDelay;
	public static int score;
	public int maxStamina = 10;
	public int maxArmor = 0;
	public int maxHunger = 10;
	public int hunger;
	public int homeSetX; 
	public int homeSetY; 
	public static int SHealth = 10;
	public static int SHunger = 10;
	public static boolean hasSetHome;
	public static boolean canSetHome;
	public static boolean canGoHome;
	public static boolean sentFromSetHome;
	public static boolean sentFromHome;
	//These 2 ints are ints saved from the first spawn - this way the spawn pos is always saved.
	public static int spawnx = 0; 
	public static int spawny = 0;
	private int onStairDelay;
	int tickCounter = 0;
	int hungerChargeDelay;
	int hungerStarveDelay;
	public int hungStamCnt;
	int timesTick;
	public int stepCount;
	boolean alreadyLostHunger;
	boolean repeatHungerCyc = false;
	public int px = this.x;
	public int py = this.y;
	public int invulnerableTime = 0;
	static public int xx;
	static public int yy;
	public boolean showinfo;
	
	public int r = 50, g = 50, b;
	
	public Player(Game game, InputHandler input) {
		this.game = game;
		this.input = input;
		x = 24;
		y = 24;
		
		stamina = maxStamina;
		hunger = maxHunger;
		System.out.println("creative mode: " + ModeMenu.creative);
		if (ModeMenu.creative) {
			//NOT WORKING!!!
			for(int i = 0; i < ListItems.items.size(); i++) {
				this.inventory.add((Item)ListItems.items.get(i));
			}
			/*
			inventory.add(new ResourceItem( Resource.torch));
			
			inventory.add(new FurnitureItem(new Workbench()));
			inventory.add(new FurnitureItem(new Furnace()));
			inventory.add(new FurnitureItem(new Anvil()));
			inventory.add(new FurnitureItem(new Oven()));
			inventory.add(new FurnitureItem(new Lantern()));
			inventory.add(new FurnitureItem(new Chest()));
			inventory.add(new FurnitureItem(new Enchanter()));
			inventory.add(new FurnitureItem(new Loom()));
			inventory.add(new FurnitureItem(new GoldLantern()));
			inventory.add(new FurnitureItem(new IronLantern()));
			inventory.add(new FurnitureItem(new Tnt()));
			inventory.add(new FurnitureItem(new bed()));
			inventory.add(new BucketItem());
			inventory.add(new BucketWaterItem());
			inventory.add(new BucketLavaItem());
			
		
			inventory.add(new ToolItem(ToolType.sword, 1));
			inventory.add(new ToolItem(ToolType.sword, 2));
			inventory.add(new ToolItem(ToolType.sword, 3));
			inventory.add(new ToolItem(ToolType.sword, 4));
			
			inventory.add(new ToolItem(ToolType.pickaxe,1));
			inventory.add(new ToolItem(ToolType.pickaxe,2));
			inventory.add(new ToolItem(ToolType.pickaxe,3));
			inventory.add(new ToolItem(ToolType.pickaxe,4));
			
			inventory.add(new ToolItem(ToolType.shovel,1));
			inventory.add(new ToolItem(ToolType.shovel,2));
			inventory.add(new ToolItem(ToolType.shovel,3));
			inventory.add(new ToolItem(ToolType.shovel,4));
			
			inventory.add(new ToolItem(ToolType.axe,1));
			inventory.add(new ToolItem(ToolType.axe,2));
			inventory.add(new ToolItem(ToolType.axe,3));
			inventory.add(new ToolItem(ToolType.axe,4));
			
			inventory.add(new ToolItem(ToolType.hoe, 1));
			inventory.add(new ToolItem(ToolType.hoe, 2));
			inventory.add(new ToolItem(ToolType.hoe, 3));
			inventory.add(new ToolItem(ToolType.hoe, 4));
			
			inventory.add(new ToolItem(ToolType.spade, 1));
			inventory.add(new ToolItem(ToolType.spade, 2));
			inventory.add(new ToolItem(ToolType.spade, 3));
			inventory.add(new ToolItem(ToolType.spade, 4));
			
			inventory.add(new ToolItem(ToolType.pick, 1));
			inventory.add(new ToolItem(ToolType.pick, 2));
			inventory.add(new ToolItem(ToolType.pick, 3));
			inventory.add(new ToolItem(ToolType.pick, 4));
			
			inventory.add(new ToolItem(ToolType.bow, 1));
			inventory.add(new ToolItem(ToolType.bow, 2));
			inventory.add(new ToolItem(ToolType.bow, 3));
			inventory.add(new ToolItem(ToolType.bow, 4));
			
			inventory.add(new ToolItem(ToolType.claymore, 1));
			inventory.add(new ToolItem(ToolType.claymore, 2));
			inventory.add(new ToolItem(ToolType.claymore, 3));
			inventory.add(new ToolItem(ToolType.claymore, 4));
			
			inventory.add(new ResourceItem(Resource.rod));
			
			inventory.add(new ResourceItem(Resource.wood));
			inventory.add(new ResourceItem(Resource.stone));
			inventory.add(new ResourceItem(Resource.flower));
			inventory.add(new ResourceItem(Resource.rose));
			inventory.add(new ResourceItem(Resource.acorn));
			inventory.add(new ResourceItem(Resource.dirt));
			inventory.add(new ResourceItem(Resource.sand));
			inventory.add(new ResourceItem( Resource.cactusFlower));
			inventory.add(new ResourceItem( Resource.seeds));
			inventory.add(new ResourceItem( Resource.wheat));
			inventory.add(new ResourceItem( Resource.bread));
			inventory.add(new ResourceItem( Resource.apple));
			inventory.add(new ResourceItem( Resource.coal));
			inventory.add(new ResourceItem( Resource.ironIngot));
			inventory.add(new ResourceItem( Resource.goldIngot));
			inventory.add(new ResourceItem( Resource.slime));
			inventory.add(new ResourceItem( Resource.glass));
			inventory.add(new ResourceItem( Resource.cloth));
			inventory.add(new ResourceItem( Resource.cloud));
			inventory.add(new ResourceItem( Resource.gem));
			inventory.add(new ResourceItem( Resource.plank));
			inventory.add(new ResourceItem( Resource.plankwall));
			inventory.add(new ResourceItem( Resource.stonewall));
			inventory.add(new ResourceItem( Resource.wdoor));
			inventory.add(new ResourceItem( Resource.sdoor));
			inventory.add(new ResourceItem( Resource.sbrick));
			inventory.add(new ResourceItem( Resource.wool));
			inventory.add(new ResourceItem( Resource.redwool));
			inventory.add(new ResourceItem( Resource.bluewool));
			inventory.add(new ResourceItem( Resource.greenwool));
			inventory.add(new ResourceItem( Resource.yellowwool));
			inventory.add(new ResourceItem( Resource.blackwool));
			inventory.add(new ResourceItem( Resource.grassseeds));
			inventory.add(new ResourceItem( Resource.bone));
			inventory.add(new ResourceItem( Resource.string));
			
			inventory.add(new ResourceItem( Resource.goldapple));
			inventory.add(new ResourceItem( Resource.lapisOre));
			inventory.add(new ResourceItem( Resource.book));
			
			
			inventory.add(new ResourceItem( Resource.gunp));
			inventory.add(new ResourceItem( Resource.bookant));
			
			
			inventory.add(new PowerGloveItem());
			*/
		}
		else {
			inventory.add(new FurnitureItem(new Enchanter()));
			inventory.add(new FurnitureItem(new Workbench()));
			inventory.add(new PowerGloveItem());
		}
	}
	
	public void tick() {
		super.tick();
		isenemy = false;
		tickCounter++;
		//System.out.println(tickCounter);
		if (invulnerableTime > 0) invulnerableTime--;
		Tile onTile = level.getTile(x >> 4, y >> 4);
		if (onTile == Tile.stairsDown || onTile == Tile.stairsUp || onTile == Tile.lightstairsDown|| onTile == Tile.lightstairsUp) {
			if (onStairDelay == 0) {
				changeLevel((onTile == Tile.stairsUp || onTile == Tile.lightstairsUp) ? 1 : -1);
				onStairDelay = 10;
				return;
			}
			onStairDelay = 10;
		} else {
			if (onStairDelay > 0) onStairDelay--;
		}
		
		if (ModeMenu.creative) {
			if (stamina <= 10)
			{
				stamina = 10;
			}
		}
		if (ModeMenu.creative) {
			if (hunger <10) hunger = 10;
		}
		if (hunger < 0) hunger = 0;
		else {
		if (stamina <= 0 && staminaRechargeDelay == 0 && staminaRecharge == 0) {
			staminaRechargeDelay = 40;
			hungStamCnt++;
			//if(isSwimming()) hungStamCnt--;
			if (StartMenu.diff == StartMenu.easy && hungStamCnt == 10){ hunger = hunger - 1;hungStamCnt = 0;}
			if (StartMenu.diff == StartMenu.norm && hungStamCnt == 7){ hunger = hunger - 1;hungStamCnt = 0;}
			if (StartMenu.diff == StartMenu.hard && hungStamCnt == 5){ hunger = hunger - 1;hungStamCnt = 0;}
			
		}
		
		
		if (staminaRechargeDelay > 0) {
			staminaRechargeDelay--;
		}
		
		
		
		SHealth = health; // this saves health
		SHunger = hunger; // this saves hunger
		Sinventory = inventory;
		
		if (staminaRechargeDelay == 0) {
			staminaRecharge++;
			if (isSwimming()) {
				staminaRecharge = 0;
			}
			while (staminaRecharge > 10) {
				staminaRecharge -= 10;
				if (stamina < maxStamina) stamina++;
			}
		}
		}
		
		if (hungerChargeDelay == 0) {
			hungerChargeDelay = 100;
		}
     if (hunger == 10 && health <10) {
    	 if (hungerChargeDelay >0) hungerChargeDelay--;
		if (hungerChargeDelay == 0) {
			health++;
		}
     
		}
     if (hungerStarveDelay == 0) {
    	 hungerStarveDelay = 120;
     }
     if (StartMenu.diff == StartMenu.norm) {
     	if (stepCount >= 10000){ hunger--; stepCount = 0;}
     }
     
     if (StartMenu.diff == StartMenu.hard) {
      	if (stepCount >= 5000){ hunger--; stepCount = 0;}
      }
     
     if (StartMenu.diff == StartMenu.norm) {
    	 if (game.tickCount == 6000) {
    		 timesTick++;
    		 if (timesTick == random.nextInt(5)) hunger--;
    	 }
     }
     if (StartMenu.diff == StartMenu.hard) {
    	 if (game.tickCount == 6000) {
    		 timesTick++;
    		 if (timesTick == random.nextInt(2)) hunger--;
    	 }
     }
     
    if (StartMenu.diff == StartMenu.easy) {
    	if (hunger == 0 && health >5) {
    		if (hungerStarveDelay >0) hungerStarveDelay--;
    		if (hungerStarveDelay == 0) {
    			hurt(this,1,attackDir);
    		}
    	}
    }
    
    if (StartMenu.diff == StartMenu.norm) {
    	if (hunger == 0 && health >3) {
    		if (hungerStarveDelay >0) hungerStarveDelay--;
    		if (hungerStarveDelay == 0) {
    			hurt(this,1,attackDir);
    		}
    	}
    }
    
    if (StartMenu.diff == StartMenu.hard) {
    	if (hunger == 0 && health >0) {
    		if (hungerStarveDelay >0) hungerStarveDelay--;
    		if (hungerStarveDelay == 0) {
    			hurt(this,1,attackDir);
    		}
    	}
    }
    
		int xa = 0;
		int ya = 0;
		if (!Game.isfishing){
		if (input.getKey("up").down) { ya--; stepCount++;}
		if (input.getKey("down").down) {ya++; stepCount++;}
		if (input.getKey("left").down){ xa--; stepCount++;}
		if (input.getKey("right").down) { xa++; stepCount++;}
		}
		
		xx = x;
		yy = y;
		if (isSwimming() && tickTime % 60 == 0) {
			if (stamina > 0) {
				stamina--;
			} else {
				hurt(this, 1, dir ^ 1);
			}
		}
		
		if(game.saving && game.savecooldown > 0) {
			xa = 0;
			ya = 0;
		}
		/* potion effect
		if(regen) {
			regentick++;
			if(regentick > 60) {
				regentick = 0;
				if(health < 10) {
					health++;
				}
			}
		}
		*/
		if(game.savecooldown > 0 && !game.saving) {
			game.savecooldown--;
		}
		
		if (staminaRechargeDelay % 2 == 0) {
			move(xa, ya);
		}
		
		if (input.getKey("attack").clicked) {
			if (stamina == 0) {
				
			} else {
				stamina--;
				staminaRecharge = 0;
				attack();
			}
		}
		if (input.getKey("menu").clicked && !use())
			game.setMenu(new InventoryMenu(this));
		if (input.getKey("pause").clicked)
			game.setMenu(new PauseMenu(this));
		if (input.getKey("craft").clicked && !use())
			game.setMenu(new CraftInvMenu(Crafting.craftRecipes, this));
		if (input.getKey("sethome").clicked)
			setHome();
		if (input.getKey("home").clicked)
			goHome();
		
		if (input.getKey("i").clicked)
			game.setMenu(new PlayerInfoMenu());
		
		//these are my test buttons. incase i need to debug something.
		if(input.getKey("r").clicked && !game.saving) {
			game.saving = true;
			new Save(this, WorldSelectMenu.worldname);
			LoadingMenu.percentage = 0;
		}
		if (input.getKey("t").clicked) {
			
		}
		
		if (ModeMenu.creative) {
			//Oh-ho-ho!
			if (input.getKey("dayTime").clicked) {
				Game.Time = 0;	
				Game.tickCount = 6000;
			}
			if (input.getKey("nightTime").clicked) {
				Game.Time = 3;
				Game.tickCount = 54000;
			}
		}
		if (attackTime > 0)
			attackTime--;
		
	}
	
	private boolean use() {
		int yo = -2;
		if (dir == 0 && use(x - 8, y + 4 + yo, x + 8, y + 12 + yo)) return true;
		if (dir == 1 && use(x - 8, y - 12 + yo, x + 8, y - 4 + yo)) return true;
		if (dir == 3 && use(x + 4, y - 8 + yo, x + 12, y + 8 + yo)) return true;
		if (dir == 2 && use(x - 12, y - 8 + yo, x - 4, y + 8 + yo)) return true;
		
		int xt = x >> 4;
		int yt = (y + yo) >> 4;
		int r = 12;
		if (attackDir == 0) yt = (y + r + yo) >> 4;
		if (attackDir == 1) yt = (y - r + yo) >> 4;
		if (attackDir == 2) xt = (x - r) >> 4;
		if (attackDir == 3) xt = (x + r) >> 4;
		
		if (xt >= 0 && yt >= 0 && xt < level.w && yt < level.h) {
			if (level.getTile(xt, yt).use(level, xt, yt, this, attackDir)) return true;
		}
		
		return false;
	}
	
	private void attack() {
		walkDist += 8;
		attackDir = dir;
		attackItem = activeItem;
		boolean done = false;
		
		if ( (attackItem instanceof ToolItem) && this.stamina-1>=0) {
		
			ToolItem tool = (ToolItem) attackItem;
			if (Game.ac > 0){
			if (tool.type == ToolType.bow  && this.stamina-1>=0) {
				
				if(!energy)stamina -= 0;
                switch (attackDir) {
                case 0:
                	level.add(new Arrow(this,0,1,tool.level, done));
            		if (ModeMenu.creative == false){
                	Game.ac--;
            		}
                	break;
                case 1:
                	level.add(new Arrow(this,0,-1,tool.level, done));
            		if (ModeMenu.creative == false){
                    	Game.ac--;
                		}
                	break;
                case 2:
                	level.add(new Arrow(this,-1,0,tool.level, done));
            		if (ModeMenu.creative == false){
                    	Game.ac--;
                		}
                	break;
                case 3:
                	level.add(new Arrow(this,1,0,tool.level, done));
            		if (ModeMenu.creative == false){
                    	Game.ac--;
                		}
                	break;
                default:
                	break;
                }
                
                
			done = true;
							
			}
			}
			
		}
		
		if (activeItem != null) {
			attackTime = 10;
			int yo = -2;
			int range = 12;
			if (dir == 0 && interact(x - 8, y + 4 + yo, x + 8, y + range + yo)) done = true;
			if (dir == 1 && interact(x - 8, y - range + yo, x + 8, y - 4 + yo)) done = true;
			if (dir == 3 && interact(x + 4, y - 8 + yo, x + range, y + 8 + yo)) done = true;
			if (dir == 2 && interact(x - range, y - 8 + yo, x - 4, y + 8 + yo)) done = true;
			if (done) return;
			
			int xt = x >> 4;
			int yt = (y + yo) >> 4;
			int r = 12;
			if (attackDir == 0) yt = (y + r + yo) >> 4;
			if (attackDir == 1) yt = (y - r + yo) >> 4;
			if (attackDir == 2) xt = (x - r) >> 4;
			if (attackDir == 3) xt = (x + r) >> 4;
			
			
			if (xt >= 0 && yt >= 0 && xt < level.w && yt < level.h) {
				if (activeItem.interactOn(level.getTile(xt, yt), level, xt, yt, this, attackDir)) {
					done = true;
				} else {
					if (level.getTile(xt, yt).interact(level, xt, yt, this, activeItem, attackDir)) {
						done = true;
					}
				}
				if (activeItem.isDepleted()) {
					activeItem = null;
				}
			}
		}
		
		if (done) return;
		
		if (activeItem == null || activeItem.canAttack()) {
			attackTime = 5;
			int yo = -2;
			int range = 20;
			if (dir == 0) hurt(x - 8, y + 4 + yo, x + 8, y + range + yo);
			if (dir == 1) hurt(x - 8, y - range + yo, x + 8, y - 4 + yo);
			if (dir == 3) hurt(x + 4, y - 8 + yo, x + range, y + 8 + yo);
			if (dir == 2) hurt(x - range, y - 8 + yo, x - 4, y + 8 + yo);
			
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
	
	private boolean use(int x0, int y0, int x1, int y1) {
		List<Entity> entities = level.getEntities(x0, y0, x1, y1);
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e != this) if (e.use(this, attackDir)) return true;
		}
		return false;
	}
	
	private boolean interact(int x0, int y0, int x1, int y1) {
		List<Entity> entities = level.getEntities(x0, y0, x1, y1);
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e != this) if (e.interact(this, activeItem, attackDir)) return true;
		}
		return false;
	}
	
	private void hurt(int x0, int y0, int x1, int y1) {
		List<Entity> entities = level.getEntities(x0, y0, x1, y1);
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e != this) e.hurt(this, getAttackDamage(e), attackDir);
		}
	}
	
	private int getAttackDamage(Entity e) {
		int dmg = random.nextInt(3) + 1;
		if (attackItem != null) {
			dmg += attackItem.getAttackDamageBonus(e);
		}
		return dmg;
	}
	
	public void render(Screen screen) {
		int col0 = Color.get(-1, 100, 110, 531);
		
		int col1 = Color.get(-1, 100, 220, 532);
		
		int col2 = Color.get(-1, 100, 110, 421);
		
	    int col3 = Color.get(-1, 0, 110, 321);
		
		int col4 = Color.get(-1, 100, 220, 532);
		
		if (isLight()) {
			col0 = Color.get(-1, 100, 220, 532);
			
			col1 = Color.get(-1, 100, 220, 532);
			
			col2 = Color.get(-1, 100, 220, 532);
			
		    col3 = Color.get(-1, 100, 220, 532);
			
			col4 = Color.get(-1, 100, 220, 532);
		} else {
			col0 = Color.get(-1, 100, 110, 531);
			
			col1 = Color.get(-1, 100, 220, 532);
			
			col2 = Color.get(-1, 100, 110, 421);
			
		    col3 = Color.get(-1, 0, 110, 321);
			
			col4 = Color.get(-1, 100, 220, 532);
		}
		
		
		int xt = 0;
		int yt = 14;
		
		
		int flip1 = (walkDist >> 3) & 1;
		int flip2 = (walkDist >> 3) & 1;
		
		if (dir == 1) {
			xt += 2;
		}
		if (dir > 1) {
			flip1 = 0;
			flip2 = ((walkDist >> 4) & 1);
			if (dir == 2) {
				flip1 = 1;
			}
			xt += 4 + ((walkDist >> 3) & 1) * 2;
		}
		
		int xo = x - 8;
		int yo = y - 11;
		if (isSwimming()) {
			yo += 4;
			int waterColor = Color.get(-1, -1, 115, 335);
			if (tickTime / 8 % 2 == 0) {
				waterColor = Color.get(-1, 335, 5, 115);
			}
			screen.render(xo + 0, yo + 3, 5 + 13 * 32, waterColor, 0);
			screen.render(xo + 8, yo + 3, 5 + 13 * 32, waterColor, 1);
		}
		
		if (attackTime > 0 && attackDir == 1) {
			screen.render(xo + 0, yo - 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 0);
			screen.render(xo + 8, yo - 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 1);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 4, yo - 4);
			}
		}
		
		if (level.dirtColor == 322){
		if (Game.Time == 0){
		int col = col0;
		if (hurtTime > 0) {
			col = Color.get(-1, 555, 555, 555);
		}
		
		if (activeItem instanceof FurnitureItem) {
			yt += 2;
		}
		screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
		screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
		if (!isSwimming()) {
			screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
			screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
		}
		
		if (attackTime > 0 && attackDir == 2) {
			screen.render(xo - 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 1);
			screen.render(xo - 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo - 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 3) {
			screen.render(xo + 8 + 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 0);
			screen.render(xo + 8 + 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 8 + 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 0) {
			screen.render(xo + 0, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			screen.render(xo + 8, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 4, yo + 8 + 4);
			}
		}
		
		if (activeItem instanceof FurnitureItem) {
			Furniture furniture = ((FurnitureItem) activeItem).furniture;
			furniture.x = x;
			furniture.y = yo;
			furniture.render(screen);
			
		}
		}
		if (Game.Time == 1){
		int col = col1;
		if (hurtTime > 0) {
			col = Color.get(-1, 555, 555, 555);
		}
		
		if (activeItem instanceof FurnitureItem) {
			yt += 2;
		}
		screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
		screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
		if (!isSwimming()) {
			screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
			screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
		}
		
		if (attackTime > 0 && attackDir == 2) {
			screen.render(xo - 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 1);
			screen.render(xo - 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo - 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 3) {
			screen.render(xo + 8 + 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 0);
			screen.render(xo + 8 + 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 8 + 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 0) {
			screen.render(xo + 0, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			screen.render(xo + 8, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 4, yo + 8 + 4);
			}
		}
		
		if (activeItem instanceof FurnitureItem) {
			Furniture furniture = ((FurnitureItem) activeItem).furniture;
			furniture.x = x;
			furniture.y = yo;
			furniture.render(screen);
			
		}
		}
		if (Game.Time == 2){
		int col = col2;
		if (hurtTime > 0) {
			col = Color.get(-1, 555, 555, 555);
		}
		
		if (activeItem instanceof FurnitureItem) {
			yt += 2;
		}
		screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
		screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
		if (!isSwimming()) {
			screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
			screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
		}
		
		if (attackTime > 0 && attackDir == 2) {
			screen.render(xo - 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 1);
			screen.render(xo - 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo - 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 3) {
			screen.render(xo + 8 + 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 0);
			screen.render(xo + 8 + 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 8 + 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 0) {
			screen.render(xo + 0, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			screen.render(xo + 8, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 4, yo + 8 + 4);
			}
		}
		
		if (activeItem instanceof FurnitureItem) {
			Furniture furniture = ((FurnitureItem) activeItem).furniture;
			furniture.x = x;
			furniture.y = yo;
			furniture.render(screen);
			
		}
		}
		if (Game.Time == 3){
		int col = col3;
		if (hurtTime > 0) {
			col = Color.get(-1, 555, 555, 555);
		}
		
		if (activeItem instanceof FurnitureItem) {
			yt += 2;
		}
		screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
		screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
		if (!isSwimming()) {
			screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
			screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
		}
		
		if (attackTime > 0 && attackDir == 2) {
			screen.render(xo - 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 1);
			screen.render(xo - 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo - 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 3) {
			screen.render(xo + 8 + 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 0);
			screen.render(xo + 8 + 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 8 + 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 0) {
			screen.render(xo + 0, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			screen.render(xo + 8, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 4, yo + 8 + 4);
			}
		}
		
		if (activeItem instanceof FurnitureItem) {
			Furniture furniture = ((FurnitureItem) activeItem).furniture;
			furniture.x = x;
			furniture.y = yo;
			furniture.render(screen);
			
		}
		}
		}
		
		
		if (level.dirtColor != 322){
		int col = col4;
		if (hurtTime > 0) {
			col = Color.get(-1, 555, 555, 555);
		}
		
		if (activeItem instanceof FurnitureItem) {
			yt += 2;
		}
		screen.render(xo + 8 * flip1, yo + 0, xt + yt * 32, col, flip1);
		screen.render(xo + 8 - 8 * flip1, yo + 0, xt + 1 + yt * 32, col, flip1);
		if (!isSwimming()) {
			screen.render(xo + 8 * flip2, yo + 8, xt + (yt + 1) * 32, col, flip2);
			screen.render(xo + 8 - 8 * flip2, yo + 8, xt + 1 + (yt + 1) * 32, col, flip2);
		}
		
		if (attackTime > 0 && attackDir == 2) {
			screen.render(xo - 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 1);
			screen.render(xo - 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo - 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 3) {
			screen.render(xo + 8 + 4, yo, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 0);
			screen.render(xo + 8 + 4, yo + 8, 7 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 8 + 4, yo + 4);
			}
		}
		if (attackTime > 0 && attackDir == 0) {
			screen.render(xo + 0, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 2);
			screen.render(xo + 8, yo + 8 + 4, 6 + 13 * 32, Color.get(-1, 555, 555, 555), 3);
			if (attackItem != null) {
				attackItem.renderIcon(screen, xo + 4, yo + 8 + 4);
			}
		}
		
		if (activeItem instanceof FurnitureItem) {
			Furniture furniture = ((FurnitureItem) activeItem).furniture;
			furniture.x = x;
			furniture.y = yo;
			furniture.render(screen);
			
		}
		}
		
	}
	
	public void touchItem(ItemEntity itemEntity) {
		itemEntity.take(this);
		if (itemEntity.item.getName() == "arrow"){
		Game.ac++;
		} else {
		inventory.add(itemEntity.item);
		}
	}
	
	public boolean canSwim() {
		return true;
	}
	
	public boolean canWool() {
		return true;
	}
	public boolean canLight() {
		return true;
	}
	
	public boolean findStartPoss(Level level) {
	int xxs = x;
    int yys = y;
	if (level.getTile(x, y) != Tile.dirt){
	this.x = xxs;
	this.y = yys;
	return true;
		
	}
	
		return false;
	}
	public boolean findStartPos(Level level) {
		while (true) {
			int x = random.nextInt(level.w);
			int y = random.nextInt(level.h);
			if (level.getTile(x, y) == Tile.grass) {
				this.x = x * 16 + 8;
				this.y = y * 16 + 8;
				spawnx = y;
				spawny = x;
				return true;
			}
		}
	}
	
	
	
	public void setHome() {
		if (Game.currentLevel == 3) {
			homeSetX = this.x; 
			 homeSetY = this.y; 
			canSetHome = true;
			sentFromSetHome = true;
			hasSetHome = true;
			game.setMenu(new InfoMenu());
		}
		else {
			canSetHome = false;
			sentFromSetHome = true;
			game.setMenu(new InfoMenu());
		}
		 
		
		
		
	}
	public void goHome() {
		if (Game.currentLevel == 3) {
			canGoHome = true;
			sentFromHome = true;
			if (hasSetHome == true) {
				this.x = homeSetX;
				this.y = homeSetY;
				if (ModeMenu.hardcore)hurt(this,2,attackDir);
				stamina = 0;
				sentFromHome = true;
			game.setMenu(new HomeMenu());
			//System.out.println(sentFromHome);
			}
			else {
				game.setMenu(new HomeMenu());
			}
		}
		else {
			canGoHome = false;
			hasSetHome = false;
			sentFromHome = true;
			game.setMenu(new HomeMenu());
		//	System.out.println(sentFromHome);

		}
	}
	
	
public boolean respawn(Level level)
	{
		while (true) {
			int x = spawnx;
		    int y = spawny;
			if (level.getTile(x, y) == Tile.grass) {
				this.x = spawny * 16 + 8;
				this.y = spawnx * 16 + 8;
				return true;
			} else {
				findStartPos(level);
			}
		}
		
	}
	
	
	public boolean payStamina(int cost) {
		if (cost > stamina) return false;
		stamina -= cost;
		return true;
	}
	
	public void changeLevel(int dir) {
		game.scheduleLevelChange(dir);
	}
	
	public int getLightRadius() {
		int r = 33;
		if (activeItem != null) {
			if (activeItem instanceof FurnitureItem) {
				int rr = ((FurnitureItem) activeItem).furniture.getLightRadius();
				if (rr > r) r = rr;
			}
		}
		return r;
	}
	
	protected void die() {
		super.die();
		int lostscore = score / 3;
		score = score - lostscore;
		Game.ism = 1;
		
		Sound.playerDeath.play();
	}
	
	protected void touchedBy(Entity entity) {
		if (!(entity instanceof Player)) {
			entity.touchedBy(this);
		}
	}
	
	protected void doHurt(int damage, int attackDir) {
		if (ModeMenu.creative == false) {
		if (hurtTime > 0 || invulnerableTime > 0) return;
		
		Sound.playerHurt.play();
		if (maxArmor <= 0){
		level.add(new TextParticle("" + damage, x, y, Color.get(-1, 504, 504, 504)));
		health -= damage;
		}
		if (maxArmor > 0){
		level.add(new TextParticle("" + damage, x, y, Color.get(-1, 333, 333, 333)));
		if (damage > maxArmor){
			int dmgleft = damage - maxArmor;
			health -= dmgleft;
			maxArmor = 0;
		}
		else maxArmor -= damage;
		}
		if (attackDir == 0) yKnockback = +6;
		if (attackDir == 1) yKnockback = -6;
		if (attackDir == 2) xKnockback = -6;
		if (attackDir == 3) xKnockback = +6;
		hurtTime = 10;
		invulnerableTime = 30;
		}
		else {
		}
	}
	
	
	public void gameWon() {
		level.player.invulnerableTime = 60 * 5;
		game.won();
	}
}