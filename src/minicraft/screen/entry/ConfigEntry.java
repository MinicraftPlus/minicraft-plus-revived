package minicraft.screen.entry;

public abstract class ConfigEntry<T> extends ListEntry {
	
	public abstract void setValue(T value);
	public abstract T getValue();
	
}
