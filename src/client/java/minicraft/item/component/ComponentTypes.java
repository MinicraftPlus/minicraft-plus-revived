package minicraft.item.component;

import minicraft.item.component.type.BookContentComponent;
import minicraft.item.component.type.FurnitureComponent;
import minicraft.item.component.type.WateringCanComponent;

public final class ComponentTypes {
	public static final ComponentType<Integer> DURABILITY = new ComponentType<>("dur");
	public static final ComponentType<Integer> FISHING_ROD_USES = new ComponentType<>("uses");
	public static final ComponentType<BookContentComponent> BOOK_CONTENT = new ComponentType<>("book_content");
	public static final ComponentType<FurnitureComponent> FURNITURE = new ComponentType<>("furniture");
	public static final ComponentType<WateringCanComponent> WATERING_CAN = new ComponentType<>("wateringcan");

	private ComponentTypes() {
	}
}
