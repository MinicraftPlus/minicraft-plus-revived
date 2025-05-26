package minicraft.item;

import minicraft.entity.Direction;
import minicraft.entity.mob.Player;
import minicraft.gfx.Screen;
import minicraft.gfx.SpriteLinker;
import minicraft.item.component.ComponentMap;
import minicraft.item.component.ComponentType;
import minicraft.item.component.ComponentTypes;
import minicraft.level.Level;
import minicraft.level.tile.Tile;

public class ItemStack {
	public static final ItemStack EMPTY = new ItemStack(null, 0);

	private final Item item;
	private int count;
	private final ComponentMap components;

	public ItemStack(Item item) {
		this(item, 1);
	}

	public ItemStack(Item item, int count) {
		this(item, count, item == null ? new ComponentMap() : new ComponentMap(item.getComponents()));
	}

	public ItemStack(Item item, int count, ComponentMap components) {
		this.item = item;
		this.components = components;
		this.setCount(count);
	}

	public ComponentMap getComponents() {
		return this.components;
	}

	public <T> void put(ComponentType<T> type, T value) {
		this.components.put(type, value);
	}

	public <T> T get(ComponentType<T> type) {
		return this.components.get(type);
	}

	public <T> T getOrDefault(ComponentType<T> type, T fallback) {
		return this.components.getOrDefault(type, fallback);
	}

	public <T> boolean has(ComponentType<T> type) {
		return this.components.has(type);
	}

	public Item getItem() {
		return this.item;
	}

	@SuppressWarnings("unchecked")
	public <T extends Item> T getItemAs() {
		return (T) this.item;
	}

	public int getDurability() {
		return this.getOrDefault(ComponentTypes.DURABILITY, 0);
	}

	public void setDurability(int durability) {
		this.put(ComponentTypes.DURABILITY, Math.max(0, durability));
	}

	public <T extends Item> boolean isOf(Class<T> clazz) {
		return this.getItem().getClass() == clazz;
	}

	public boolean canAttack() {
		return this.item.canAttack(this);
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = Math.max(0, count);
	}

	public void decrement(int amount) {
		this.setCount(this.count - amount);
	}

	public void increment(int amount) {
		this.setCount(this.count + amount);
	}

	public boolean isStackable() {
		return this.item.maxCount > 1;
	}

	public String getName() {
		return this.item.getName();
	}

	public String getDescription() {
		return this.item.getDescription();
	}

	public int getMaxCount() {
		return this.item.maxCount;
	}

	public void renderHUD(Screen screen, int x, int y, int fontColor) {
		this.item.renderHUD(screen, x, y, fontColor, this);
	}

	public String getData() {
		return this.item.getData(this);
	}

	public SpriteLinker.LinkedSprite getSprite() {
		return this.item.getSprite(this);
	}

	public boolean interactsWithWorld() {
		return this.item.interactsWithWorld(this);
	}

	public boolean interactOn(Tile tile, Level level, int xt, int yt, Player player, Direction attackDir) {
		return this.item.interactOn(tile, level, xt, yt, player, attackDir, this);
	}

	public boolean isDepleted() {
		return this.item == null || this.item.isDepleted(this) || this.count <= 0;
	}

	public boolean isEmpty() {
		return this == EMPTY || this.item == null || this.count <= 0;
	}

	public ItemStack copy() {
		return new ItemStack(this.item, this.count, this.components.copy());
	}

	@Override
	public boolean equals(Object item) {
		if (!(item instanceof ItemStack)) return false;
		ItemStack stack = (ItemStack) item;
		return this.item != null && this.item.equals(stack.item);
	}

	@Override
	public int hashCode() {
		return this.item.getName().hashCode();
	}

	@Override
	public String toString() {
		return this.item.toString();
	}
}
