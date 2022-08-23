package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.core.io.Localization;
import minicraft.entity.Direction;
import minicraft.entity.mob.ObsidianKnight;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.Sprite;
import minicraft.item.Item;

public class KnightStatue extends Furniture {

	private boolean touched = false; // second warn
	private boolean tapped = false; // first warn
	private boolean awoken = false; // awaking
	private boolean touched1; // second warn spam failsafe
	private boolean tapped1; // first warn spam failsafe;
	public int obkHealth;
	public static boolean active;

	public KnightStatue(int health) {
		super("KnightStatue", new Sprite(8, 16, 2, 2, 2), 3, 2);
		obkHealth = health;
		ObsidianKnight.active = false;
		ObsidianKnight.failed = false;
		touched = false;
		tapped = false;
		awoken = false;
		touched1 = false;
		tapped1 = false;
		active = true;
	}

	@Override
	public void tick() {
		super.tick();

		if (!ObsidianKnight.active) {
			if (tapped && !tapped1) {
				// Warn the player
				Game.notifications.add(Localization.getLocalized("minicraft.notifications.statue_tapped"));
				tapped1 = true;
			}
			if (touched && !touched1) {
				// Warn the player again
				Game.notifications.add(Localization.getLocalized("minicraft.notifications.statue_touched"));
				touched1 = true;
			}
			if (awoken) {
				// Awoken notifications is in Boss class
				// Summon the Obsidian Knight boss
				ObsidianKnight obk = new ObsidianKnight(obkHealth);
				level.add(obk, x, y, false);
				super.remove();
			}
		}
		else {
			Game.notifications.add(Localization.getLocalized("minicraft.notification.boss_limit"));
		}
	}

	@Override
	public void render(Screen screen) {
		super.render(screen);
	}

	@Override
	public boolean interact(Player player, Item heldItem, Direction attackDir) {
		if (!tapped) {
			tapped = true;
			return true;
		}
		if (!touched && tapped) {
			touched = true;
			return true;
		}
		if (!awoken && touched){
			awoken = true;
			return true;
		}
		return false;
	}

	@Override
	public void tryPush(Player player) {

	}

	@Override
	public Furniture clone(){
		return new KnightStatue(5000);
	}
}

