package minicraft.item.component;

public class ComponentType<T> {
	private final String name;

	public ComponentType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
