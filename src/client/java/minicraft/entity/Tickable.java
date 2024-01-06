package minicraft.entity;

public interface Tickable {

	/**
	 * Called every frame before Render() is called. Most game functionality in the game is based on this method.
	 */
	void tick();

}
