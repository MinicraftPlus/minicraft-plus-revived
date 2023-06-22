package minicraft.entity;

@SuppressWarnings("unused") // Reserved for future reference
public interface ClientTickable extends Tickable {

	default void clientTick() {
		tick();
	}

}
