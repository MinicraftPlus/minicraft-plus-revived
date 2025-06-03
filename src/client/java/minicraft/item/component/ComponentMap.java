package minicraft.item.component;

import java.util.HashMap;
import java.util.Map;

public class ComponentMap {
	private final ComponentMap baseMap;
	private final Map<ComponentType<?>, Object> map;

	public ComponentMap() {
		this(null, new HashMap<>());
	}

	public ComponentMap(ComponentMap baseMap) {
		this(baseMap, new HashMap<>());
	}

	public ComponentMap(Map<ComponentType<?>, Object> components) {
		this(null, components);
	}

	public ComponentMap(ComponentMap baseMap, Map<ComponentType<?>, Object> components) {
		this.baseMap = baseMap;
		this.map = components;
	}

	public static Builder builder() {
		return new Builder();
	}

	public <T> void put(ComponentType<T> type, T value) {
		this.map.put(type, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ComponentType<T> type) {
		if (this.map.containsKey(type)) {
			return (T) this.map.get(type);
		}
		return this.baseMap == null ? null : this.baseMap.get(type);
	}

	public <T> T getOrDefault(ComponentType<T> type, T fallback) {
		T value = this.get(type);
		return value != null ? value : fallback;
	}

	public boolean has(ComponentType<?> type) {
		return this.map.containsKey(type) || (this.baseMap != null && this.baseMap.has(type));
	}

	public int size() {
		return this.map.size();
	}

	public ComponentMap copy() {
		return new ComponentMap(this.baseMap, new HashMap<>(this.map));
	}

	public ImmutableComponentMap asImmutable() {
		return new ImmutableComponentMap(this);
	}

	public static class ImmutableComponentMap extends ComponentMap {
		public ImmutableComponentMap(ComponentMap baseMap) {
			super(baseMap);
		}

		public ImmutableComponentMap(Map<ComponentType<?>, Object> map) {
			super(map);
		}

		@Override
		public <T> void put(ComponentType<T> type, T value) {
			throw new UnsupportedOperationException();
		}
	}

	public static class Builder {
		private final Map<ComponentType<?>, Object> map;

		private Builder() {
			this.map = new HashMap<>();
		}

		public <T> Builder add(ComponentType<T> type, T value) {
			this.map.put(type, value);
			return this;
		}

		public ComponentMap build() {
			return new ImmutableComponentMap(this.map);
		}
	}
}
