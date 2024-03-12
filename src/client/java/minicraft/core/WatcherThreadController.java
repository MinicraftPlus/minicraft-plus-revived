package minicraft.core;

/**
 * This provides an ability to control over a watcher thread to ensure content synchronization and thread safety.
 */
public interface WatcherThreadController {
	/**
	 * Suspends the watcher thread from running.
	 * @throws IllegalStateException if the thread has already been suspended
	 */
	void suspend();

	/**
	 * Resumes the watcher thread from suspended.
	 * @throws IllegalStateException if the thread has already been resumed or not suspended
	 */
	void resume();
}
