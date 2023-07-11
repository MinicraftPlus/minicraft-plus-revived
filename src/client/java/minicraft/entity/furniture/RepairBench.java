package minicraft.entity.furniture;

import minicraft.core.Game;
import minicraft.entity.mob.Player;
import minicraft.gfx.SpriteLinker;
import minicraft.screen.RepairBenchDisplay;
import org.jetbrains.annotations.NotNull;

public class RepairBench extends Furniture {
	private static final SpriteLinker.LinkedSprite sprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Entity, "repair_bench");
	private static final SpriteLinker.LinkedSprite itemSprite = new SpriteLinker.LinkedSprite(SpriteLinker.SpriteType.Item, "repair_bench");

	public RepairBench() {
		super("Repair Bench", sprite, itemSprite);
	}

	@Override
	public boolean use(Player player) {
		Game.setDisplay(new RepairBenchDisplay(this, player));
		return true;
	}

	@Override
	public @NotNull Furniture copy() {
		return new RepairBench();
	}
}
